package esql.data;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.stream.JsonGenerator;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * implementation of data_tree type as an unmodifiableMap
 *
 */
public class ValueDataTreeImpl extends ValueDataTree {
    public static final ValueDataTreeImpl NULL_DATA_TREE = new ValueDataTreeImpl(null);
    private static final NullElement NULL_ELEMENT = new NullElement(Types.TYPE_STRING);
    private static final BooleanElement TRUE_ELEMENT = new BooleanElement(true);
    private static final BooleanElement FALSE_ELEMENT = new BooleanElement(false);

    private final Map<Key, ValueDataTreeElement> map;
    ValueDataTreeImpl(Map<Key, ValueDataTreeElement> map) {
        this.map = map;
    }

    @Override
    public boolean isJSONNative() {
        return true;
    }

    @Override
    public void writeAsJSON(OutputStream output) throws IOException {
        if(this.map == null)
            return;
        //dung writer de write
        writeAsJSON(new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8)));
    }

    @Override
    public void writeAsJSON(Writer writer) throws IOException {
        if(this.map == null)
            return;
        try(JsonGenerator gen = Json.createGenerator(writer)) {
            writeAsJSON(gen);
        }
        catch (JsonException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void writeAsJSON(JsonGenerator generator) throws JsonException {
        if(this.map == null)
            return;
        generator.writeStartObject();
        for (Entry<Key, ValueDataTreeElement> e: this.map.entrySet()) {
            e.getValue().writeAsJSONPair(generator, e.getKey());
        }
        generator.writeEnd();
    }

    @Override
    public boolean isXMLNative() {
        return true;
    }

    @Override
    public void writeAsXML(String namespaceURI, String rootElementName, OutputStream output) throws IOException {
        //dung writer de write
        writeAsXML(namespaceURI, rootElementName, new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8)));
    }

    @Override
    public void writeAsXML(String namespaceURI, String rootElementName, Writer writer) throws IOException {
        if(this.map == null)
            return;
        XMLOutputFactory factory = XMLOutputFactory.newFactory();
        try {
            XMLStreamWriter xmlWriter = factory.createXMLStreamWriter(writer);
            try {
                xmlWriter.writeStartDocument(StandardCharsets.UTF_8.name(),"1.0");
                writeAsXML(namespaceURI, rootElementName, xmlWriter);
                xmlWriter.writeEndDocument();
            }
            finally {
                xmlWriter.close();
            }
        } catch (XMLStreamException e) {
            throw new IOException(e.getMessage(),e);
        }
    }

    @Override
    public void writeAsXML(String namespaceURI, String rootElementName, XMLStreamWriter writer) throws XMLStreamException {
        if(this.map == null)
            return;
        if(namespaceURI != null && !namespaceURI.isBlank()) {
            writer.writeStartElement(rootElementName);
            writer.writeDefaultNamespace(namespaceURI);
            for (Entry<Key, ValueDataTreeElement> e: this.map.entrySet()) {
                e.getValue().writeAsXML(namespaceURI, writer, e.getKey());
            }
            writer.writeEndElement();
        }
        else {
            writer.writeStartElement(rootElementName);
            for (Entry<Key, ValueDataTreeElement> e: this.map.entrySet()) {
                e.getValue().writeAsXML(writer, e.getKey());
            }
            writer.writeEndElement();
        }
    }

    static class TreeBuilderImpl extends TreeBuilder {
        final HashMap<Key, ValueDataTreeElement> map;
        final HashMap<Key, ListBuilderImpl> subListMap;
        final HashMap<Key, TreeBuilderImpl> subTreeMap;

        TreeBuilderImpl() {
            this.map = new HashMap<>();
            this.subListMap = new HashMap<>(1);
            this.subTreeMap = new HashMap<>(3);
        }

        @Override
        public ValueDataTreeElement nullElement() {
            return NULL_ELEMENT;
        }

        @Override
        public ValueDataTreeElement valueOf(boolean val) {
            return val ? TRUE_ELEMENT : FALSE_ELEMENT;
        }

        @Override
        public ValueDataTreeElement valueOf(long val) {
            return new NumberElement(Types.TYPE_LONG, Long.valueOf(val));
        }

        @Override
        public ValueDataTreeElement valueOf(double val) {
            return new NumberElement(Types.TYPE_DOUBLE, Double.valueOf(val));
        }

        @Override
        public ValueDataTreeElement valueOf(BigDecimal val) {
            return new NumberElement(Types.TYPE_DECIMAL, val);
        }

        @Override
        public ValueDataTreeElement valueOf(BigInteger val) {
            return new NumberElement(Types.TYPE_ULONG, val);
        }

        @Override
        public ValueDataTreeElement valueOf(String val) {
            return new StringElement(val);
        }

        @Override
        public ValueDataTreeElement valueOf(byte[] val) {
            return new StringElement(ValueBytes.bytesToHex(val));
        }

        @Override
        public TreeBuilder putNull(Key key) {
            map.put(key, NULL_ELEMENT);
            return this;
        }

        @Override
        public TreeBuilder putElement(Key key, ValueDataTreeElement element) {
            map.put(key, element);
            return this;
        }

        @Override
        public TreeBuilder putElement(Key key, boolean val) {
            return putElement(key, valueOf(val));
        }

        @Override
        public TreeBuilder putElement(Key key, long val) {
            return putElement(key, valueOf(val));
        }

        @Override
        public TreeBuilder putElement(Key key, double val) {
            return putElement(key, valueOf(val));
        }

        @Override
        public TreeBuilder putElement(Key key, BigDecimal val) {
            if(val == null)
                return putNull(key);
            return putElement(key, valueOf(val));
        }

        @Override
        public TreeBuilder putElement(Key key, BigInteger val) {
            if(val == null)
                return putNull(key);
            return putElement(key, valueOf(val));
        }

        @Override
        public TreeBuilder putElement(Key key, String val) {
            if(val == null)
                return putNull(key);
            return putElement(key, valueOf(val));
        }

        @Override
        public TreeBuilder putElement(Key key, byte[] val) {
            if(val == null)
                return putNull(key);
            return putElement(key, valueOf(val));
        }

        @Override
        public TreeBuilder putSubTree(Key key, TreeBuilder subTreeBuilder) {
            if(!(subTreeBuilder instanceof TreeBuilderImpl))
                throw new IllegalArgumentException("subTreeBuilder must be same set of implementation as "+this.getClass());
            subTreeMap.put(key, (TreeBuilderImpl) subTreeBuilder);
            return this;
        }

        @Override
        public ListBuilder createList(Key key) {
            var listBuilder = new ListBuilderImpl();
            subListMap.put(key, listBuilder);
            return listBuilder;
        }

        @Override
        public TreeBuilder putList(Key key, ListBuilder listBuilder) {
            if(!(listBuilder instanceof ListBuilderImpl))
                throw new IllegalArgumentException("listBuilder must be same set of implementation as "+this.getClass());
            subListMap.put(key, (ListBuilderImpl) listBuilder);
            return this;
        }

        @Override
        public TreeBuilder createTree(Key key) {
            var treeBuilder = new TreeBuilderImpl();
            subTreeMap.put(key, treeBuilder);
            return treeBuilder;
        }

        @Override
        public ValueDataTree build() {
            popylateSubTreeOrList();
            return new ValueDataTreeImpl(Collections.unmodifiableMap(map));
        }

        private void popylateSubTreeOrList() {
            for(var entry: subListMap.entrySet())
                map.put(entry.getKey(), entry.getValue().buildListElement());
            for(var entry: subTreeMap.entrySet())
                map.put(entry.getKey(), entry.getValue().buildTreeElement());
        }

        private ValueDataTreeElement buildTreeElement() {
            popylateSubTreeOrList();
            return new SubTreeElement(Collections.unmodifiableMap(map));
        }

        @Override
        public int size() {
            return map.size();
        }

        @Override
        public boolean isEmpty() {
            return map.isEmpty();
        }

        @Override
        public Iterator<Key> iterator() {
            return map.keySet().iterator();
        }

        @Override
        public TreeBuilder putAll(Map<Key, ? extends ValueDataTreeElement> m) {
            map.putAll(m);
            return this;
        }

        @Override
        public Spliterator<Key> spliterator() {
            return map.keySet().spliterator();
        }
    }

    static class ListBuilderImpl extends ListBuilder {
        private final LinkedList<ValueDataTreeElement> list;

        ListBuilderImpl() {
            this.list = new LinkedList<>();
        }

        @Override
        public ListBuilder addAll(Collection<ValueDataTreeElement> elements) {
            list.addAll(elements);
            return this;
        }

        @Override
        public ListBuilder addElement(ValueDataTreeElement... element) {
            for(ValueDataTreeElement e: element)
                list.add(e);
            return this;
        }

        @Override
        ValueDataTreeElement buildListElement() {
            return new SubListElement(Collections.unmodifiableList(list));
        }

        @Override
        public Iterator<ValueDataTreeElement> iterator() {
            return list.iterator();
        }

        @Override
        public Spliterator<ValueDataTreeElement> spliterator() {
            return list.spliterator();
        }
    }

    @Override
    public String toString() {
        if(map == null)
            return "";
        return "({"+
                map.entrySet().stream()
                        .map(e -> e.getKey()+"="+e.getValue().toString())
                        .collect(Collectors.joining(","))
                +"})";
    }

    @Override
    public int size() {
        if(map == null)
            return 0;
        return map.size();
    }

    @Override
    public boolean isNull() {
        return map == null;
    }

    @Override
    public boolean isEmpty() {
        if(map == null)
            return true;
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        if(map == null || key == null)
            return false;
        if(key instanceof Key)
            return map.containsKey(key);
        return map.containsKey(Key.ofDataTreeKey(key.toString()));
    }

    @Override
    public boolean containsValue(Object value) {
        if(map == null)
            return false;
        if(value == null)
            return map.containsValue(NULL_ELEMENT);
        return map.containsValue(value);
    }

    @Override
    public ValueDataTreeElement get(Object key) {
        if(map == null || key == null)
            return NULL_ELEMENT;
        if(key instanceof Key)
            return map.get(key);
        return map.get(Key.ofDataTreeKey(key.toString()));
    }

    @Override
    public ValueDataTreeElement put(Key key, ValueDataTreeElement value) {
        throw new UnsupportedOperationException("data_tree can not be modified");
    }

    @Override
    public ValueDataTreeElement remove(Object key) {
        throw new UnsupportedOperationException("data_tree can not be modified");
    }

    @Override
    public void putAll(Map<? extends Key, ? extends ValueDataTreeElement> m) {
        throw new UnsupportedOperationException("data_tree can not be modified");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("data_tree can not be modified");
    }

    @Override
    public Set<Key> keySet() {
        if(map == null)
            return Collections.emptySet();
        return map.keySet();
    }

    @Override
    public Collection<ValueDataTreeElement> values() {
        if(map == null)
            return Collections.emptyList();
        return map.values();
    }

    @Override
    public Set<Entry<Key, ValueDataTreeElement>> entrySet() {
        if(map == null)
            return Collections.emptySet();
        return map.entrySet();
    }

    @Override
    public boolean isTrue() {
        return !isEmpty();
    }

    @Override
    public Value convertTo(Types type) {
        return null;
    }

    @Override
    public Types getType() {
        return Types.TYPE_DATA_TREE;
    }

    @Override
    public String stringValue() {
        //data tree does not have "serialization string form"
        return toString();
    }

    @Override
    public int compareTo(Value o) {
        //TODO: compare trees is complicated
        return 0;
    }
}

