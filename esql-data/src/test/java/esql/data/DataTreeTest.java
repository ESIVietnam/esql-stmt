package esql.data;

import org.junit.jupiter.api.Test;

import esql.data.ValueDataTree.TreeBuilder;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonGeneratorFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class DataTreeTest {

	@Test
	public void NullTreeTest() {
		ValueDataTree data_tree = (ValueDataTree) Value.nullOf(Types.TYPE_DATA_TREE);
		assertEquals(ValueDataTree.nullOfDataTree(), data_tree);
		assertEquals(Types.TYPE_DATA_TREE, data_tree.getType());
		assertTrue(data_tree.isYAMLWritable(),"can write YAML");
		assertTrue(data_tree.isJSONWritable(),"can write JSON");
		assertTrue(data_tree.isXMLWritable(),"can write XML");
		assertTrue(data_tree.isNull(),"must not null");
		assertTrue(data_tree.isEmpty(),"must be empty");
		assertEquals(0, data_tree.size());
		assertEquals("", data_tree.stringValue());

		try {
			ByteArrayOutputStream bo = new ByteArrayOutputStream(1024);
			data_tree.writeAsJSON(bo);
			bo.close();
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
			data_tree.writeAsYAML(bo);
			assertEquals(4, bo.size()); //only ---\n
			assertEquals("---\n", bo.toString(StandardCharsets.UTF_8), "Must be empty YAMLSON Object");
		} catch (IOException e) {
			fail(e.toString());
		}

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
			data_tree.writeAsYAML(bo);
			String s = bo.toString(StandardCharsets.UTF_8);
			System.out.println(s);
			assertEquals("---\ntest: 1\n", bo.toString(StandardCharsets.UTF_8));
		} catch (IOException e) {
			fail(e.toString());
		}

		try {
			ByteArrayOutputStream bo = new ByteArrayOutputStream(1024);
			data_tree.writeAsJSON(bo);
			String s = bo.toString(StandardCharsets.UTF_8);
			System.out.println(s);
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
		tb.putElement(Key.of("_nu2"), tb.nullElement());
		tb.putElement(Key.of("big_dec"), BigDecimal.TEN);

		tb.putElement(Key.of("big_neg"), BigDecimal.valueOf(-1298283729L));
		tb.putElement(Key.of("bigint"), new BigInteger("2888765432"));
		tb.putElement(Key.of("str_multi"), "value\nnew line");
		tb.putElement(Key.of("key with space"), "as breaked");
		tb.putElement(Key.of("special"), "at sign @ is special");
		byte[] a = { 0x01, 0x45, (byte) 0xa3 };
		tb.putElement(Key.of("abc"), a);


		var data_tree = tb.build();
		System.out.println("String: "+data_tree.toString());
		assertEquals(Types.TYPE_DATA_TREE, data_tree.getType());
		assertFalse(data_tree.isNull(),"must not null");
		assertFalse(data_tree.isEmpty(),"must be empty");
		assertEquals(13, data_tree.size());
		//assertEquals("({str="value",bigdec=10,b=true,nu=<null>,dou=4.5,n=1})", data_tree.stringValue());
		assertEquals(Value.valueOf(1), data_tree.get(Key.of("n")).getValue());
		assertEquals("value", data_tree.get("str").getObject());

		try {
			ByteArrayOutputStream bo = new ByteArrayOutputStream(1024);
			data_tree.writeAsYAML(bo);
			bo.close();
			String v = bo.toString(StandardCharsets.UTF_8);
			//System.out.println(v);
			assertEquals("---\n" +
					"n: 1\n" +
					"str: value\n" +
					"dou: 4.5\n" +
					"b: true\n"+
					"nu: ~\n" +
					"_nu2: ~\n" +
					"big_dec: 10\n" +
					"big_neg: -1298283729\n" +
					"bigint: 2888765432\n" +
					"str_multi: |-\n" +
							" value\n" +
							" new line\n" +
					"'key with space': \"as breaked\"\n"+
					"special: \"at sign @ is special\"\n"+
					"abc: 0145a3\n"
					, v);
		} catch (IOException e) {
			fail(e.toString());
		}

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
			//System.out.println(v);
			assertTrue(v.contains("<str>value</str>"));
			assertTrue(v.contains("<abc>0145a3</abc>"));
			assertTrue(v.contains("<big_dec>10</big_dec>"));
			assertTrue(v.contains("<big_neg>-1298283729</big_neg>"));
			assertTrue(v.contains("<bigint>2888765432</bigint>"));
			assertTrue(v.contains("<nu/>"));
			assertTrue(v.contains("<_nu2/>"));
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

		ValueDataTree.TreeBuilder treeBuilder = tb.createTree(Key.of("SubMap"));

		treeBuilder.putElement(Key.of("empty"), "");
		treeBuilder.putElement(Key.of("str2"), "value");
		treeBuilder.putElement(Key.of("str3"), "value with\nnew line\n");
		treeBuilder.putElement(Key.of("dou2"), 4.5d);
		treeBuilder.putElement(Key.of("b2"), true);
		treeBuilder.putElement(Key.of("nu3"), tb.nullElement());
		treeBuilder.putElement(Key.of("big_dec2"), BigDecimal.TEN);

		treeBuilder.putElement(Key.of("big_neg2"), BigDecimal.valueOf(-1298283729L));
		treeBuilder.putElement(Key.of("bigint2"), new BigInteger("2888765432"));
		byte[] a2 = { 0x01, 0x45, (byte) 0xa3 };
		treeBuilder.putElement(Key.of("abc2"), a);

		ValueDataTree.ListBuilder listBuilder = tb.createList(Key.of("SubList"));

		listBuilder.addElement(new StringElement(""),
				new StringElement("Value of SubList"),
				new StringElement("Value of\nnew line"),
				new NumberElement(Types.TYPE_INT, 27),
				new NumberElement(Types.TYPE_LONG, 9l),
				new NumberElement(Types.TYPE_DOUBLE, 9.5d),
				new NumberElement(Types.TYPE_DECIMAL, BigDecimal.valueOf(-12312894612L)),
				new NumberElement(Types.TYPE_ULONG, new BigInteger("1231259787813"))
				);

		ValueDataTree data_tree = tb.build();

		try {
			ByteArrayOutputStream bo = new ByteArrayOutputStream(1024);
			data_tree.writeAsYAML(bo);
			bo.close();
			String v = bo.toString(StandardCharsets.UTF_8);

			System.out.println("------------Level 2 Data Tree Test YAML-------------");
			System.out.println(v);
			System.out.println("-----------------------------------------------------");

			assertEquals("---\n" +
				"str: value\n" +
				"dou: 4.5\n" +
				"b: true\n" +
				"nu: ~\n" +
				"nu2: ~\n" +
				"big_dec: 10\n" +
				"big_neg: -1298283729\n" +
				"bigint: 2888765432\n" +
				"abc: 0145a3\n" +
				"SubMap:\n" +
				"  empty: \"\"\n" +
				"  str2: value\n" +
				"  str3: |\n" +
				"   value with\n" +
				"   new line\n" +
				"  dou2: 4.5\n" +
				"  b2: true\n" +
				"  nu3: ~\n" +
				"  big_dec2: 10\n" +
				"  big_neg2: -1298283729\n" +
				"  bigint2: 2888765432\n" +
				"  abc2: 0145a3\n" +
				"SubList:\n" +
				" - \"\"\n" +
				" - \"Value of SubList\"\n" +
				" - |-\n" +
				"   Value of\n" +
				"   new line\n" +
				" - 27\n" +
				" - 9\n" +
				" - 9.5\n" +
				" - -12312894612\n" +
				" - 1231259787813\n"
				, v);
		} catch (IOException e) {
			fail(e.toString());
		}

		try(OutputStream outputStream = new ByteArrayOutputStream();) {
			data_tree.writeAsJSON(outputStream);

			JsonReader reader = Json.createReader(new ByteArrayInputStream(((ByteArrayOutputStream) outputStream).toByteArray()));

			JsonObject jsonObject = reader.readObject();

			System.out.println("------------Level 2 Data Tree Test JSON-------------");
			System.out.println(jsonObject);
			System.out.println("-----------------------------------------------------");

			assertEquals("value", jsonObject.getJsonObject("SubMap").getString("str2"));
			assertEquals(4.5d, jsonObject.getJsonNumber("dou").doubleValue());
			assertEquals("\"Value of SubList\"", jsonObject.getJsonArray("SubList").get(1).toString());
			assertEquals(new BigInteger("2888765432"), jsonObject.getJsonObject("SubMap").getJsonNumber("bigint2").bigIntegerValue());
		} catch (IOException e) {
			fail(e.toString());
		}

		try(ByteArrayOutputStream bo = new ByteArrayOutputStream(1024);) {
			data_tree.writeAsXML("opt", bo);
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
		} catch (IOException e) {
			fail(e.toString());
		}
	}

	@Test
	public void ThreeLevelTreeTest() throws IOException {
		ValueDataTree.TreeBuilder tb = ValueDataTree.builderOfDataTree();

		tb.putElement(Key.of("str"), "value");
		tb.putElement(Key.of("dou"), 4.5d);
		tb.putElement(Key.of("b"), true);
		tb.putNull(Key.of("nu"));
		tb.putElement(Key.of("_nu2"), tb.nullElement());
		tb.putElement(Key.of("big_dec"), BigDecimal.TEN);

		tb.putElement(Key.of("big_neg"), BigDecimal.valueOf(-1298283729L));
		tb.putElement(Key.of("bigint"), new BigInteger("2888765432"));
		byte[] a = { 0x01, 0x45, (byte) 0xa3 };
		tb.putElement(Key.of("abc"), a);

		tb.createTree(Key.of("emptyTree"));
		tb.createList(Key.of("emptyList"));

		TreeBuilder subTree =  tb.createTree(Key.of("SubTree"));

		subTree.putElement(Key.of("douSub"), 4.5d);
		subTree.putElement(Key.of("bSub"), true);
		subTree.putNull(Key.of("nuSub"));
		subTree.putElement(Key.of("nu2Sub"), tb.nullElement());
		subTree.putElement(Key.of("big_decSub"), BigDecimal.TEN);

		subTree.putElement(Key.of("big_negSub"), BigDecimal.valueOf(-1298283729L));
		subTree.putElement(Key.of("bigintSub"), new BigInteger("2888765432"));

		ValueDataTree.ListBuilder listBuilder = subTree.createList(Key.of("SubList"));

		listBuilder.createTreeBuilder().putElement(Key.of("ss1"), 1);

		listBuilder.addElement(new StringElement("Value of SubList"),
				new NumberElement(Types.TYPE_INT, 27),
				new NumberElement(Types.TYPE_LONG, 9l),
				new NumberElement(Types.TYPE_DOUBLE, 9.5d),
				new NumberElement(Types.TYPE_DECIMAL, BigDecimal.valueOf(-12312894612L)),
				new NumberElement(Types.TYPE_ULONG, new BigInteger("1231259787813"))
		);
		listBuilder.createListBuilder().addElement(new NumberElement(Types.TYPE_INT, 28),
				new NumberElement(Types.TYPE_INT, 29));

		byte[] asubTree = { 0x01, 0x45, (byte) 0xa3 };
		subTree.putElement(Key.of("abcSub"), a);

		ValueDataTree data_tree = tb.build();


		//System.out.println(data_tree);

		try {
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			data_tree.writeAsYAML(bo);
			bo.close();
			String v = bo.toString(StandardCharsets.UTF_8);
			assertEquals("---\n" +
							"str: value\n" +
							"dou: 4.5\n" +
							"b: true\n" +
							"nu: ~\n" +
							"_nu2: ~\n" +
							"big_dec: 10\n" +
							"big_neg: -1298283729\n" +
							"bigint: 2888765432\n" +
							"abc: 0145a3\n" +
							"emptyTree: {}\n" +
							"emptyList: []\n" +
							"SubTree:\n" +
							"  douSub: 4.5\n" +
							"  bSub: true\n" +
							"  nuSub: ~\n" +
							"  nu2Sub: ~\n" +
							"  big_decSub: 10\n" +
							"  big_negSub: -1298283729\n" +
							"  bigintSub: 2888765432\n" +
							"  SubList:\n" +
							"   - ss1: 1\n" +
							"   - \"Value of SubList\"\n" +
							"   - 27\n" +
							"   - 9\n" +
							"   - 9.5\n" +
							"   - -12312894612\n" +
							"   - 1231259787813\n" +
							"   - - 28\n" +
							"     - 29\n" +
							"  abcSub: 0145a3\n"
					, v);
		}
		catch (IOException e) {
			fail(e.toString());
		}

		try {
			OutputStream outputStream = new ByteArrayOutputStream();
			data_tree.writeAsJSON(outputStream);
			outputStream.close();

			JsonReader reader = Json.createReader(new ByteArrayInputStream(
					((ByteArrayOutputStream) outputStream).toByteArray())

			);

			JsonObject jsonObject = reader.readObject();
			System.out.println("------------Level 3 Data Tree Test JSON-------------");
			System.out.println(jsonObject);
			System.out.println("------------------------------------------------");
		}
		catch (IOException e) {
			fail(e.toString());
		}

		try {
			ByteArrayOutputStream bo = new ByteArrayOutputStream(1024);
			data_tree.writeAsXML("opt", bo);
			bo.close();
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
		catch (IOException e) {
			fail(e.toString());
		}
	}

	@Test
	public void NullElementTest() {
		NullElement nullElement = new NullElement(Types.TYPE_INT);

		assertEquals("<null>", nullElement.toString());
		assertEquals(Types.TYPE_INT, nullElement.getType());
		assertEquals(null, nullElement.getValue());
		assertEquals(null, nullElement.getObject());
		assertEquals(null, nullElement.getTree());
		assertEquals(null,nullElement.getList());



	}

	@Test
	public void StringElementTest() throws XMLStreamException {
		StringElement stringElement = new StringElement("Testing Value");
		StringWriter stringWriter = new StringWriter();
		StringWriter stringWriter2 = new StringWriter();

		assertEquals(Types.TYPE_STRING, stringElement.getType());
		assertEquals(ValueString.buildString(Types.TYPE_STRING, (String) stringElement.getObject()), stringElement.getValue());


		XMLOutputFactory factory = XMLOutputFactory.newFactory();
		XMLStreamWriter writer = factory.createXMLStreamWriter(stringWriter);

		//test writing only element
		ValueDataTreeImpl.writeElementAsXML(null, Key.of("string_value"), stringElement, writer);
		String xmlString = stringWriter.getBuffer().toString();
		assertEquals("<string_value>Testing Value</string_value>", xmlString);
		writer.close();

		JsonGenerator generator = Json.createGenerator(stringWriter2);

		generator.writeStartObject();
		ValueDataTreeImpl.writeElementAsJSON(Key.of("Testing Key"), stringElement, generator);
		generator.writeEnd();
		generator.close();

		String jsonString = stringWriter2.toString();
		assertEquals("{\"Testing Key\":\"Testing Value\"}", jsonString);
	}

	@Test
	public void NumberElementTest() throws XMLStreamException {
		NumberElement typeDecimal = new NumberElement(Types.TYPE_DECIMAL, BigDecimal.valueOf(128937128937182973.123));
		NumberElement typeULong = new NumberElement(Types.TYPE_ULONG, BigInteger.valueOf(19028390123890l));
		NumberElement typeLong = new NumberElement(Types.TYPE_LONG, 190390123890l);

		StringWriter stringTypeDecimal = new StringWriter();
		StringWriter stringTypeULong = new StringWriter();
		StringWriter stringTypeLong = new StringWriter();

		XMLOutputFactory factory = XMLOutputFactory.newFactory();
		XMLStreamWriter xmlWriterDECIMAL = factory.createXMLStreamWriter(stringTypeDecimal);
		XMLStreamWriter xmlWriterULONG = factory.createXMLStreamWriter(stringTypeULong);
		XMLStreamWriter xmlWriterLONG = factory.createXMLStreamWriter(stringTypeLong);

		ValueDataTreeImpl.writeElementAsXML(null, Key.of("key_decimal"), typeDecimal, xmlWriterDECIMAL);
		ValueDataTreeImpl.writeElementAsXML(null, Key.of("key_ULong"), typeULong, xmlWriterULONG);
		ValueDataTreeImpl.writeElementAsXML(null, Key.of("key_Long"), typeLong, xmlWriterLONG);

		String xmlStringDecimal = stringTypeDecimal.getBuffer().toString();
		String xmlStringULong = stringTypeULong.getBuffer().toString();
		String xmlStringLong = stringTypeLong.getBuffer().toString();

		assertEquals("<key_decimal>128937128937182976</key_decimal>", xmlStringDecimal);
		assertEquals("<key_ULong>19028390123890</key_ULong>",xmlStringULong);
		assertEquals("<key_Long>190390123890</key_Long>",xmlStringLong);

		StringWriter stringJsonTypeDecimal = new StringWriter();
		StringWriter stringJsonTypeULong = new StringWriter();
		StringWriter stringJsonTypeLong = new StringWriter();

		JsonGenerator generator = Json.createGenerator(stringJsonTypeDecimal);
		generator.writeStartObject();
		ValueDataTreeImpl.writeElementAsJSON(Key.of("Decimal_Value"), typeDecimal, generator);
		generator.writeEnd();
		generator.close();

		JsonGenerator generator2 = Json.createGenerator(stringJsonTypeULong);

		generator2.writeStartObject();
		ValueDataTreeImpl.writeElementAsJSON(Key.of("ULONG_Value"), typeULong, generator2);
		generator2.writeEnd();
		generator2.close();

		JsonGenerator generator3 = Json.createGenerator(stringJsonTypeLong);

		generator3.writeStartObject();
		ValueDataTreeImpl.writeElementAsJSON(Key.of("LONG_Value"), typeLong, generator3);
		generator3.writeEnd();
		generator3.close();

		String jsonStringDecimal = stringJsonTypeDecimal.toString();
		String jsonStringULong = stringJsonTypeULong.toString();
		String jsonStringLong = stringJsonTypeLong.toString();

		assertEquals("{\"Decimal_Value\":128937128937182976}", jsonStringDecimal);
		assertEquals("{\"ULONG_Value\":19028390123890}", jsonStringULong);
		assertEquals("{\"LONG_Value\":190390123890}", jsonStringLong);
	}

	@Test
	public void BooleanElementTest() throws XMLStreamException {
		BooleanElement booleanElement = new BooleanElement(true);
		StringWriter stringWriter = new StringWriter();


		XMLOutputFactory factory = XMLOutputFactory.newFactory();
		XMLStreamWriter xmlStreamWriter = factory.createXMLStreamWriter(stringWriter);
		ValueDataTreeImpl.writeElementAsXML(null, Key.of("Boolean_key"), booleanElement, xmlStreamWriter);
		xmlStreamWriter.close();

		String xmlStringBoolean = stringWriter.getBuffer().toString();
		assertEquals("<Boolean_key>true</Boolean_key>", xmlStringBoolean);

		StringWriter stringJsonBoolean = new StringWriter();

		JsonGenerator generator = Json.createGenerator(stringJsonBoolean);

		generator.writeStartObject();
		ValueDataTreeImpl.writeElementAsJSON(Key.of("Boolean_key"), booleanElement, generator);
		generator.writeEnd();
		generator.close();

		String jsonStringBoolean = stringJsonBoolean.toString();
		assertEquals("{\"Boolean_key\":true}", jsonStringBoolean);

	}

	@Test
	public void SubTreeElementTest() throws XMLStreamException, IOException {
		NumberElement numberElement = new NumberElement(Types.TYPE_DECIMAL, BigDecimal.valueOf(12312321312l));
		BooleanElement booleanElement = new BooleanElement(false);
		StringElement stringElement = new StringElement("Value_String");
		NullElement nullElement = new NullElement(Types.TYPE_STRING);

		Map<Key, ValueDataTreeElement> map = new LinkedHashMap<>();
		List<ValueDataTreeElement> list = new LinkedList<ValueDataTreeElement>();

		list.add(numberElement);
		list.add(stringElement);
		list.add(booleanElement);
		list.add(nullElement);

		SubListElement subListElement = new SubListElement(list);


		map.put(Key.of("Number_value"), numberElement);
		map.put(Key.of("String_value"), stringElement);
		map.put(Key.of("Boolean_value"), booleanElement);
		map.put(Key.of("Null_value"), nullElement);
		map.put(Key.of("SubList_value"), subListElement);

		SubTreeElement subTreeElement = new SubTreeElement(map);

		StringWriter stringWriterYAML = new StringWriter();
		int[] stack = new int[ValueDataTreeImpl.DATA_TREE_MAXIMUM_LEVELS];
		stack[0] = ValueDataTreeImpl.TREE_CURR_IS_KEY;
		ValueDataTreeImpl.writeElementAsYaml(stack, 1, true, subTreeElement.getTree(), stringWriterYAML);

		stringWriterYAML.close();
		String yamlStringSubList = stringWriterYAML.toString();
		assertEquals(ValueDataTreeImpl.lineSeparator+
				"  Number_value: 12312321312"+ValueDataTreeImpl.lineSeparator+
						"  String_value: Value_String"+ValueDataTreeImpl.lineSeparator+
						"  Boolean_value: false"+ValueDataTreeImpl.lineSeparator+
						"  Null_value: ~"+ValueDataTreeImpl.lineSeparator+
						"  SubList_value:"+ValueDataTreeImpl.lineSeparator+
						"   - 12312321312" +ValueDataTreeImpl.lineSeparator+
						"   - Value_String" +ValueDataTreeImpl.lineSeparator+
						"   - false" +ValueDataTreeImpl.lineSeparator+
						"   - ~"+ValueDataTreeImpl.lineSeparator, yamlStringSubList);

		StringWriter stringWriterXML = new StringWriter();

		XMLOutputFactory factory = XMLOutputFactory.newFactory();
		XMLStreamWriter xmlStreamWriter = factory.createXMLStreamWriter(stringWriterXML);
		xmlStreamWriter.writeStartDocument();
		xmlStreamWriter.writeStartElement("root");

		ValueDataTreeImpl.writeElementAsXML(null, Key.of("SubTree_key"), subTreeElement, xmlStreamWriter);

		xmlStreamWriter.writeEndElement();
		xmlStreamWriter.writeEndDocument();
		xmlStreamWriter.close();

		String xmlStringSubTree = stringWriterXML.getBuffer().toString();

		assertEquals("<?xml version=\"1.0\" ?><root><SubTree_key>"
				+ "<Number_value>12312321312</Number_value>"
				+ "<String_value>Value_String</String_value><Boolean_value>false</Boolean_value>"
				+ "<Null_value/><SubList_value>12312321312</SubList_value><SubList_value>Value_String</SubList_value><SubList_value>false</SubList_value><SubList_value/>"
				+ "</SubTree_key></root>", xmlStringSubTree);


		StringWriter stringJsonSubTree = new StringWriter();

		JsonGenerator generator = Json.createGenerator(stringJsonSubTree);

		generator.writeStartObject();
		ValueDataTreeImpl.writeElementAsJSON(Key.of("SubTree_key"), subTreeElement, generator);
		generator.writeEnd();
		generator.close();
		String jsonStringSubTree = stringJsonSubTree.toString();

		assertEquals("{\"SubTree_key\":{\"Number_value\":12312321312,\"String_value\":\"Value_String\",\"Boolean_value\":false,\"Null_value\":null,\"SubList_value\":[12312321312,\"Value_String\",false,null]}}", jsonStringSubTree);
	}


	@Test
	public void SubListElementTest() throws XMLStreamException, IOException {
		NumberElement numberElement = new NumberElement(Types.TYPE_DECIMAL, BigDecimal.valueOf(12312321312l));
		BooleanElement booleanElement = new BooleanElement(false);
		StringElement stringElement = new StringElement("Value_String");
		

		List<ValueDataTreeElement> list = new LinkedList<ValueDataTreeElement>();

		list.add(numberElement);
		list.add(stringElement);
		list.add(booleanElement);

		SubListElement subListElement = new SubListElement(list);

		StringWriter stringWriterYAML = new StringWriter();
		int[] stack = new int[ValueDataTreeImpl.DATA_TREE_MAXIMUM_LEVELS];
		stack[0] = 0;
		stack[1] = ValueDataTreeImpl.TREE_CURR_IS_LIST;
		ValueDataTreeImpl.writeElementAsYaml(stack, 1, false, subListElement.getList(), stringWriterYAML);

		stringWriterYAML.close();
		String yamlStringSubList = stringWriterYAML.toString();
		assertEquals(" - 12312321312" +ValueDataTreeImpl.lineSeparator+
				" - Value_String" +ValueDataTreeImpl.lineSeparator+
				" - false"+ValueDataTreeImpl.lineSeparator, yamlStringSubList);

		StringWriter stringWriterXML = new StringWriter();
		XMLOutputFactory factory = XMLOutputFactory.newFactory();
		XMLStreamWriter xmlStreamWriter = factory.createXMLStreamWriter(stringWriterXML);
		xmlStreamWriter.writeStartDocument();
		xmlStreamWriter.writeStartElement("root");
		ValueDataTreeImpl.writeElementAsXML(null, Key.of("SubList_key"), subListElement, xmlStreamWriter);
		xmlStreamWriter.writeEndElement();
		xmlStreamWriter.writeEndDocument();
		xmlStreamWriter.close();
		String xmlStringSubList = stringWriterXML.toString();
		assertEquals("<?xml version=\"1.0\" ?><root><SubList_key>12312321312</SubList_key><SubList_key>Value_String</SubList_key><SubList_key>false</SubList_key></root>", xmlStringSubList);
		
		StringWriter stringJsonSubList = new StringWriter();

		JsonGenerator generator = Json.createGenerator(stringJsonSubList);

		generator.writeStartObject();

		ValueDataTreeImpl.writeElementAsJSON(Key.of("SubList_key"), subListElement, generator);

		generator.writeEnd();
		generator.close();
		String jsonStringSubList = stringJsonSubList.toString();
		
		assertEquals("{\"SubList_key\":[12312321312,\"Value_String\",false]}", jsonStringSubList);

	}

}
