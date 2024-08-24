package esql.data;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ValueCLOBCreator  implements Closeable {
    private final boolean national;
    CharBuffer first_block = CharBuffer.allocate(ValueLOB.MAX_BUFFERED_SIZE);
    private FileChannel fo = null;
    private Path tempFile = null;
    private long length = 0;
    private MessageDigest[] hasher = {};
    private int[] hashAlgos = {};

    ValueCLOBCreator(boolean national, int... hashAlgorithms) throws NoSuchAlgorithmException {
        this.national = national;
        if(hashAlgorithms.length > 0) {
            hasher = new MessageDigest[hashAlgorithms.length];
            hashAlgos = new int[hashAlgorithms.length];
            for(int i=0;i < hashAlgorithms.length;i++) {
                try {
                    hashAlgos[i] = hashAlgorithms[i];
                    hasher[i] = ValueLOB.messageDigestFromAlgorithm(hashAlgorithms[i]);
                } catch (NoSuchAlgorithmException e) {
                    throw new AssertionError();
                }
            }
        }
    }

    public void writeToLOB(String s) throws IOException {
        ByteBuffer x = ValueString.stringCharset(national).encode(s);
        for(MessageDigest hash: hasher) {
            hash.update(x);
            x.rewind();
        }
        length += s.length();
        if (first_block.hasRemaining() &&  first_block.remaining() >= s.length()) {
            first_block.put(s);
            return;
        }
        //splitted to first_block and other.
        int len = Math.min(first_block.remaining(), s.length());
        if(len > 0) {
            first_block.put(s, 0, len);
            s = s.substring(len);
        }

        //check to write first time
        if (fo == null) {
            tempFile = Files.createTempFile(ValueLOB.tempDir, "ESQL-CLOB", ".txt");
            //write first block
            first_block.flip();
            fo = FileChannel.open(tempFile, StandardOpenOption.WRITE);
            ByteBuffer fb = ValueString.stringCharset(national).encode(first_block);
            while (fb.hasRemaining())
                fo.write(fb);
            //the position is limit now.
        }
        //convert again
        x = ValueString.stringCharset(national).encode(s);
        while (x.hasRemaining())
            fo.write(x);
    }

    public void writeToLOB(CharBuffer data) throws IOException {
        if (!data.hasRemaining())
            return;
        //HASH calculation
        ByteBuffer x = ValueString.stringCharset(national).encode(data);
        for(MessageDigest hash: hasher) {
            hash.update(x);
            x.rewind();
        }
        length += data.remaining();
        if (first_block.hasRemaining()) {
            int remain = first_block.remaining();
            if (remain >= data.remaining()) {
                first_block.put(data);
                return;
            } else
                while (first_block.hasRemaining())
                    first_block.put(data.get());
        }
        //check to write first time
        if (fo == null) {
            tempFile = Files.createTempFile(ValueLOB.tempDir, "ESQL-CLOB", ".txt");
            //write first block
            fo = FileChannel.open(tempFile, StandardOpenOption.WRITE);
            ByteBuffer fb = ValueString.stringCharset(national).encode(first_block);
            while (fb.hasRemaining())
                fo.write(fb);
            //the position is limit now.
        }
        //convert again
        x = ValueString.stringCharset(national).encode(data);
        while (x.hasRemaining())
            fo.write(x);
    }

    public ValueCLOB buildCLOB() {
        if (length == 0)
            return national ? ValueCLOB.EMPTY_NCLOB : ValueCLOB.EMPTY_CLOB;
        first_block.flip();
        char[] first_in_mem = new char[first_block.limit()];
        first_block.get(first_in_mem);

        byte[][] hash_holder = new byte[ValueLOB.HASHES.length][];
        for(int i = 0; i<hasher.length;i++)
            hash_holder[hashAlgos[i]] = hasher[i].digest();
        return ValueCLOB.buildCLOB(tempFile, first_in_mem, length, national,
                hash_holder);
    }

    @Override
    public void close() throws IOException {
        if (fo != null && fo.isOpen())
            fo.close();
    }
}