/**
 * Null element, with type.
 */
class NullElement implements ValueDataTreeElement {
    private final Types type;

    NullElement(Types type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "<null>";
    }

    @Override
    public Types getType() {
        return type;
    }

    @Override
    public Value getValue() {
        return null;
    }

    @Override
    public Object getObject() {
        return null;
    }

    @Override
    public boolean isScalar() {
        return true;
    }

    @Override
    public Map<Key, ValueDataTreeElement> getTree() {
        return null;
    }

    @Override
    public List<ValueDataTreeElement> getList() {
        return null;
    }

    @Override
    public void writeAsJSONValue(JsonGenerator generator) throws JsonException {
        generator.writeNull();
    }

    @Override
    public void writeAsJSONPair(JsonGenerator generator, Key name) throws JsonException {
        generator.writeNull(name.origString());
    }

    @Override
    public void writeAsXML(XMLStreamWriter writer, Key name) throws XMLStreamException {
        writer.writeEmptyElement(name.origString());
    }

    @Override
    public void writeAsXML(String namespaceURI, XMLStreamWriter writer, Key name) throws XMLStreamException {
        writer.writeEmptyElement(namespaceURI, name.origString());
    }
}

class BooleanElement implements ValueDataTreeElement {
    private final Boolean bool;

    BooleanElement(boolean bool) {
        this.bool = bool ? Boolean.TRUE : Boolean.FALSE;
    }

