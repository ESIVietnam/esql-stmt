package esql.data;

import jakarta.json.JsonException;
import jakarta.json.stream.JsonGenerator;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * The Data Tree is structure Value object, it's abstraction of 3 implementations of a map-tree, JSON, XML data type.
 * They can be convert to both JSON/XML easily.
 * But it may not be reversal convert from JSON or XML.
 *
 * The data tree is always "key => value" style at root of the tree, the array root style like JSON is not supported. XML does not
 * have array style at root, either. So the data tree is an implementation of Map&lt;String, DataTreeElement&gt; in Java too.
 * The DataTreeElement is element to build a data tree, it is easy to build from Java primitive type and let programmer
 * to build the tree by creating each elements.
 *
 * The data tree structure is immutable, so there is a tree builder (as map) to build it, or, simply create it by parsing
 * from other structures like JSON or XML.
 */
public abstract class ValueDataTree extends Value implements Map<Key, ValueDataTreeElement> {

    public static final int DATA_TREE_MAXIMUM_LEVELS = 128;
    public static final ValueNULL NULL_XML = new ValueNULL(Types.TYPE_XML);
    public static final ValueNULL NULL_JSON = new ValueNULL(Types.TYPE_JSON);

    public static final int detectSpecialChars(CharSequence cs) {
        //"{, }, [, ], ,, &, :, *, #, ?, |. -, <. >, =, !, %, @, \."
        for(int i = 0; i < cs.length(); i++) {
            switch (cs.charAt(i)) {
                case '{':
                case '}':
                case '[':
                case ']':
                case ',':
                case '&':
                case ':':
                case '*':
                case '#':
                case '?':
                case '|':
                case '-':
                case '<':
                case '>':
                case '=':
                case '!':
                case '%':
                case '@':
                    return i;
            }
        }
        return -1;
    }

    public abstract static class TreeBuilder implements Iterable<Key> {
        //protected tree builder
        protected TreeBuilder() {

        }

        public abstract ValueDataTreeElement nullElement();
        public abstract ValueDataTreeElement valueOf(boolean val);
        public abstract ValueDataTreeElement valueOf(long val);
        public abstract ValueDataTreeElement valueOf(double val);
        public abstract ValueDataTreeElement valueOf(BigDecimal val);
        public abstract ValueDataTreeElement valueOf(BigInteger val);
        public abstract ValueDataTreeElement valueOf(String val);
        public abstract ValueDataTreeElement valueOf(byte[] val);

        public abstract int size();

        public abstract boolean isEmpty();
        /**
         * put a null value
         *
         * @param key
         * @return this
         */
        public abstract TreeBuilder putNull(Key key);

        /**
         * put an element, this better than map.put because it returns "this"
         *
         * @param key
         * @param element
         * @return this
         */
        public abstract TreeBuilder putElement(Key key, ValueDataTreeElement element);

        /**
         * short hand call of putElement + valueOf
         * @param key
         * @param val
         * @return
         */
        public abstract TreeBuilder putElement(Key key, boolean val);
        /**
         * short hand call of putElement + valueOf
         * @param key
         * @param val
         * @return
         */
        public abstract TreeBuilder putElement(Key key, long val);
        /**
         * short hand call of putElement + valueOf
         * @param key
         * @param val
         * @return
         */
        public abstract TreeBuilder putElement(Key key, double val);
        /**
         * short hand call of putElement + valueOf
         * @param key
         * @param val
         * @return
         */
        public abstract TreeBuilder putElement(Key key, BigDecimal val);
        /**
         * short hand call of putElement + valueOf
         * @param key
         * @param val
         * @return
         */
        public abstract TreeBuilder putElement(Key key, BigInteger val);
        /**
         * short hand call of putElement + valueOf
         * @param key
         * @param val
         * @return
         */
        public abstract TreeBuilder putElement(Key key, String val);
        /**
         * short hand call of putElement + valueOf
         * @param key
         * @param val
         * @return
         */
        public abstract TreeBuilder putElement(Key key, byte[] val);

        /**
         * put all entry of map
         *
         * @param map
         * @return
         */
        public abstract TreeBuilder putAll(Map<Key, ? extends ValueDataTreeElement> map);

