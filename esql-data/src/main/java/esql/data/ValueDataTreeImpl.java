package esql.data;

import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.stream.JsonGenerator;
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


    static final String lineSeparator = System.getProperty("esql.data.tree.line.break", System.lineSeparator());

    private static char[][] yamlIndentChars;
    private static char[] yamlArrayItemBegin;
    private static char[] yamlArrayItemContinous;
    static final int TREE_CURR_IS_KEY = 1;
    static final int TREE_CURR_IS_LIST = 2;

    static final String YAML_NULL_VALUE = System.getProperty("esql.data.tree.yaml.null", "~");

    static {
        String yamlIndent = System.getProperty("esql.data.tree.yaml.indentation.size", "2");
        String yamlItemSpaces = System.getProperty("esql.data.tree.yaml.array.item.spaces", "1");
        int indent = Integer.parseInt(yamlIndent);
        int itemSpaces = Integer.parseInt(yamlItemSpaces);
        if(indent <= 0)
            throw new IllegalArgumentException("esql.data.tree.yaml.indentation.size must be larger than zero");
        if(itemSpaces <= 0)
            throw new IllegalArgumentException("esql.data.tree.yaml.array.item.spaces must be larger than zero");
        yamlIndentChars = new char[DATA_TREE_MAXIMUM_LEVELS][];//maximum levels of indentation
        for(int i=0;i<DATA_TREE_MAXIMUM_LEVELS;i++) {
            yamlIndentChars[i] = new char[indent];
            Arrays.fill(yamlIndentChars[i], ' '); //space
        }
        yamlArrayItemBegin = new char[itemSpaces+1];
        yamlArrayItemContinous = new char[itemSpaces+1];
        Arrays.fill(yamlArrayItemBegin,' ');
        yamlArrayItemBegin[yamlArrayItemBegin.length-1] = '-'; //last char for array marker
        Arrays.fill(yamlArrayItemContinous, ' ');
    }

    private final Map<Key, ValueDataTreeElement> map;
    ValueDataTreeImpl(Map<Key, ValueDataTreeElement> map) {
        this.map = map;
    }

    @Override
    public boolean isYAMLWritable() {
        return true;
    }

    @Override
    public void writeAsYAML(OutputStream output) throws IOException {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8), true)) {
            writeAsYAML(writer);
        }
    }

    @Override
    public void writeAsYAML(Writer writer) throws IOException {
        //start document
        writer.write("---");
        writer.write(lineSeparator);
        if(this.map == null)
            return;

        int[] stack = new int[DATA_TREE_MAXIMUM_LEVELS+1];
        //int curr = 0; //curr level at beginning
        //stack[0] = -1;//document level
        stack[0] = TREE_CURR_IS_KEY; //document level
        int i = 0;
        for (Entry<Key, ValueDataTreeElement> e: this.map.entrySet()) {
            Key k = e.getKey();
            writer.append(k.isSpaceInside() ? k.quotedIdentifier() : k.identifier()).append(':');
            boolean next;
            if(e.getValue().isScalar()) {
                next = writeScalarElementAsYaml(-1, 0, e.getValue(), writer);
            }
            else { //tree or list element
                next = true;
                List<ValueDataTreeElement> list = e.getValue().getList();
                if(list != null) {
                    next = writeElementAsYaml(stack, 1, true, list, writer);
                }
                Map<Key, ValueDataTreeElement> tree = e.getValue().getTree();
                if(tree != null) {
                    next = writeElementAsYaml(stack, 1, true, tree, writer);
                }
            }
            //writing new line if not writen yet
            if(next)
                writer.write(lineSeparator);
            i++;
        }
    }

    static boolean writeScalarElementAsYaml(int lastLevel, int curr, ValueDataTreeElement element, Writer writer) throws IOException {
        if(!element.isScalar())
            throw new IllegalArgumentException("Not a scalar");

        if(element.isNull()) {
            writer.append(" ~");
            return true;
        }
        else if(element.isMultiLines() && !element.isNeedEscape()) {
            StringBuffer buff = new StringBuffer(128);
            boolean lastIsNewLine = element.isNewLineAtLast();
            //int l = 0;
            for (CharSequence cs : element.getLineIterator(-1)) {
                if(curr > 0) {
                    //last level is list
                    if (lastLevel == TREE_CURR_IS_LIST) {
                        //array prefix is already imply a level
                        if(curr > 1)
                            buff.append(yamlIndentChars[curr - 2]);
                        //continuous of last level
                        buff.append(yamlArrayItemContinous);
                    }
                    else {
                        buff.append(yamlIndentChars[curr - 1]);
                    }
                }
                buff.append(" ").append(cs).append(lineSeparator);
                //l++;
            }
            //YAML multi lines
            if(lastIsNewLine)
                writer.append(" |");
            else
                writer.append(" |-");
            writer.append(lineSeparator).append(buff);
        }
        else if(element.isNeedQuote()) {
            //YAML quoted
            writer.append(" ").append(element.getQuotedString());
            return true;
        }
        else {
            //literal string
            StringBuffer buff = new StringBuffer(128);
            int l = 0;
            for (CharSequence cs : element.getLineIterator(72)) {
                if(l > 0 && curr > 0) {
                    //last level is list
                    if (lastLevel == TREE_CURR_IS_LIST) {
                        //array prefix is already imply a level
                        if(curr > 1)
                            buff.append(yamlIndentChars[curr - 2]);
                        //continuous of last level
                        buff.append(yamlArrayItemContinous);
                    }
                    else {
                        buff.append(yamlIndentChars[curr - 1]);
                    }
                }
                buff.append(" ")
                        .append(cs).append(lineSeparator);
                l++;
            }
            //YAML long line including separator
            if(l > 1)
                writer.append(" >-").append(lineSeparator).append(buff);
            else
                writer.append(buff);
        }
        return false;
    }
    /**
     * write the element of tree or list
     * @param stack
     * @param curr
     * @param list
     * @param writer
     * @return the state of last writing, needing a new line if string quote, literal
     * @throws IOException
     */
    static boolean writeElementAsYaml(int[] stack, int curr, boolean notIndent, List<ValueDataTreeElement> list, Writer writer) throws IOException {
        stack[curr] = TREE_CURR_IS_LIST;
        if (list.isEmpty()) {
            writer.write(" []");
            return true;
        }

        //new line if behind a key and notIndent
        if (notIndent) {
            writer.append(lineSeparator);
        }

        int i = 0;
        for (ValueDataTreeElement item : list) {
            //array prefix is already imply a level
            if (curr > 1 && (i > 0 || i == 0 && notIndent)) {
                //last level is list
                if (stack[curr - 1] == TREE_CURR_IS_LIST) {
                    writer.write(yamlIndentChars[curr - 2]);
                    //continuous of last level
                    writer.write(yamlArrayItemContinous);
                } else {
                    writer.write(yamlIndentChars[curr - 1]);
                }
            }

            writer.write(yamlArrayItemBegin);
            if (item.isScalar()) {
                if(writeScalarElementAsYaml(TREE_CURR_IS_LIST, curr, item, writer))
                    writer.append(lineSeparator);
            } else { //recursive
                List<ValueDataTreeElement> subList = item.getList();
                if(subList != null) {
                    if(writeElementAsYaml(stack, curr + 1, false, subList, writer))
                        writer.append(lineSeparator);
                }
                Map<Key, ValueDataTreeElement> subTree = item.getTree();
                if(subTree != null) {
                    if(writeElementAsYaml(stack, curr + 1, false, subTree, writer))
                        writer.append(lineSeparator);
                }
            }
            i++;
        }
        stack[curr] = 0; //go up
        return false;
    }

    static boolean writeElementAsYaml(int[] stack, int curr, boolean notIndent, Map<Key, ValueDataTreeElement> tree, Writer writer) throws IOException {
        stack[curr] = TREE_CURR_IS_KEY;
        if(tree.isEmpty()) {
            writer.write(" {}");
            return true;
        }

        //new line if behind a key and notIndent
        if (notIndent) {
            writer.append(lineSeparator);
        }
        int i = 0;
        for(Entry<Key, ValueDataTreeElement> e: tree.entrySet()) {
            //writing for current level
            if (curr > 0) {
                if(i > 0 || notIndent && i == 0) {
                    //last level is list
                    if (stack[curr - 1] == TREE_CURR_IS_LIST) {
                        //array prefix is already imply a level
                        if (curr > 1)
                            writer.write(yamlIndentChars[curr - 2]);
                        //continuous of last level
                        writer.write(yamlArrayItemContinous);
                    } else {
                        writer.write(yamlIndentChars[curr - 1]);
                    }
                }
                if (stack[curr-1] == TREE_CURR_IS_LIST) {
                    writer.append(" ");
                }
            }

            Key k = e.getKey();
            writer.append(k.isSpaceInside() ? k.quotedIdentifier() : k.identifier()).append(':');

            if(e.getValue().isScalar()) {
                if (writeScalarElementAsYaml(TREE_CURR_IS_KEY, curr, e.getValue(), writer))
                    writer.write(lineSeparator);
            }
            else {
                List<ValueDataTreeElement> subList = e.getValue().getList();
                if(subList != null) {
                    if(writeElementAsYaml(stack, curr + 1, true, subList, writer))
                        writer.append(lineSeparator);
                }
                Map<Key, ValueDataTreeElement> subTree = e.getValue().getTree();
                if(subTree != null) {
                    if(writeElementAsYaml(stack, curr + 1, true, subTree, writer))
                        writer.append(lineSeparator);
                }
            }
            i++;
        }
        return false;
    }

    @Override
    public boolean isJSONWritable() {
        return true;
    }

    @Override
    public void writeAsJSON(OutputStream output) throws IOException {
        if(this.map == null)
            return;
        //dung writer de write
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8), true)) {
            writeAsJSON(writer);
        }
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
            writeElementAsJSON(e.getKey(), e.getValue(), generator);
        }
        generator.writeEnd();
    }

    static void writeElementAsJSON(Key key, ValueDataTreeElement element, JsonGenerator generator) throws JsonException {
        Object obj = element.getObject();
        if(obj == null) {
            generator.writeNull(key.origString());
            return;
        }

        if(element.isScalar()) {
            switch (element.getType()) {
                case TYPE_BOOLEAN:
                    generator.write(key.origString(), (Boolean) element.getObject());
                    break;
                case TYPE_STRING:
                    generator.write(key.origString(), (String) element.getObject());
                    break;
                case TYPE_DECIMAL:
                    generator.write(key.origString(), (BigDecimal) element.getObject());
                    break;
                case TYPE_ULONG:
                    generator.write(key.origString(), (BigInteger) element.getObject());
                    break;
                case TYPE_LONG:
                    generator.write(key.origString(), ((Number) element.getObject()).longValue());
                    break;
                default:
                    assert (Types.isNumber(element.getType())) : "It is not number type "+element.getType();
                    if(Types.isInteger(element.getType()))
                        generator.write(key.origString(), ((Number) element.getObject()).intValue());
                    else
                        generator.write(key.origString(), ((Number) element.getObject()).doubleValue());
                    break;
            }
            return;
        }

        Map<Key, ValueDataTreeElement> subMap = element.getTree();
        if(subMap != null) {
            generator.writeStartObject(key.origString());
            for (Entry<Key, ValueDataTreeElement> e: subMap.entrySet()) {
                writeElementAsJSON(e.getKey(), e.getValue(), generator);
            }
            generator.writeEnd();
            return;
        }

        List<ValueDataTreeElement> subList = element.getList();
        if(subList != null) {
            generator.writeStartArray(key.origString());
            for(ValueDataTreeElement i : subList) {
                writeElementAsJSON(i, generator);
            }
            generator.writeEnd();
            return;
        }
    }

    /**
     * write element in array / root context.
     *
     * @param element
     * @param generator
     * @throws JsonException
     */
     static void writeElementAsJSON(ValueDataTreeElement element, JsonGenerator generator) throws JsonException {
        Object obj = element.getObject();
        if(obj == null) {
            generator.writeNull();
            return;
        }

        if(element.isScalar()) {
            switch (element.getType()) {
                case TYPE_BOOLEAN:
                    generator.write((Boolean) element.getObject());
                    break;
                case TYPE_STRING:
                    generator.write((String) element.getObject());
                    break;
                case TYPE_DECIMAL:
                    generator.write((BigDecimal) element.getObject());
                    break;
                case TYPE_ULONG:
                    generator.write((BigInteger) element.getObject());
                    break;
                case TYPE_LONG:
                    generator.write(((Number) element.getObject()).longValue());
                    break;
                default:
                    assert (Types.isNumber(element.getType())) : "It is not number type "+element.getType();
                    if(Types.isInteger(element.getType()))
                        generator.write(((Number) element.getObject()).intValue());
                    else
                        generator.write(((Number) element.getObject()).doubleValue());
                    break;
            }
            return;
        }

        Map<Key, ValueDataTreeElement> subMap = element.getTree();
        if(subMap != null) {
            generator.writeStartObject();
            for (Entry<Key, ValueDataTreeElement> e: subMap.entrySet()) {
                writeElementAsJSON(e.getKey(), e.getValue(), generator);
            }
            generator.writeEnd();
            return;
        }

        List<ValueDataTreeElement> subList = element.getList();
        if(subList != null) {
            generator.writeStartArray();
            for(ValueDataTreeElement i : subList) {
                writeElementAsJSON(i, generator);
            }
            generator.writeEnd();
            return;
        }
    }

    @Override
    public boolean isXMLWritable() {
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

        writer.writeStartElement(rootElementName);
        if(namespaceURI != null && !namespaceURI.isBlank()) {
            writer.writeDefaultNamespace(namespaceURI);
        }
        else //force null on empty
            namespaceURI = null;
        for (Entry<Key, ValueDataTreeElement> e: this.map.entrySet()) {
            writeElementAsXML(namespaceURI, e.getKey(), e.getValue(), writer);
        }
        writer.writeEndElement();
    }

    /**
     * recursive interal write
     * @param namespaceURI
     * @param tagName
     * @param element
     * @param writer
     * @throws XMLStreamException
     */
     static void writeElementAsXML(String namespaceURI, Key tagName, ValueDataTreeElement element, XMLStreamWriter writer) throws XMLStreamException {
        if(element.isScalar()) {
            if(element.isNull()) {
                if(namespaceURI != null)
                    writer.writeEmptyElement(namespaceURI, tagName.origString());
                else
                    writer.writeEmptyElement(tagName.origString());
                return;
            }
            if(namespaceURI != null)
                writer.writeStartElement(namespaceURI, tagName.origString());
            else
                writer.writeStartElement(tagName.origString());
            if(element.isNeedEscape() || element.isMultiLines())
                writer.writeCData(element.getObject().toString());
            else { //it is a number or string
                writer.writeCharacters(element.getObject().toString());
            }
            writer.writeEndElement();
            return;
        }

        //loop each key in map
        Map<Key, ValueDataTreeElement> subMap = element.getTree();
        if(subMap != null) {
            if (namespaceURI != null)
                writer.writeStartElement(namespaceURI, tagName.origString());
            else
                writer.writeStartElement(tagName.origString());
            for (Entry<Key, ValueDataTreeElement> e : subMap.entrySet()) {
                writeElementAsXML(namespaceURI, e.getKey(), e.getValue(), writer);
            }
            writer.writeEndElement();
            return;
        }

        //loop each item in list.
        List<ValueDataTreeElement> subList = element.getList();
        if(subList != null) {
            for (ValueDataTreeElement i: subList) {
                writeElementAsXML(namespaceURI, tagName, i, writer);
            }
        }
    }

    private static class TripleOfTreeBuilder {
        final ValueDataTreeElement value;
        final ListBuilder listBuilder;
        final TreeBuilder treeBuilder;

        private TripleOfTreeBuilder(ValueDataTreeElement value, ListBuilder listBuilder, TreeBuilder treeBuilder) {
            this.value = value;
            this.listBuilder = listBuilder;
            this.treeBuilder = treeBuilder;
        }

        TripleOfTreeBuilder(ValueDataTreeElement value) {
            this(value, null, null);
        }

        TripleOfTreeBuilder(ListBuilder listBuilder) {
            this(null, listBuilder, null);
        }

        TripleOfTreeBuilder(TreeBuilder treeBuilder) {
            this(null, null, treeBuilder);
        }

        boolean isValueStored() {
            return this.value != null;
        }

        ValueDataTreeElement buildElement() {
            if(this.value != null)
                return value;
            if(this.listBuilder != null)
                return listBuilder.buildListElement();
            return treeBuilder.buildTreeElement();
        }
    }

    static class TreeBuilderImpl extends TreeBuilder {

        final LinkedHashMap<Key, Integer> keys;
        final ArrayList<TripleOfTreeBuilder> builtValues;
        TreeBuilderImpl() {
            this.keys = new LinkedHashMap<>();
            this.builtValues = new ArrayList<>(8);
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
            Integer n = this.builtValues.size();
            Integer i = this.keys.put(key, n);
            if(i == null)
                this.builtValues.add(new TripleOfTreeBuilder(NULL_ELEMENT));
            else
                this.builtValues.set(i, new TripleOfTreeBuilder(NULL_ELEMENT));
            return this;
        }

        @Override
        public TreeBuilder putElement(Key key, ValueDataTreeElement element) {
            Integer n = this.builtValues.size();
            Integer i = this.keys.put(key, n);
            if(i == null)
                this.builtValues.add(new TripleOfTreeBuilder(element));
            else
                this.builtValues.set(i, new TripleOfTreeBuilder(element));
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
            Integer n = this.builtValues.size();
            Integer i = this.keys.put(key, n);
            if(i == null)
                this.builtValues.add(new TripleOfTreeBuilder(valueOf(val)));
            else
                this.builtValues.set(i, new TripleOfTreeBuilder(valueOf(val)));
            return this;
        }

        @Override
        public TreeBuilder putElement(Key key, BigInteger val) {
            if(val == null)
                return putNull(key);
            Integer n = this.builtValues.size();
            Integer i = this.keys.put(key, n);
            if(i == null)
                this.builtValues.add(new TripleOfTreeBuilder(valueOf(val)));
            else
                this.builtValues.set(i, new TripleOfTreeBuilder(valueOf(val)));
            return this;
        }

        @Override
        public TreeBuilder putElement(Key key, String val) {
            if(val == null)
                return putNull(key);
            Integer n = this.builtValues.size();
            Integer i = this.keys.put(key, n);
            if(i == null)
                this.builtValues.add( new TripleOfTreeBuilder(valueOf(val)));
            else
                this.builtValues.set(i, new TripleOfTreeBuilder(valueOf(val)));
            return this;
        }

        @Override
        public TreeBuilder putElement(Key key, byte[] val) {
            if(val == null)
                return putNull(key);
            Integer n = this.builtValues.size();
            Integer i = this.keys.put(key, n);
            if(i == null) { //append new item
                this.builtValues.add(new TripleOfTreeBuilder(valueOf(val)));
            }
            else
                this.builtValues.set(i, new TripleOfTreeBuilder(valueOf(val)));
            return this;
        }

        @Override
        public TreeBuilder putSubTree(Key key, TreeBuilder treeBuilder) {
            Integer n = this.builtValues.size();
            Integer i = this.keys.put(key, n);
            if(i == null) { //append new item
                builtValues.add(new TripleOfTreeBuilder(treeBuilder));
            }
            else //overwrite old value
                builtValues.set(i, new TripleOfTreeBuilder(treeBuilder));
            return this;
        }

        @Override
        public TreeBuilder createTree(Key key) {
            var treeBuilder = new TreeBuilderImpl();
            Integer n = this.builtValues.size();
            Integer i = this.keys.put(key, n);
            if(i == null) { //append new item
                builtValues.add(new TripleOfTreeBuilder(treeBuilder));
            }
            else //overwrite old value
                builtValues.set(i, new TripleOfTreeBuilder(treeBuilder));
            return treeBuilder;
        }

        @Override
        public ListBuilder createList(Key key) {
            var listBuilder = new ListBuilderImpl();
            Integer n = this.builtValues.size();
            Integer i = this.keys.put(key, n);
            if(i == null) { //append new item
                builtValues.add(new TripleOfTreeBuilder(listBuilder));
            }
            else //overwrite old value
                builtValues.set(i, new TripleOfTreeBuilder(listBuilder));
            return listBuilder;
        }

        @Override
        public TreeBuilder putList(Key key, ListBuilder listBuilder) {
            Integer n = this.builtValues.size();
            Integer i = this.keys.put(key, n);
            if(i == null) { //append new item
                builtValues.add(new TripleOfTreeBuilder(listBuilder));
            }
            else //overwrite old value
                builtValues.set(i, new TripleOfTreeBuilder(listBuilder));
            return this;
        }

        @Override
        public ValueDataTree build() {
            var map = populateSubTreeOrList();
            return new ValueDataTreeImpl(Collections.unmodifiableMap(map));
        }

        private Map<Key, ValueDataTreeElement> populateSubTreeOrList() {
            Map<Key, ValueDataTreeElement> map = new LinkedHashMap<>(builtValues.size());
            for(var e: keys.entrySet()) {
                map.put(e.getKey(),
                        builtValues.get(e.getValue()).buildElement());
            }
            return map;
        }

        @Override
        public ValueDataTreeElement buildTreeElement() {
            var map = populateSubTreeOrList();
            return new SubTreeElement(Collections.unmodifiableMap(map));
        }

        @Override
        public int size() {
            return builtValues.size();
        }

        @Override
        public boolean isEmpty() {
            return builtValues.isEmpty();
        }

        @Override
        public Iterator<Key> iterator() {
            return keys.keySet().iterator();
        }

        @Override
        public TreeBuilder putAll(Map<Key, ? extends ValueDataTreeElement> m) {
            for(var e: m.entrySet()) {
                putElement(e.getKey(), e.getValue());
            }
            return this;
        }

        @Override
        public Spliterator<Key> spliterator() {
            return keys.keySet().spliterator();
        }
    }

    static class ListBuilderImpl extends ListBuilder {
        private final ArrayList<TripleOfTreeBuilder> list;

        ListBuilderImpl() {
            this.list = new ArrayList<>(4);
        }

        @Override
        public ListBuilder addAll(Collection<ValueDataTreeElement> elements) {
            for(var e: elements)
                list.add(new TripleOfTreeBuilder(e));
            return this;
        }

        @Override
        public ListBuilder addElement(ValueDataTreeElement... element) {
            for(var e: element)
                list.add(new TripleOfTreeBuilder(e));
            return this;
        }

        @Override
        public ListBuilder addElement(ListBuilder... builders) {
            for(var e: builders)
                list.add(new TripleOfTreeBuilder(e));
            return this;
        }

        @Override
        public ListBuilder addElement(TreeBuilder... builders) {
            for(var e: builders)
                list.add(new TripleOfTreeBuilder(e));
            return this;
        }

        @Override
        public TreeBuilder createTreeBuilder() {
            var treeBuilder = new TreeBuilderImpl();
            this.addElement(treeBuilder);
            return treeBuilder;
        }

        @Override
        public ListBuilder createListBuilder() {
            var listBuilder = new ListBuilderImpl();
            this.addElement(listBuilder);
            return listBuilder;
        }

        @Override
        public ValueDataTreeElement buildListElement() {
            List<ValueDataTreeElement> list = this.list.stream().map(e -> e.buildElement()).collect(Collectors.toUnmodifiableList());
            return new SubListElement(list);
        }

        @Override
        public Iterator<ValueDataTreeElement> iterator() {
            return list.stream().map(e -> e.buildElement()).iterator();
        }

        @Override
        public Spliterator<ValueDataTreeElement> spliterator() {
            return list.stream().map(e -> e.buildElement()).spliterator();
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
        return map.containsKey(Key.of(key.toString()));
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
        return map.get(Key.of(key.toString()));
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

/****************** element implementation **********************/

/**
 * Boolean value implementation
 */
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
    public boolean isNull() {
        return bool == null;
    }

    @Override
    public boolean isNeedEscape() {
        return false;
    }

    @Override
    public boolean isNeedQuote() {
        return false;
    }

    @Override
    public boolean isMultiLines() {
        return false;
    }

    @Override
    public boolean isNewLineAtLast() {
        return false;
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
    public CharSequence getQuotedString() {
        if(bool == null)
            return '"'+Value.ValueNULL.STRING_OF_NULL+'"';
        return '"'+(bool ? ValueBoolean.STR_TRUE : ValueBoolean.STR_FALSE)+'"';
    }

    @Override
    public Iterable<CharSequence> getLineIterator(int maxLength) {
        if(bool == null)
            return Collections.emptyList();
        return Collections.singletonList(bool ? ValueBoolean.STR_TRUE : ValueBoolean.STR_FALSE);
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
    public boolean isNull() {
        return true;
    }

    @Override
    public boolean isNeedEscape() {
        return false;
    }

    @Override
    public boolean isNeedQuote() {
        return false;
    }

    @Override
    public boolean isMultiLines() {
        return false;
    }

    @Override
    public boolean isNewLineAtLast() {
        return false;
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
    public CharSequence getQuotedString() {
        return '"'+Value.ValueNULL.STRING_OF_NULL+'"';
    }

    @Override
    public Iterable<CharSequence> getLineIterator(int maxLength) {
        return Collections.emptyList();
    }

}

/**
 * Number (Integer, Decimal, Double...) implementation
 */
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
    public boolean isNull() {
        //number is never null
        return false;
    }

    @Override
    public boolean isNeedEscape() {
        return false;
    }

    @Override
    public boolean isNeedQuote() {
        return false;
    }

    @Override
    public boolean isMultiLines() {
        return false;
    }

    @Override
    public boolean isNewLineAtLast() {
        return false;
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
    public CharSequence getQuotedString() {
        return '"'+number.toString()+'"';
    }

    @Override
    public Iterable<CharSequence> getLineIterator(int maxLength) {
        String buff;
        if(type.equals(Types.TYPE_DECIMAL))
            buff = ((BigDecimal) number).toPlainString();
        else
            buff = this.number.toString();
        return Collections.singletonList(buff);
    }
}

/**
 * String value implementation
 */
class StringElement implements ValueDataTreeElement {
    private final String value;
    private final boolean safe;

    StringElement(String value) {
        this.value = value;
        if(value == null || value.isBlank())
            this.safe = true;
        else {
            this.safe = ValueDataTree.detectSpecialChars(this.value) < 0;
        }
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
    public boolean isNull() {
        return this.value == null;
    }

    @Override
    public boolean isNeedQuote() {
        if(this.value == null)
            return false;

        if(this.value.isEmpty() || this.value.contains(" ") || this.value.contains("\t"))
            return true;
        return isNeedEscape();
    }

    @Override
    public boolean isNeedEscape() {
        return !this.safe;
    }

    @Override
    public boolean isMultiLines() {
        if(this.value == null)
            return false;
        return this.value.contains(ValueDataTreeImpl.lineSeparator);
    }

    @Override
    public boolean isNewLineAtLast() {
        if(this.value == null)
            return false;
        return this.value.endsWith(ValueDataTreeImpl.lineSeparator);
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
    public CharSequence getQuotedString() {
        if(this.value == null)
            return '"'+Value.ValueNULL.STRING_OF_NULL+'"';
        return toString();
    }

    @Override
    public Iterable<CharSequence> getLineIterator(int lineLength) {
        if(this.value == null)
            return Collections.emptyList();
        int ni = this.value.indexOf(ValueDataTreeImpl.lineSeparator);
        if(ni < 0 && (lineLength < 0 || this.value.length() <= lineLength)) {
            return Collections.singletonList(this.value);
        }
        if(lineLength < 0)
            lineLength = this.value.length();
        ArrayList<CharSequence> list = new ArrayList<>(this.value.length() / lineLength + 1);
        int p = 0;
        while (p < this.value.length()) {
            if(ni > 0 && ni < p + lineLength) {
                list.add(this.value.subSequence(p, ni));
                p = ni + 1;
            }
            else {
                int j = Math.min(this.value.length(), p + lineLength);
                    list.add(this.value.subSequence(p, j));
                p = j;
            }

            if(ni > 0)
                ni = this.value.indexOf(ValueDataTreeImpl.lineSeparator, p);
        }
        return list;
    }

}

/**
 * List (Array) implementation
 */
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
        return Types.TYPE_DATA_TREE;
    }

    @Override
    public Value getValue() {
        return null;
    }

    @Override
    public Object getObject() {
        return list;
    }

    @Override
    public boolean isScalar() {
        return false;
    }

    @Override
    public boolean isNull() {
        return this.list == null;
    }

    @Override
    public boolean isNeedEscape() {
        return false;
    }

    @Override
    public boolean isNeedQuote() {
        return false;
    }

    @Override
    public boolean isMultiLines() {
        return false;
    }

    @Override
    public boolean isNewLineAtLast() {
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
    public CharSequence getQuotedString() {
        throw new IllegalStateException("subList is not quoted");
    }

    @Override
    public Iterable<CharSequence> getLineIterator(int maxLength) {
        throw new IllegalStateException("subList can not lines iterate");
    }
}

/**
 * Tree (Map) implementation
 */
class SubTreeElement implements ValueDataTreeElement {
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
        return Types.TYPE_DATA_TREE;
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
        return map;
    }

    @Override
    public boolean isScalar() {
        return false;
    }

    @Override
    public boolean isNull() {
        return this.map == null;
    }

    @Override
    public boolean isNeedEscape() {
        return false;
    }

    @Override
    public boolean isNeedQuote() {
        return false;
    }


    @Override
    public boolean isMultiLines() {
        return false;
    }

    @Override
    public boolean isNewLineAtLast() {
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
    public CharSequence getQuotedString() {
        throw new IllegalStateException("subTree is not quoted");
    }

    @Override
    public Iterable<CharSequence> getLineIterator(int maxLength) {
        throw new IllegalStateException("subTree is not lines iterable.");
    }
}