    @Override
    public String toString() {
        return String.valueOf(bool);
    }

    @Override
    public Types getType() {
        return Types.TYPE_BOOLEAN;
    }

    @Override
    public Value getValue() {
        return ValueBoolean.buildBoolean(this.bool.booleanValue());
    }

    @Override
    public Object getObject() {
        return bool;
    }

    @Override
    public boolean isScalar() {
        return true;
    }

    @Override
    public Map<Key, ValueDataTreeElement> getTree() {
        return null;
    }

    @Override
    public List<ValueDataTreeElement> getList() {
        return null;
    }

    @Override
    public void writeAsJSONValue(JsonGenerator generator) throws JsonException {
        generator.write(this.bool.booleanValue());
    }

    @Override
    public void writeAsJSONPair(JsonGenerator generator, Key name) throws JsonException {
        generator.write(name.origString(), this.bool.booleanValue());
    }

    @Override
    public void writeAsXML(XMLStreamWriter writer, Key name) throws XMLStreamException {
        writer.writeStartElement(name.origString());
        writer.writeCharacters(this.bool ? ValueBoolean.STR_TRUE : ValueBoolean.STR_FALSE);
        writer.writeEndElement();
    }

    @Override
    public void writeAsXML(String namespaceURI, XMLStreamWriter writer, Key name) throws XMLStreamException {
        writer.writeStartElement(namespaceURI, name.origString());
        writer.writeCharacters(this.bool ? ValueBoolean.STR_TRUE : ValueBoolean.STR_FALSE);
        writer.writeEndElement();
    }
}