        /**
         * put a new tree builder with the key inside this builder.
         *
         * @param key
         * @return new TreeBuilder to manipulate as sub-tree
         */
        public abstract TreeBuilder createTree(Key key);

        /**
         * put a sub-tree builder
         *
         * @param key
         * @param subTreeBuilder
         * @return this
         */
        public abstract TreeBuilder putSubTree(Key key, TreeBuilder subTreeBuilder);

        /**
         * put a new list builder with the key inside this builder.
         *
         * @param key
         * @return new ListBuilder to manipulate as sub-tree
         */
        public abstract ListBuilder createList(Key key);

        /**
         * put a sub list
         * @param key
         * @return this
         */
        public abstract TreeBuilder putList(Key key, ListBuilder listBuilder);

        /**
         * build complete Value
         *
         * @return new value
         */
        public abstract ValueDataTree build();

        /**
         * build a tree but it is element of another tree/list
         *
         * @return new tree element
         */
        public abstract ValueDataTreeElement buildTreeElement();
    }

    /**
     * This collection class is the builder for child lists in DataTree.
     * Despite it's name, it is a collection.
     */
    public abstract static class ListBuilder implements Iterable<ValueDataTreeElement> {

        /**
         * add all element of another collection
         * @param elements to add
         * @return
         */
        public abstract ListBuilder addAll(Collection<ValueDataTreeElement> elements);

        /**
         * add one or more elements.
         *
         * @param element or multiple element to add
         * @return
         */
        public abstract ListBuilder addElement(ValueDataTreeElement... element);

        /**
         * add list builders, that create element when building at the whole.
         *
         * @param builders to add
         * @return
         */
        public abstract ListBuilder addElement(ListBuilder... builders);

        /**
         * add tree builders, that create element when building at the whole
         * @param builders to add
         * @return
         */
        public abstract ListBuilder addElement(TreeBuilder... builders);

        /**
         * create a list builder then adding to the end of list.
         * @return
         */
        public abstract ListBuilder createListBuilder();

        /**
         * create a tree builder then adding to the end of list.
         * @return
         */
        public abstract TreeBuilder createTreeBuilder();

        /**
         * Build the tree element (to add as one element of the parent data tree)
         * @return new DataTree (of List type)
         */
        public abstract ValueDataTreeElement buildListElement();
    }

    public static final ValueDataTree nullOfDataTree() {
        return ValueDataTreeImpl.NULL_DATA_TREE;
    }

    public static final TreeBuilder builderOfDataTree() {
        return new ValueDataTreeImpl.TreeBuilderImpl();
    }

    /**
     * checking if it could write as YAML tree
     * @return true if it can
     */
    public abstract boolean isYAMLWritable();

    /**
     * writing the DataTree as YAML (if applicable).
     * JSON and XML will be converted into YAML.
     *
     * @param output the output stream to write to (UTF-8)
     * @throws IOException
     */
    public abstract void writeAsYAML(OutputStream output) throws IOException;

    /**
     * writing the DataTree as YAML (if applicable).
     * JSON and XML will be converted into YAML.
     *
     * @param writer the writer to write to (as characters)
     * @throws IOException
     */
    public abstract void writeAsYAML(Writer writer) throws IOException;

    /**
     * check if it can write to JSON.
     * @return true if JSON writable
     */
    public abstract boolean isJSONWritable();
    public abstract void writeAsJSON(OutputStream output) throws IOException;
    public abstract void writeAsJSON(Writer writer) throws IOException;
    public abstract void writeAsJSON(JsonGenerator generator) throws JsonException;

    public abstract boolean isXMLWritable();
    public abstract void writeAsXML(String namespaceURI, String rootElementName, OutputStream output) throws IOException;
    public void writeAsXML(String rootElementName, OutputStream output) throws IOException {
        writeAsXML(null, rootElementName, output);
    }
    public abstract void writeAsXML(String namespaceURI, String rootElementName, Writer writer) throws IOException;
    public void writeAsXML(String rootElementName, Writer writer) throws IOException {
        writeAsXML(null, rootElementName, writer);
    }

    public abstract void writeAsXML(String namespaceURI, String rootElementName, XMLStreamWriter writer) throws XMLStreamException;
    public void writeAsXML(String rootElementName, XMLStreamWriter writer) throws XMLStreamException {
        writeAsXML(null, rootElementName, writer);
    }
}
