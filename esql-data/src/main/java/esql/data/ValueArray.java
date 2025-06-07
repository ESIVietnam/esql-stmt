package esql.data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * array of value, implements internally as ArrayList<Value>
 */
public abstract class ValueArray extends Value implements List<Value> {

    public static final String ARRAY_PREFIX = Optional.ofNullable(System.getenv("ESQL_ARRAY_PREFIX")).orElse("[");
    public static final char[] ARRAY_PREFIX_CHARSEQ = ARRAY_PREFIX.toCharArray();
    public static final String ARRAY_POSTFIX = Optional.ofNullable(System.getenv("ESQL_ARRAY_POSTFIX")).orElse("]");
    public static final char[] ARRAY_POSTFIX_CHARSEQ = ARRAY_POSTFIX.toCharArray();
    public static final String ARRAY_SEPARATOR = Optional.ofNullable(System.getenv("ESQL_ARRAY_SEPARATOR")).orElse(",");
    public static final char[] ARRAY_SEPARATOR_CHARSEQ = ARRAY_SEPARATOR.toCharArray();

    public static final ValueArray NULL_STRING_ARRAY = new ValueArrayNULLEmpty(Types.TYPE_STRING, true);
    public static final ValueArray NULL_NSTRING_ARRAY = new ValueArrayNULLEmpty(Types.TYPE_NSTRING, true);
    public static final ValueArray NULL_BYTES_ARRAY = new ValueArrayNULLEmpty(Types.TYPE_BYTES, true);
    public static final ValueArray NULL_BYTE_ARRAY = new ValueArrayNULLEmpty(Types.TYPE_BYTE, true);
    public static final ValueArray NULL_SHORT_ARRAY = new ValueArrayNULLEmpty(Types.TYPE_SHORT, true);
    public static final ValueArray NULL_INT_ARRAY = new ValueArrayNULLEmpty(Types.TYPE_INT, true);
    public static final ValueArray NULL_LONG_ARRAY = new ValueArrayNULLEmpty(Types.TYPE_LONG, true);
    public static final ValueArray NULL_UBYTE_ARRAY = new ValueArrayNULLEmpty(Types.TYPE_UBYTE, true);
    public static final ValueArray NULL_USHORT_ARRAY = new ValueArrayNULLEmpty(Types.TYPE_USHORT, true);
    public static final ValueArray NULL_UINT_ARRAY = new ValueArrayNULLEmpty(Types.TYPE_UINT, true);
    public static final ValueArray NULL_ULONG_ARRAY = new ValueArrayNULLEmpty(Types.TYPE_ULONG, true);
    public static final ValueArray NULL_DECIMAL_ARRAY = new ValueArrayNULLEmpty(Types.TYPE_DECIMAL, true);
    public static final ValueArray NULL_FLOAT_ARRAY = new ValueArrayNULLEmpty(Types.TYPE_FLOAT, true);
    public static final ValueArray NULL_DOUBLE_ARRAY = new ValueArrayNULLEmpty(Types.TYPE_DOUBLE, true);
    public static final ValueArray NULL_DATE_ARRAY = new ValueArrayNULLEmpty(Types.TYPE_DATE, true);
    public static final ValueArray NULL_TIME_ARRAY = new ValueArrayNULLEmpty(Types.TYPE_TIME, true);
    public static final ValueArray NULL_DATETIME_ARRAY = new ValueArrayNULLEmpty(Types.TYPE_DATETIME, true);
    public static final ValueArray NULL_TIMESTAMP_ARRAY = new ValueArrayNULLEmpty(Types.TYPE_TIMESTAMP, true);
    public static final ValueArray NULL_BOOLEAN_ARRAY = new ValueArrayNULLEmpty(Types.TYPE_BOOLEAN, true);
    public static final ValueArray EMPTY_STRING_ARRAY = new ValueArrayNULLEmpty(Types.TYPE_STRING, false);
    public static final ValueArray EMPTY_NSTRING_ARRAY = new ValueArrayNULLEmpty(Types.TYPE_NSTRING, false);
    public static final ValueArray EMPTY_BYTES_ARRAY = new ValueArrayNULLEmpty(Types.TYPE_BYTES, false);
    public static final ValueArray EMPTY_BYTE_ARRAY = new ValueArrayNULLEmpty(Types.TYPE_BYTE, false);
    public static final ValueArray EMPTY_SHORT_ARRAY = new ValueArrayNULLEmpty(Types.TYPE_SHORT, false);
    public static final ValueArray EMPTY_INT_ARRAY = new ValueArrayNULLEmpty(Types.TYPE_INT, false);
    public static final ValueArray EMPTY_LONG_ARRAY = new ValueArrayNULLEmpty(Types.TYPE_LONG, false);
    public static final ValueArray EMPTY_UBYTE_ARRAY = new ValueArrayNULLEmpty(Types.TYPE_UBYTE, false);
    public static final ValueArray EMPTY_USHORT_ARRAY = new ValueArrayNULLEmpty(Types.TYPE_USHORT, false);
    public static final ValueArray EMPTY_UINT_ARRAY = new ValueArrayNULLEmpty(Types.TYPE_UINT, false);
    public static final ValueArray EMPTY_ULONG_ARRAY = new ValueArrayNULLEmpty(Types.TYPE_ULONG, false);
    public static final ValueArray EMPTY_DECIMAL_ARRAY = new ValueArrayNULLEmpty(Types.TYPE_DECIMAL, false);
    public static final ValueArray EMPTY_FLOAT_ARRAY = new ValueArrayNULLEmpty(Types.TYPE_FLOAT, false);
    public static final ValueArray EMPTY_DOUBLE_ARRAY = new ValueArrayNULLEmpty(Types.TYPE_DOUBLE, false);
    public static final ValueArray EMPTY_DATE_ARRAY = new ValueArrayNULLEmpty(Types.TYPE_DATE, false);
    public static final ValueArray EMPTY_TIME_ARRAY = new ValueArrayNULLEmpty(Types.TYPE_TIME, false);
    public static final ValueArray EMPTY_DATETIME_ARRAY = new ValueArrayNULLEmpty(Types.TYPE_DATETIME, false);
    public static final ValueArray EMPTY_TIMESTAMP_ARRAY = new ValueArrayNULLEmpty(Types.TYPE_TIMESTAMP, false);
    public static final ValueArray EMPTY_BOOLEAN_ARRAY = new ValueArrayNULLEmpty(Types.TYPE_BOOLEAN, false);
    static Value[] EMPTY_ARRAY = new Value[0];

