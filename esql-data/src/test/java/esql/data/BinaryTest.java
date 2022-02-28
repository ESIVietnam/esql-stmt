package esql.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BinaryTest {
    @Test
    public void testBytesToHex1() {
        byte[] in = {0x64, 0x65, 0x6d, 0x6f};//=demo
        String ou = ValueBytes.bytesToHex(in);
        assertEquals("64656d6f", ou.toLowerCase(), "BytesToHex not same");
    }

    @Test
    public void testBytesToHex2() {
        byte[] in = {0x00};//=null
        String ou = ValueBytes.bytesToHex(in);
        assertEquals("00", ou.toLowerCase(), "BytesToHex not same");
    }

    @Test
    public void testBytesToHex3() {
        byte[] in = {(byte) 0xf4, 0x65, 0x6d, 0x60};//=demo
        String ou = ValueBytes.bytesToHex(in);
        assertEquals("f4656d60", ou.toLowerCase(), "BytesToHex not same");
    }

    @Test
    public void testHexToBytes1() {
        byte[] in = {0x64, 0x65, 0x6d, 0x6f};//=demo
        byte[] ou = ValueBytes.hexToBytes("64656d6f");
        assertArrayEquals(in, ou, "HexToBytes not same");
    }

    @Test
    public void testHexToBytes2() {
        byte[] in = {(byte) 0xf4, 0x02, 0x6d, 0x0f};//=demo
        byte[] ou = ValueBytes.hexToBytes("f4026d0f");
        assertArrayEquals(in, ou, "HexToBytes not same");
    }

    @Test
    public void testHexToBytes3() {
        byte[] in = {0x00, 0x65, 0x6d, 0x60};//=demo
        byte[] ou = ValueBytes.hexToBytes("00656d6");
        assertArrayEquals(in, ou, "HexToBytes not same");
    }

    @Test
    public void nullValueTest() {
        ValueBytes v = ValueBytes.buildBytes((String) null);
        assertEquals("", v.stringValue());
        assertEquals(0, v.length(), "length should correct");
        assertFalse(v.isArray(), "Must not be array");
        assertTrue(v.isNull(), "Must be null");
        assertTrue(v.isEmpty(), "Must be empty");
        assertFalse(v.isTrue(), "Must be false");
        assertTrue(v.is(Types.TYPE_BYTES), "Must be bytes");

        ValueArray nv = v.bytesValueArray();
        //assertEquals(ValueArray.ARRAY_PREFIX+ValueArray.ARRAY_POSTFIX, nv.stringValue());
        assertEquals("", nv.stringValue());
        assertEquals(0, nv.size(), "array use size should correct");
        assertTrue(nv.isArray(), "Must be array");
        assertTrue(nv.isNull(), "Must be null");
        assertTrue(nv.isEmpty(), "Must be empty");
        assertFalse(nv.isTrue(), "Must be false");
        assertTrue(nv.is(Types.TYPE_BYTE), "Must be byte[]");
    }

    @Test
    public void emptyValueTest() {
        ValueBytes v = ValueBytes.buildBytes();
        assertEquals("", v.stringValue());
        assertEquals(0, v.length(), "length should correct");
        assertFalse(v.isArray(), "Must not be array");
        assertFalse(v.isNull(), "Must be null");
        assertTrue(v.isEmpty(), "Must be empty");
        assertFalse(v.isTrue(), "Must be false");
        assertTrue(v.is(Types.TYPE_BYTES), "Must be bytes");

        ValueArray nv = v.bytesValueArray();
        assertEquals(ValueArray.ARRAY_PREFIX+ValueArray.ARRAY_POSTFIX, nv.stringValue());
        assertEquals(0, nv.size(), "array use size should correct");
        assertTrue(nv.isArray(), "Must be array");
        assertFalse(nv.isNull(), "Must be null");
        assertTrue(nv.isEmpty(), "Must be empty");
        assertFalse(nv.isTrue(), "Must be false");
        assertTrue(nv.is(Types.TYPE_BYTE), "Must be byte[]");
    }

    @Test
    public void singleValueTest() {
        ValueBytes v = ValueBytes.buildBytes(new byte[] { 0 });
        //hexa
        assertEquals("00", v.stringValue());
        assertEquals(1, v.length(), "length should correct");
        assertFalse(v.isArray(), "Must not be array");
        assertFalse(v.isNull(), "Must be null");
        assertFalse(v.isEmpty(), "Must be empty");
        assertTrue(v.isTrue(), "Must be false");
        assertTrue(v.is(Types.TYPE_BYTES), "Must be bytes");

        v = ValueBytes.buildBytes(new byte[] { 1 });
        //hexa
        assertEquals("01", v.stringValue());
        assertEquals(1, v.length(), "length should correct");

        v = ValueBytes.buildBytes(new byte[] { 0x7f });
        //hexa
        assertEquals("7f", v.stringValue());

        var nv = v.convertTo(Types.TYPE_STRING);
        assertEquals("7f", nv.stringValue());

        v = ValueBytes.buildBytes(new byte[] { 0x18, 0x7f });
        //hexa
        assertEquals("187f", v.stringValue());

        nv = v.convertTo(Types.TYPE_STRING);
        assertEquals("187f", nv.stringValue());
    }
}
