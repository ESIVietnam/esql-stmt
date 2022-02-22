package esql.data;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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

    private static final ValueString EMPTY_STRING = new ValueString(false, "");
    private static final ValueString EMPTY_NSTRING = new ValueString(true, "");

    private final boolean national;
    private final String value;

    ValueString(boolean national, String val) {
        this.national = national;
        this.value = val;
    }

    public static final Value buildString(Types type, String val) {
        if(!Types.isString(type))
            throw new IllegalArgumentException("not for type other than string/nstring");
        if(val == null)
            return Value.nullOf(type);
        if(val.isEmpty())
            return Types.TYPE_NSTRING.equals(type) ? EMPTY_NSTRING:EMPTY_STRING;
        return new ValueString(false, val);
    }

    public static final Charset stringCharset(boolean national) {
        return national ? STRING_DEFAULT_CHARSET : STRING_NATIONAL_CHARSET;
    }

    @Override
    public boolean isEmpty() {
        return this.value.isEmpty();
    }

    @Override
    public boolean isTrue() {
        return !this.value.isEmpty();
    }

    @Override
    public Value convertTo(Types type) {
        if(getType().equals(type))
            return this; //same
        if(Types.isString(type))
            return buildString(type, this.value);
        //TODO: them sau
        throw new IllegalArgumentException("can not cast from string to "+type.getAbbreviation());
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
            return this.value.equals(((ValueString) obj).value);
        if (obj instanceof Value)
            return !((Value) obj).isNull() && this.value.equals(((Value) obj).stringValue());
        return this.value.equals(obj);
    }

    @Override
    public String stringValue() {
        if(value == null)
            return Value.STRING_OF_NULL;
        return value;
    }
}
