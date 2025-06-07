package esql.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParsePosition;
import java.time.*;
import java.util.regex.Pattern;

/**
 * ESQL Stmt data generic value, hold a scalar or vector data (array or data tree), or LOB data.
 */
public abstract class Value implements Serializable, Comparable<Value> {

    public static final String STRING_OF_NULL = "";

    /*
     * This patterns for checking correct format
     */
    final static boolean USING_REGEX_PATTERN = Boolean.getBoolean(System.getProperty("esql.data.value.use.regex","false"));
    final static Pattern INTEGER_PATTERN = Pattern.compile("^\\s*([+-]?\\d+)\\s*$");
    final static Pattern DECIMAL_PATTERN = Pattern.compile("^\\s*([+-]?\\d+(\\.\\d*)?)\\s*$");
    final static Pattern FLOATING_NUMBER_PATTERN = Pattern.compile("^\\s*([+-]?\\d+(\\.\\d*)?([Ee][+-]?\\d+)?)\\s*$");
    static final Pattern BASE64_LINE_PATTERN = Pattern.compile("\\s*([0-9A-Za-z+/]+=?=?)\\s*");
    static final Pattern BASE64URL_LINE_PATTERN = Pattern.compile("\\s*([0-9A-Za-z\\-_]+=?=?)\\s*");

    /* Hex, Integer, Decimal, Base64... for StringLeftRightChecker */
    private static final char[] HEX_SORTED_CHARS = "0123456789ABCDEFabcdef".toCharArray();
    private static final char[] HEX_START_CHAR_SEQUENCE = {'0', 'x'};

    /**
     * a hex string checking, like "0x1234abcd" or "1234abcd" or "0x1234ABCD"
     * prefix/postfix spaces and tabs are allowed.
     *
     * The result position of start/end can be extracted as hex substring, like "1234abcd" only.
     *
     * @param stringValue to be checked
     * @param outputStart position to start match, can be null
     * @param outputEnd position to end match, can be null
     * @return the Matcher for hex string
     */
    public static boolean isHexString(CharSequence stringValue, ParsePosition outputStart, ParsePosition outputEnd) {
        return StringLeftRightChecker.STRING_GRACEFULLY_END_MATCHED == StringLeftRightChecker.checkStringOfPattern(0,true,
                false, HEX_START_CHAR_SEQUENCE, true, false, //start chars
                false, null, false, false, //end chars
                HEX_SORTED_CHARS, false, //valid chars
                stringValue, outputStart, outputEnd);
    }

