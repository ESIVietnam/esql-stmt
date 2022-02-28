package esql.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.regex.Matcher;

/**
 * ESQL Stmt data generic value, hold a scalar or vector data (array or data tree), or LOB data.
 */
public abstract class Value implements Serializable, Comparable<Value> {

    public static final String STRING_OF_NULL = "";
    public static final ValueNULL NULL_DATA_TREE = new ValueNULL(Types.TYPE_DATA_TREE);
    public static final ValueNULL NULL_XML = new ValueNULL(Types.TYPE_XML);
    public static final ValueNULL NULL_JSON = new ValueNULL(Types.TYPE_JSON);

    public static Matcher matchHexString(CharSequence value) {
        return ValueBytes.HEX_STRING_MATCH.matcher(value);
    }

    public static Matcher matchIntegerString(CharSequence val) {
        return ValueNumber.INTEGER_PATTERN.matcher(val);
    }

    public static Matcher matchDecimalString(CharSequence val) {
        var m = matchIntegerString(val);
        if(m == null)
            return m;
        return ValueNumber.DECIMAL_PATTERN.matcher(val);
    }

    public static boolean isBase64String(CharSequence value) {
        if(value.length() == 0 || value.length() % 4 != 0)
            return false;
        var m = ValueBLOB.BASE64_PATTERN.matcher(value);
        return m.matches();
    }

    /**
     * The value is empty as definition.
     * NULL treats as empty value.
     *
     * @return true if empty or null
     */
    public abstract boolean isEmpty();

    /**
     * The value is "true" in boolean operator, like castTo(TYPE_BOOLEAN).booleanValue();
     *
     * @return true if
     */
    public abstract boolean isTrue();

    /**
     * casting value to the new value with type.
     *
     * @param type
     * @return new value in type.
     * @throws IllegalArgumentException when can not convert to the type.
     */
    public abstract Value convertTo(Types type);

    /**
     * check the value is null
     * @return true if null.
     *
     */
    public boolean isNull() {
        return false;
    }

    /**
     * type of value
     * @return type code
     */
    public abstract Types getType();

    /**
     * check for type
     * @return true if same type
     */
    public boolean is(Types type) {
        return getType().equals(type);
    }

    /**
     * the value is Array, a composition of type in multi-values
     * @return true if array
     */
    public boolean isArray() { //default implementation
        return false;
    }

/*
public static Value buildValue(ResultSet rs, int coltype, int column) throws ESIException, SQLException {
    Value v = null;
    if(coltype == Types.ARRAY) {
        Array r = rs.getArray(column);
        assert(r != null);

        BaseTypes at = BaseTypes.getTypeFromSQLTypes(r.getBaseType());
        if(at==null)
            throw new ESIException("Unsupported Array of SQL type"+r.getBaseType());
        v = new Value(at,true);
        v.setArray((Object[])r.getArray());
    }
    else {
        BaseTypes t = BaseTypes.getTypeFromSQLTypes(coltype);
        if(t==null)
            throw new ESIException("Unsupported SQL type"+coltype);
        v = new Value(t);
        v.setVal(getValue(v.getType(),rs, column));
    }
    return v;
}
 */

