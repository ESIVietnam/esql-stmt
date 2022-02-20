package esql.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

}
