package esql.data;

import java.math.BigDecimal;
import java.math.BigInteger;

public abstract class ValueNumber extends Value {

    public static final ValueNumber NULL_BYTE = new ValueNULLNumber(Types.TYPE_BYTE);
    public static final ValueNumber NULL_SHORT = new ValueNULLNumber(Types.TYPE_SHORT);
    public static final ValueNumber NULL_INT = new ValueNULLNumber(Types.TYPE_INT);
    public static final ValueNumber NULL_LONG = new ValueNULLNumber(Types.TYPE_LONG);
    public static final ValueNumber NULL_UBYTE = new ValueNULLNumber(Types.TYPE_UBYTE);
    public static final ValueNumber NULL_USHORT = new ValueNULLNumber(Types.TYPE_USHORT);
    public static final ValueNumber NULL_UINT = new ValueNULLNumber(Types.TYPE_UINT);
    public static final ValueNumber NULL_ULONG = new ValueNULLNumber(Types.TYPE_ULONG);
    public static final ValueNumber NULL_DECIMAL = new ValueNULLNumber(Types.TYPE_DECIMAL);
    public static final ValueNumber NULL_FLOAT = new ValueNULLNumber(Types.TYPE_FLOAT);
    public static final ValueNumber NULL_DOUBLE = new ValueNULLNumber(Types.TYPE_DOUBLE);

    public final static ValueNumberInt BYTE_ZERO = new ValueNumberInt(Types.TYPE_BYTE, 0);
    public final static ValueNumberInt BYTE_ONE = new ValueNumberInt(Types.TYPE_BYTE, 1);
    public final static ValueNumberInt BYTE_TWO = new ValueNumberInt(Types.TYPE_BYTE, 2);
    public final static ValueNumberInt BYTE_TEN = new ValueNumberInt(Types.TYPE_BYTE, 10);
    public final static ValueNumberInt UBYTE_ZERO = new ValueNumberInt(Types.TYPE_UBYTE, 0);
    public final static ValueNumberInt UBYTE_ONE = new ValueNumberInt(Types.TYPE_UBYTE, 1);
    public final static ValueNumberInt UBYTE_TWO = new ValueNumberInt(Types.TYPE_UBYTE, 2);
    public final static ValueNumberInt UBYTE_TEN = new ValueNumberInt(Types.TYPE_UBYTE, 10);
    public final static ValueNumberInt SHORT_ZERO = new ValueNumberInt(Types.TYPE_BYTE, 0);
    public final static ValueNumberInt SHORT_ONE = new ValueNumberInt(Types.TYPE_SHORT, 1);
    public final static ValueNumberInt SHORT_TWO = new ValueNumberInt(Types.TYPE_SHORT, 2);
    public final static ValueNumberInt SHORT_TEN = new ValueNumberInt(Types.TYPE_SHORT, 10);
    public final static ValueNumberInt USHORT_ZERO = new ValueNumberInt(Types.TYPE_USHORT, 0);
    public final static ValueNumberInt USHORT_ONE = new ValueNumberInt(Types.TYPE_USHORT, 1);
    public final static ValueNumberInt USHORT_TWO = new ValueNumberInt(Types.TYPE_USHORT, 2);
    public final static ValueNumberInt USHORT_TEN = new ValueNumberInt(Types.TYPE_USHORT, 10);
    public final static ValueNumberInt INT_ZERO = new ValueNumberInt(Types.TYPE_INT, 0);
    public final static ValueNumberInt INT_ONE = new ValueNumberInt(Types.TYPE_INT, 1);
    public final static ValueNumberInt INT_TWO = new ValueNumberInt(Types.TYPE_INT, 2);
    public final static ValueNumberInt INT_TEN = new ValueNumberInt(Types.TYPE_INT, 10);
    public final static ValueNumberUInt UINT_ZERO = new ValueNumberUInt(0);
    public final static ValueNumberUInt UINT_ONE = new ValueNumberUInt(1);
    public final static ValueNumberUInt UINT_TWO = new ValueNumberUInt(2);
    public final static ValueNumberUInt UINT_TEN = new ValueNumberUInt(10);

    public final static ValueNumberLong LONG_ZERO = new ValueNumberLong(0);
    public final static ValueNumberLong LONG_ONE = new ValueNumberLong(1);
    public final static ValueNumberLong LONG_TWO = new ValueNumberLong(2);
    public final static ValueNumberLong LONG_TEN = new ValueNumberLong(10);

