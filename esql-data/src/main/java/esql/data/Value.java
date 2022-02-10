package esql.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;

/**
 * ESQL Stmt data generic value, hold a scalar or vector data (array or data tree), or LOB data.
 */
public abstract class Value implements Serializable, Comparable<Value> {

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
            ValueNULL.buildNULL(Types.TYPE_BOOLEAN);
        return ValueBoolean.buildBoolean(val);
    }

    public final static Value valueOf(Byte val) {
        if(val==null)
            ValueNULL.buildNULL(Types.TYPE_BYTE);
        return ValueNumber.buildNumber(Types.TYPE_BYTE, val.longValue());
    }

    public final static Value valueOf(Short val) {
        if(val==null)
            ValueNULL.buildNULL(Types.TYPE_SHORT);
        return ValueNumber.buildNumber(Types.TYPE_SHORT, val.longValue());
    }

    public final static Value valueOf(Integer val) {
        if(val==null)
            ValueNULL.buildNULL(Types.TYPE_INT);
        return ValueNumber.buildNumber(Types.TYPE_INT, val.longValue());
    }
    public final static Value valueOf(Long val) {
        if(val==null)
            ValueNULL.buildNULL(Types.TYPE_LONG);
        return ValueNumber.buildNumber(Types.TYPE_LONG, val.longValue());
    }

    public final static Value valueOf(Float val) {
        if(val==null)
            ValueNULL.buildNULL(Types.TYPE_FLOAT);
        return ValueNumber.buildNumber(Types.TYPE_FLOAT, val);
    }
    public final static Value valueOf(Double val) {
        if(val==null)
            ValueNULL.buildNULL(Types.TYPE_DOUBLE);
        return ValueNumber.buildNumber(Types.TYPE_DOUBLE, val);
    }

    public final static Value valueOf(BigDecimal num) {
        if(num==null)
            ValueNULL.buildNULL(Types.TYPE_DECIMAL);
        return ValueNumber.buildNumber(Types.TYPE_DECIMAL, num);
    }

    public final static Value valueOf(BigInteger val) {
        if(val==null)
            ValueNULL.buildNULL(Types.TYPE_ULONG);
        return ValueNumber.buildNumber(Types.TYPE_ULONG, val);
    }

    public final static Value valueOf(java.sql.Date val) {
        if(val==null)
            ValueNULL.buildNULL(Types.TYPE_DATE);
        return ValueDateTime.buildDateTime(val);
    }

    public final static Value valueOf(java.sql.Time val) {
        if(val==null)
            ValueNULL.buildNULL(Types.TYPE_TIME);
        return ValueDateTime.buildDateTime(val);
    }

    public static Value valueOf(java.sql.Timestamp val) {
        if(val==null)
            ValueNULL.buildNULL(Types.TYPE_TIMESTAMP);
        return ValueDateTime.buildDateTime(val);
    }

    public static Value valueOf(LocalDate val) {
        if(val==null)
            ValueNULL.buildNULL(Types.TYPE_DATE);
        return ValueDateTime.buildDateTime(val);
    }

    public static Value valueOf(LocalTime val) {
        if(val==null)
            ValueNULL.buildNULL(Types.TYPE_TIME);
        return ValueDateTime.buildDateTime(val);
    }

    public static Value valueOf(LocalDateTime val) {
        if(val==null)
            ValueNULL.buildNULL(Types.TYPE_DATETIME);
        return ValueDateTime.buildDateTime(val);
    }

    public static Value valueOf(ZonedDateTime val) {
        if(val==null)
            ValueNULL.buildNULL(Types.TYPE_TIMESTAMP);
        return ValueDateTime.buildDateTime(val);
    }

    public static Value valueOf(Instant val) {
        if(val==null)
            ValueNULL.buildNULL(Types.TYPE_TIMESTAMP);
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


}