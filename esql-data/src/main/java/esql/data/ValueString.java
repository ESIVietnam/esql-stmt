package esql.data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.text.ParsePosition;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Base64;
import java.util.Optional;

public class ValueString extends Value {

    public static final ValueString NULL_STRING = new ValueString(false, null);
    public static final ValueString NULL_NSTRING = new ValueString(true, null);

    private static final Charset STRING_DEFAULT_CHARSET =
            Charset.forName(Optional.ofNullable(System.getenv("ESQL_STRING_DEFAULT_CHARSET"))
                    .orElse(StandardCharsets.UTF_8.name()));
    private static final Charset STRING_NATIONAL_CHARSET =
            Charset.forName(Optional.ofNullable(System.getenv("ESQL_STRING_NATIONAL_CHARSET"))
                    .orElse(StandardCharsets.UTF_8.name()));

    public static final ValueString EMPTY_STRING = new ValueString(false, "");
    public static final ValueString EMPTY_NSTRING = new ValueString(true, "");

    private final boolean national;
    private final String value;

    ValueString(boolean national, String val) {
        this.national = national;
        this.value = val;
    }

    public static final ValueString buildString(Types type, String val) {
        if(!Types.isString(type))
            throw new IllegalArgumentException("not for type other than string/nstring");
        if(val == null)
            return (ValueString) Value.nullOf(type);
        if(val.isEmpty())
            return Types.TYPE_NSTRING.equals(type) ? EMPTY_NSTRING:EMPTY_STRING;
        return new ValueString(false, val);
    }

    public static final Charset stringCharset(boolean national) {
        return national ? STRING_DEFAULT_CHARSET : STRING_NATIONAL_CHARSET;
    }

    @Override
    public boolean isNull() {
        return this.value == null;
    }

    @Override
    public boolean isEmpty() {
        return isNull() || this.value.isEmpty();
    }

    @Override
    public boolean isTrue() {
        return !isEmpty();
    }
    

    @Override
    public Value convertTo(Types type) {
        if(getType().equals(type))
            return this; //same
        if(this.isNull())
            return Value.nullOf(type);
        if(Types.isString(type))
            return buildString(type, this.value);
        if(this.isEmpty())
            return Value.nullOf(type);
        //String se chuyen thanh cac loai khac neu safely convertible
        switch (type) {
            case TYPE_DATE:
               //parse as date
               return ValueDateTime.buildDateTime(LocalDate.parse(this.value));
            case TYPE_TIME:
               //parse as date
               return ValueDateTime.buildDateTime(LocalTime.parse(this.value));
            case TYPE_DATETIME:
               //parse as date
               return ValueDateTime.buildDateTime(LocalDateTime.parse(this.value));
            case TYPE_TIMESTAMP:
               //parse as date
               return ValueDateTime.buildDateTime(Instant.parse(this.value));
            case TYPE_BYTE:
            case TYPE_UBYTE:
            case TYPE_SHORT:
            case TYPE_USHORT:
            case TYPE_INT:
            case TYPE_UINT:
            case TYPE_LONG:
                {//as integer types
                    var start = new ParsePosition(0);
                    var decimalPos = new ParsePosition(0);
                    var end = new ParsePosition(this.value.length());
                    if(Value.isIntegerString(this.value, start, end)) {
                        return ValueNumber.buildNumber(type, Long.parseLong(
                                this.value.substring(start.getIndex(), end.getIndex())
                        ));
                    }
                    //try decimal
                    else if(Value.isDecimalString(this.value, start, decimalPos, end)) {
                        //decimal string
                        return ValueNumber.buildNumber(type, Long.parseLong(
                                this.value.substring(start.getIndex(), decimalPos.getIndex())
                        )); //first group
                    }
                }
                throw new NumberFormatException("NaN");
            case TYPE_ULONG:
                {
                    var start = new ParsePosition(0);
                    var decimalPos = new ParsePosition(0);
                    var end = new ParsePosition(this.value.length());
                    if(Value.isIntegerString(this.value, start, end)) {
                        return ValueNumber.buildNumber(type, new BigInteger(
                                this.value.substring(start.getIndex(), end.getIndex())
                        ));
                    }
                    //try decimal
                    else if(Value.isDecimalString(this.value, start, decimalPos, end)) {
                        //decimal string
                        return ValueNumber.buildNumber(type, new BigInteger(
                                this.value.substring(start.getIndex(), decimalPos.getIndex())
                        )); //first group
                    }
                }
                throw new NumberFormatException("NaN");
            case TYPE_DECIMAL:
                try {
                    //parsing as BigDecimal
                    var v = new BigDecimal(this.value);
                    return ValueNumber.buildNumber(type, v);
                }
                catch (NumberFormatException e) {
                    //ignore
                    assert true;
                }
                throw new NumberFormatException("NaN");
            case TYPE_FLOAT:
            case TYPE_DOUBLE:
                //parsing as Double
                return ValueNumber.buildNumber(type, Double.parseDouble(this.value));
            case TYPE_BOOLEAN:
                return ValueBoolean.buildBoolean(!this.value.isEmpty());
            case TYPE_DATA_TREE:
                //TODO: implement sau
                break;
            case TYPE_XML:
                //TODO: implement sau
                break;
            case TYPE_JSON:
                //TODO: implement sau
                break;
            case TYPE_CLOB:
            case TYPE_NCLOB:
                try {
                    return ValueCLOB.wrap(this.value, Types.TYPE_NCLOB.equals(type));
                } catch (NoSuchAlgorithmException e) {
                    throw new AssertionError(e);
                }
            case TYPE_BLOB:
                //base64 convert?
                try {
                    if(Value.matchBase64String(this.value, false)) {
                        var decoder = Base64.getDecoder();
                        var buff = decoder.decode(this.value);
                        return ValueBLOB.wrap(buff, buff.length);
                    }
                } catch (NoSuchAlgorithmException e) {
                    throw new AssertionError(e);
                }
                return Value.nullOf(type);
            case TYPE_BYTES:
                //var bm = Value.matchHexString(this.value);
                var start = new ParsePosition(0);
                var end = new ParsePosition(this.value.length());
                if(Value.isHexString(this.value, start, end)) {
                    return ValueBytes.buildBytes(
                            ValueBytes.hexToBytes(this.value, start.getIndex(), end.getIndex())
                    );
                }
                throw new NumberFormatException("not a hex string");
            default:
                break;
        }
        throw new IllegalArgumentException("can not convert from string to "+type.getAbbreviation());
        //return null;
    }

    @Override
    public Types getType() {
        return national ? Types.TYPE_NSTRING : Types.TYPE_STRING;
    }

    @Override
    public int compareTo(Value o) {
        if(o == null || o.isNull())
            return this.value == null ? 0 : 1;
        if(this.value == null)
            return -1;
        return this.value.compareTo(o.stringValue());
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null)
            return false;
        if(obj == this)
            return true;
        if (obj instanceof ValueString)
            return this.value != null && this.value.equals(((ValueString) obj).value);
        if (obj instanceof Value)
            return this.value != null && this.value.equals(((Value) obj).stringValue());
        return false;
    }

    @Override
    public String stringValue() {
        if(value == null)
            return Value.STRING_OF_NULL;
        return value;
    }
}