    public static Value nullOf(Types type) {
        switch(type) {
            case TYPE_STRING:
                return ValueString.NULL_STRING;
            case TYPE_NSTRING:
                return ValueString.NULL_NSTRING;
            case TYPE_CLOB:
                return ValueCLOB.NULL_CLOB;
            case TYPE_NCLOB:
                return ValueCLOB.NULL_NCLOB;
            case TYPE_BLOB:
                return ValueBLOB.NULL_BLOB;
            case TYPE_BYTES:
                return ValueBytes.NULL_BYTES;

            case TYPE_DATA_TREE:
                return NULL_DATA_TREE;
            case TYPE_XML:
                return NULL_XML;
            case TYPE_JSON:
                return NULL_JSON;

            case TYPE_BYTE:
                return ValueNumber.NULL_BYTE;
            case TYPE_SHORT:
                return ValueNumber.NULL_SHORT;
            case TYPE_INT:
                return ValueNumber.NULL_INT;
            case TYPE_LONG:
                return ValueNumber.NULL_LONG;
            case TYPE_UBYTE:
                return ValueNumber.NULL_UBYTE;
            case TYPE_USHORT:
                return ValueNumber.NULL_USHORT;
            case TYPE_UINT:
                return ValueNumber.NULL_UINT;
            case TYPE_ULONG:
                return ValueNumber.NULL_ULONG;

            case TYPE_DECIMAL:
                return ValueNumber.NULL_DECIMAL;

            case TYPE_FLOAT:
                return ValueNumber.NULL_FLOAT;
            case TYPE_DOUBLE:
                return ValueNumber.NULL_DOUBLE;

            case TYPE_DATE:
                return ValueDateTime.NULL_DATE;
            case TYPE_TIME:
                return ValueDateTime.NULL_TIME;
            case TYPE_DATETIME:
                return ValueDateTime.NULL_DATETIME;
            case TYPE_TIMESTAMP:
                return ValueDateTime.NULL_TIMESTAMP;
            case TYPE_BOOLEAN:
                return ValueBoolean.NULL_BOOLEAN;
        }
        return null;
    }

    /*
     * Factory methods does not parse any data
     * String is using string build
     * Date/Time/DateTime/Timestamp using long value method (dual-purpose)
     * Number using number value method
     */

    /* Shorthand factory methods */
    public final static Value valueOf(String val) {
        return ValueString.buildString(Types.TYPE_STRING, val);
    }

    public final static Value valueOf(boolean v) {
        return ValueBoolean.buildBoolean(v);
    }

    public final static Value valueOf(byte num) {
        return ValueNumber.buildNumber(Types.TYPE_BYTE, num);
    }

    public final static Value valueOf(short num) {
        return ValueNumber.buildNumber(Types.TYPE_SHORT, num);
    }

    public final static Value valueOf(int num) {
        return ValueNumber.buildNumber(Types.TYPE_INT, num);
    }

    public final static Value valueOf(long num) {
        return ValueNumber.buildNumber(Types.TYPE_LONG, num);
    }

    public final static Value valueOf(float num) {
        return ValueNumber.buildNumber(Types.TYPE_FLOAT, num);
    }

    public final static Value valueOf(double num) {
        return ValueNumber.buildNumber(Types.TYPE_DOUBLE, num);
    }

    public final static Value valueOf(Boolean val) {
        if(val==null)
            nullOf(Types.TYPE_BOOLEAN);
        return ValueBoolean.buildBoolean(val);
    }

    public final static Value valueOf(Byte val) {
        if(val==null)
            nullOf(Types.TYPE_BYTE);
        return ValueNumber.buildNumber(Types.TYPE_BYTE, val.longValue());
    }

    public final static Value valueOf(Short val) {
        if(val==null)
            nullOf(Types.TYPE_SHORT);
        return ValueNumber.buildNumber(Types.TYPE_SHORT, val.longValue());
    }

    public final static Value valueOf(Integer val) {
        if(val==null)
            nullOf(Types.TYPE_INT);
        return ValueNumber.buildNumber(Types.TYPE_INT, val.longValue());
    }
    public final static Value valueOf(Long val) {
        if(val==null)
            nullOf(Types.TYPE_LONG);
        return ValueNumber.buildNumber(Types.TYPE_LONG, val.longValue());
    }

    public final static Value valueOf(Float val) {
        if(val==null)
            nullOf(Types.TYPE_FLOAT);
        return ValueNumber.buildNumber(Types.TYPE_FLOAT, val);
    }
    public final static Value valueOf(Double val) {
        if(val==null)
            nullOf(Types.TYPE_DOUBLE);
        return ValueNumber.buildNumber(Types.TYPE_DOUBLE, val);
    }

    public final static Value valueOf(BigDecimal num) {
        if(num==null)
            nullOf(Types.TYPE_DECIMAL);
        return ValueNumber.buildNumber(Types.TYPE_DECIMAL, num);
    }

    public final static Value valueOf(BigInteger val) {
        if(val==null)
            nullOf(Types.TYPE_ULONG);
        return ValueNumber.buildNumber(Types.TYPE_ULONG, val);
    }