    private static char[] INTEGER_START_CHARS = {'+','-'};
    private static char[] INTEGER_SORTED_CHARS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    };
    private static char[] DECIMAL_POINT_CHARS = {'.'};
    private static char[] FLOATING_POWER_NOTATION = {'E','e'};

    /**
     * match a integer string check, like "1234" or "-1234" or "+1234" or "01234".
     * prefix/postfix spaces and tabs are allowed.
     * @param val to check
     * @return Matcher for integer string
     */
    public static boolean isIntegerString(CharSequence val) {
        return isIntegerString(val, null, null);
    }

    /**
     * match a integer string check, like "1234" or "-1234" or "+1234" or "01234".
     * prefix/postfix spaces and tabs are allowed.
     * @param val to check
     * @param outputStart position to start match, can be null
     * @param outputEnd position to end match, can be null
     * @return Matcher for integer string
     */
    public static boolean isIntegerString(CharSequence val, ParsePosition outputStart, ParsePosition outputEnd) {
        if(val == null || val.length() == 0)
            return false;
        if(USING_REGEX_PATTERN) {
            var m = INTEGER_PATTERN.matcher(val);
            if(m.find()) {
                if(outputStart != null)
                    outputStart.setIndex(m.start(1)); //start of integer part
                if(outputEnd != null)
                    outputEnd.setIndex(m.end(1)); //end of integer part
                return true;
            }
            return false;
        }

        return StringLeftRightChecker.STRING_GRACEFULLY_END_MATCHED == StringLeftRightChecker.checkStringOfPattern(0,true,
                true, INTEGER_START_CHARS, true, true, //start set
                false, null, false, false, //end chars
                INTEGER_SORTED_CHARS, false, //valid chars
                val, outputStart, outputEnd);
    }

    /**
     * match a integer string check, like "1234" or "-1234" or "+1234" or "01234".
     * prefix/postfix spaces and tabs are allowed.
     * @param val to check
     * @return true for match integer string
     */
    public static boolean isDecimalString(CharSequence val) {
        return isDecimalString(val, null, null, null);
    }
    /**
     * match a decimal string check, like "1234.5678" or "-1234.5678" or "+1234.5678" or "01234.5678" or integer.
     * prefix/postfix spaces and tabs are allowed.
     * @param val to check
     * @param outputStart position to start match, can be null
     * @param outputEnd position to end match, can be null
     * @return true for match decimal string
     */
     public static boolean isDecimalString(CharSequence val, ParsePosition outputStart, ParsePosition decimalPointOutput, ParsePosition outputEnd) {
         if(val == null || val.length() == 0)
             return false;
         if(decimalPointOutput == null)
             decimalPointOutput = new ParsePosition(0); //create default position

         int part1 = StringLeftRightChecker.checkStringOfPattern(0,true,
                 true, INTEGER_START_CHARS, true, true, //start chars
                 false, DECIMAL_POINT_CHARS, false, false, //end chars
                 INTEGER_SORTED_CHARS, false, //valid chars
                 val, outputStart, decimalPointOutput);
         //gracefully: 'xxx.'; right sequence not match 'xxx';
        if(part1 != StringLeftRightChecker.STRING_GRACEFULLY_END_MATCHED && part1 != StringLeftRightChecker.STRING_END_OF_TEMPLATE
                && part1 != StringLeftRightChecker.STRING_RIGHT_SEQUENCE_MATCH) //not match integer part
            return false; //not match integer part
         //end of template in case of 'xxx.yy' (no gracefully);
        if(part1 != StringLeftRightChecker.STRING_END_OF_TEMPLATE) { //no decimal point or only decimal point, just integer
            if(outputEnd != null)
                outputEnd.setIndex(decimalPointOutput.getIndex()); //set end index to decimal point index (exclude decimal point)
            return true;
        }
        //part2, check decimal point and decimal part
        return StringLeftRightChecker.STRING_GRACEFULLY_END_MATCHED == StringLeftRightChecker.checkStringOfPattern(0,false,
                true, DECIMAL_POINT_CHARS, false, false, //start chars
                false, null, false, false, //end chars
                INTEGER_SORTED_CHARS, false, //valid chars
                val, decimalPointOutput, outputEnd);
    }

    /**
     * match a floating number string check, like "1234.5678e10" or "-1234.5678E-10" or "1234.5678e+10" or "01234.5678e10".
     * prefix/postfix spaces and tabs are allowed.
     * @param value to check
     * @return true for match floating number string
     */
    public static boolean isFloatingNumberString(CharSequence value) {
        return isFloatingNumberString(value, null, null, null, null);
    }

    /**
     * match a floating number string check, like "1234.5678e10" or "-1234.5678E-10" or "1234.5678e+10" or "01234.5678e10".
     * prefix/postfix spaces and tabs are allowed.
     * @param value to check
     * @param outputStart output start position, can be null to ignore
     * @param decimalPointOutput output position of decimal point, can be null to ignore
     * @param powerNotationOutput output position of power notation (e or E), can be null to ignore
     * @param outputEnd output end position, can be null to ignore
     * @return true for match floating number string
     */
    public static boolean isFloatingNumberString(CharSequence value, ParsePosition outputStart, ParsePosition decimalPointOutput, ParsePosition powerNotationOutput, ParsePosition outputEnd) {
        if(value == null || value.length() == 0)
            return false;
        if(decimalPointOutput == null)
            decimalPointOutput = new ParsePosition(0); //create default position
        if(powerNotationOutput == null)
            powerNotationOutput = new ParsePosition(0); //create default position

        //first part, match integer or decimal part
        int part1 = StringLeftRightChecker.checkStringOfPattern(0,true,
                true, INTEGER_START_CHARS, true, true, //start chars
                false, DECIMAL_POINT_CHARS, false, false, //end chars
                INTEGER_SORTED_CHARS, false, //valid chars
                value, outputStart, decimalPointOutput);
        if(part1 != StringLeftRightChecker.STRING_GRACEFULLY_END_MATCHED && part1 != StringLeftRightChecker.STRING_END_OF_TEMPLATE
                && part1 != StringLeftRightChecker.STRING_RIGHT_SEQUENCE_MATCH) //not match integer part
            return false; //not match integer part
        //end of template in case of 'xxx.yy' (no gracefully);
        if(part1 != StringLeftRightChecker.STRING_END_OF_TEMPLATE) { //no decimal point or only decimal point, just integer
            if(outputEnd != null)
                outputEnd.setIndex(decimalPointOutput.getIndex()); //set end index to decimal point index (exclude decimal point)
            return true;
        }

        //part2, check decimal point and decimal part
        int part2 = StringLeftRightChecker.checkStringOfPattern(0, false,
                true, DECIMAL_POINT_CHARS, false, false, //start chars
                false, FLOATING_POWER_NOTATION, false, true, //end chars
                INTEGER_SORTED_CHARS, false, //valid chars
                value, decimalPointOutput, powerNotationOutput);

        if(part2 != StringLeftRightChecker.STRING_GRACEFULLY_END_MATCHED && part2 != StringLeftRightChecker.STRING_END_OF_TEMPLATE
                && part2 != StringLeftRightChecker.STRING_CONTINUE_RIGHT_SEQUENCE) //not match decimal part
            return false; //not match decimal part
        if(part2 != StringLeftRightChecker.STRING_CONTINUE_RIGHT_SEQUENCE) { //no power notation or only decimal point, just decimal
            if (outputEnd != null)
                outputEnd.setIndex(decimalPointOutput.getIndex()); //set end index to decimal point index (exclude decimal point)
            return true;
        }

        var savePos = powerNotationOutput.getIndex();
        powerNotationOutput.setIndex(savePos + 1); //move to next char after e or E
        //part3, check power notation and integer part
        int part3 = StringLeftRightChecker.checkStringOfPattern(0, false,
                true, INTEGER_START_CHARS, true, true, //start chars should be optional
                false, null, false, false, //end chars
                INTEGER_SORTED_CHARS, false, //valid chars
                value, powerNotationOutput, outputEnd);
        powerNotationOutput.setIndex(savePos); //restore position
        return part3 == StringLeftRightChecker.STRING_GRACEFULLY_END_MATCHED;
    }

    private static final char[] BASE64_SORTED_CHAR_SET = {
            '+','/',
            //digits
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
            'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
            'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
            'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
            'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
            'y', 'z',
    };

    private static final char[] BASE64URL_SORTED_CHAR_SET = {
            '-',
            //digits
            '0','1','2','3','4','5','6','7','8','9',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
            'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
            'Y', 'Z',
            '_',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
            'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
            'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
            'y', 'z',
    };
    private static final char[] BASE64_PADDING = { '=' };

    /**
     * match a base64 string check, like "aGVsbG8gd29ybGQ=" or "aGVsbG8gd29ybGQ" or "aGVsbG8gd29ybGQ=="
     * prefix/postfix spaces and tabs are allowed.
     * @param value to check
     * @param isBase64URL true if base64url encoding, false if base64 encoding
     * @return true if match base64 string
     */
    public static boolean matchBase64String(CharSequence value, boolean isBase64URL) {
        if(value.length() == 0 || value.length() % 4 != 0)
            return false;
        if(USING_REGEX_PATTERN) {
            var m = isBase64URL ? BASE64URL_LINE_PATTERN.matcher(value) : BASE64_LINE_PATTERN.matcher(value);
            return m.matches();
        }

        //using StringLeftRightChecker
        int state = StringLeftRightChecker.checkStringOfPattern(0,true,
                true, null, false, false, //start chars
                true, BASE64_PADDING, true, true, //end chars
                isBase64URL ? BASE64URL_SORTED_CHAR_SET : BASE64_SORTED_CHAR_SET, false, //valid chars
                value, null, null);
        return state == StringLeftRightChecker.STRING_GRACEFULLY_END_MATCHED;
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
                return ValueDataTreeImpl.NULL_DATA_TREE;
            case TYPE_XML:
                return ValueDataTree.NULL_XML;
            case TYPE_JSON:
                return ValueDataTree.NULL_JSON;

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
     * This is different from toString that toString may print "type:trimmed value" for debugging instead of it's value.
     * For DataTree and LOB, string printing out with size limitation (eg. max 0.1MB).
     *
     * @return string representation of value, or empty string if null.
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