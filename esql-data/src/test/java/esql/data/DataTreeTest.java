package esql.data;

import org.junit.jupiter.api.Test;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class DataTreeTest {

    @Test
    public void NullTreeTest() {
        ValueDataTree data_tree = (ValueDataTree) Value.nullOf(Types.TYPE_DATA_TREE);
        assertEquals(ValueDataTree.nullOfDataTree(), data_tree);
        assertEquals(Types.TYPE_DATA_TREE, data_tree.getType());
        assertTrue(data_tree.isNull(),"must not null");
        assertTrue(data_tree.isEmpty(),"must be empty");
        assertEquals(0, data_tree.size());
        assertEquals("", data_tree.stringValue());

        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream(1024);
            data_tree.writeAsJSON(bo);
            assertEquals(0, bo.size()); //only {}
        } catch (IOException e) {
            fail(e.toString());
        }

        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream(1024);
            data_tree.writeAsXML("empty",bo);
            assertEquals(0, bo.size()); //null is empty
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    @Test
    public void EmptyTreeTest() {
        assertEquals("", Key.of(null).origString());
        assertEquals("", Key.of("").origString());
        ValueDataTree.TreeBuilder tb = ValueDataTree.builderOfDataTree();

        var data_tree = tb.build();
        assertEquals(Types.TYPE_DATA_TREE, data_tree.getType());
        assertFalse(data_tree.isNull(),"must not null");
        assertTrue(data_tree.isEmpty(),"must be empty");
        assertEquals(0, data_tree.size());
        assertEquals("({})", data_tree.stringValue());

        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream(1024);
            data_tree.writeAsJSON(bo);
            assertEquals(2, bo.size()); //only {}
            assertEquals("{}", bo.toString(StandardCharsets.UTF_8), "Must be empty JSON Object");
        } catch (IOException e) {
            fail(e.toString());
        }

        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream(1024);
            data_tree.writeAsXML("empty",bo);
            assertEquals(53, bo.size()); //only <empty />
            assertTrue(bo.toString(StandardCharsets.UTF_8).contains("<empty></empty>"),
                    "Must be empty XML");
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    @Test
    public void SingleValTreeTest() {
        ValueDataTree.TreeBuilder tb = ValueDataTree.builderOfDataTree();
        tb.putElement(Key.of("test"), 1);

        var data_tree = tb.build();
        assertEquals(Types.TYPE_DATA_TREE, data_tree.getType());
        assertFalse(data_tree.isNull(),"must not null");
        assertFalse(data_tree.isEmpty(),"must be empty");
        assertEquals(1, data_tree.size());
        assertEquals("({test=1})", data_tree.stringValue());

        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream(1024);
            data_tree.writeAsJSON(bo);
            assertEquals("{\"test\":1}", bo.toString(StandardCharsets.UTF_8));
        } catch (IOException e) {
            fail(e.toString());
        }

        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream(1024);
            data_tree.writeAsXML("urn:test","single",bo);
            String s = bo.toString(StandardCharsets.UTF_8);
            System.out.println(s);
            assertTrue(s.contains("<single xmlns=\"urn:test\"><test>1</test></single>"));
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    @Test
    public void OneLevelTreeTest() {
        ValueDataTree.TreeBuilder tb = ValueDataTree.builderOfDataTree();
        tb.putElement(Key.of("n"), 1);
        tb.putElement(Key.of("str"), "value");
        tb.putElement(Key.of("dou"), 4.5d);
        tb.putElement(Key.of("b"), true);
        tb.putNull(Key.of("nu"));
        tb.putElement(Key.of("nu2"), tb.nullElement());
        tb.putElement(Key.of("big_dec"), BigDecimal.TEN);
        tb.putElement(Key.of("big_neg"), BigDecimal.valueOf(-1298283729L));
        tb.putElement(Key.of("bigint"), new BigInteger("2888765432"));
        byte[] a = { 0x01, 0x45, (byte) 0xa3 };
        tb.putElement(Key.of("abc"), a);

        var data_tree = tb.build();
        System.out.println("String: "+data_tree.toString());
        assertEquals(Types.TYPE_DATA_TREE, data_tree.getType());
        assertFalse(data_tree.isNull(),"must not null");
        assertFalse(data_tree.isEmpty(),"must be empty");
        assertEquals(10, data_tree.size());
        //assertEquals("({str="value",bigdec=10,b=true,nu=<null>,dou=4.5,n=1})", data_tree.stringValue());
        assertEquals(Value.valueOf(1), data_tree.get(Key.of("n")).getValue());
        assertEquals("value", data_tree.get("str").getObject());

        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream(1024);
            data_tree.writeAsJSON(bo);
            JsonReader reader = Json.createReader(new ByteArrayInputStream(bo.toByteArray()));
            JsonObject obj = reader.readObject();
            assertEquals("value", obj.getString("str"));
            assertEquals(1, obj.getInt("n"));
            assertEquals(4.5d, obj.getJsonNumber("dou").doubleValue());
        } catch (IOException e) {
            fail(e.toString());
        }

        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream(1024);
            data_tree.writeAsXML("opt",bo);
            String v = bo.toString(StandardCharsets.UTF_8);
            System.out.println(v);
            assertTrue(v.contains("<str>value</str>"));
            assertTrue(v.contains("<abc>0145a3</abc>"));
            assertTrue(v.contains("<big_dec>10</big_dec>"));
            assertTrue(v.contains("<big_neg>-1298283729</big_neg>"));
            assertTrue(v.contains("<bigint>2888765432</bigint>"));
            assertTrue(v.contains("<nu/>"));
            assertTrue(v.contains("<nu2/>"));
            assertTrue(v.contains("<b>true</b>"));
        } catch (IOException e) {
            fail(e.toString());
        }
    }
}