    public final static Value valueOf(java.sql.Date val) {
        if(val==null)
            nullOf(Types.TYPE_DATE);
        return ValueDateTime.buildDateTime(val);
    }

    public final static Value valueOf(java.sql.Time val) {
        if(val==null)
            nullOf(Types.TYPE_TIME);
        return ValueDateTime.buildDateTime(val);
    }

    public static Value valueOf(java.sql.Timestamp val) {
        if(val==null)
            nullOf(Types.TYPE_TIMESTAMP);
        return ValueDateTime.buildDateTime(val);
    }

    public static Value valueOf(LocalDate val) {
        if(val==null)
            nullOf(Types.TYPE_DATE);
        return ValueDateTime.buildDateTime(val);
    }

    public static Value valueOf(LocalTime val) {
        if(val==null)
            nullOf(Types.TYPE_TIME);
        return ValueDateTime.buildDateTime(val);
    }

    public static Value valueOf(LocalDateTime val) {
        if(val==null)
            nullOf(Types.TYPE_DATETIME);
        return ValueDateTime.buildDateTime(val);
    }

    public static Value valueOf(ZonedDateTime val) {
        if(val==null)
            nullOf(Types.TYPE_TIMESTAMP);
        return ValueDateTime.buildDateTime(val);
    }

    public static Value valueOf(Instant val) {
        if(val==null)
            nullOf(Types.TYPE_TIMESTAMP);
        return ValueDateTime.buildDateTime(val);
    }

    /* full fled build value */
    /*
    public static Value buildValue(Types type, String val) {
        if(!Types.isString(type))
            throw new IllegalArgumentException("Must be string type.");
        return ValueString.buildString(type, val);
    }

    public static Value buildValue(Types type, long num) {
        if(Types.TYPE_BOOLEAN.equals(type))
            return buildValue(num!=0);
        if(Types.isDateOrTime(type))
            return ValueDateTime.buildDateTime(type, num);
        return ValueNumber.buildNumber(type, num);
    }

    public static Value buildValue(Types type, java.util.Date value) {
        return ValueDateTime.buildDateTime(type, value);
    }

    public static Value buildValue(Types type, double num) {
        return ValueNumber.buildNumber(type, num);
    }

    public static Value buildValue(Types type, BigDecimal num) {
        return ValueNumber.buildNumber(type,num);
    }

	/*public static Value buildValue(Types type, String val) throws ESIException {
		if(val == null)
			return null;
		ValueParse s = new ValueParse(type);
		return s.parseValue(val);
	}

	public static Value buildValue(String type, String val,String format, boolean trimSpace) throws ESIException {
		if(val == null)
			return null;
		ValueParse s = new ValueParse(type);
		s.setParseFormat(format);
		s.setTrimSpace(trimSpace);
		return s.parseValue(val);
	}

	public static Value buildValue(String type, String[] valarray) throws ESIException {
		if(valarray == null)
			return null;
		ValueParse s = new ValueParse(type,true);//alway be array
		return s.parseValue(valarray);
	}*/

    /**
     * The value as string (to print out or reference).
     * This is different from toString that toString may print "class name"+"value" for debugging instead of it's value.
     * For DataTree and LOB, string printing out with size limitation (eg. max 0.1MB).
     *
     * @return
     */
    public abstract String stringValue();

    @Override
    public String toString() {
        return getType()+":"+stringValue();
    }

    public static class ValueNULL extends Value {

        private final Types type;
        ValueNULL(Types type) {
            this.type = type;
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
        public boolean isTrue() {
            return false;
        }

        @Override
        public String stringValue() {
            return STRING_OF_NULL;
        }

        @Override
        public int compareTo(Value o) {
            if(o == null || o.isNull())
                return 0;
            return -1;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj == null || obj == this)
                return true;
            if (obj instanceof Value)
                return ((Value) obj).isNull();
            return false;
        }

        @Override
        public Value convertTo(Types type) {
            return nullOf(type);
        }

        @Override
        public Types getType() {
            return type;
        }

    }
}