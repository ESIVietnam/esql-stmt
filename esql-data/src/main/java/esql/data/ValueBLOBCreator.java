package esql.data;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class ValueBLOBCreator implements Closeable {
    ByteBuffer first_block = ByteBuffer.allocate(ValueLOB.MAX_BUFFERED_SIZE);
    private FileChannel fo = null;
    private Path tempFile = null;
    private long lobSize = 0;
    private MessageDigest[] hasher = {};
    private int[] hashAlgos = {};

    ValueBLOBCreator(int... hashAlgorithms) {
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

    public ValueBLOBCreator writeToLOB(ByteBuffer data) throws IOException {
        if (!data.hasRemaining())
            return this;
        //HASH calculation
        if (data.hasArray()) {
            byte[] temp_array = data.array();
            for(MessageDigest hash: hasher)
                hash.update(temp_array, data.position(), data.limit());
        } else {
            data.mark();
            while (data.hasRemaining()) {
                byte d = data.get();
                for(MessageDigest hash: hasher)
                    hash.update(d);
            }
            data.reset();
        }
        lobSize += data.remaining();
        if (first_block.hasRemaining()) {
            int remain = first_block.remaining();
            if (remain >= data.remaining()) {
                first_block.put(data);
                return this;
            } else
                while (first_block.hasRemaining())
                    first_block.put(data.get());
        }
        //check to write first time
        if (fo == null) {
            tempFile = Files.createTempFile(ValueLOB.tempDir, "ESQL-LOB", ".bin");
            //write first block
            fo = FileChannel.open(tempFile, StandardOpenOption.WRITE);
            first_block.flip();
            while (first_block.hasRemaining())
                fo.write(first_block);
            //the position is limit now.
        }
        while (data.hasRemaining())
            fo.write(data);
        return this;
    }

    public ValueLOB buildBLOB() {
        if (lobSize == 0)
            return ValueBLOB.EMPTY_BLOB;
        first_block.flip();

        byte[] first_in_mem = new byte[first_block.limit()];
        first_block.get(first_in_mem);

        byte[][] hash_holder = new byte[ValueLOB.HASHES.length][];
        for(int i = 0; i<hasher.length;i++)
            hash_holder[hashAlgos[i]] = hasher[i].digest();
        return ValueBLOB.buildBLOB(tempFile, first_in_mem, lobSize,
                hash_holder);
    }

    @Override
    public void close() throws IOException {
        if (fo != null && fo.isOpen())
            fo.close();
    }
}
