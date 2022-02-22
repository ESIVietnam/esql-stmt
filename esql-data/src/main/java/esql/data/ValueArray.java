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
    public static final String ARRAY_POSTFIX = Optional.ofNullable(System.getenv("ESQL_ARRAY_POSTFIX")).orElse("]");
    public static final String ARRAY_SEPARATOR = Optional.ofNullable(System.getenv("ESQL_ARRAY_SEPARATOR")).orElse(",");

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
    public static ValueArray buildArrayOfObject(Types type, Object...data) {
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
    public static ValueArray buildArrayOfObject(Types type, Collection<Object> list) {
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
}

