package esql.data;

import org.junit.jupiter.api.Test;

import esql.data.ValueDataTree.TreeBuilder;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
        
        System.out.println(data_tree.stringValue());
        
        
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
    
    @Test
    public void TwoLevelTreeTest() throws IOException {
    	ValueDataTree.TreeBuilder tb = ValueDataTree.builderOfDataTree();
    	
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
         
         
         tb.putElement(Key.of("str2"), "value");
         tb.putElement(Key.of("dou2"), 4.5d);
         tb.putElement(Key.of("b2"), true);
         tb.putNull(Key.of("nu2"));
         tb.putElement(Key.of("nu3"), tb.nullElement());
         tb.putElement(Key.of("big_dec2"), BigDecimal.TEN);
         
         tb.putElement(Key.of("big_neg2"), BigDecimal.valueOf(-1298283729L));
         tb.putElement(Key.of("bigint2"), new BigInteger("2888765432"));
         byte[] a2 = { 0x01, 0x45, (byte) 0xa3 };
         tb.putElement(Key.of("abc2"), a);
         
         ValueDataTree.ListBuilder listBuilder = tb.createList(Key.of("SubList"));
     	
     	
     	listBuilder.addElement(new StringElement("Value of SubList"),
     			new NumberElement(Types.TYPE_INT, 27),
     			new NumberElement(Types.TYPE_LONG, 9l),
     			new NumberElement(Types.TYPE_DOUBLE, 9.5d),
     			new NumberElement(Types.TYPE_DECIMAL, BigDecimal.valueOf(-12312894612L)),
     			new NumberElement(Types.TYPE_ULONG, new BigInteger("1231259787813"))
     			);
     	
     	
     			
     		
    	ValueDataTree data_tree = tb.build();
    	
    	
    	OutputStream outputStream = new ByteArrayOutputStream();
        data_tree.writeAsJSON(outputStream);
        
        JsonReader reader = Json.createReader(new ByteArrayInputStream(((ByteArrayOutputStream) outputStream).toByteArray()));
        
        JsonObject jsonObject = reader.readObject();
        
        System.out.println("------------Level 2 Data Tree Test JSON-------------");
        System.out.println(jsonObject);
        System.out.println("-----------------------------------------------------");
        
        assertEquals("value", jsonObject.getString("str2"));
        assertEquals(4.5d, jsonObject.getJsonNumber("dou").doubleValue());
        assertEquals(new BigInteger("2888765432"), jsonObject.getJsonNumber("bigint2").bigIntegerValue());
        
        
        ByteArrayOutputStream bo = new ByteArrayOutputStream(1024);
        data_tree.writeAsXML("opt",bo);
        String v = bo.toString(StandardCharsets.UTF_8);
        System.out.println("------------Level 2 Data Tree Test XML-------------");
        System.out.println(v);
        
        System.out.println("-----------------------------------------------------");
       
        assertTrue(v.contains("<str2>value</str2>"));
        assertTrue(v.contains("<abc2>0145a3</abc2>"));
        assertTrue(v.contains("<big_dec>10</big_dec>"));
        assertTrue(v.contains("<big_dec2>10</big_dec2>"));
        assertTrue(v.contains("<SubList>Value of SubList</SubList>"));
        
        assertTrue(v.contains("<nu3/>"));
        assertTrue(v.contains("<b2>true</b2>"));
    }
    
    @Test
    public void ThreeLevelTreeTest() throws IOException {
    	ValueDataTree.TreeBuilder tb = ValueDataTree.builderOfDataTree();
    	
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
        
        
       TreeBuilder subTree =  tb.createTree(Key.of("SubTree"));
       
       subTree.putElement(Key.of("douSub"), 4.5d);
       subTree.putElement(Key.of("bSub"), true);
       subTree.putNull(Key.of("nuSub"));
       subTree.putElement(Key.of("nu2Sub"), tb.nullElement());
       subTree.putElement(Key.of("big_decSub"), BigDecimal.TEN);
      
       subTree.putElement(Key.of("big_negSub"), BigDecimal.valueOf(-1298283729L));
       subTree.putElement(Key.of("bigintSub"), new BigInteger("2888765432"));
       byte[] asubTree = { 0x01, 0x45, (byte) 0xa3 };
       subTree.putElement(Key.of("abcSub"), a);
       
       
       ValueDataTree.ListBuilder listBuilder = tb.createList(Key.of("SubList"));
    	
    	
    	listBuilder.addElement(new StringElement("Value of SubList"),
    			new NumberElement(Types.TYPE_INT, 27),
    			new NumberElement(Types.TYPE_LONG, 9l),
    			new NumberElement(Types.TYPE_DOUBLE, 9.5d),
    			new NumberElement(Types.TYPE_DECIMAL, BigDecimal.valueOf(-12312894612L)),
    			new NumberElement(Types.TYPE_ULONG, new BigInteger("1231259787813"))
    			);
    	
        
    	ValueDataTree data_tree = tb.build();
    	
    	
    	System.out.println(data_tree);
    	
        
    	
     	OutputStream outputStream = new ByteArrayOutputStream();
        data_tree.writeAsJSON(outputStream);
        
        JsonReader reader = Json.createReader(new ByteArrayInputStream(
        		((ByteArrayOutputStream) outputStream).toByteArray())
        		
        		);
        
        JsonObject jsonObject = reader.readObject();
        System.out.println("------------Level 3 Data Tree Test JSON-------------");
        System.out.println(jsonObject);
        System.out.println("------------------------------------------------");
        
        
        
        ByteArrayOutputStream bo = new ByteArrayOutputStream(1024);
        data_tree.writeAsXML("opt",bo);
        String v = bo.toString(StandardCharsets.UTF_8);
        System.out.println("------------Level 3 Data Tree Test XML-------------");
        System.out.println(v);
        System.out.println("------------------------------------------------");
       
        
        assertTrue(v.contains("<abcSub>0145a3</abcSub>"));
        assertTrue(v.contains("<big_decSub>10</big_decSub>"));
       
        assertTrue(v.contains("<SubList>Value of SubList</SubList>"));
        assertTrue(v.contains("<SubTree>"));
        assertTrue(v.contains("</SubTree>"));
 
    }
    
    
    
}
