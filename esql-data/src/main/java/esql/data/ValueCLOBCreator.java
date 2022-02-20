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
import java.util.Arrays;

public class ValueCLOBCreator  implements Closeable {
    CharBuffer first_block = CharBuffer.allocate(ValueLOB.MAX_BUFFERED_SIZE);
    private FileChannel fo = null;
    private Path tempFile = null;
    private long stringLen = 0;
    private long lobSize = 0;
    private MessageDigest[] hasher = {};
    private int[] hashAlgos = {};

    ValueCLOBCreator(int... hashAlgorithms) throws NoSuchAlgorithmException {
        if(hashAlgorithms.length > 0) {
            hasher = new MessageDigest[hashAlgorithms.length];
            hashAlgos = new int[hashAlgorithms.length];
            for(int i=0;i < hashAlgorithms.length;i++) {
                try {
                    hashAlgos[i] = hashAlgorithms[i];
                    hasher[i] = MessageDigest.getInstance(ValueLOB.HASHES[hashAlgorithms[i]]);
                } catch (NoSuchAlgorithmException e) {
                    throw new AssertionError();
                }
            }
        }
    }

    public void writeToLOB(CharBuffer data) throws IOException {
        if (!data.hasRemaining())
            return;
        //HASH calculation
        ByteBuffer x = ValueString.STRING_CONVERT_CHARSET.encode(data);
        if (x.hasArray()) {
            byte[] temp_array = x.array();
            for(MessageDigest hash: hasher)
                hash.update(temp_array, data.position(), data.limit());
        } else {
            x.mark();
            while (x.hasRemaining()) {
                byte d = x.get();
                for(MessageDigest hash: hasher)
                    hash.update(d);
            }
            x.reset();
        }
        stringLen += data.remaining();
        lobSize += x.remaining();
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
            tempFile = Files.createTempFile(ValueLOB.tempDir, "ESQL-LOB", ".bin");
            //write first block
            fo = FileChannel.open(tempFile, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            ByteBuffer fb = ValueString.STRING_CONVERT_CHARSET.encode(first_block);
            while (fb.hasRemaining())
                fo.write(fb);
            //the position is limit now.
        }
        //convert again
        x = ValueString.STRING_CONVERT_CHARSET.encode(data);
        while (x.hasRemaining())
            fo.write(x);
    }

    public ValueLOB buildCLOB(boolean national) {
        if (lobSize == 0)
            return national ? ValueLOB.EMPTY_NCLOB : ValueLOB.EMPTY_CLOB;
        int max = Arrays.stream(hashAlgos).max().orElse(-1);
        first_block.flip();
        if(max < 0)
            return new ValueCLOB(tempFile, Arrays.copyOf(first_block.array(), first_block.length()), lobSize, stringLen,
                    national);
        byte[][] hash_holder = new byte[max][];
        for(int i = 0; i<hasher.length;i++)
            hash_holder[hashAlgos[i]] = hasher[i].digest();
        return new ValueCLOB(tempFile, Arrays.copyOf(first_block.array(), first_block.length()), lobSize, stringLen,
                national, hash_holder);
    }

    @Override
    public void close() throws IOException {
        if (fo != null && fo.isOpen())
            fo.close();
    }
}