    public final static ValueNumberObject ULONG_ZERO = new ValueNumberObject(Types.TYPE_ULONG, BigInteger.ZERO);
    public final static ValueNumberObject ULONG_ONE = new ValueNumberObject(Types.TYPE_ULONG, BigInteger.ONE);
    public final static ValueNumberObject ULONG_TWO = new ValueNumberObject(Types.TYPE_ULONG, BigInteger.TWO);
    public final static ValueNumberObject ULONG_TEN = new ValueNumberObject(Types.TYPE_ULONG, BigInteger.TEN);

    public final static ValueNumberObject DECIMAL_ZERO = new ValueNumberObject(Types.TYPE_DECIMAL, BigDecimal.ZERO);
    public final static ValueNumberObject DECIMAL_ONE = new ValueNumberObject(Types.TYPE_DECIMAL, BigDecimal.ONE);
    public final static ValueNumberObject DECIMAL_TWO = new ValueNumberObject(Types.TYPE_DECIMAL, BigDecimal.valueOf(2L));
    public final static ValueNumberObject DECIMAL_TEN = new ValueNumberObject(Types.TYPE_DECIMAL, BigDecimal.TEN);

    public final static ValueNumberObject FLOAT_ZERO = new ValueNumberObject(Types.TYPE_FLOAT, BigDecimal.ZERO);
    public final static ValueNumberObject FLOAT_ONE = new ValueNumberObject(Types.TYPE_FLOAT, BigDecimal.ONE);
    public final static ValueNumberObject FLOAT_TWO = new ValueNumberObject(Types.TYPE_FLOAT, BigDecimal.valueOf(2L));
    public final static ValueNumberObject FLOAT_TEN = new ValueNumberObject(Types.TYPE_FLOAT, BigDecimal.TEN);

    public final static ValueNumberObject DOUBLE_ZERO = new ValueNumberObject(Types.TYPE_FLOAT, BigDecimal.ZERO);
    public final static ValueNumberObject DOUBLE_ONE = new ValueNumberObject(Types.TYPE_FLOAT, BigDecimal.ONE);
    public final static ValueNumberObject DOUBLE_TWO = new ValueNumberObject(Types.TYPE_FLOAT, BigDecimal.valueOf(2L));
    public final static ValueNumberObject DOUBLE_TEN = new ValueNumberObject(Types.TYPE_FLOAT, BigDecimal.TEN);

    private static final ValueNumber[][] STATIC_VAL_MATRIX = {
            { BYTE_ZERO, BYTE_ONE, BYTE_TWO, BYTE_TEN, }, //0
            { UBYTE_ZERO, UBYTE_ONE, UBYTE_TWO, UBYTE_TEN, }, //1
            { SHORT_ZERO, SHORT_ONE, SHORT_TWO, SHORT_TEN, }, //2
            { USHORT_ZERO, USHORT_ONE, USHORT_TWO, USHORT_TEN, }, //3
            { INT_ZERO, INT_ONE, INT_TWO, INT_TEN, }, //4
            { UINT_ZERO, UINT_ONE, UINT_TWO, UINT_TEN, }, //5
            { LONG_ZERO, LONG_ONE, LONG_TWO, LONG_TEN, }, //6
            { ULONG_ZERO, ULONG_ONE, ULONG_TWO, ULONG_TEN, }, //7
            { DECIMAL_ZERO, DECIMAL_ONE, DECIMAL_TWO, DECIMAL_TEN,  }, //8
            { FLOAT_ZERO, FLOAT_ONE, FLOAT_TWO, FLOAT_TEN, }, //9
            { DOUBLE_ZERO, DOUBLE_ONE, DOUBLE_TWO, DOUBLE_TEN, }, //10
    };

    private static boolean integerUsingPool = "true".equals(System.getProperty("ESQL_INTEGER_USING_POOL", System.getenv("ESQL_INTEGER_USING_POOL")));

