package esql.data;

import org.junit.jupiter.api.Test;

import java.text.ParsePosition;

import static org.junit.jupiter.api.Assertions.*;

class ValueTest {

    @Test
    void matchHexStringTest1() {
        // Test for matching hex string
        String hexString = "0A2B3C";
        var start = new ParsePosition(0);
        var end = new ParsePosition(hexString.length());
        boolean r = Value.isHexString(hexString, start, end);
        assertTrue(r);
        assertEquals(0, start.getIndex());
        assertEquals(hexString.length(), end.getIndex());
    }

    @Test
    void matchHexStringTest2() {
        // Test for matching hex string
        String hexString = "0x1A2B3C";
        var start = new ParsePosition(0);
        var end = new ParsePosition(hexString.length());
        boolean r = Value.isHexString(hexString, start, end);
        assertTrue(r);
        assertEquals(2, start.getIndex());
        assertEquals(hexString.length(), end.getIndex());
    }

    @Test
    void isIntegerString() {
        // Test for matching integer string
        String intString = "12345";
        assertTrue(Value.isIntegerString(intString));
        var start = new ParsePosition(0);
        var end = new ParsePosition(intString.length());
        boolean r = Value.isIntegerString(intString, start, end);
        assertTrue(r);
        assertEquals(0, start.getIndex());
        assertEquals(intString.length(), end.getIndex());

        // Test for non-matching integer string
        String nonIntString = "12.34";
        start.setIndex(0);
        end.setIndex(nonIntString.length());
        r = Value.isIntegerString(nonIntString, start, end);
        assertFalse(r);
    }

    @Test
    void isPositiveIntegerString() {
        // test for matching positive integer string
        String posIntString = "+2345";
        var start = new ParsePosition(0);
        var end = new ParsePosition(posIntString.length());
        boolean r = Value.isIntegerString(posIntString, start, end);
        assertTrue(r);
        assertEquals(0, start.getIndex());
        assertEquals(posIntString.length(), end.getIndex());
        // Test for non-matching positive integer string
        String nonPosIntString = "+ 2345";
        start.setIndex(0);
        end.setIndex(nonPosIntString.length());
        r = Value.isIntegerString(nonPosIntString, start, end);
        assertFalse(r);
    }

    @Test
    void isNegativeIntegerString() {
        // Test for matching negative integer string
        String negIntString = "-12345";
        var start = new ParsePosition(0);
        var end = new ParsePosition(negIntString.length());
        boolean r = Value.isIntegerString(negIntString, start, end);
        assertTrue(r);
        assertEquals(0, start.getIndex());
        assertEquals(negIntString.length(), end.getIndex());

        // Test for non-matching negative integer string
        String nonNegIntString = "- 12345";
        start.setIndex(0);
        end.setIndex(nonNegIntString.length());
        r = Value.isIntegerString(nonNegIntString, start, end);
        assertFalse(r);

        // Test for non-matching negative integer string
        nonNegIntString = "-k12345";
        start.setIndex(0);
        end.setIndex(nonNegIntString.length());
        r = Value.isIntegerString(nonNegIntString, start, end);
        assertFalse(r);

    }

    @Test
    void isInvalidIntegerString() {
        // Test for non-matching negative integer string
        String invalidString = "- ";
        var start = new ParsePosition(0);
        var end = new ParsePosition(invalidString.length());
        boolean r = Value.isIntegerString(invalidString, start, end);
        assertFalse(r);

        invalidString = "  -";
        start.setIndex(0);
        end.setIndex(invalidString.length());
        r = Value.isIntegerString(invalidString, start, end);
        assertFalse(r);
    }

    @Test
    void isDecimalString1() {
        // Test for matching decimal string
        String decimalString = "123.45";
        assertTrue(Value.isDecimalString(decimalString));
        var start = new ParsePosition(0);
        var decimalPos = new ParsePosition(0);
        var end = new ParsePosition(decimalString.length());
        boolean r = Value.isDecimalString(decimalString, start, decimalPos, end);
        assertTrue(r);
        assertEquals(0, start.getIndex());
        assertEquals(3, decimalPos.getIndex());
        assertEquals(6, end.getIndex());
    }

    @Test
    void isDecimalString2() {
        // Test for matching decimal string
        String decimalString = "-123.45";
        var start = new ParsePosition(0);
        var decimalPos = new ParsePosition(0);
        var end = new ParsePosition(decimalString.length());
        boolean r = Value.isDecimalString(decimalString, start, decimalPos, end);
        assertTrue(r);
        assertEquals(0, start.getIndex());
        assertEquals(4, decimalPos.getIndex());
        assertEquals(7, end.getIndex());
    }

    @Test
    void isDecimalString3() {
        // Test for integer matching is covered by decimal string
        String nonDecimalString = "12345";
        var start = new ParsePosition(0);
        var decimalPos = new ParsePosition(0);
        var end = new ParsePosition(nonDecimalString.length());
        boolean r = Value.isDecimalString(nonDecimalString, start, decimalPos, end);
        assertTrue(r);
        assertEquals(0, start.getIndex());
        assertEquals(5, decimalPos.getIndex());
        assertEquals(nonDecimalString.length(), end.getIndex());
    }

