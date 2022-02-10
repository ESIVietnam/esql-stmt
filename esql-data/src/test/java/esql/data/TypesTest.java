package esql.data;

import org.junit.jupiter.api.Test;

import static esql.data.Types.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class TypesTest {
    final static Types[] string_types = {
            TYPE_STRING, TYPE_NSTRING, TYPE_BYTES
    };

    @Test
    public void CorrectTypeTest() {
        assertEquals(Types.of("string"), TYPE_STRING);
        assertEquals(Types.of("nstring"), TYPE_NSTRING);
        assertEquals(Types.of("bytes"), TYPE_BYTES);
        assertEquals(Types.of("binary"), TYPE_BYTES);

        assertEquals(Types.of("byte"), TYPE_BYTE);
        assertEquals(Types.of("ubyte"), TYPE_UBYTE);
        assertEquals(Types.of("short"), TYPE_SHORT);
        assertEquals(Types.of("ushort"), TYPE_USHORT);
        assertEquals(Types.of("int"), TYPE_INT);
        assertEquals(Types.of("uint"), TYPE_UINT);
        assertEquals(Types.of("long"), TYPE_LONG);
        assertEquals(Types.of("ulong"), TYPE_ULONG);
        assertEquals(Types.of("bigint"), TYPE_ULONG);

        assertEquals(Types.of("float"), TYPE_FLOAT);
        assertEquals(Types.of("real"), TYPE_FLOAT);
        assertEquals(Types.of("double"), TYPE_DOUBLE);

        assertEquals(Types.of("date"), TYPE_DATE);
        assertEquals(Types.of("datetime"), TYPE_DATETIME);
        assertEquals(Types.of("timestamp"), TYPE_TIMESTAMP);
        assertEquals(Types.of("time"), TYPE_TIME);

        assertEquals(Types.of("clob"), TYPE_CLOB);
        assertEquals(Types.of("nclob"), TYPE_NCLOB);
        assertEquals(Types.of("blob"), TYPE_BLOB);

        assertEquals(Types.of("data-tree"), TYPE_DATA_TREE);
        assertEquals(Types.of("json"), TYPE_JSON);
        assertEquals(Types.of("xml"), TYPE_XML);
    }

    @Test
    public void AbbreviationArrayTest() {
        assertEquals("string",Types.extractAbbreviation("string[]"));
        assertEquals("string",Types.extractAbbreviation(" string[]"));
        assertEquals("string",Types.extractAbbreviation(" string[] "));
        assertNotEquals("string",Types.extractAbbreviation("string []"));
    }
}