    private static final int typeToIndex(Types type) {
        int i = -1;
        switch (type) {
            case TYPE_BYTE:
                i = 0;
                break;
            case TYPE_UBYTE:
                i = 1;
                break;
            case TYPE_SHORT:
                i = 2;
                break;
            case TYPE_USHORT:
                i = 3;
                break;
            case TYPE_INT:
                i = 4;
                break;
            case TYPE_UINT:
                i = 5;
                break;
            case TYPE_LONG:
                i = 6;
                break;
            case TYPE_ULONG:
                i = 7;
                break;
            case TYPE_DECIMAL:
                i = 8;
                break;
            case TYPE_FLOAT:
                i = 9;
                break;
            case TYPE_DOUBLE:
                i = 10;
                break;
            default:
                break;
        }
        return i;
    }

    private static final ValueNumber useStaticValue(Types type, long value) {
        int i = typeToIndex(type);
        int j = -1;
        if (value == 0 || value == 1 || value == 2) {
            j = (int) value;
        }
        else if (value == 10L) {
            j = 3;
        }
        if(i >=0 && j >= 0)
            return STATIC_VAL_MATRIX[i][j];
        return null;
    }

    private static final ValueNumber useStaticValue(Types type, double value) {
        int i = typeToIndex(type);
        int j = -1;
        if (value == 0 || value == 1 || value == 2) {
            j = (int) value;
        }
        else if (value == 10) {
            j = 3;
        }
        if(i >=0 && j >= 0)
            return STATIC_VAL_MATRIX[i][j];
        return null;
    }

    private static final BigDecimal BIG_DECIMAL_TWO = BigDecimal.valueOf(2L);
    private static final ValueNumber useStaticValue(Types type, BigDecimal value) {
        int i = typeToIndex(type);
        int j = -1;
        if (value.equals(BigDecimal.ZERO)|| value.equals(BigDecimal.ONE) || value.equals(BIG_DECIMAL_TWO)) {
            j = value.intValue();
        }
        else if (value.equals(BigDecimal.TEN)) {
            j = 3;
        }
        if(i >=0 && j >= 0)
            return STATIC_VAL_MATRIX[i][j];
        return null;
    }
    private static final ValueNumber useStaticValue(Types type, BigInteger value) {
        int i = typeToIndex(type);
        int j = -1;
        if (value.equals(BigInteger.ZERO)|| value.equals(BigInteger.ONE) || value.equals(BigInteger.TWO)) {
            j = value.intValue();
        }
        else if (value.equals(BigInteger.TEN)) {
            j = 3;
        }
        if(i >=0 && j >= 0)
            return STATIC_VAL_MATRIX[i][j];
        return null;
    }

    static Value buildNumber(Types type, double num) {
        if(!Types.isNumber(type) && !Types.TYPE_BOOLEAN.equals(type))
            throw new IllegalArgumentException("type \""+type+"\" is not number / number convertible");

        ValueNumber v = useStaticValue(type, num);
        if(v != null)
            return v;
        switch(type) {
            case TYPE_BOOLEAN:
                return ValueBoolean.buildBoolean(num != 0);
            case TYPE_BYTE:
                //using pool
                if(integerUsingPool)
                    return new ValueNumberObject(type, (byte)((int) num & 0xff));
            case TYPE_UBYTE:
            case TYPE_SHORT:
            case TYPE_USHORT:
            case TYPE_INT:
                return new ValueNumberInt(type, (int) num);
            case TYPE_UINT:
                return new ValueNumberUInt((long) num);
            case TYPE_LONG:
                return new ValueNumberLong((long) num);
            case TYPE_ULONG:
                return new ValueNumberObject(type, new BigDecimal(String.valueOf(num)).toBigInteger());
            case TYPE_DECIMAL:
                return new ValueNumberObject(type, BigDecimal.valueOf(num));
            case TYPE_FLOAT:
                return new ValueNumberObject(type, Float.valueOf((float) num));
            case TYPE_DOUBLE:
                return new ValueNumberObject(type, Double.valueOf(num));
            default:
                break;
        }
        throw new AssertionError();
    }

