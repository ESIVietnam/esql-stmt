package esql.data;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class ValueLOB extends Value implements Closeable {
    public static final int MD5 = 0;
    public static final int SHA1 = 1;
    public static final int SHA256 = 2;
    public static final int SHA384 = 3;
    public static final int SHA512 = 4;
    static final String[] HASHES = {
            "MD5", "SHA-1","SHA-256", "SHA-384", "SHA-512"
    };

    public static final int DEFAULT_HASH_ALGO = MD5;
    public static final int[] DEFAULT_HASH_ALGOS = { MD5 };

    public static final int MAX_BUFFERED_SIZE =
           Integer.parseInt(Optional.ofNullable( System.getenv("ESQL_LOB_BUFFER_SIZE"))
                   .orElse(String.valueOf( 1024*32)));//32KB in-memory by default
    public static final long MAX_CONVERTIBLE_TO_STRING_SIZE =
            Long.parseLong(Optional.ofNullable( System.getenv("ESQL_LOB_TO_STRING_SIZE"))
                    .orElse(String.valueOf( 1024*1024*32)));//First 32Megabyte can convert to String.
    static Path tempDir = Paths.get(
            Optional.ofNullable( System.getenv("ESQL_LOB_TEMP_DIR"))
                    .orElse(System.getProperty("java.io.tmpdir")));

    static final byte[][] EMPTY_HASHES = {
            ValueBytes.hexToBytes("d41d8cd98f00b204e9800998ecf8427e"),
            ValueBytes.hexToBytes("da39a3ee5e6b4b0d3255bfef95601890afd80709"),
            ValueBytes.hexToBytes("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"),
            ValueBytes.hexToBytes("38b060a751ac96384cd9327eb1b1e36a21fdb71114be07434c0cc7bf63f6e1da274edebfe76f65fbd51ad2f14898b95b"),
            ValueBytes.hexToBytes("cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce47d0d13c5d85f2b0ff8318d2877eec2f63b931bd47417a81a538327af927da3e")
    };

    static final byte[] EMPTY_BYTES = new byte[0];
    static final char[] EMPTY_CHARS = new char[0];

    //LOB Size for BLOB is byte counting.
    //LOB Size for CLOB is character counting.
    protected final long lobSize;
    final byte[][] preHash;

    protected ValueLOB(long lobSize, byte[]... pre_hash) {
        this.lobSize = lobSize;
        this.preHash = pre_hash;
    }

    /**
     * create BLOBCreator (or builder) that to writing data for the new BLOB.
     *
     * @param hashAlgorithms
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static ValueBLOBCreator getBLOBCreator(int... hashAlgorithms) throws NoSuchAlgorithmException {
        return new ValueBLOBCreator(hashAlgorithms.length == 0 ? DEFAULT_HASH_ALGOS: hashAlgorithms);
    }

    /**
     *
     * create a CLOBCreator with national char (to build NCLOB).
     *
     * @param national true to create NCLOB, otherwise CLOB
     * @param hashAlgorithms hash to digest
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static ValueCLOBCreator getCLOBCreator(boolean national, int... hashAlgorithms) throws NoSuchAlgorithmException {
        return new ValueCLOBCreator(national, hashAlgorithms.length == 0 ? DEFAULT_HASH_ALGOS: hashAlgorithms);
    }

    /**
     * get a convenience hash (inherit or default)
     *
     * @return hashAlgos.
     */

    protected int[] findConvenienceAlgorithms() {
        var used = IntStream.range(0, HASHES.length)
                .filter(t -> preHash[t] != null).toArray();
        if(used.length == 0)
            return DEFAULT_HASH_ALGOS;
        return used;
    }

    @Override
    public boolean isEmpty() {
        return isNull() || this.lobSize == 0;
    }

    @Override
    public int compareTo(Value o) {
        if(o == this)
            return 0;
        //only compare LOBs
        if(o instanceof ValueLOB) {
            int c = Long.compareUnsigned(this.size(), ((ValueLOB) o).size());
            if(c != 0)
                return c;
            if(preHash.length > 0 && ((ValueLOB) o).preHash.length > 0) { //compare hashed
                for(int algos = 0;algos < HASHES.length; algos++) {
                    if(preHash.length <= algos+1 || ((ValueLOB) o).preHash.length <= algos+1)
                        break;
                    byte[] h1 = preHash[algos];
                    byte[] h2 = ((ValueLOB) o).preHash[algos];
                    if(h1 != null && h2 != null)
                        return Arrays.compare(h1, h2);
                }
            }

            throw new IllegalStateException("No available hashes to compare objects");
            //TODO: force comparing by hash, it is slow for large, I still considering...
            /*try {
                return Arrays.compare(forceHash(MD5), ((ValueLOB) o).forceHash(MD5));
            } catch (IOException | NoSuchAlgorithmException e) {
                throw new RuntimeException("forceHash for compare error "+e.toString());
            }*/
        }
        //LOB always bigger than others
        return 1;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null)
            return false;
        if(obj == this)
            return true;
        if(obj instanceof ValueLOB) {
            if(this.size() != ((ValueLOB) obj).size())
                return false;
            if (preHash.length > 0 && ((ValueLOB) obj).preHash.length > 0) { //compare hashed
                for (int algos = 0; algos < HASHES.length; algos++) {
                    if (preHash.length <= algos + 1 || ((ValueLOB) obj).preHash.length <= algos + 1)
                        break;
                    byte[] h1 = preHash[algos];
                    byte[] h2 = ((ValueLOB) obj).preHash[algos];
                    if (h1 != null && h2 != null)
                        return Arrays.equals(h1, h2);
                }
            }
        }
        return false;
    }

    /**
     * get a new input stream to read from LOB
     *
     * @return new/backed input stream;
     * @throws IOException
     */
    public abstract InputStream getInputStream() throws IOException;

    /**
     * LOB length in bytes for BLOB and chars for CLOB.
     *
     * @return number of bytes/chars
     */
    public long size() {
        return lobSize;
    }

    /**
     * Get pre-calculate hash by algorithm.
     * If result is null, meaning no register to calculate it or not yet calculable.
     *
     * To force calculating use <b>forceHash()</b>
     *
     * @param algo
     * @return null if no calculated
     */
    public byte[] getHash(int algo) {
        if(preHash.length < algo+1 || preHash[algo] == null)
            return null;
        return Arrays.copyOf(preHash[algo], preHash[algo].length);
    }

    /**
     * ensure that calculate hash (md5, sha1, sha256, etc) and return correct value.
     * it may force the LOB to calculate registered hashes (if not calculated yet).
     *
     * @return true if ensure, false is it can not ensure.
     * @throws IOException
     */
    public abstract byte[] forceHash(int algo) throws IOException, NoSuchAlgorithmException;

    /**
     * method for internal use.
     *
     * @return temporally file path
     */
    abstract Path getTempFile();
}
