package esql.data;

import jakarta.json.JsonException;
import jakarta.json.JsonStructure;
import jakarta.json.stream.JsonGenerator;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ValueDataTreeElement {

    /**
     * Return the "type" of the element, only sub-set of type can be hold or store.
     * the set of types here are compatible with JSON.
     *
     * - String (DateTime and Bytes are coded as String)
     * - Integers
     * - Float and Double
     * - Boolean
     * - Null of all types
     *
     * @return the type asValue() can produce.
     */
    public Types getType();

    /**
     * as the Value with type.
     *
     * @return
     */
    public Value getValue();

    /**
     * Element as is it (backing object). This method is for tree output.
     *
     * @return
     */
    Object getObject();

    /**
     * this element is scalar (nor tree neither list)
     *
     * @return true if isScalar
     */
    boolean isScalar();

    /**
     * Element as Map of key => child element (if capable, throw IllegalStateException)
     *
     * @return
     * @throws IllegalStateException
     */
    public Map<Key, ValueDataTreeElement> getTree();

    /**
     * Element as List of child elements (if capable, throw IllegalStateException)
     *
     * @return
     * @throws IllegalStateException
     */
    public List<ValueDataTreeElement> getList();

    public void writeAsJSONValue(JsonGenerator generator) throws JsonException;
    public void writeAsJSONPair(JsonGenerator generator, Key name) throws JsonException;

    public void writeAsXML(XMLStreamWriter writer, Key name) throws XMLStreamException;
    public void writeAsXML(String namespaceURI, XMLStreamWriter writer, Key name) throws XMLStreamException;
}