    static Value buildNumber(Types type, long num) {
        if(!Types.isNumber(type) && !Types.TYPE_BOOLEAN.equals(type))
            throw new IllegalArgumentException("type \""+type+"\" is not number / number convertible");

        ValueNumber v = useStaticValue(type, num);
        if(v != null)
            return v;
        switch(type) {
            case TYPE_BOOLEAN:
                return ValueBoolean.buildBoolean(num != 0);
            case TYPE_BYTE:
                //using Integer pool is better memory?
                //using pool
                if(integerUsingPool)
                    return new ValueNumberObject(type, (byte)(num & 0xff));
            case TYPE_UBYTE:
            case TYPE_SHORT:
            case TYPE_USHORT:
            case TYPE_INT:
                return new ValueNumberInt(type, (int) num);
            case TYPE_UINT:
                return new ValueNumberUInt(num);
            case TYPE_LONG:
                return new ValueNumberLong(num);
            case TYPE_ULONG:
                return new ValueNumberObject(type, BigInteger.valueOf(num));
            case TYPE_DECIMAL:
                return new ValueNumberObject(type, BigDecimal.valueOf(num));
            case TYPE_FLOAT:
                return new ValueNumberObject(type, Float.valueOf(num));
            case TYPE_DOUBLE:
                return new ValueNumberObject(type, Double.valueOf(num));
            default:
                break;
        }
        throw new AssertionError();
    }

    static Value buildNumber(Types type, BigDecimal num) {
        if(num==null)
            return Value.nullOf(type);
        ValueNumber v = useStaticValue(type, num);
        if(v != null)
            return v;
        if(Types.TYPE_ULONG.equals(type))
            return new ValueNumberObject(Types.TYPE_ULONG, num.unscaledValue());
        if(Types.TYPE_BOOLEAN.equals(type))
            return ValueBoolean.buildBoolean(!num.equals(BigDecimal.ZERO));
        //integer values
        if(Types.isInteger(type))
            return buildNumber(type, num.longValue());
        //decimal object
        if(Types.TYPE_DECIMAL.equals(type))
            return new ValueNumberObject(Types.TYPE_DECIMAL, num);
        //double value
        return new ValueNumberObject(type, num.doubleValue());
    }

    static Value buildNumber(Types type, BigInteger num) {
        if(num==null)
            return Value.nullOf(type);
        ValueNumber v = useStaticValue(type, num);
        if(v != null)
            return v;
        if(Types.TYPE_ULONG.equals(type))
            return new ValueNumberObject(Types.TYPE_ULONG, num);
        if(Types.TYPE_BOOLEAN.equals(type))
            return ValueBoolean.buildBoolean(!num.equals(BigInteger.ZERO));
        //integer values
        if(Types.isInteger(type))
            return buildNumber(type, num.longValue());
        //decimal object
        if(Types.TYPE_DECIMAL.equals(type))
            return new ValueNumberObject(Types.TYPE_DECIMAL, num);
        //double value
        return new ValueNumberObject(type, num.doubleValue());
    }

    public abstract boolean booleanValue();
    public abstract byte byteValue();
    public abstract short shortValue();
    public abstract int intValue();
    public abstract long longValue();
    public abstract float floatValue();
    public abstract double doubleValue();
    public abstract BigDecimal decimalValue();
    public abstract BigInteger bigIntValue();

    @Override
    public boolean isEmpty() {
        return false; //never empty
    }
}

final class ValueNULLNumber extends ValueNumber {
    private final Types type;

    ValueNULLNumber(Types type) {
        this.type = type;
    }

    @Override
    public boolean isTrue() {
        return false;
    }

    @Override
    public Value convertTo(Types type) {
        return Value.nullOf(type);
    }

    @Override
    public Types getType() {
        return type;
    }

    @Override
    public boolean isNull() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public String stringValue() {
        return Value.STRING_OF_NULL;
    }

    @Override
    public boolean booleanValue() {
        return false;
    }

    @Override
    public byte byteValue() {
        return 0;
    }

    @Override
    public short shortValue() {
        return 0;
    }

    @Override
    public int intValue() {
        return 0;
    }

    @Override
    public long longValue() {
        return 0;
    }

    @Override
    public float floatValue() {
        return 0;
    }

    @Override
    public double doubleValue() {
        return 0;
    }

    @Override
    public BigDecimal decimalValue() {
        return null;
    }

    @Override
    public BigInteger bigIntValue() {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this)
            return true;
        return false;
    }

    @Override
    public int compareTo(Value o) {
        if(o != null && o.isNull())
            return 0;
        return -1;
    }
}
/**
 * class for BigInteger  (ulong), BigDecimal, Float, Double,
 * or, for Integer from -128 to 127 due to Java's pool of Integer to save memory.
 */
final class ValueNumberObject extends ValueNumber {
    private final Types type;
    private final Number number;

