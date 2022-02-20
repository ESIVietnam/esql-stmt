package esql.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
    final int BASE64_BUFFER_SIZE = 3 * 4096; //multiply with 3.
    protected final byte[] first_in_mem;
    protected final Path tempFile;

    ValueBLOB(byte[] in_mem, long lobSize, byte[]... pre_hash) {
        super(lobSize, pre_hash);
        this.first_in_mem = in_mem;
        this.tempFile = null;
    }

    ValueBLOB(Path tempFile, byte[] in_mem, long lobSize, byte[]... pre_hash) {
        super(lobSize, pre_hash);
        this.first_in_mem = in_mem; //first block of file
        this.tempFile = tempFile;
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
        //TODO: create convert
        return null;
    }

    @Override
    public Types getType() {
        return Types.TYPE_BLOB;
    }

    @Override
    public String stringValue() {
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
                throw new RuntimeException("LOB temp "+tempFile+" read error. "+e.getMessage());
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

    public static ValueBLOB wrap(InputStream lobSourceInput, long lobSize, int... hashAlgorithms) throws NoSuchAlgorithmException {
        if (lobSize == 0) //nothing to read
            return ValueLOB.EMPTY_BLOB;
        //tempFile trong truong hop nay thi khong create, ma dung UUID
        return new ValueBLOB.ValueBLOBonDemand(lobSourceInput, ValueLOB.tempDir.resolve("ESQL-LOB"+ UUID.randomUUID()+".bin"), lobSize, hashAlgorithms);
    }

    protected static InputStream cascadeDigestInput(InputStream source, MessageDigest... hash) {
        DigestInputStream[] dis = new DigestInputStream[hash.length];
        for(int i = 0;i<hash.length;i++) {
            //serial of digest
            dis[i] = new DigestInputStream(i==0 ? source : dis[i-1], hash[i]);
        }
        return dis[dis.length-1];
    }

    public static ValueBLOB load(InputStream lobSourceInput, int...hashAlgos) throws IOException, NoSuchAlgorithmException {
        //tempFile trong truong hop nay thi khong create, ma dung UUID
        Path temp = ValueLOB.tempDir.resolve("ESQL-LOB"+ UUID.randomUUID()+".bin");
        long lobSize = 0;
        int max = Arrays.stream(hashAlgos).max().orElse(-1);
        if(hashAlgos.length>0 && max >= 0) {
            byte[][] hash_holder = new byte[max+1][];
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
            return ValueLOB.EMPTY_BLOB;
        return new ValueBLOB(temp, EMPTY_BYTES, lobSize);
    }

    @Override
    public String toString() {
        Base64.Encoder encoder = Base64.getEncoder();
        if(lobSize> first_in_mem.length)
            return "(blob="+lobSize+")"+encoder.encodeToString(first_in_mem)+" remain "+(lobSize- first_in_mem.length)+"bytes";
        return "(blob="+lobSize+")"+encoder.encodeToString(first_in_mem);
    }

    @Override
    public int compareTo(Value o) {
        if(o == this)
            return 0;
        if(o instanceof ValueLOB)
            return Long.compareUnsigned(this.size(), ((ValueLOB) o).size());
        //LOB always bigger than others
        return 1;
    }

    @Override
    Path getTempFile() {
        return tempFile;
    }

    @Override
    public void close() throws IOException {
        if(tempFile != null) {
            Files.deleteIfExists(tempFile);
        }
    }

    @Override
    public byte[] ensureHash(int algo) throws NoSuchAlgorithmException, IOException {
        if(preHash.length >= algo+1 && preHash[algo] != null)
            return preHash[algo];
        //calc if no pre-calc
        MessageDigest hash = MessageDigest.getInstance(HASHES[algo]);
        if(lobSize <= first_in_mem.length)
            return hash.digest(first_in_mem);
        try (InputStream is = Files.newInputStream(tempFile, StandardOpenOption.READ);
             DigestInputStream dis = new DigestInputStream(is, hash))
        {
            byte[] buff = new byte[1024];
            long acc_read = 0;
            int nread;
            while (0<= (nread = dis.read(buff))) {
                acc_read += nread;
            }
            if(acc_read != lobSize)
                throw new IllegalStateException("temp read bytes differs lobSize");
        }
        return hash.digest();
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
            int max = Arrays.stream(hashAlgos).max().orElse(-1);
            this.hashAlgos = hashAlgos;
            if(hashAlgos.length>0 && max >= 0) {
                byte[][] hash_holder = new byte[max+1][];
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
                Files.copy(digestSourceInput, tempFile);
                closed.set(true); //can not copy again
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
                    throw new RuntimeException("LOB Input Source read error. "+e.getMessage());
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
            return lobSourceInput;
        }

        @Override
        public void close() throws IOException {
            closed.set(true);
            lobSourceInput.close();
            super.close();
        }

        @Override
        public byte[] getHash(int algo) {
            byte[] s = super.getHash(algo);
            if(s == null && hash.length>0)
                for (int i = 0; i<hash.length;i++) {
                    preHash[hashAlgos[i]] = hash[i].digest();
                }
            return preHash[algo];
        }

        @Override
        public byte[] ensureHash(int algo) throws NoSuchAlgorithmException, IOException {
            if(Files.exists(tempFile))
                return super.ensureHash(algo);
            if(closed.get())
                throw new IllegalStateException("LOB onDemand has closed");
            if(lobSourceInput.markSupported() && lobSize <= MAX_CONVERTIBLE_TO_STRING_SIZE) {
                MessageDigest hash = MessageDigest.getInstance(HASHES[algo]);
                lock.lock();
                try {
                    lobSourceInput.mark((int) lobSize);
                    try (DigestInputStream dis = new DigestInputStream(lobSourceInput, hash))
                    {
                        byte[] buff = new byte[1024];
                        while (0<=dis.read(buff));
                    }
                    finally {
                        //back to the old position
                        lobSourceInput.reset();
                    }
                    return hash.digest();
                } catch (IOException e) {
                    throw new RuntimeException("LOB Input Source read error. "+e.toString());
                } finally {
                    lock.unlock();
                }
            }
            copyInputToTemp();
            return super.ensureHash(algo);
        }
    }
}