    /**
     * generic list to manipulate
     *
     * @return new ValueArray
     */
    public static ValueArray builderOf(Types type) {
        return new ValueArrayByList(type, new ArrayList<>());
    }

    public static ValueArray builderOf(Types type, int capacity) {
        return new ValueArrayByList(type, new ArrayList<>(capacity));
    }

    public static ValueArray unmodifiableArray(ValueArray val) {
        if(val instanceof ValueArrayByArray)
            return val;
        if(val instanceof ValueArrayByList)
            return ValueArrayByList.createUnmodifiableVersion((ValueArrayByList) val);
        throw new AssertionError();
    }

    /**
     * short hand to build array of string
     *
     * @param data
     * @return
     */
    public static ValueArray arrayOf(String... data) { //build from array of string
        if(data.length == 0)
            return ValueArrayNULLEmpty.buildEmptyArray(Types.TYPE_STRING);
        Value[] array = new Value[data.length];
        IntStream.range(0, data.length).forEach((i) -> array[i] = Value.valueOf(data[i]));
        return new ValueArrayByArray(array[0].getType(), array);
    }

    /**
     * short hand to build array of temporal
     *
     * @param data
     * @return
     */
    public static ValueArray arrayOf(Temporal... data) { //build from array of temporal
        if(data.length == 0)
            return ValueArrayNULLEmpty.buildEmptyArray(Types.TYPE_DATETIME);
        Value[] array = new Value[data.length];
        for(int i = 0; i< data.length; i++) {
            array[i] = ValueDateTime.buildDateTime(data[i]);
            if(!array[i].is(array[0].getType()))
                throw new IllegalArgumentException("Non-uni type of data");
        }
        return new ValueArrayByArray(array[0].getType(), array);
    }