    @Test
    void isDecimalString4() {
        // Test for integer matching is covered by decimal string
        String nonDecimalString = "1234.";
        var start = new ParsePosition(0);
        var decimalPos = new ParsePosition(0);
        var end = new ParsePosition(nonDecimalString.length());
        boolean r = Value.isDecimalString(nonDecimalString, start, decimalPos, end);
        assertTrue(r);
        assertEquals(0, start.getIndex());
        assertEquals(4, decimalPos.getIndex());
        assertEquals(4, end.getIndex());
    }

    @Test
    void isFloatingNumberString1() {
        // Test for matching float string
        String floatString = "123.45e0";
        assertTrue(Value.isFloatingNumberString(floatString));
        var start = new ParsePosition(0);
        var decimalPos = new ParsePosition(0);
        var powerNotationPos = new ParsePosition(0);
        var end = new ParsePosition(floatString.length());
        boolean r = Value.isFloatingNumberString(floatString, start, decimalPos, powerNotationPos, end);
        assertTrue(r);
        assertEquals(0, start.getIndex());
        assertEquals(3, decimalPos.getIndex());
        assertEquals(6, powerNotationPos.getIndex());
        assertEquals(floatString.length(), end.getIndex());
    }

    @Test
    void isFloatingNumberString2() {
        // Test for matching float string
        String floatString = "123.45e+2";
        assertTrue(Value.isFloatingNumberString(floatString));
        var start = new ParsePosition(0);
        var decimalPos = new ParsePosition(0);
        var powerNotationPos = new ParsePosition(0);
        var end = new ParsePosition(floatString.length());
        boolean r = Value.isFloatingNumberString(floatString, start, decimalPos, powerNotationPos, end);
        assertTrue(r);
        assertEquals(0, start.getIndex());
        assertEquals(3, decimalPos.getIndex());
        assertEquals(6, powerNotationPos.getIndex());
        assertEquals(floatString.length(), end.getIndex());
    }

    @Test
    void isFloatingNumberString3() {
        // Test for matching float string
        String floatString = "0.12345e-12";
        assertTrue(Value.isFloatingNumberString(floatString));
        var start = new ParsePosition(0);
        var decimalPos = new ParsePosition(0);
        var powerNotationPos = new ParsePosition(0);
        var end = new ParsePosition(floatString.length());
        boolean r = Value.isFloatingNumberString(floatString, start, decimalPos, powerNotationPos, end);
        assertTrue(r);
        assertEquals(0, start.getIndex());
        assertEquals(1, decimalPos.getIndex());
        assertEquals(7, powerNotationPos.getIndex());
        assertEquals(floatString.length(), end.getIndex());
    }

    @Test
    void matchBase64String1() {
        // Test for matching base64 string
        String base64String = "U29tZSBkYXRh";
        assertTrue(Value.matchBase64String(base64String, false));
        var decoder = java.util.Base64.getDecoder();
        byte[] decodedBytes = decoder.decode(base64String);
        assertArrayEquals("Some data".getBytes(), decodedBytes);

        // Test for non-matching base64 string
        String nonBase64String = "Invalid base64!";
        assertFalse(Value.matchBase64String(nonBase64String, false));
    }

    @Test
    void matchBase64String2() {
        // Test for matching base64 string
        String base64String = "U29tZSBkYXR=";
        assertTrue(Value.matchBase64String(base64String, false));

        // Test for non-matching base64 string
        String nonBase64String = "Invalid=base64=";
        assertFalse(Value.matchBase64String(nonBase64String, false));
    }

    @Test
    void matchBase64String3() {
        // Test for matching base64 string
        String base64String = "U29tZSBkYXR#";
        assertFalse(Value.matchBase64String(base64String, false));

        // Test for non-matching base64 string
        String nonBase64String = "=Invalid+base64";
        assertFalse(Value.matchBase64String(nonBase64String, false));
    }

    @Test
    void nullOf() {
    }

    @Test
    void valueOf() {
    }

    @Test
    void testValueOf() {
    }

    @Test
    void testValueOf1() {
    }

    @Test
    void testValueOf2() {
    }

    @Test
    void testValueOf3() {
    }

    @Test
    void testValueOf4() {
    }

    @Test
    void testValueOf5() {
    }

    @Test
    void testValueOf6() {
    }

    @Test
    void testValueOf7() {
    }

    @Test
    void testValueOf8() {
    }

    @Test
    void testValueOf9() {
    }

    @Test
    void testValueOf10() {
    }

    @Test
    void testValueOf11() {
    }

    @Test
    void testValueOf12() {
    }

    @Test
    void testValueOf13() {
    }

    @Test
    void testValueOf14() {
    }

    @Test
    void testValueOf15() {
    }

    @Test
    void testValueOf16() {
    }

    @Test
    void testValueOf17() {
    }

    @Test
    void testValueOf18() {
    }

    @Test
    void testValueOf19() {
    }

    @Test
    void testValueOf20() {
    }

    @Test
    void testValueOf21() {
    }

    @Test
    void testValueOf22() {
    }

    @Test
    void testValueOf23() {
    }

    @Test
    void stringValue() {
    }

    @Test
    void testToString() {
    }
}