    ValueNumberObject(Types type, Number number) {
        this.type = type;
        switch (type) {
            case TYPE_ULONG:
                if(number instanceof BigInteger)
                    this.number = number;
                else {
                    if(number instanceof BigDecimal) {
                        this.number = ((BigDecimal) number).unscaledValue();
                    }
                    else if(number.longValue() == 0)
                        this.number = BigInteger.ZERO;
                    else if(number.longValue() == 1)
                        this.number = BigInteger.ONE;
                    else if(number.longValue() == 2)
                        this.number = BigInteger.TWO;
                    else if(number.longValue() == 10)
                        this.number = BigInteger.TEN;
                    else if(number instanceof Float)
                        this.number = new BigInteger (((Float) number).toString());
                    else if(number instanceof Double)
                        this.number = new BigInteger (((Double) number).toString());
                    else
                        this.number = BigInteger.valueOf(number.longValue());
                }
                break;
            case TYPE_DECIMAL:
                if(number instanceof BigDecimal)
                    this.number = number;
                else if(number instanceof BigInteger) {
                    if (number.equals(BigInteger.ZERO))
                        this.number = BigDecimal.ZERO;
                    else if (number.equals(BigInteger.ONE))
                        this.number = BigDecimal.ONE;
                    else if (number.equals(BigInteger.TEN))
                        this.number = BigDecimal.TEN;
                    else
                        this.number = new BigDecimal((BigInteger) number);
                }
                else
                    this.number = BigDecimal.valueOf(number.doubleValue());
                break;
            case TYPE_FLOAT:
                if(number instanceof Float)
                    this.number = number;
                else
                    this.number = number.floatValue();
                break;
            case TYPE_DOUBLE:
                if(number instanceof Double)
                    this.number = number;
                else
                    this.number = number.doubleValue();
                break;
            default:
                //assert (false):"Should not use for other type";
                this.number = number;
        }

    }

    @Override
    public Types getType() {
        return type;
    }

    @Override
    public String stringValue() {
        if(number instanceof BigDecimal)
            ((BigDecimal) number).toPlainString();
        return number.toString();
    }

    @Override
    public boolean booleanValue() {
        if(number instanceof Integer)
            return number.intValue()!=0?true:false;
        return number.longValue()!=0?true:false;
    }

    @Override
    public byte byteValue() {
        return number.byteValue();
    }

    @Override
    public short shortValue() {
        return number.shortValue();
    }

    @Override
    public int intValue() {
        return number.intValue();
    }

    @Override
    public long longValue() {
        return number.longValue();
    }

    @Override
    public float floatValue() {
        return number.floatValue();
    }

    @Override
    public double doubleValue() {
        return number.doubleValue();
    }

    @Override
    public BigDecimal decimalValue() {
        if(number instanceof BigDecimal)
            return (BigDecimal) number;
        if(number instanceof BigInteger)
            return new BigDecimal((BigInteger) number);
        //as Integer for Java Integer pool
        if(number instanceof Integer)
            return new BigDecimal(number.intValue());
        if(number instanceof Long)
            return new BigDecimal(number.longValue());
        if(number instanceof Float)
            return new BigDecimal(number.floatValue());
        //if(number instanceof Double)
            //return new BigDecimal(number.doubleValue());
        return BigDecimal.valueOf(number.doubleValue());
    }

    @Override
    public BigInteger bigIntValue() {
        if(number instanceof BigInteger)
            return (BigInteger) number;
        if(number instanceof BigDecimal)
            return ((BigDecimal) number).toBigInteger();
        //as Integer for Java Integer pool
        if(number instanceof Long || number instanceof Integer)
            return BigInteger.valueOf(number.longValue());
        return new BigInteger((number).toString());
    }