    /**
     * short hand to build array of decimal
     * @param data
     * @return
     */
    public static ValueArray arrayOf(BigDecimal...data) { //build from array of long
        if(data.length == 0)
            return ValueArrayNULLEmpty.buildEmptyArray(Types.TYPE_DECIMAL);
        Value[] array = new Value[data.length];
        IntStream.range(0, data.length).forEach((i) -> array[i] = ValueNumber.buildNumber(Types.TYPE_DECIMAL, data[i]));
        return new ValueArrayByArray(array[0].getType(), array);
    }

    /**
     * short hand to build array of long
     * @param data
     * @return
     */
    public static ValueArray arrayOf(long...data) { //build from array of long
        if(data.length == 0)
            return ValueArrayNULLEmpty.buildEmptyArray(Types.TYPE_LONG);
        Value[] array = new Value[data.length];
        IntStream.range(0, data.length).forEach((i) -> array[i] = ValueNumber.buildNumber(Types.TYPE_LONG, data[i]));
        return new ValueArrayByArray(array[0].getType(), array);
    }

    /**
     * short hand to build array of int
     * @param data
     * @return
     */
    public static ValueArray arrayOf(int...data) { //build from array of int
        if(data.length == 0)
            return ValueArrayNULLEmpty.buildEmptyArray(Types.TYPE_INT);
        Value[] array = new Value[data.length];
        IntStream.range(0, data.length).forEach((i) -> array[i] = ValueNumber.buildNumber(Types.TYPE_INT, data[i]));
        return new ValueArrayByArray(array[0].getType(), array);
    }

    /**
     * short hand to build array of boolean
     * @param data
     * @return
     */
    public static ValueArray arrayOf(boolean...data) { //build from array of int
        if(data.length == 0)
            return ValueArrayNULLEmpty.buildEmptyArray(Types.TYPE_BOOLEAN);
        Value[] array = new Value[data.length];
        IntStream.range(0, data.length).forEach((i) -> array[i] = ValueBoolean.buildBoolean(data[i]));
        return new ValueArrayByArray(array[0].getType(), array);
    }

    /**
     * universal method to build array of integer, best for type in (long, uint, int, short, ushort, byte, ubyte or boolean)
     * @param type
     * @param data
     * @return
     */
    public static ValueArray buildArrayOfInteger(Types type, long...data) {
        if(!Types.isInteger(type) && !Types.TYPE_BOOLEAN.equals(type))
            throw new IllegalArgumentException();
        if(data.length == 0)
            return ValueArrayNULLEmpty.buildEmptyArray(type);
        Value[] array = new Value[data.length];
        if(Types.TYPE_BOOLEAN.equals(type))
            IntStream.range(0, data.length).forEach((i) -> array[i] = ValueBoolean.buildBoolean(data[i]));
        else
            IntStream.range(0, data.length).forEach((i) -> array[i] = ValueNumber.buildNumber(type, data[i]));
        return new ValueArrayByArray(type, array);
    }

    /**
     * universal method to build array of number, best for type in (decimal, ulong-big integer, float, double)
     * @param type
     * @param data
     * @return
     */
    public static ValueArray buildArrayOfNumber(Types type, Number...data) {
        if(!Types.isNumber(type))
            throw new IllegalArgumentException();
        if(data.length == 0)
            return ValueArrayNULLEmpty.buildEmptyArray(type);
        Value[] array = new Value[data.length];
        if(data[0] instanceof BigDecimal)
            IntStream.range(0, data.length).forEach((i) -> array[i] = ValueNumber.buildNumber(type,(BigDecimal) data[i]));
        else if(data[0] instanceof BigInteger)
            IntStream.range(0, data.length).forEach((i) -> array[i] = ValueNumber.buildNumber(type,(BigInteger) data[i]));
        else if(data[0] instanceof Float || data[0] instanceof Double)
            IntStream.range(0, data.length).forEach((i) -> array[i] = ValueNumber.buildNumber(type,data[i].doubleValue()));
        else
            IntStream.range(0, data.length).forEach((i) -> array[i] = ValueNumber.buildNumber(type,data[i].longValue()));
        return new ValueArrayByArray(type, array);
    }

