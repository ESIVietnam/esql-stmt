package esql.data;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

public class BLOBTest {

    @Test
    public void testSmallLob() throws IOException, NoSuchAlgorithmException {
        byte[] in = { '9' , 11, 32, 49, 0x7f, 32, 4, 65, 0, 10, 14, 15 };

        ValueBLOBCreator creator = ValueLOB.getBLOBCreator(ValueLOB.MD5);
        Path tempFile;
        try(ValueLOB v = creator.writeToLOB(ByteBuffer.wrap(in)).buildBLOB()) {
            tempFile = v.getTempFile();
            assertEquals(Base64.getEncoder().encodeToString(in), v.stringValue(), "LOB to string not same");
            assertEquals(in.length, (int) v.size(), "LOB size should correct");

            assertArrayEquals(MessageDigest.getInstance("MD5").digest(in), v.getHash(ValueLOB.MD5), "not correct MD5 pre-hash");
            assertNull(v.getHash(ValueLOB.SHA1)); //must null.
            assertArrayEquals(MessageDigest.getInstance("SHA-1").digest(in), v.ensureHash(ValueLOB.SHA1), "not correct SHA1 ensure hash");

            //LOB Read out
            byte[] buff = new byte[(int) v.size()];
            try (InputStream input = v.getInputStream()) {
                int n_read = input.read(buff);
                assertEquals(v.size(), n_read, "not same size on read");
            }
            assertArrayEquals(in, buff, "read back does not correct");
            System.out.append("testSmallLob size : ").append(NumberFormat.getNumberInstance().format(v.size()))
                    .append(" of MD5 ").append(ValueBytes.bytesToHex(v.getHash(ValueLOB.MD5)))
                    .append(" with toString: ").append(v.toString()).println();
        }
        assertNull(tempFile, "Temp file should not created.");
    }

    @Test
    public void testMediumLob() throws IOException, NoSuchAlgorithmException {
        byte[] in = new byte[ValueLOB.MAX_BUFFERED_SIZE+10]; //a bit larger

        Random rand = new Random();
        rand.nextBytes(in);

        ValueBLOBCreator creator = ValueLOB.getBLOBCreator(ValueLOB.SHA1, ValueLOB.SHA384);
        Path tempFile;
        try(ValueLOB v = creator.writeToLOB(ByteBuffer.wrap(in)).buildBLOB()) {
            tempFile = v.getTempFile();
            assertEquals(Base64.getEncoder().encodeToString(in), v.stringValue(), "LOB to string not same");
            assertEquals(in.length, (int) v.size(), "LOB size should correct");
            assertArrayEquals(MessageDigest.getInstance("SHA-1").digest(in), v.getHash(ValueLOB.SHA1), "not correct SHA1 hash");
            assertArrayEquals(MessageDigest.getInstance("SHA-384").digest(in), v.getHash(ValueLOB.SHA384), "not correct SHA384 hash");

            //LOB Read out
            byte[] buff = new byte[(int) v.size()];
            try (InputStream input = v.getInputStream()) {
                int n_read = input.read(buff);
                assertEquals(v.size(), n_read, "not same size on read");
            }
            assertArrayEquals(in, buff, "read back does not correct");
            assertNotNull(tempFile, "Temp file should be created.");
            System.out.append("testMediumLob size : ").append(NumberFormat.getNumberInstance().format(v.size()))
                    .append(" of MD5 ").append(ValueBytes.bytesToHex(v.ensureHash(ValueLOB.MD5)))
                    .append(" of SHA1 ").append(ValueBytes.bytesToHex(v.getHash(ValueLOB.SHA1)))
                    .append(" with temp file ").append(tempFile.toString())
                    .println();
        }

        assertFalse(Files.exists(tempFile), "Temp file should be deleted after close");
    }

    @Test
    public void testOnDemandLob() throws IOException, NoSuchAlgorithmException {
        int testSize = 5 * 1024;
        byte[] in = new byte[testSize]; //a bit larger

        Random rand = new Random();
        rand.nextBytes(in);

        Path tempFile;

        try(ValueLOB v = ValueBLOB.wrap(new ByteArrayInputStream(in), testSize, ValueLOB.MD5)) {
            tempFile = v.getTempFile();

            //force to cache it
            String v_str = v.stringValue();
            assertEquals(Base64.getEncoder().encodeToString(in), v_str, "LOB to string not correct ");
            assertEquals(testSize, (int) v.size(), "LOB size should correct");
            assertArrayEquals(MessageDigest.getInstance("MD5").digest(in), v.ensureHash(ValueLOB.MD5), "not correct MD5 hash on ensure");

            //LOB Read out, it is from original
            byte[] buff = new byte[4096];
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            long acc_read = 0;
            try (InputStream test_input = v.getInputStream()) {
                test_input.mark(testSize);
                int n_read;
                while (0 <= (n_read = test_input.read(buff))) {
                    acc_read += n_read;
                    md5.update(buff, 0, n_read);
                }
                test_input.reset();
            }
            assertEquals(v.size(), acc_read, "not same size on read");
            buff = md5.digest();
            assertArrayEquals(MessageDigest.getInstance("MD5").digest(in), buff, "not correct MD5 hash on read back");

            System.out.append("testOnDemandLob size : ").append(NumberFormat.getNumberInstance().format(v.size()))
                    .append(", hash MD5 ").append(ValueBytes.bytesToHex(v.getHash(ValueLOB.MD5)))
                    .append(" content MD5 ").append(ValueBytes.bytesToHex(MessageDigest.getInstance("MD5").digest(in)))
                    .println();
        }

        assertFalse(Files.exists(tempFile), "Temp file should be deleted after close");
    }