class StringElement implements ValueDataTreeElement {
    private final String value;

    StringElement(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        if(value == null)
            return "<null>";
        return "\""+value.replaceAll("\n", "\\n").replaceAll("\r", "\\r")
                .replaceAll("\"","\\\"")+"\"";
    }

    @Override
    public Types getType() {
        return Types.TYPE_STRING;
    }

    @Override
    public Value getValue() {
        return ValueString.buildString(Types.TYPE_STRING, value);
    }

    @Override
    public Object getObject() {
        return value;
    }

    @Override
    public boolean isScalar() {
        return true;
    }

    @Override
    public Map<Key, ValueDataTreeElement> getTree() {
        return null;
    }

    @Override
    public List<ValueDataTreeElement> getList() {
        return null;
    }

    @Override
    public void writeAsJSONValue(JsonGenerator generator) throws JsonException {
        if(value == null)
            generator.writeNull();
        else
            generator.write(this.value);
    }

    @Override
    public void writeAsJSONPair(JsonGenerator generator, Key name) throws JsonException {
        if(value == null)
            generator.writeNull(name.origString());
        else
            generator.write(name.origString(), this.value);
    }

    @Override
    public void writeAsXML(XMLStreamWriter writer, Key name) throws XMLStreamException {
        writer.writeStartElement(name.origString());
        if(this.value.length()<=1024
                && !(this.value.contains("<")
                || this.value.contains("&")))
            writer.writeCharacters(this.value);
        else
            writer.writeCData(this.value);
        writer.writeEndElement();
    }

