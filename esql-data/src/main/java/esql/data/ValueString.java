package esql.data;

public class ValueString extends Value {

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
            return ValueNULL.buildNULL(type);
        if(val.isEmpty())
            return Types.TYPE_NSTRING.equals(type) ? EMPTY_NSTRING:EMPTY_STRING;
        return new ValueString(false, val);
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
            return 1;
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
        return value;
    }
}
