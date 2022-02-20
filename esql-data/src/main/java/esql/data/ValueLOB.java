package esql.data;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Optional;

public abstract class ValueLOB extends Value implements Closeable {
    public static final int MD5 = 0;
    public static final int SHA1 = 1;
    public static final int SHA256 = 2;
    public static final int SHA384 = 3;
    public static final int SHA512 = 4;
    static final String[] HASHES = {
            "MD5", "SHA-1","SHA-256", "SHA-384", "SHA-512"
    };

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
    static final ValueBLOB NULL_BLOB = new ValueBLOB(null, 0L);
    static final ValueBLOB EMPTY_BLOB = new ValueBLOB(EMPTY_BYTES, 0L, EMPTY_HASHES[MD5], EMPTY_HASHES[SHA1], EMPTY_HASHES[SHA256], EMPTY_HASHES[SHA384], EMPTY_HASHES[SHA512]);

    static final char[] EMPTY_CHARS = new char[0];
    static final ValueCLOB NULL_CLOB = new ValueCLOB(null, null, 0L, 0L, false, null, null, null);
    static final ValueCLOB NULL_NCLOB = new ValueCLOB(null, null, 0L, 0L, true, null, null, null);
    static final ValueCLOB EMPTY_CLOB = new ValueCLOB(null, EMPTY_CHARS, 0L, 0L, false, EMPTY_HASHES[MD5], EMPTY_HASHES[SHA1], EMPTY_HASHES[SHA256], EMPTY_HASHES[SHA384], EMPTY_HASHES[SHA512]);
    static final ValueCLOB EMPTY_NCLOB = new ValueCLOB(null, EMPTY_CHARS, 0L, 0L, true, EMPTY_HASHES[MD5], EMPTY_HASHES[SHA1], EMPTY_HASHES[SHA256], EMPTY_HASHES[SHA384], EMPTY_HASHES[SHA512]);

    protected final long lobSize;
    final byte[][] preHash;

    protected ValueLOB(long lobSize, byte[]... pre_hash) {
        this.lobSize = lobSize;
        this.preHash = pre_hash;
    }

    public static ValueBLOBCreator getBLOBCreator(int... hashAlgorithms) throws NoSuchAlgorithmException {
        return new ValueBLOBCreator(hashAlgorithms);
    }

    @Override
    public boolean isEmpty() {
        return isNull() || this.lobSize == 0;
    }

    /**
     * get a new input stream to read from LOB
     *
     * @return new/backed input stream;
     * @throws IOException
     */
    public abstract InputStream getInputStream() throws IOException;

    /**
     * LOB size in bytes.
     *
     * @return
     */
    public long size() {
        return lobSize;
    }

    /**
     * get pre-calculate hash by algorithm.
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
     * ensure that calculate hash (md5, sha1, sha256, etc)
     *
     * @return true if ensure, false is it can not ensure.
     * @throws IOException
     */
    public abstract byte[] ensureHash(int algo) throws IOException, NoSuchAlgorithmException;

    /**
     * method for internal use.
     *
     * @return temporally file path
     */
    abstract Path getTempFile();
}