    @Override
    public int compareTo(Value o) {
        if(o.isNull())
            return 1;
        if(o instanceof ValueNumberObject) {
            if(number instanceof BigInteger)
                return ((BigInteger)number).compareTo(((ValueNumberObject) o).bigIntValue());
            if(number instanceof BigDecimal)
                return ((BigDecimal)number).compareTo(((ValueNumberObject) o).decimalValue());
            if(number instanceof Float)
                return ((Float)number).compareTo(((ValueNumberObject) o).floatValue());
            if(number instanceof Double)
                return ((Double)number).compareTo(((ValueNumberObject) o).doubleValue());
            if(number instanceof Integer)
                return ((Integer)number).compareTo(((ValueNumberObject) o).intValue());
            return Long.compare(number.longValue(), ((ValueNumberObject) o).longValue());
        }
        else {
            if (number instanceof BigInteger)
                return ((BigInteger) number).compareTo(new BigInteger(o.stringValue()));
            if (number instanceof BigDecimal)
                return ((BigDecimal) number).compareTo(new BigDecimal(o.stringValue()));
            if (number instanceof Float)
                return ((Float) number).compareTo(Float.valueOf(o.stringValue()));
            if (number instanceof Double)
                return ((Double) number).compareTo(Double.valueOf(o.stringValue()));
            if (number instanceof Integer)
                return ((Integer) number).compareTo(Integer.valueOf(o.stringValue()));
            if (number instanceof Long)
                return ((Long) number).compareTo(Long.valueOf(o.stringValue()));
        }
        //other types
        return Long.compare(number.longValue(), Long.parseLong( o.stringValue()));
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null)
            return false;
        if(obj == this)
            return true;
        if (obj instanceof ValueNumberObject)
            return this.number.equals(((ValueNumberObject) obj).number);
        if (obj instanceof ValueNumber) {
            if(this.number instanceof BigInteger)
                return this.number.equals( ((ValueNumber) obj).bigIntValue() );
            return this.number.equals( ((ValueNumber) obj).decimalValue() );
        }
        //compare strings
        if(obj instanceof Value)
            return !((Value) obj).isNull() && number.toString().equals(((Value) obj).stringValue());
        return number.equals(obj);
    }

    @Override
    public boolean isTrue() {
        if (number instanceof BigInteger)
            return !number.equals(BigInteger.ZERO);
        if (number instanceof BigDecimal)
            return !number.equals(BigDecimal.ZERO);
        if (number instanceof Float)
            return !number.equals(Float.NaN);
        if (number instanceof Double)
            return !number.equals(Double.NaN);
        if (number instanceof Integer)
            return !number.equals(Integer.valueOf(0));
        return number.longValue() != 0;
    }

    @Override
    public Value convertTo(Types type) {
        if(Types.isString(type))
            return ValueString.buildString(type, number.toString());
        if(Types.TYPE_BOOLEAN.equals(type))
            return ValueBoolean.buildBoolean(this.isTrue());
        if(Types.isNumber(type)) {
            if (number instanceof BigInteger)
                return buildNumber(type, (BigInteger) number);
            if (number instanceof BigDecimal)
                return buildNumber(type, (BigDecimal) number);
            if (number instanceof Float)
                return buildNumber(type, (Float) number);
            if (number instanceof Double)
                return buildNumber(type, (Double) number);
            if (number instanceof Integer)
                return buildNumber(type, (Integer) number);
            if (number instanceof Long)
                return buildNumber(type, (Long) number);
        }
        if(Types.isDateOrTime(type))
            return ValueDateTime.buildDateTime(type, number.longValue());
        throw new IllegalArgumentException("Can not convert to "+type);//can not cast, set null.
    }
}

/**
 * Long value, 8 bytes
 */
class ValueNumberLong extends ValueNumber {
    private final long value;

    ValueNumberLong(long value) {
        this.value = value;
    }

    @Override
    public boolean isTrue() {
        return value != 0;
    }

    @Override
    public Value convertTo(Types type) {
        if(Types.isString(type))
            return ValueString.buildString(type, String.valueOf(value));
        if(Types.TYPE_BOOLEAN.equals(type))
            return ValueBoolean.buildBoolean(this.isTrue());
        if(Types.isNumber(type))
            return ValueNumber.buildNumber(type, value);
        if(Types.isDateOrTime(type))
            return ValueDateTime.buildDateTime(type, value);
        throw new IllegalArgumentException("Can not convert to "+type);//can not cast, set null.
    }

    @Override
    public Types getType() {
        return Types.TYPE_LONG;
    }

    @Override
    public String stringValue() {
        return String.valueOf(value);
    }

    @Override
    public boolean booleanValue() {
        return value != 0;
    }

    @Override
    public byte byteValue() {
        return (byte) value;
    }

    @Override
    public short shortValue() {
        return (short) value;
    }

    @Override
    public int intValue() {
        return (int) value;
    }

    @Override
    public long longValue() {
        return value;
    }

    @Override
    public float floatValue() {
        return value;
    }