    /**
     * universal building method for any from Java array.
     * @param type of array
     * @param data
     * @return
     */
    public static ValueArray arrayOfObject(Types type, Object...data) {
        if(data.length == 0)
            return ValueArrayNULLEmpty.buildEmptyArray(type);
        Value[] array = new Value[data.length];
        ConvertObjectToValue f = new ConvertObjectToValue(type);
        IntStream.range(0, data.length)
                .forEach((i) -> array[i] = f.apply(data[i]));
        return new ValueArrayByArray(type, array);
    }

    /**
     * universal building method for any from Java Collection.
     * @param type
     * @param list
     * @return
     */
    public static ValueArray arrayOfObject(Types type, Collection<Object> list) {
        if(list.isEmpty())
            return ValueArrayNULLEmpty.buildEmptyArray(type);
        ConvertObjectToValue f = new ConvertObjectToValue(type);
        return new ValueArrayByArray(type, list.stream().map(f).collect(Collectors.toUnmodifiableList()));
    }

    @Override
    public boolean isArray() {
        return true;
    }

    /**
     * This method is for convert/create Java array from ValueArray for JDBC Binding.
     *
     * @return new array of Object.
     */
    public abstract Object[] toObjectArray();

    /**
     * this method is for convert to List of primitive for some purpose
     *
     * @return
     */
    public abstract List<Object> toObjectList();

    public static class ValueArrayNULLEmpty extends ValueArray {

        private final Types type;
        private final boolean isNull; //is null array

        private ValueArrayNULLEmpty(Types type, boolean isNull) {
            this.type = type;
            this.isNull = isNull;
        }

        @Override
        public Types getType() {
            return this.type;
        }

        static ValueArray buildNullArray(Types type) {
            switch(type) {
                case TYPE_STRING:
                    return NULL_STRING_ARRAY;
                case TYPE_NSTRING:
                    return NULL_NSTRING_ARRAY;
                case TYPE_BYTES:
                    return NULL_BYTES_ARRAY;
                case TYPE_BYTE:
                    return NULL_BYTE_ARRAY;
                case TYPE_SHORT:
                    return NULL_SHORT_ARRAY;
                case TYPE_INT:
                    return NULL_INT_ARRAY;
                case TYPE_LONG:
                    return NULL_LONG_ARRAY;
                case TYPE_UBYTE:
                    return NULL_UBYTE_ARRAY;
                case TYPE_USHORT:
                    return NULL_USHORT_ARRAY;
                case TYPE_UINT:
                    return NULL_UINT_ARRAY;
                case TYPE_ULONG:
                    return NULL_ULONG_ARRAY;
                case TYPE_DECIMAL:
                    return NULL_DECIMAL_ARRAY;
                case TYPE_FLOAT:
                    return NULL_FLOAT_ARRAY;
                case TYPE_DOUBLE:
                    return NULL_DOUBLE_ARRAY;

                case TYPE_DATE:
                    return NULL_DATE_ARRAY;
                case TYPE_TIME:
                    return NULL_TIME_ARRAY;
                case TYPE_DATETIME:
                    return NULL_DATETIME_ARRAY;
                case TYPE_TIMESTAMP:
                    return NULL_TIMESTAMP_ARRAY;
                case TYPE_BOOLEAN:
                    return NULL_BOOLEAN_ARRAY;
                default:
                    break;
            }
            throw new AssertionError();
        }

        static ValueArray buildEmptyArray(Types type) {
            switch(type) {
                case TYPE_STRING:
                    return EMPTY_STRING_ARRAY;
                case TYPE_NSTRING:
                    return EMPTY_NSTRING_ARRAY;
                case TYPE_BYTES:
                    return EMPTY_BYTES_ARRAY;
                case TYPE_BYTE:
                    return EMPTY_BYTE_ARRAY;
                case TYPE_SHORT:
                    return EMPTY_SHORT_ARRAY;
                case TYPE_INT:
                    return EMPTY_INT_ARRAY;
                case TYPE_LONG:
                    return EMPTY_LONG_ARRAY;
                case TYPE_UBYTE:
                    return EMPTY_UBYTE_ARRAY;
                case TYPE_USHORT:
                    return EMPTY_USHORT_ARRAY;
                case TYPE_UINT:
                    return EMPTY_UINT_ARRAY;
                case TYPE_ULONG:
                    return EMPTY_ULONG_ARRAY;
                case TYPE_DECIMAL:
                    return EMPTY_DECIMAL_ARRAY;
                case TYPE_FLOAT:
                    return EMPTY_FLOAT_ARRAY;
                case TYPE_DOUBLE:
                    return EMPTY_DOUBLE_ARRAY;

                case TYPE_DATE:
                    return EMPTY_DATE_ARRAY;
                case TYPE_TIME:
                    return EMPTY_TIME_ARRAY;
                case TYPE_DATETIME:
                    return EMPTY_DATETIME_ARRAY;
                case TYPE_TIMESTAMP:
                    return EMPTY_TIMESTAMP_ARRAY;
                case TYPE_BOOLEAN:
                    return EMPTY_BOOLEAN_ARRAY;
                default:
                    break;
            }
            throw new AssertionError();
        }