    @Test
    public void testLargeOnDemandLob() throws IOException, NoSuchAlgorithmException {
        long testSize = 3 * 1024 * 1024;
        ExecutorService exec = Executors.newSingleThreadExecutor();
        System.out.println("creating LOB of size="+testSize);
        final PipedOutputStream out = new PipedOutputStream();
        InputStream input = new PipedInputStream(out);
        MessageDigest hash = MessageDigest.getInstance("MD5");
        DigestInputStream digestInput = new DigestInputStream(input, hash);
        Path tempFile = null;

        try {
            Future<?> task = exec.submit(new RandomPipeOut(out, testSize));
            try (ValueLOB v = ValueBLOB.wrap(digestInput, testSize, ValueLOB.MD5, ValueLOB.SHA1)) {
                tempFile = v.getTempFile();

                //view string, LOB input read to copy it
                String v_str = v.stringValue();
                assertEquals(testSize * 4 / 3, v_str.length(), "LOB to string length not correct");
                assertEquals(testSize, (int) v.size(), "LOB size should correct");
                assertArrayEquals(hash.digest(), v.ensureHash(ValueLOB.MD5), "not correct MD5 hash on ensure");

                //LOB Read again, it is from TempFile
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                byte[] buff = new byte[4096];
                long acc_read = 0;
                try (InputStream test_input = v.getInputStream()) {
                    int n_read;
                    while (0 <= (n_read = test_input.read(buff))) {
                        acc_read += n_read;
                        md5.update(buff, 0, n_read);
                    }
                }
                assertEquals(v.size(), acc_read, "not same size on read");
                assertArrayEquals(md5.digest(), v.getHash(ValueLOB.MD5), "not correct MD5 hash on read back");
                assertNotNull(tempFile, "Temp file should be created.");
                System.out.append("testLargeOnDemandLob size : ").append(NumberFormat.getNumberInstance().format(v.size()))
                        .append(" of SHA1 ").append(ValueBytes.bytesToHex(v.getHash(ValueLOB.SHA1)))
                        .append(" with temp file ").append(tempFile.toString())
                        .println();
                task.get();

                assertTrue(Files.exists(tempFile), "Temp file should be used");
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        assertFalse(Files.exists(tempFile), "Temp file should be deleted after close");
    }

    @Test
    public void testLargeLob() throws IOException, NoSuchAlgorithmException {
        long testSize = 5 * 1024 * 3 * 1024;
        ExecutorService exec = Executors.newSingleThreadExecutor();
        System.out.println("creating LOB of size="+testSize);
        final PipedOutputStream out = new PipedOutputStream();
        InputStream input = new PipedInputStream(out);
        MessageDigest hash = MessageDigest.getInstance("MD5");
        DigestInputStream digestInput = new DigestInputStream(input, hash);
        Path tempFile = null;

        try {
            Future<?> task = exec.submit(new RandomPipeOut(out, testSize));
            try (ValueLOB v = ValueBLOB.load(digestInput, ValueLOB.MD5, ValueLOB.SHA1)) {
                tempFile = v.getTempFile();

                //force to cache it
                String v_str = v.stringValue();
                assertEquals(testSize * 4 / 3, v_str.length(), "LOB to string length not correct");
                assertEquals(testSize, (int) v.size(), "LOB size should correct");
                assertArrayEquals(hash.digest(), v.ensureHash(ValueLOB.MD5), "not correct MD5 hash on ensure");

                //LOB Read again, it is from TempFile
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                byte[] buff = new byte[4096];
                long acc_read = 0;
                try (InputStream test_input = v.getInputStream()) {
                    int n_read;
                    while (0 <= (n_read = test_input.read(buff))) {
                        acc_read += n_read;
                        md5.update(buff, 0, n_read);
                    }
                }
                assertEquals(v.size(), acc_read, "not same size on read");
                assertArrayEquals(md5.digest(), v.getHash(ValueLOB.MD5), "not correct MD5 hash on read back");
                assertNotNull(tempFile, "Temp file should be created.");
                System.out.append("testLargeLob size : ").append(NumberFormat.getNumberInstance().format(v.size()))
                        .append(" of SHA1 ").append(ValueBytes.bytesToHex(v.getHash(ValueLOB.SHA1)))
                        .append(" with temp file ").append(tempFile.toString())
                        .println();
                task.get();

                assertTrue(Files.exists(tempFile), "Temp file should be used");
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        assertFalse(Files.exists(tempFile), "Temp file should be deleted after close");
    }

    static class RandomPipeOut implements Runnable {
        final PipedOutputStream out;
        final long size;

        RandomPipeOut(PipedOutputStream out, long size) {
            this.out = out;
            this.size = size;
        }

        @Override
        public void run() {
            System.out.println("Start writing random data");
            Random rand = new Random();
            try {
                try {
                    for (int i = 0; i < size; ) {
                        out.write(rand.nextInt() & 0xff);
                        i ++;
                    }
                } finally {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Done writing random data size="+size);
        }
    }

}
