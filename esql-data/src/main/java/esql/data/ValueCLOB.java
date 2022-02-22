package esql.data;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public class ValueCLOB extends ValueLOB {

    public static final ValueCLOB NULL_CLOB = new ValueCLOB(null, null, 0L, false);
    public static final ValueCLOB NULL_NCLOB = new ValueCLOB(null, null, 0L, true);
    public static final ValueCLOB EMPTY_CLOB = new ValueCLOB(null, EMPTY_CHARS, 0L, false, EMPTY_HASHES[MD5], EMPTY_HASHES[SHA1], EMPTY_HASHES[SHA256], EMPTY_HASHES[SHA384], EMPTY_HASHES[SHA512]);
    public static final ValueCLOB EMPTY_NCLOB = new ValueCLOB(null, EMPTY_CHARS, 0L, true, EMPTY_HASHES[MD5], EMPTY_HASHES[SHA1], EMPTY_HASHES[SHA256], EMPTY_HASHES[SHA384], EMPTY_HASHES[SHA512]);

    protected final char[] first_in_mem;
    protected final Path tempFile;
    final boolean national;

    private ValueCLOB(Path tempFile, char[] first_in_mem, long stringLen, boolean national, byte[]... preHash) {
        super(stringLen, preHash);
        this.first_in_mem = first_in_mem;
        this.tempFile = tempFile;
        this.national = national;
    }

    public static ValueCLOB wrap(String data, boolean national, int...hashAlgos) throws NoSuchAlgorithmException {
        byte[][] hash_holder = new byte[HASHES.length][];
        byte[] buff = data.getBytes(ValueString.stringCharset(national));
        for(int i = 0;i<hashAlgos.length;i++) {
            //digest
            MessageDigest hash = MessageDigest.getInstance(HASHES[hashAlgos[i]]);
            hash_holder[hashAlgos[i]] = hash.digest(buff);
        }
        return new ValueCLOB(null, data.toCharArray(), data.length(), national, hash_holder);
    }

    /**
     * wrapping a reader as CLOB, read/hash calculate on-demand.
     * 
     * @param lobSourceReader
     * @param lobSize
     * @param national
     * @param hashAlgorithms
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static ValueCLOB wrap(Reader lobSourceReader, long lobSize, boolean national, int... hashAlgorithms) throws NoSuchAlgorithmException {
        if (lobSize == 0) //nothing to read
            return national ?  ValueCLOB.EMPTY_NCLOB : ValueCLOB.EMPTY_CLOB;
        //tempFile trong truong hop nay thi khong create, ma dung UUID
        return new ValueCLOB.ValueCLOBonDemand(lobSourceReader, ValueLOB.tempDir.resolve("ESQL-CLOB"+ UUID.randomUUID()+".txt"), lobSize, national, hashAlgorithms);
    }

    /**
     * create a new CLOB from reader.
     *
     * @param lobSourceReader
     * @param national
     * @param hashAlgorithms
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public static ValueCLOB load(Reader lobSourceReader, boolean national, int... hashAlgorithms) throws NoSuchAlgorithmException, IOException {
        if(hashAlgorithms.length == 0)
            hashAlgorithms = DEFAULT_HASH_ALGOS;

        try(ValueCLOBCreator creator = ValueLOB.getCLOBCreator(national,
                hashAlgorithms)) {

            CharBuffer buff = CharBuffer.allocate(1024);
            while (0 <= lobSourceReader.read(buff)) {
                creator.writeToLOB(buff);
            }
            return creator.buildCLOB();
        }
    }

    static ValueCLOB buildCLOB(Path tempFile, char[] first_in_mem, long stringLen, boolean national, byte[]... preHash) {
        return new ValueCLOB(tempFile, first_in_mem, stringLen, national, preHash);
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
                //Clone new CLOB
                if(lobSize <= first_in_mem.length)
                    return new ValueCLOB(null, Arrays.copyOf(first_in_mem, (int) lobSize), lobSize,
                            Types.TYPE_NCLOB.equals(type) ? true : false, preHash);
                try {
                    if(Files.exists(tempFile)) {//creating new file
                        if(is(type)) { //same type, copy file for faster
                            Path newTemp = ValueLOB.tempDir.resolve("ESQL-CLOB" + UUID.randomUUID() + ".txt");
                            try {
                                //create link with better speed/disk space
                                Files.createLink(newTemp, tempFile);
                            } catch (UnsupportedOperationException e) {
                                //fall back to copy file.
                                Files.copy(tempFile, newTemp);
                            }
                            return new ValueCLOB(newTemp, EMPTY_CHARS, lobSize,
                                    Types.TYPE_NCLOB.equals(type), preHash);
                        }
                        else {
                            //from CLOB to NCLOB or vice versa
                            try(ValueCLOBCreator creator = ValueLOB.getCLOBCreator(Types.TYPE_NCLOB.equals(type),
                                    IntStream.range(0, HASHES.length).filter(t -> preHash[t] != null).findFirst()
                                            .orElse(MD5)
                            )) {
                                try(Reader reader =
                                            new FileReader(tempFile.toFile(), ValueString.stringCharset(national))) {
                                    CharBuffer buff = CharBuffer.allocate(1024);
                                    while (0<=reader.read(buff)) {
                                        creator.writeToLOB(buff);
                                    }
                                }
                                return creator.buildCLOB();
                            }
                        }
                    }
                    //empty case
                    return  Types.TYPE_NCLOB.equals(type) ? EMPTY_NCLOB : EMPTY_CLOB;
                } catch (IOException | NoSuchAlgorithmException e) {
                    throw new RuntimeException("CLOB convert error "+e.toString());
                }
            case TYPE_BYTES: //read all bytes to memory
                if(lobSize<=MAX_CONVERTIBLE_TO_STRING_SIZE/2) {
                    try {
                        if(lobSize <= first_in_mem.length)
                            return ValueBytes.buildBytes(String.valueOf(first_in_mem, 0, (int) lobSize)
                                    .getBytes(ValueString.stringCharset(national)));
                        if(Files.exists(tempFile)) { //read text to big memory
                            return ValueBytes.buildBytes(Files.readAllBytes(tempFile));
                        }
                        return ValueBytes.EMPTY_BINARY_STRING;
                    } catch (IOException e) {
                        throw new RuntimeException("CLOB convert readAllBytes error "+e.toString());
                    }
                }
                throw new IllegalStateException("CLOB is too large to convert");
            case TYPE_BLOB:
                //create a new BLOB
                if(lobSize <= first_in_mem.length)
                    return ValueBLOB.buildBLOB(null,
                            String.valueOf(first_in_mem, 0, (int) lobSize)
                            .getBytes(ValueString.stringCharset(national)), lobSize, preHash);
                try {
                    if(Files.exists(tempFile)) {//already created file, clone it
                        Path newTemp = ValueLOB.tempDir.resolve("ESQL-LOB" + UUID.randomUUID() + ".bin");
                        try {
                            //create link with better speed/disk space
                            Files.createLink(newTemp, tempFile);
                        }
                        catch (UnsupportedOperationException e) {
                            //fall back to copy file.
                            Files.copy(tempFile, newTemp);
                        }
                        return ValueBLOB.buildBLOB(newTemp, EMPTY_BYTES, lobSize, preHash);
                    }
                    //empty case
                    return ValueBLOB.EMPTY_BLOB;
                } catch (IOException e) {
                    throw new RuntimeException("CLOB convert error "+e.toString());
                }
            default:
                throw new IllegalArgumentException("CLOB can not cast to other types");
        }
    }

    @Override
    public Types getType() {
        return national ? Types.TYPE_NCLOB : Types.TYPE_CLOB;
    }

    @Override
    public String stringValue() {
        if(lobSize <= first_in_mem.length)
            return new String(first_in_mem,0, (int) lobSize);

        if(tempFile != null && Files.exists(tempFile)) {
            try {
                if(lobSize <= MAX_CONVERTIBLE_TO_STRING_SIZE/2)
                    return new String(Files.readAllBytes(tempFile), ValueString.stringCharset(national));

                //only read first n bytes (converted to lines)
                StringBuilder sb = new StringBuilder();
                try(Reader reader =
                        new FileReader(tempFile.toFile(), ValueString.stringCharset(national))) {
                    CharBuffer buff = CharBuffer.allocate(1024);
                    while (0<=reader.read(buff)) {
                        sb.append(buff); //append here
                        if (sb.length() > MAX_CONVERTIBLE_TO_STRING_SIZE / 2)
                            break;
                    }
                }
                return sb.toString();
            } catch (IOException e) {
                throw new RuntimeException("LOB temp read error. "+e.toString());
            }
        }
        throw new IllegalStateException("Temp file gone, in_memory data does not have full CLOB length.");
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if(tempFile != null && Files.exists(tempFile)) {
            //don't care about Charset, provides as is.
            return Files.newInputStream(tempFile, StandardOpenOption.READ);
        }
        if(lobSize > first_in_mem.length) {
            throw new IllegalStateException("Temp file gone, in_memory data does not have full data length.");
        }
        byte[] t_array = ValueString.stringCharset(national).encode(CharBuffer.wrap(first_in_mem)).array();
        return new ByteArrayInputStream(t_array);
    }

    /**
     * CLOB can be read by a Reader
     * @return reader to read data
     * @throws IOException
     */
    public Reader getReader() throws IOException {
        if(tempFile != null && Files.exists(tempFile)) {
            //return charset
            return Files.newBufferedReader(tempFile, ValueString.stringCharset(national));
        }
        if(lobSize > first_in_mem.length) {
            throw new IllegalStateException("Temp file gone, in_memory data does not have full data length.");
        }
        return new CharArrayReader(first_in_mem);
    }

    @Override
    public byte[] forceHash(int algo) throws IOException, NoSuchAlgorithmException {
        return new byte[0];
    }

    @Override
    Path getTempFile() {
        return tempFile;
    }

    @Override
    public int compareTo(Value o) {
        return 0;
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

        StringBuilder sb = new StringBuilder();
        if(national)
            sb.append("(nclob, size=");
        else
            sb.append("(clob, size=");
        sb.append(lobSize);
        if(fh != null)
            sb.append(", ").append(HASHES[algo]).append(" ").append(ValueBytes.bytesToHex(fh));
        sb.append(")");
        if(first_in_mem.length>0)
            sb.append(" ").append(first_in_mem);
        if(lobSize> first_in_mem.length)
            sb.append(" ").append((lobSize- first_in_mem.length)).append(" chars remaining.");
        return sb.toString();
    }

    @Override
    public void close() throws IOException {
        if(tempFile != null) {
            Files.deleteIfExists(tempFile);
        }
    }

    static class DigestReader extends FilterReader {
        private final Charset charset;
        private final MessageDigest[] hash;

        DigestReader(Reader origReader, Charset charset, MessageDigest... hash) {
            super(origReader);
            this.charset = charset;
            this.hash = hash;
        }

        @Override
        public int read() throws IOException {
            char theChar = (char) super.read();
            Arrays.stream(hash).forEach(h -> h.update(charset.encode(String.valueOf(theChar))));
            return theChar;
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            int read = super.read(cbuf, off, len);
            if(read > 0)
                Arrays.stream(hash).forEach(h -> h.update(charset.encode(String.valueOf(cbuf, off, read))));
            return read;
        }
    }

    protected static Reader cascadeDigestReader(Reader origReader, Charset charset, MessageDigest... hash) {
        return new DigestReader(origReader, charset, hash);
    }

    static class InputWrapReader extends InputStream {
        private final Reader origReader;
        private final Charset charset;
        private final CharBuffer cbuff;
        private final ByteBuffer buff;

        InputWrapReader(Reader origReader, Charset charset) {
            this.charset = charset;
            this.origReader = origReader;
            this.cbuff = CharBuffer.allocate(1024);
            this.buff = ByteBuffer.allocate(cbuff.capacity()*6); //bigger sixes time than text
        }

        private int nextRead() throws IOException {
            buff.compact();
            cbuff.compact();
            int r = origReader.read(cbuff);
            cbuff.flip();
            if(r > 0) {
                buff.put(charset.encode(cbuff));
            }
            buff.flip();
            return r;
        }

        @Override
        public int read() throws IOException {
            while (!buff.hasRemaining()) {//empty, next read
                if(nextRead() < 0)
                    return -1;
            }
            return buff.get();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if(!buff.hasRemaining()) {//empty, next read
                int r = nextRead();
                if(r <= 0)
                    return r;
            }
            len = Math.min(len, buff.remaining());
            int old_pos = buff.position();
            buff.get(b, off, len);
            return buff.position() - old_pos;
        }
    }

    static class ValueCLOBonDemand extends ValueCLOB {
        //lock for concurrency read from lobSource control
        protected final ReentrantLock lock;
        protected final AtomicBoolean closed;
        protected final Reader lobSourceReader;
        protected final MessageDigest[] hash;
        protected final int[] hashAlgos;
        protected final Reader digestSourceReader;

        ValueCLOBonDemand(Reader lobSourceReader, Path tempFile, long stringLen, boolean national, int... hashAlgos) throws NoSuchAlgorithmException {
            super(tempFile, EMPTY_CHARS, stringLen, national, new byte[HASHES.length][]);
            this.lobSourceReader = lobSourceReader;
            this.lock = new ReentrantLock();
            this.closed = new AtomicBoolean(false);
            this.hashAlgos = hashAlgos;
            if(hashAlgos.length>0) {
                hash = new MessageDigest[hashAlgos.length];
                for(int i = 0;i<hashAlgos.length;i++) {
                    //serial of digest
                    hash[i] = MessageDigest.getInstance(HASHES[hashAlgos[i]]);
                }
                digestSourceReader = cascadeDigestReader(lobSourceReader, ValueString.stringCharset(national), hash);
            }
            else {
                hash = new MessageDigest[0];
                digestSourceReader = lobSourceReader;
            }
        }

        private void copyReaderToTemp() {
            lock.lock();
            try {
                if(!Files.exists(tempFile)) {//check again
                    digestSourceReader.transferTo(new FileWriter(tempFile.toFile(), ValueString.stringCharset(national)));
                }
                closed.set(true); //can not copy again
            } catch (IOException e) {
                throw new RuntimeException("LOB Copy error. "+e.toString());
            } finally {
                lock.unlock();
            }
        }

        @Override
        public String stringValue() {
            //first in mem; should never be here due to zero first_in_mem
            if(lobSize <= first_in_mem.length) {
                return new String(first_in_mem);
            }
            if(Files.exists(tempFile))
                return super.stringValue();
            if(closed.get())
                throw new IllegalStateException("CLOB onDemand has closed");
            //read direct from Reader
            if(lobSourceReader.markSupported()) {
                lock.lock();
                try {
                    lobSourceReader.mark((int) Math.min(MAX_CONVERTIBLE_TO_STRING_SIZE/2+1024, lobSize));
                    StringBuilder sb = new StringBuilder();
                    try {
                        char[] b_read = new char[1024];
                        int nRead;
                        while (0 <= (nRead = lobSourceReader.read(b_read))) {
                            if(nRead < b_read.length)
                                sb.append(Arrays.copyOf(b_read, nRead));
                            else
                                sb.append(b_read);
                            if(sb.length() >= MAX_CONVERTIBLE_TO_STRING_SIZE/2)
                                break;
                        }
                    }
                    finally {
                        //back to the old position
                        lobSourceReader.reset();
                    }
                    return sb.toString();
                } catch (IOException e) {
                    throw new RuntimeException("CLOB Reader Source read error. "+e.toString());
                } finally {
                    lock.unlock();
                }
            }
            copyReaderToTemp();
            return super.stringValue();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            if(lobSize <= first_in_mem.length) {
                return new ByteArrayInputStream(ValueString.stringCharset(national)
                        .encode(CharBuffer.wrap(Arrays.copyOf(first_in_mem, (int) lobSize))).array());
            }

            if(Files.exists(tempFile)) {
                return Files.newInputStream(tempFile, StandardOpenOption.READ);
            }
            //set close because source input should not read anymore.
            closed.set(true);
            return new InputWrapReader(digestSourceReader, ValueString.stringCharset(national));
        }

        @Override
        public Reader getReader() throws IOException {
            if(lobSize <= first_in_mem.length) {
                return new CharArrayReader(first_in_mem, 0, (int) lobSize);
            }

            if(Files.exists(tempFile)) {
                return new BufferedReader(new FileReader(tempFile.toFile(), ValueString.stringCharset(national)));
            }
            //set close because source input should not read anyway.
            closed.set(true);
            return digestSourceReader;
        }

        @Override
        public void close() throws IOException {
            closed.set(true);
            lobSourceReader.close();
            digestSourceReader.close();
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
        public byte[] forceHash(int algo) throws NoSuchAlgorithmException, IOException {
            if(Files.exists(tempFile))
                return super.forceHash(algo);
            if(preHash.length >= algo+1 && preHash[algo] != null)
                return preHash[algo];
            if(closed.get())
                throw new IllegalStateException("LOB onDemand has closed");
            if(lobSourceReader.markSupported() && lobSize <= MAX_CONVERTIBLE_TO_STRING_SIZE/2) {
                MessageDigest hash = MessageDigest.getInstance(HASHES[algo]);
                lock.lock();
                try {
                    lobSourceReader.mark((int) lobSize);
                    try {
                        var buff = CharBuffer.allocate(1024);
                        int n_read;
                        while (0<=(n_read = lobSourceReader.read(buff)))
                            if(n_read>0) {
                                hash.update(ValueString.stringCharset(national).encode(buff));
                            }
                    }
                    finally {
                        //back to the old position
                        lobSourceReader.reset();
                    }
                    return preHash[algo] = hash.digest();
                } catch (IOException e) {
                    throw new RuntimeException("LOB Input Source read error. "+e.toString());
                } finally {
                    lock.unlock();
                }
            }
            copyReaderToTemp();
            return super.forceHash(algo);
        }
    }
}