        @Override
        public boolean contains(Object o) {
            return false;
        }

        @Override
        public Iterator<Value> iterator() {
            return Collections.emptyListIterator();
        }

        @Override
        public Value[] toArray() {
            return EMPTY_ARRAY;
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return a;
        }

        @Override
        public boolean add(Value value) {
            throw new UnsupportedOperationException("Unmodifiable of NULL/Empty Array");
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException("Unmodifiable of NULL/Empty Array");
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean addAll(Collection<? extends Value> c) {
            throw new UnsupportedOperationException("Unmodifiable of NULL/Empty Array");
        }

        @Override
        public boolean addAll(int index, Collection<? extends Value> c) {
            throw new UnsupportedOperationException("Unmodifiable of NULL/Empty Array");
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException("Unmodifiable of NULL/Empty Array");
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException("Unmodifiable of NULL/Empty Array");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("Unmodifiable of NULL/Empty Array");
        }

        @Override
        public Value get(int index) {
            throw new IndexOutOfBoundsException("No item in NULL/Empty Array");
        }

        @Override
        public Value set(int index, Value element) {
            throw new UnsupportedOperationException("Unmodifiable of NULL/Empty Array");
        }

        @Override
        public void add(int index, Value element) {
            throw new UnsupportedOperationException("Unmodifiable of NULL/Empty Array");
        }

        @Override
        public Value remove(int index) {
            throw new UnsupportedOperationException("Unmodifiable of NULL/Empty Array");
        }

        @Override
        public int indexOf(Object o) {
            return -1;
        }

        @Override
        public int lastIndexOf(Object o) {
            return -1;
        }

        @Override
        public ListIterator<Value> listIterator() {
            return Collections.emptyListIterator();
        }

        @Override
        public ListIterator<Value> listIterator(int index) {
            return Collections.emptyListIterator();
        }

        @Override
        public List<Value> subList(int fromIndex, int toIndex) {
            throw new IndexOutOfBoundsException("No item in NULL/Empty Array");
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isNull() {
            return isNull;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean isTrue() {
            return false;
        }

        @Override
        public String stringValue() {
            if(isNull)
                return "";
            return ARRAY_PREFIX+ARRAY_POSTFIX;
        }

        @Override
        public int compareTo(Value o) {
            if(o == null || o.isNull())
                return isNull ? 0:1;
            if(!o.isArray())
                throw new IllegalArgumentException("Not an Array");
            if(o.isEmpty())
                return isNull ? -1 : 0;
            return -1;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj == null)
                return false;
            if(obj == this)
                return true;
            if (obj instanceof ValueArrayNULLEmpty)
                return isNull == ((ValueArrayNULLEmpty) obj).isNull;
            if (obj instanceof ValueArray)
                return isNull ? false : ((ValueArray) obj).isEmpty();
            //compare primitive
            if (obj instanceof Value[])
                return isNull ? false : ((Value[]) obj).length == 0;
            //other never equal
            return false;
        }

        @Override
        public Value convertTo(Types type) {
            if(isNull)
                return buildNullArray(type);
            return buildEmptyArray(type);
        }

        @Override
        public Object[] toObjectArray() {
            return EMPTY_ARRAY;//Object is any, even Value.
        }

        @Override
        public List<Object> toObjectList() {
            return Collections.emptyList();
        }
    }
}

