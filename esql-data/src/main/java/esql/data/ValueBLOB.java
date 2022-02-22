package esql.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public class ValueBLOB extends ValueLOB {

    public static final ValueBLOB NULL_BLOB = new ValueBLOB(null, null, 0L, EMPTY_HASHES[MD5], EMPTY_HASHES[SHA1], EMPTY_HASHES[SHA256], EMPTY_HASHES[SHA384], EMPTY_HASHES[SHA512]);
    public static final ValueBLOB EMPTY_BLOB = new ValueBLOB(null, EMPTY_BYTES, 0L, EMPTY_HASHES[MD5], EMPTY_HASHES[SHA1], EMPTY_HASHES[SHA256], EMPTY_HASHES[SHA384], EMPTY_HASHES[SHA512]);

    final int BASE64_BUFFER_SIZE = 3 * 1024; //multiply with 3.
    protected final byte[] first_in_mem;
    protected final Path tempFile;

    private ValueBLOB(Path tempFile, byte[] in_mem, long lobSize, byte[]... pre_hash) {
        super(lobSize, pre_hash);
        this.first_in_mem = in_mem; //first block of file
        this.tempFile = tempFile;
    }

    public static ValueBLOB wrap(byte[] in_mem, long lobSize, int... hashAlgorithms) throws NoSuchAlgorithmException {
        if(hashAlgorithms.length == 0)
            hashAlgorithms = DEFAULT_HASH_ALGOS;
        byte[][] hash_holder = new byte[HASHES.length][];
        for(int i = 0;i<hashAlgorithms.length;i++) {
            //serial of digest
            hash_holder[hashAlgorithms[i]] = MessageDigest.getInstance(HASHES[hashAlgorithms[i]]).digest(in_mem);
        }
        return buildBLOB(null, in_mem, lobSize, hash_holder);
    }

    /**
     * create on-demand BLOB value, that wraps an InputStream and process hash/size on-actions.
     *
     * @param lobSourceInput
     * @param lobSize
     * @param hashAlgorithms
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static ValueBLOB wrap(InputStream lobSourceInput, long lobSize, int... hashAlgorithms) throws NoSuchAlgorithmException {
        if (lobSize == 0) //nothing to read
            return ValueBLOB.EMPTY_BLOB;
        //tempFile trong truong hop nay thi khong create, ma dung UUID
        return new ValueBLOB.ValueBLOBonDemand(lobSourceInput, ValueLOB.tempDir.resolve("ESQL-LOB"+ UUID.randomUUID()+".bin"), lobSize, hashAlgorithms);
    }

    /**
     * create new BLOB by loading all data from lobSourceInput.
     * new BLOB using temporally file to store data, and it is calculating hash as requested,
     *
     * @param lobSourceInput input stream to load
     * @param hashAlgos hash algorithms to apply
     * @return new BLOB
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public static ValueBLOB load(InputStream lobSourceInput, int...hashAlgos) throws IOException, NoSuchAlgorithmException {
        //tempFile trong truong hop nay thi khong create, ma dung UUID
        Path temp = ValueLOB.tempDir.resolve("ESQL-LOB"+ UUID.randomUUID()+".bin");
        long lobSize = 0;

        if(hashAlgos.length>0) {
            byte[][] hash_holder = new byte[HASHES.length][];
            MessageDigest[] hash = new MessageDigest[hashAlgos.length];
            for(int i = 0;i<hashAlgos.length;i++)
                //serial of digest
                hash[i] = MessageDigest.getInstance(HASHES[hashAlgos[i]]);
            InputStream finalInput = cascadeDigestInput(lobSourceInput, hash);
            try {
                lobSize = Files.copy(finalInput, temp);
            }
            finally {
                finalInput.close();
                if(lobSize>0) {
                    for (int i = 0; i < hash.length; i++) {
                        hash_holder[hashAlgos[i]] = hash[i].digest();
                    }
                }
            }
            if(lobSize > 0)
                return new ValueBLOB(temp, EMPTY_BYTES, lobSize,
                        hash_holder);
        }
        else
            lobSize = Files.copy(lobSourceInput, temp);
        if (lobSize == 0) //nothing to read
            return ValueBLOB.EMPTY_BLOB;
        return new ValueBLOB(temp, EMPTY_BYTES, lobSize);
    }

    static ValueBLOB buildBLOB(Path tempFile, byte[] in_mem, long lobSize, byte[]... pre_hash) {
        return new ValueBLOB(tempFile, in_mem, lobSize, pre_hash);
    }

    @Override
    public boolean isNull() {
        return this.first_in_mem == null;
    }

    @Override
    public boolean isTrue() {
        return !isEmpty();
    }

    @Override
    public Value convertTo(Types type) {
        if(this.isNull())
            return Value.nullOf(type);
        switch (type) {
            case TYPE_STRING:
            case TYPE_NSTRING:
                return ValueString.buildString(type, this.stringValue());
            case TYPE_CLOB:
            case TYPE_NCLOB:
                //create new CLOB of base64 string
                Base64.Encoder encoder = Base64.getEncoder();
                if(lobSize <= first_in_mem.length) {
                    String str = encoder.encodeToString(Arrays.copyOf(first_in_mem, (int) lobSize));
                    try {
                        return ValueCLOB.wrap(str, Types.TYPE_NCLOB.equals(type), findConvenienceAlgorithms());
                    } catch (NoSuchAlgorithmException e) {
                        throw new RuntimeException("LOB digest error. "+e.toString());
                    }
                }
                if(Files.exists(tempFile)) {//already creating file
                    try(FileChannel fc = FileChannel.open(tempFile, StandardOpenOption.READ);
                        ValueCLOBCreator creator = ValueLOB.getCLOBCreator(Types.TYPE_NCLOB.equals(type), findConvenienceAlgorithms())) {
                        ByteBuffer t_read = ByteBuffer.allocate(BASE64_BUFFER_SIZE);
                        while (0 <= fc.read(t_read)) {
                            t_read.flip();
                            ByteBuffer t_out = encoder.encode(t_read);
                            String s = new String(t_out.array());
                            creator.writeToLOB(s);
                            t_read.compact();
                        }
                        return creator.buildCLOB();
                    } catch (IOException | NoSuchAlgorithmException e) {
                        throw new RuntimeException("LOB convert IO error. "+e.toString());
                    }
                }
                //empty case
                return Types.TYPE_NCLOB.equals(type) ? ValueCLOB.EMPTY_NCLOB : ValueCLOB.EMPTY_CLOB;
            case TYPE_BYTES: //read all bytes to memory
                if(lobSize<MAX_CONVERTIBLE_TO_STRING_SIZE) {
                    try {
                        if(lobSize <= first_in_mem.length)
                            return ValueBytes.buildBytes(first_in_mem);
                        if(Files.exists(tempFile))
                            return ValueBytes.buildBytes(Files.readAllBytes(tempFile));
                        return ValueBytes.EMPTY_BINARY_STRING;
                    } catch (IOException e) {
                        throw new RuntimeException("BLOB convert readAllBytes error "+e.toString());
                    }
                }
                throw new IllegalStateException("BLOB is too large to convert");
            case TYPE_BLOB:
                //create a new BLOB
                if(lobSize <= first_in_mem.length)
                    return ValueBLOB.buildBLOB(null,
                            Arrays.copyOf(first_in_mem, (int) lobSize), lobSize, preHash);
                try {
                    if(Files.exists(tempFile)) {//already creating file
                        Path newTemp = ValueLOB.tempDir.resolve("ESQL-LOB" + UUID.randomUUID() + ".bin");
                        try {
                            //create link with better speed/disk space
                            Files.createLink(newTemp, tempFile);
                        }
                        catch (UnsupportedOperationException e) {
                            //fall back to copy file.
                            Files.copy(tempFile, newTemp);
                        }
                        return new ValueBLOB(newTemp, EMPTY_BYTES, lobSize, preHash);
                    }
                    //empty case
                    return EMPTY_BLOB;
                } catch (IOException e) {
                    throw new RuntimeException("BLOB convert error "+e.toString());
                }
            default:
                throw new IllegalArgumentException("BLOB can not cast to other types");
        }
    }

    @Override
    public Types getType() {
        return Types.TYPE_BLOB;
    }

    @Override
    public String stringValue() {
        if(first_in_mem == null || lobSize == 0) //NULL LOB.
            return "";
        Base64.Encoder encoder = Base64.getEncoder();
        if(lobSize <= first_in_mem.length)
            return encoder.encodeToString(Arrays.copyOf(first_in_mem, (int) lobSize));
        if(tempFile != null && Files.exists(tempFile)) {
            StringBuilder sb = new StringBuilder();
            try(FileChannel fc = FileChannel.open(tempFile, StandardOpenOption.READ)) {
                ByteBuffer t_read = ByteBuffer.allocate(BASE64_BUFFER_SIZE);
                while (0 <= fc.read(t_read)) {
                    t_read.flip();
                    ByteBuffer t_out = encoder.encode(t_read);
                    sb.append(new String(t_out.array()));
                    t_read.compact();
                    if(sb.length() >= MAX_CONVERTIBLE_TO_STRING_SIZE*4/3)
                        break;
                }
            } catch (IOException e) {
                throw new RuntimeException("LOB temp read error. "+e.toString());
            }
            return sb.toString();
        }
        throw new IllegalStateException("Temp file gone, in_memory data does not have full data length.");
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if(tempFile != null && Files.exists(tempFile)) {
            return Files.newInputStream(tempFile, StandardOpenOption.READ);
        }
        if(lobSize > first_in_mem.length) {
            throw new IllegalStateException("Temp file gone, in_memory data does not have full data length.");
        }
        return new ByteArrayInputStream(first_in_mem, 0, (int) lobSize);
    }



    protected static InputStream cascadeDigestInput(InputStream source, MessageDigest... hash) {
        DigestInputStream[] dis = new DigestInputStream[hash.length];
        for(int i = 0;i<hash.length;i++) {
            //serial of digest
            dis[i] = new DigestInputStream(i==0 ? source : dis[i-1], hash[i]);
        }
        return dis[dis.length-1];
    }

    @Override
    public String toString() {
        byte[] fh = null;
        int algo = 0;
        for (; algo < preHash.length; algo ++) {
            fh = preHash[algo];
            if(fh != null)
                break;
        }

        Base64.Encoder encoder = Base64.getEncoder();
        StringBuilder sb = new StringBuilder("(blob, size=");
        sb.append(lobSize);
        if(fh != null)
            sb.append(", ").append(HASHES[algo]).append(" ").append(ValueBytes.bytesToHex(fh));
        sb.append(")");
        if(first_in_mem.length>0)
            sb.append(" ").append(encoder.encodeToString(first_in_mem));
        if(lobSize> first_in_mem.length)
            sb.append(" ").append((lobSize- first_in_mem.length)).append(" bytes remaining.");
        return sb.toString();
    }

    @Override
    Path getTempFile() {
        return tempFile;
    }

    /**
     * close LOB by delete temporally file (if exists)
     * close input stream (if open)
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        if(tempFile != null) {
            Files.deleteIfExists(tempFile);
        }
    }

    @Override
    public byte[] forceHash(int algo) throws NoSuchAlgorithmException, IOException {
        if(preHash.length >= algo+1 && preHash[algo] != null)
            return preHash[algo];
        //calc if no pre-calc
        MessageDigest hash = MessageDigest.getInstance(HASHES[algo]);
        if(lobSize <= first_in_mem.length)
            return preHash[algo] = hash.digest(first_in_mem);
        try (InputStream is = Files.newInputStream(tempFile, StandardOpenOption.READ);
             DigestInputStream dis = new DigestInputStream(is, hash)) {
            byte[] buff = new byte[1024];
            long acc_read = 0;
            int nread;
            while (0<= (nread = dis.read(buff))) {
                acc_read += nread;
            }
            if(acc_read != lobSize)
                throw new IllegalStateException("temp read bytes differs lobSize");
        }
        return preHash[algo] = hash.digest();
    }

    static class ValueBLOBonDemand extends ValueBLOB {
        //lock for concurrency read from lobSource control
        protected final ReentrantLock lock;
        protected final AtomicBoolean closed;
        protected final InputStream lobSourceInput;
        protected final MessageDigest[] hash;
        protected final int[] hashAlgos;
        protected final InputStream digestSourceInput;

        ValueBLOBonDemand(InputStream lobSourceInput, Path tempFile, long lobSize, int...hashAlgos) throws NoSuchAlgorithmException {
            super(tempFile, EMPTY_BYTES, lobSize, new byte[HASHES.length][]);
            this.lock = new ReentrantLock();
            this.closed = new AtomicBoolean(false);
            this.lobSourceInput = lobSourceInput;
            this.hashAlgos = hashAlgos;
            if(hashAlgos.length>0) {
                hash = new MessageDigest[hashAlgos.length];
                for(int i = 0;i<hashAlgos.length;i++) {
                    //serial of digest
                    hash[i] = MessageDigest.getInstance(HASHES[hashAlgos[i]]);
                }
                digestSourceInput = cascadeDigestInput(lobSourceInput, hash);
            }
            else {
                hash = new MessageDigest[0];
                digestSourceInput = lobSourceInput;
            }
        }

        private void copyInputToTemp() {
            lock.lock();
            try {
                if(!Files.exists(tempFile)) //check again
                    Files.copy(digestSourceInput, tempFile);
                closed.set(true); //can not copy again
                //hash update
                for (int i = 0; i<hash.length;i++) {
                    if(hash[i] != null)
                        preHash[hashAlgos[i]] = hash[i].digest();
                }
            } catch (IOException e) {
                throw new RuntimeException("LOB Copy error. "+e.toString());
            } finally {
                lock.unlock();
            }
        }

        @Override
        public String stringValue() {
            Base64.Encoder encoder = Base64.getEncoder();
            //first in mem; should never be here due to zero first_in_mem
            if(lobSize <= first_in_mem.length) {
                return encoder.encodeToString(first_in_mem);
            }
            if(Files.exists(tempFile))
                return super.stringValue();
            if(closed.get())
                throw new IllegalStateException("LOB onDemand has closed");
            //read direct from InputStream
            if(lobSourceInput.markSupported()) {
                lock.lock();
                try {
                    lobSourceInput.mark((int) Math.min(MAX_CONVERTIBLE_TO_STRING_SIZE+BASE64_BUFFER_SIZE, lobSize));
                    StringBuilder sb = new StringBuilder();
                    try {
                        byte[] b_read = new byte[BASE64_BUFFER_SIZE];
                        int nRead;
                        while (0 <= (nRead = lobSourceInput.read(b_read))) {
                            if(nRead < b_read.length)
                                sb.append(encoder.encodeToString(Arrays.copyOf(b_read, nRead)));
                            else
                                sb.append(encoder.encodeToString(b_read));
                            if(sb.length() >= MAX_CONVERTIBLE_TO_STRING_SIZE*4/3)
                                break;
                        }
                    }
                    finally {
                        //back to the old position
                        lobSourceInput.reset();
                    }
                    return sb.toString();
                } catch (IOException e) {
                    throw new RuntimeException("LOB Input Source read error. "+e.toString());
                } finally {
                    lock.unlock();
                }
            }
            copyInputToTemp();
            return super.stringValue();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            if(lobSize <= first_in_mem.length) {
                return new ByteArrayInputStream(first_in_mem, 0, (int) lobSize);
            }

            if(Files.exists(tempFile)) {
                return Files.newInputStream(tempFile, StandardOpenOption.READ);
            }
            //set close because source input should not read anyway.
            closed.set(true);
            //read and digest
            return digestSourceInput;
        }

        @Override
        public void close() throws IOException {
            closed.set(true);
            digestSourceInput.close();
            lobSourceInput.close();
            super.close();
        }

        @Override
        public byte[] forceHash(int algo) throws NoSuchAlgorithmException, IOException {
            if(Files.exists(tempFile))
                return super.forceHash(algo);
            if(preHash.length >= algo+1 && preHash[algo] != null)
                return preHash[algo];
            if(closed.get())
                throw new IllegalStateException("LOB onDemand has closed");
            if(lobSourceInput.markSupported() && lobSize <= MAX_CONVERTIBLE_TO_STRING_SIZE) {
                MessageDigest hash = MessageDigest.getInstance(HASHES[algo]);
                lock.lock();
                try {
                    lobSourceInput.mark((int) lobSize);
                    try {
                        ReadableByteChannel channel = Channels.newChannel(lobSourceInput);
                        ByteBuffer buff = ByteBuffer.allocate(1024);
                        int n_read;
                        while (0<=(n_read = channel.read(buff))) {
                            buff.flip();
                            if (n_read > 0)
                                hash.update(buff);
                            buff.compact();
                        }
                    }
                    finally {
                        //back to the old position
                        lobSourceInput.reset();
                    }
                    return preHash[algo] = hash.digest();
                } catch (IOException e) {
                    throw new RuntimeException("LOB Input Source read error. "+e.toString());
                } finally {
                    lock.unlock();
                }
            }
            copyInputToTemp();
            return super.forceHash(algo);
        }

        @Override
        public Value convertTo(Types type) {
            if(this.isNull())
                return Value.nullOf(type);
            switch (type) {
                case TYPE_STRING:
                case TYPE_NSTRING:
                    return ValueString.buildString(type, this.stringValue());
                default:
                    if(Files.exists(tempFile)) //already created file, convert by super class.
                        return super.convertTo(type);
                    if(closed.get())
                        throw new IllegalStateException("LOB onDemand has closed");

                    try {
                        //save mark
                        if(digestSourceInput.markSupported() && lobSize <= MAX_CONVERTIBLE_TO_STRING_SIZE) {
                            lock.lock();
                            try {
                                digestSourceInput.mark((int) (BASE64_BUFFER_SIZE + lobSize));
                                try {
                                    switch (type) {
                                        case TYPE_CLOB:
                                        case TYPE_NCLOB:
                                            //create new CLOB of base64 string
                                            Base64.Encoder encoder = Base64.getEncoder();
                                            try (ValueCLOBCreator creator = ValueLOB.getCLOBCreator(Types.TYPE_NCLOB.equals(type), findConvenienceAlgorithms())) {
                                                ReadableByteChannel channel = Channels.newChannel(digestSourceInput);
                                                ByteBuffer buff = ByteBuffer.allocate(BASE64_BUFFER_SIZE);
                                                int n_read;
                                                while (0 <= (n_read = channel.read(buff))) {
                                                    buff.flip();
                                                    if (n_read > 0)
                                                        creator.writeToLOB(new String(encoder.encode(buff).array()));
                                                    buff.compact();
                                                }
                                                return creator.buildCLOB();
                                            }
                                        case TYPE_BYTES: //read all bytes
                                            return ValueBLOB.load(digestSourceInput).convertTo(Types.TYPE_BYTES);
                                        case TYPE_BLOB:
                                            //create a new BLOB
                                            return ValueBLOB.load(digestSourceInput, hashAlgos);
                                        default:
                                            throw new IllegalArgumentException("BLOB can not cast to other types");
                                    }
                                } finally {
                                    digestSourceInput.reset();
                                    //hash update
                                    for (int i = 0; i<hash.length;i++) {
                                        if (hash[i] != null)
                                            preHash[hashAlgos[i]] = hash[i].digest();
                                    }
                                }
                            }
                            catch (IOException e) {
                                throw new RuntimeException("LOB Input Source read error. "+e.toString());
                            }
                            finally {
                                lock.unlock();
                            }
                        }

                        copyInputToTemp();
                        return super.convertTo(type);
                    } catch (NoSuchAlgorithmException e) {
                        throw new RuntimeException("BLOB convert error "+e.toString());
                    }
            }
        }
    }
}