    @Override
    public double doubleValue() {
        return value;
    }

    @Override
    public BigDecimal decimalValue() {
        return BigDecimal.valueOf(value);
    }

    @Override
    public BigInteger bigIntValue() {
        return BigInteger.valueOf(value);
    }

    @Override
    public int compareTo(Value o) {
        if(o == null || o.isNull())
            return 1;
        if(o.getType().equals(Types.TYPE_ULONG)) //BigInteger
            return -((ValueNumberObject) o).bigIntValue().compareTo(BigInteger.valueOf(value));
        if(Types.isNumber(o.getType())) {
            return Long.compare(value, ((ValueNumber)o).longValue());
        }
        return -1;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null)
            return false;
        if(obj == this)
            return true;
        if (obj instanceof ValueNumberLong)
            return this.value == ((ValueNumberLong) obj).value;
        if (obj instanceof ValueNumber)
            return this.value == ((ValueNumber) obj).longValue();
        //compare strings
        if(obj instanceof Value)
            return !((Value) obj).isNull() && String.valueOf(value).equals(((Value) obj).stringValue());
        return Long.valueOf(this.value).equals(obj);
    }
}

/**
 * Uint value, 8 bytes
 */
class ValueNumberUInt extends ValueNumberLong {

    ValueNumberUInt(long value) {
        super(value & 0xffffffffL);
    }

    @Override
    public Types getType() {
        return Types.TYPE_UINT;
    }
}

/**
 * class for Int, Short, UShort, Byte, UByte (8 bytes each).
 * Byte will be as ValueNumberObject for utilizing Java's Integer Pool
 */
final class ValueNumberInt extends ValueNumber {
    private final Types type;
    private final int value;

    ValueNumberInt(Types type, int value) {
        this.type = type;
        switch(type) {
            case TYPE_BYTE:
                this.value = (byte) (value & 0xff);
                break;
            case TYPE_UBYTE:
                this.value = value & 0xff; //without sign
                break;
            case TYPE_SHORT:
                this.value = (short) (value & 0xffff);
                break;
            case TYPE_USHORT:
                this.value = value & 0xffff; //without sign
                break;
            default:
                this.value = value;
                break;
        }
    }

    @Override
    public boolean isTrue() {
        return value != 0;
    }

    @Override
    public Value convertTo(Types type) {
        if(Types.isString(type))
            return ValueString.buildString(type, String.valueOf(value));
        if(Types.TYPE_BOOLEAN.equals(type))
            return ValueBoolean.buildBoolean(this.isTrue());
        if(Types.isNumber(type))
            return ValueNumber.buildNumber(type, value);
        throw new IllegalArgumentException("Can not convert to "+type);//can not cast, set null.
    }

    @Override
    public Types getType() {
        return type;
    }

    @Override
    public String stringValue() {
        return String.valueOf(value);
    }

    @Override
    public boolean booleanValue() {
        return value != 0;
    }

    @Override
    public byte byteValue() {
        return (byte) value;
    }

    @Override
    public short shortValue() {
        return (short) value;
    }

    @Override
    public int intValue() {
        return value;
    }

    @Override
    public long longValue() {
        return value;
    }

    @Override
    public float floatValue() {
        return value;
    }

    @Override
    public double doubleValue() {
        return value;
    }

    @Override
    public BigDecimal decimalValue() {
        return BigDecimal.valueOf(value);
    }

    @Override
    public BigInteger bigIntValue() {
        return BigInteger.valueOf(value);
    }

    @Override
    public int compareTo(Value o) {
        if(o == null || o.isNull())
            return 1;
        if(o.getType().equals(Types.TYPE_ULONG)) //BigInteger
            return -((ValueNumberObject) o).bigIntValue().compareTo(BigInteger.valueOf(value));
        if(Types.isNumber(o.getType())) {
            return Long.compare(value, ((ValueNumber)o).longValue());
        }
        return -1;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null)
            return false;
        if(obj == this)
            return true;
        if (obj instanceof ValueNumberInt)
            return this.value == ((ValueNumberInt) obj).value;
        if (obj instanceof ValueNumber)
            return this.value == ((ValueNumber) obj).longValue();
        //compare strings
        if(obj instanceof Value)
            return !((Value) obj).isNull() && String.valueOf(value).equals(((Value) obj).stringValue());
        return Integer.valueOf(this.value).equals(obj);
    }
}