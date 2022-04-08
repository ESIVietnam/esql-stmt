//package esql.data;
//
//import org.junit.jupiter.api.Test;
//
//import java.io.*;
//import java.nio.ByteBuffer;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.security.DigestInputStream;
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//import java.text.NumberFormat;
//import java.util.Base64;
//import java.util.Random;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.Future;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//public class BLOBTest {
//
//    @Test
//    public void testNullLob() {
//        ValueLOB v = ValueBLOB.NULL_BLOB;
//
//        assertEquals("", v.stringValue());
//        assertEquals(0, (int) v.size(), "LOB size should correct");
//        assertTrue(v.isNull(), "Must not be null");
//        assertTrue(v.isEmpty(), "Must be empty");
//        assertFalse(v.isTrue(), "Must be false");
//        assertTrue(v.is(Types.TYPE_BLOB), "Must be blob");
//
//        var nv1 = v.convertTo(Types.TYPE_STRING);
//        assertTrue(nv1.isNull(), "Must not be null");
//        assertTrue(nv1.isEmpty(), "Must be empty");
//        assertFalse(nv1.isTrue(), "Must be false");
//        assertEquals(Types.TYPE_STRING, nv1.getType());
//
//        var nv2 = v.convertTo(Types.TYPE_BLOB);
//        assertTrue(nv2.isNull(), "Must not be null");
//        assertTrue(nv2.isEmpty(), "Must be empty");
//        assertFalse(nv2.isTrue(), "Must be false");
//        assertEquals(Types.TYPE_BLOB, nv2.getType());
//
//        var nv3 = v.convertTo(Types.TYPE_CLOB);
//        assertTrue(nv3.isNull(), "Must not be null");
//        assertTrue(nv3.isEmpty(), "Must be empty");
//        assertFalse(nv3.isTrue(), "Must be false");
//        assertEquals(Types.TYPE_CLOB, nv3.getType());
//    }
//
//    @Test
//    public void testEmptyLob() throws IOException, NoSuchAlgorithmException {
//        byte[] in = { };
//
//        Path tempFile;
//        try(ValueLOB v = ValueBLOB.load(new ByteArrayInputStream(in))) {
//            tempFile = v.getTempFile();
//            assertEquals(Base64.getEncoder().encodeToString(in), v.stringValue(), "LOB to string not same");
//            assertEquals(in.length, (int) v.size(), "LOB size should correct");
//
//            assertArrayEquals(ValueLOB.EMPTY_HASHES[ValueLOB.MD5], v.getHash(ValueLOB.MD5), "not correct MD5 pre-hash");
//
//            //LOB Read out
//            byte[] buff = new byte[(int) v.size()];
//            try (InputStream input = v.getInputStream()) {
//                input.read(buff);
//            }
//
//            assertArrayEquals(in, buff, "read back does not correct");
//            assertFalse(v.isNull(), "Must not be null");
//            assertTrue(v.isEmpty(), "Must be empty");
//            assertFalse(v.isTrue(), "Must be false");
//            assertTrue(v.is(Types.TYPE_BLOB), "Must be blob");
//            assertNull(tempFile, "Temp file should not created.");
//
//            var nv1 = v.convertTo(Types.TYPE_BLOB);
//            assertFalse(nv1.isNull(), "Must not be null");
//            assertTrue(nv1.isEmpty(), "Must be empty");
//            assertFalse(nv1.isTrue(), "Must be false");
//            assertEquals(Types.TYPE_BLOB, nv1.getType());
//
//            var nv2 = v.convertTo(Types.TYPE_STRING);
//            assertFalse(nv2.isNull(), "Must not be null");
//            assertTrue(nv2.isEmpty(), "Must be empty");
//            assertFalse(nv2.isTrue(), "Must be false");
//            assertEquals(Types.TYPE_STRING, nv2.getType());
//
//            var nv3 = v.convertTo(Types.TYPE_CLOB);
//            assertFalse(nv3.isNull(), "Must not be null");
//            assertTrue(nv3.isEmpty(), "Must be empty");
//            assertFalse(nv3.isTrue(), "Must be false");
//            assertEquals(Types.TYPE_CLOB, nv3.getType());
//
//            System.out.append("testEmptyBLob size : ").append(NumberFormat.getNumberInstance().format(v.size()))
//                    .append(" of MD5 ").append(ValueBytes.bytesToHex(v.getHash(ValueLOB.MD5)))
//                    .append(" with toString: ").append(v.toString()).println();
//        }
//    }
//
//    @Test
//    public void testSmallLob() throws IOException, NoSuchAlgorithmException {
//        byte[] in = { '9' , 11, 32, 49, 0x7f, 32, 4, 65, 0, 10, 14, 15 };
//
//        Path tempFile;
//        try(ValueLOB v = ValueBLOB.wrap(in, in.length)) {
//            tempFile = v.getTempFile();
//            assertEquals(Base64.getEncoder().encodeToString(in), v.stringValue(), "LOB to string not same");
//            assertEquals(in.length, (int) v.size(), "LOB size should correct");
//
//            assertArrayEquals(MessageDigest.getInstance("MD5").digest(in), v.getHash(ValueLOB.MD5), "not correct MD5 pre-hash");
//            assertNull(v.getHash(ValueLOB.SHA1)); //must null.
//            assertArrayEquals(MessageDigest.getInstance("SHA-1").digest(in), v.forceHash(ValueLOB.SHA1), "not correct SHA1 ensure hash");
//
//            //LOB Read out
//            byte[] buff = new byte[(int) v.size()];
//            try (InputStream input = v.getInputStream()) {
//                int n_read = input.read(buff);
//                assertEquals(v.size(), n_read, "not same size on read");
//            }
//            assertArrayEquals(in, buff, "read back does not correct");
//            System.out.append("testSmallLob size : ").append(NumberFormat.getNumberInstance().format(v.size()))
//                    .append(" of MD5 ").append(ValueBytes.bytesToHex(v.getHash(ValueLOB.MD5)))
//                    .append(" with toString: ").append(v.toString()).println();
//        }
//        assertNull(tempFile, "Temp file should not created.");
//    }
//
//    @Test
//    public void testSmallLobCreator() throws IOException, NoSuchAlgorithmException {
//        byte[] in = { '9' , 11, 32, 49, 0x7f, 32, 4, 65, 0, 10, 14, 15 };
//
//        ValueBLOBCreator creator = ValueLOB.getBLOBCreator(ValueLOB.MD5);
//        Path tempFile;
//        try(ValueLOB v = creator.writeToLOB(ByteBuffer.wrap(in)).buildBLOB()) {
//            tempFile = v.getTempFile();
//            assertEquals(Base64.getEncoder().encodeToString(in), v.stringValue(), "LOB to string not same");
//            assertEquals(in.length, (int) v.size(), "LOB size should correct");
//
//            assertArrayEquals(MessageDigest.getInstance("MD5").digest(in), v.getHash(ValueLOB.MD5), "not correct MD5 pre-hash");
//            assertNull(v.getHash(ValueLOB.SHA1)); //must null.
//            assertArrayEquals(MessageDigest.getInstance("SHA-1").digest(in), v.forceHash(ValueLOB.SHA1), "not correct SHA1 ensure hash");
//
//            //LOB Read out
//            byte[] buff = new byte[(int) v.size()];
//            try (InputStream input = v.getInputStream()) {
//                int n_read = input.read(buff);
//                assertEquals(v.size(), n_read, "not same size on read");
//            }
//            assertArrayEquals(in, buff, "read back does not correct");
//            System.out.append("testSmallLob size : ").append(NumberFormat.getNumberInstance().format(v.size()))
//                    .append(" of MD5 ").append(ValueBytes.bytesToHex(v.getHash(ValueLOB.MD5)))
//                    .append(" with toString: ").append(v.toString()).println();
//        }
//        assertNull(tempFile, "Temp file should not created.");
//    }
//
//    @Test
//    public void testMediumLob() throws IOException, NoSuchAlgorithmException {
//        byte[] in = new byte[ValueLOB.MAX_BUFFERED_SIZE+10]; //a bit larger to force writing to temp file.
//
//        Random rand = new Random();
//        rand.nextBytes(in);
//
//        ValueBLOBCreator creator = ValueLOB.getBLOBCreator(ValueLOB.SHA1, ValueLOB.SHA384);
//        Path tempFile;
//        try(ValueLOB v = creator.writeToLOB(ByteBuffer.wrap(in)).buildBLOB()) {
//            tempFile = v.getTempFile();
//            assertEquals(Base64.getEncoder().encodeToString(in), v.stringValue(), "LOB to string not same");
//            assertEquals(in.length, (int) v.size(), "LOB size should correct");
//            assertArrayEquals(MessageDigest.getInstance("SHA-1").digest(in), v.getHash(ValueLOB.SHA1), "not correct SHA1 hash");
//            assertArrayEquals(MessageDigest.getInstance("SHA-384").digest(in), v.getHash(ValueLOB.SHA384), "not correct SHA384 hash");
//
//            //LOB Read out
//            byte[] buff = new byte[(int) v.size()];
//            try (InputStream input = v.getInputStream()) {
//                int n_read = input.read(buff);
//                assertEquals(v.size(), n_read, "not same size on read");
//            }
//            assertArrayEquals(in, buff, "read back does not correct");
//            assertNotNull(tempFile, "Temp file should be created.");
//            assertTrue(Files.exists(tempFile), "Temp file should be create");
//            System.out.append("testMediumLob size : ").append(NumberFormat.getNumberInstance().format(v.size()))
//                    .append(" of MD5 ").append(ValueBytes.bytesToHex(v.forceHash(ValueLOB.MD5)))
//                    .append(" of SHA1 ").append(ValueBytes.bytesToHex(v.getHash(ValueLOB.SHA1)))
//                    .append(" with temp file ").append(tempFile.toString())
//                    .println();
//        }
//
//        assertFalse(Files.exists(tempFile), "Temp file should be deleted after close");
//    }
//
//    @Test
//    public void testOnDemandLob() throws IOException, NoSuchAlgorithmException {
//        int testSize = 5 * 1024;
//        byte[] in = new byte[testSize]; //a bit larger
//
//        Random rand = new Random();
//        rand.nextBytes(in);
//
//        Path tempFile;
//
//        try(ValueLOB v = ValueBLOB.wrap(new ByteArrayInputStream(in), testSize, ValueLOB.MD5)) {
//            tempFile = v.getTempFile();
//
//            //force to cache it
//            String v_str = v.stringValue();
//            assertEquals(Base64.getEncoder().encodeToString(in), v_str, "LOB to string not correct ");
//            assertEquals(testSize, (int) v.size(), "LOB size should correct");
//            assertEquals(v, v.convertTo(Types.TYPE_BLOB), "LOB convert same");
//            //convert to binary
//            assertArrayEquals(in, ((ValueBytes)v.convertTo(Types.TYPE_BYTES)).bytesArray(), "LOB convert bytes");
//            assertArrayEquals(MessageDigest.getInstance("MD5").digest(in), v.forceHash(ValueLOB.MD5), "not correct MD5 hash on ensure");
//
//            //LOB Read out, it is from original
//            byte[] buff = new byte[4096];
//            MessageDigest md5 = MessageDigest.getInstance("MD5");
//            long acc_read = 0;
//            try (InputStream test_input = v.getInputStream()) {
//                test_input.mark(testSize);
//                int n_read;
//                while (0 <= (n_read = test_input.read(buff))) {
//                    if(n_read > 0) {
//                        acc_read += n_read;
//                        md5.update(buff, 0, n_read);
//                    }
//                }
//                test_input.reset();
//            }
//            assertEquals(v.size(), acc_read, "not same size on read");
//            buff = md5.digest();
//            assertArrayEquals(MessageDigest.getInstance("MD5").digest(in), buff, "not correct MD5 hash on read back");
//
//            System.out.append("testOnDemandLob size : ").append(NumberFormat.getNumberInstance().format(v.size()))
//                    .append(", hash MD5 ").append(ValueBytes.bytesToHex(v.getHash(ValueLOB.MD5)))
//                    .append(" content MD5 ").append(ValueBytes.bytesToHex(MessageDigest.getInstance("MD5").digest(in)))
//                    .println();
//        }
//
//        assertFalse(Files.exists(tempFile), "Temp file should be deleted after close");
//    }
//
//    @Test
//    public void testOnDemandLob2() throws IOException, NoSuchAlgorithmException {
//        int testSize = 6 * 1024;
//        byte[] in = new byte[testSize]; //a bit larger
//
//        Random rand = new Random();
//        rand.nextBytes(in);
//
//        MessageDigest hash = MessageDigest.getInstance("MD5");
//        Path tempFile;
//
//        try(ValueLOB v = ValueBLOB.wrap(new ByteArrayInputStream(in), testSize, ValueLOB.MD5)) {
//            tempFile = v.getTempFile();
//
//            //force to cache it
//            assertEquals(testSize, (int) v.size(), "LOB size should correct");
//            assertNull(v.getHash(ValueLOB.MD5), "not calc-MD5 yet");
//
//            //convert test
//            Value nv1 = v.convertTo(Types.TYPE_CLOB);
//
//            assertArrayEquals(MessageDigest.getInstance("MD5").digest(in), v.getHash(ValueLOB.MD5), "not correct MD5 hash after convert");
//
//            String v_str = v.stringValue();
//
//            assertEquals(v_str.substring(0, 500), nv1.stringValue().substring(0, 500), "To CLOB same");
//
//            Value nv2 = v.convertTo(Types.TYPE_STRING);
//
//            assertEquals(v_str, nv2.stringValue(), "To String same");
//
//            Value nv3 = v.convertTo(Types.TYPE_BLOB);
//
//            assertEquals(v, nv3, "new must be same as old");
//            assertNotEquals(tempFile, ((ValueLOB)nv3).getTempFile(), "new differ temp file");
//
//            ValueBytes nv4 = (ValueBytes) v.convertTo(Types.TYPE_BYTES);
//
//            assertEquals(v.size(), nv4.backedBytesArray().length, "To byte[] length same");
//            assertArrayEquals(v.getHash(ValueLOB.MD5), hash.digest(nv4.backedBytesArray()), "To byte[] hash same");
//
//            assertFalse(Files.exists(tempFile), "Temp file should not use");
//
//            System.out.append("testOnDemandLob2 size : ").append(NumberFormat.getNumberInstance().format(v.size()))
//                    .append(", hash MD5 ").append(ValueBytes.bytesToHex(v.getHash(ValueLOB.MD5)))
//                    .append(" content MD5 ").append(ValueBytes.bytesToHex(MessageDigest.getInstance("MD5").digest(in)))
//                    .println();
//        }
//
//    }
//
//    @Test
//    public void testLargeOnDemandLob() throws IOException, NoSuchAlgorithmException {
//        long testSize = 3 * 1024 * 1024;
//        ExecutorService exec = Executors.newSingleThreadExecutor();
//        System.out.println("creating LOB of size="+testSize);
//        final PipedOutputStream out = new PipedOutputStream();
//        InputStream input = new PipedInputStream(out);
//        MessageDigest hash = MessageDigest.getInstance("MD5");
//        DigestInputStream digestInput = new DigestInputStream(input, hash);
//        Path tempFile = null;
//
//        try {
//            Future<?> task = exec.submit(new RandomPipeOut(out, testSize));
//            try (ValueLOB v = ValueBLOB.wrap(digestInput, testSize, ValueLOB.MD5, ValueLOB.SHA1)) {
//                tempFile = v.getTempFile();
//
//                //view string, LOB input read to copy it
//                String v_str = v.stringValue();
//                assertEquals(testSize * 4 / 3, v_str.length(), "LOB to string length not correct");
//                assertEquals(testSize, (int) v.size(), "LOB size should correct");
//                assertArrayEquals(hash.digest(), v.forceHash(ValueLOB.MD5), "not correct MD5 hash on ensure");
//
//                //LOB Read again, it is from TempFile
//                MessageDigest md5 = MessageDigest.getInstance("MD5");
//                byte[] buff = new byte[4096];
//                long acc_read = 0;
//                try (InputStream test_input = v.getInputStream()) {
//                    int n_read;
//                    while (0 <= (n_read = test_input.read(buff))) {
//                        acc_read += n_read;
//                        md5.update(buff, 0, n_read);
//                    }
//                }
//                assertEquals(v.size(), acc_read, "not same size on read");
//                assertArrayEquals(md5.digest(), v.getHash(ValueLOB.MD5), "not correct MD5 hash on read back");
//                assertNotNull(tempFile, "Temp file should be created.");
//                System.out.append("testLargeOnDemandLob size : ").append(NumberFormat.getNumberInstance().format(v.size()))
//                        .append(" of SHA1 ").append(ValueBytes.bytesToHex(v.getHash(ValueLOB.SHA1)))
//                        .append(" with temp file ").append(tempFile.toString())
//                        .println();
//                task.get();
//
//                assertTrue(Files.exists(tempFile), "Temp file should be used");
//            }
//        } catch (ExecutionException | InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        assertFalse(Files.exists(tempFile), "Temp file should be deleted after close");
//    }
//
//    @Test
//    public void testLargeOnDemandLob2() throws IOException, NoSuchAlgorithmException {
//        long testSize = 2 * 1024 * 1024;
//        ExecutorService exec = Executors.newSingleThreadExecutor();
//        System.out.println("creating LOB of size="+testSize);
//        final PipedOutputStream out = new PipedOutputStream();
//        InputStream input = new PipedInputStream(out);
//        MessageDigest hash = MessageDigest.getInstance("MD5");
//        DigestInputStream digestInput = new DigestInputStream(input, hash);
//        Path tempFile = null;
//
//        try {
//            Future<?> task = exec.submit(new RandomPipeOut(out, testSize));
//            try (ValueLOB v = ValueBLOB.wrap(digestInput, testSize, ValueLOB.SHA1)) {
//                tempFile = v.getTempFile();
//
//                assertEquals(testSize, (int) v.size(), "LOB size should correct");
//                byte[] en = v.forceHash(ValueLOB.MD5);
//                assertArrayEquals(hash.digest(), en, "not correct MD5 hash on ensure");
//
//                assertNotNull(v.getHash(ValueLOB.MD5), "must calculate");
//                assertNull(v.getHash(ValueLOB.SHA256), "not calculate");
//
//                task.get();
//
//                assertTrue(Files.exists(tempFile), "Temp file should be used");
//
//                String v_str = v.stringValue();
//                //convert test
//                Value nv = v.convertTo(Types.TYPE_BLOB);
//
//                assertEquals(v, nv, "new must be same as old");
//                assertNotEquals(tempFile, ((ValueLOB)nv).getTempFile(), "new differ temp file");
//
//                Value nv2 = v.convertTo(Types.TYPE_STRING);
//
//                assertEquals(v_str, nv2.stringValue(), "To String same");
//
//                Value nv3 = v.convertTo(Types.TYPE_CLOB);
//
//                assertEquals(v_str.substring(0, 500), nv3.stringValue().substring(0, 500), "To CLOB same");
//
//                ValueBytes nv4 = (ValueBytes) v.convertTo(Types.TYPE_BYTES);
//
//                assertEquals(v.size(), nv4.backedBytesArray().length, "To byte[] length same");
//                assertArrayEquals(v.getHash(ValueLOB.MD5), hash.digest(nv4.backedBytesArray()), "To byte[] hash same");
//
//                System.out.append("testLargeOnDemandLob2 size : ").append(NumberFormat.getNumberInstance().format(v.size()))
//                        .append(" of SHA1 ").append(ValueBytes.bytesToHex(v.getHash(ValueLOB.SHA1)))
//                        .append(" with temp file ").append(tempFile.toString())
//                        .println();
//            }
//        } catch (ExecutionException | InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        assertFalse(Files.exists(tempFile), "Temp file should be deleted after close");
//    }
//
//    @Test
//    public void testLargeLob() throws IOException, NoSuchAlgorithmException {
//        long testSize = 5 * 1024 * 3 * 1024;
//        ExecutorService exec = Executors.newSingleThreadExecutor();
//        System.out.println("creating LOB of size="+testSize);
//        final PipedOutputStream out = new PipedOutputStream();
//        InputStream input = new PipedInputStream(out);
//        MessageDigest hash = MessageDigest.getInstance("MD5");
//        DigestInputStream digestInput = new DigestInputStream(input, hash);
//        Path tempFile = null;
//
//        try {
//            Future<?> task = exec.submit(new RandomPipeOut(out, testSize));
//            try (ValueLOB v = ValueBLOB.load(digestInput, ValueLOB.MD5, ValueLOB.SHA1)) {
//                tempFile = v.getTempFile();
//
//                //force to cache it
//                String v_str = v.stringValue();
//                assertEquals(testSize * 4 / 3, v_str.length(), "LOB to string length not correct");
//                assertEquals(testSize, (int) v.size(), "LOB size should correct");
//                assertArrayEquals(hash.digest(), v.forceHash(ValueLOB.MD5), "not correct MD5 hash on ensure");
//
//                //LOB Read again, it is from TempFile
//                MessageDigest md5 = MessageDigest.getInstance("MD5");
//                byte[] buff = new byte[4096];
//                long acc_read = 0;
//                try (InputStream test_input = v.getInputStream()) {
//                    int n_read;
//                    while (0 <= (n_read = test_input.read(buff))) {
//                        acc_read += n_read;
//                        md5.update(buff, 0, n_read);
//                    }
//                }
//                assertEquals(v.size(), acc_read, "not same size on read");
//                assertArrayEquals(md5.digest(), v.getHash(ValueLOB.MD5), "not correct MD5 hash on read back");
//                assertNotNull(tempFile, "Temp file should be created.");
//                System.out.append("testLargeLob size : ").append(NumberFormat.getNumberInstance().format(v.size()))
//                        .append(" of SHA1 ").append(ValueBytes.bytesToHex(v.getHash(ValueLOB.SHA1)))
//                        .append(" with temp file ").append(tempFile.toString())
//                        .println();
//                task.get();
//
//                assertTrue(Files.exists(tempFile), "Temp file should be used");
//            }
//        } catch (ExecutionException | InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        assertFalse(Files.exists(tempFile), "Temp file should be deleted after close");
//    }
//
//    static class RandomPipeOut implements Runnable {
//        final PipedOutputStream out;
//        final long size;
//
//        RandomPipeOut(PipedOutputStream out, long size) {
//            this.out = out;
//            this.size = size;
//        }
//
//        @Override
//        public void run() {
//            System.out.println("Start writing random data");
//            Random rand = new Random();
//            try {
//                try {
//                    for (int i = 0; i < size; ) {
//                        out.write(rand.nextInt() & 0xff);
//                        i ++;
//                    }
//                } finally {
//                    out.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            System.out.println("Done writing random data size="+size);
//        }
//    }
//
//}