    @Override
    public void writeAsXML(String namespaceURI, XMLStreamWriter writer, Key name) throws XMLStreamException {
        writer.writeStartElement(namespaceURI, name.origString());
        if(this.value.length()<=1024
                && !(this.value.contains("<")
                || this.value.contains("&")))
            writer.writeCharacters(this.value);
        else
            writer.writeCData(this.value);
        writer.writeEndElement();
    }
}

class NumberElement implements ValueDataTreeElement {
    private final Types type;
    private final Number number;

    NumberElement(Types type, Number number) {
        this.type = type;
        this.number = number;
    }

    @Override
    public String toString() {
        return number.toString();
    }

    @Override
    public Types getType() {
        return type;
    }

    @Override
    public Value getValue() {
        if(type.equals(Types.TYPE_DECIMAL))
            return ValueNumber.buildNumber(type, (BigDecimal) number);
        if(type.equals(Types.TYPE_ULONG))
            return ValueNumber.buildNumber(type, (BigInteger) number);
        if(Types.isInteger(type))
            return ValueNumber.buildNumber(type, number.longValue());
        return ValueNumber.buildNumber(type, number.doubleValue());
    }

    @Override
    public Object getObject() {
        return number;
    }

    @Override
    public boolean isScalar() {
        return true;
    }

    @Override
    public Map<Key, ValueDataTreeElement> getTree() {
        return null;
    }

    @Override
    public List<ValueDataTreeElement> getList() {
        return null;
    }

    @Override
    public void writeAsJSONValue(JsonGenerator generator) throws JsonException {
        if(type.equals(Types.TYPE_DECIMAL))
            generator.write((BigDecimal) number);
        else if(type.equals(Types.TYPE_ULONG))
            generator.write((BigInteger) number);
        else if(Types.isInteger(type))
            generator.write(number.longValue());
        else
            generator.write( number.doubleValue());
    }

    @Override
    public void writeAsJSONPair(JsonGenerator generator, Key name) throws JsonException {
        if(type.equals(Types.TYPE_DECIMAL))
            generator.write(name.origString(), (BigDecimal) number);
        else if(type.equals(Types.TYPE_ULONG))
            generator.write(name.origString(), (BigInteger) number);
        else if(Types.isInteger(type))
            generator.write(name.origString(), number.longValue());
        else
            generator.write(name.origString(), number.doubleValue());
    }

    @Override
    public void writeAsXML(XMLStreamWriter writer, Key name) throws XMLStreamException {
        writer.writeStartElement(name.origString());
        if(type.equals(Types.TYPE_DECIMAL))
            writer.writeCharacters(((BigDecimal) number).toPlainString());
        else
            writer.writeCharacters(this.number.toString());
        writer.writeEndElement();
    }

    @Override
    public void writeAsXML(String namespaceURI, XMLStreamWriter writer, Key name) throws XMLStreamException {
        writer.writeStartElement(namespaceURI, name.origString());
        if(type.equals(Types.TYPE_DECIMAL))
            writer.writeCharacters(((BigDecimal) number).toPlainString());
        else
            writer.writeCharacters(this.number.toString());
        writer.writeEndElement();
    }
}

class SubTreeElement    implements ValueDataTreeElement {
    private final Map<Key, ValueDataTreeElement> map;

    SubTreeElement(Map<Key, ValueDataTreeElement> map) {
        this.map = map;
    }

