package esql.data;

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
     * this element is scalar (not tree neither list neither LOB)
     *
     * @return true if isScalar
     */
    boolean isScalar();

    /**
     * this value is null
     * @return
     */
    public boolean isNull();

    /**
     * detect for special character to force escape
     *
     * {, }, [, ], ,, &, :, *, #, ?, |. -, <. >, =, !, %, @, \.
     *
     * @return true it has at least one
     */
    public boolean isNeedEscape();

    /**
     * need quoted, for example empty string or space inside
     *
     * @return
     */
    public boolean isNeedQuote();
    /**
     * this value is multi lines printing (string with line break)
     * @return true if multi lines
     */
    public boolean isMultiLines();

    /**
     * check for last char is new line character (for YAML)
     *
     * @return
     */
    public boolean isNewLineAtLast();
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

    /**
     * get Quoted "" string with escape character
     * @return
     */
    public CharSequence getQuotedString();

    /**
     * get series (iterator) of CharSequence. Only capable for scalar or LOB.
     *
     * each char sequence is a text line or break lines if the text's length is over the maxLength.
     * if maxLength is -1, there no line breaking by length.
     *
     * Special characters may return (no escape yet).
     *
     * If value is null, the method return empty iterator.
     * Otherwise this method is always return at least one element.
     *
     * @param maxLength the maximum length of a line (-1 if unlimited)
     * @return iterator
     * @throws IllegalStateException when can not iterate the value.
     */
    public Iterable<CharSequence> getLineIterator(int maxLength);


}