    /**
     * no type can be store, this is sub-tree
     *
     * @return
     */
    @Override
    public Types getType() {
        return null;
    }

    @Override
    public String toString() {
        return "{"+
                map.entrySet().stream()
                        .map(e -> e.getKey()+"="+e.getValue().toString())
                        .collect(Collectors.joining(","))
                +"}";
    }

    @Override
    public Value getValue() {
        return null;
    }

    @Override
    public Object getObject() {
        return null;
    }

    @Override
    public boolean isScalar() {
        return false;
    }

    @Override
    public Map<Key, ValueDataTreeElement> getTree() {
        return map;
    }

    @Override
    public List<ValueDataTreeElement> getList() {
        return null;
    }

    @Override
    public void writeAsJSONValue(JsonGenerator generator) throws JsonException {
        generator.writeStartObject();
        for (Map.Entry<Key, ValueDataTreeElement> e: this.map.entrySet()) {
            e.getValue().writeAsJSONPair(generator, e.getKey());
        }
        generator.writeEnd();
    }

    @Override
    public void writeAsJSONPair(JsonGenerator generator, Key name) throws JsonException {
        generator.writeStartObject(name.origString());
        for (Map.Entry<Key, ValueDataTreeElement> e: this.map.entrySet()) {
            e.getValue().writeAsJSONPair(generator, e.getKey());
        }
        generator.writeEnd();
    }

    @Override
    public void writeAsXML(XMLStreamWriter writer, Key name) throws XMLStreamException {
        writer.writeStartElement(name.origString());
        for (Map.Entry<Key, ValueDataTreeElement> e: this.map.entrySet()) {
            e.getValue().writeAsXML(writer, e.getKey());
        }
        writer.writeEndElement();
    }

    @Override
    public void writeAsXML(String namespaceURI, XMLStreamWriter writer, Key name) throws XMLStreamException {
        writer.writeStartElement(namespaceURI, name.origString());
        for (Map.Entry<Key, ValueDataTreeElement> e: this.map.entrySet()) {
            e.getValue().writeAsXML(namespaceURI, writer, e.getKey());
        }
        writer.writeEndElement();
    }
}

class SubListElement implements ValueDataTreeElement {
    private final List<ValueDataTreeElement> list;

    SubListElement(List<ValueDataTreeElement> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return "["+list.stream().map(v -> v.toString())
                .collect(Collectors.joining(","))+"]";
    }

    /**
     * no type can be store, this is sub-tree
     *
     * @return
     */
    @Override
    public Types getType() {
        return null;
    }

    @Override
    public Value getValue() {
        return null;
    }

    @Override
    public Object getObject() {
        return null;
    }

    @Override
    public boolean isScalar() {
        return false;
    }

    @Override
    public Map<Key, ValueDataTreeElement> getTree() {
        return null;
    }

    @Override
    public List<ValueDataTreeElement> getList() {
        return list;
    }

    @Override
    public void writeAsJSONValue(JsonGenerator generator) throws JsonException {
        generator.writeStartArray();
        for (ValueDataTreeElement i: this.list) {
            i.writeAsJSONValue(generator);
        }
        generator.writeEnd();
    }

    @Override
    public void writeAsJSONPair(JsonGenerator generator, Key name) throws JsonException {
        generator.writeStartArray(name.origString());
        for (ValueDataTreeElement i: this.list) {
            i.writeAsJSONValue(generator);
        }
        generator.writeEnd();
    }

    @Override
    public void writeAsXML(XMLStreamWriter writer, Key name) throws XMLStreamException {
        for (ValueDataTreeElement i: this.list) {
            i.writeAsXML(writer, name);
        }
    }

    @Override
    public void writeAsXML(String namespaceURI, XMLStreamWriter writer, Key name) throws XMLStreamException {
        for (ValueDataTreeElement i: this.list) {
            i.writeAsXML(namespaceURI, writer, name);
        }
    }
}