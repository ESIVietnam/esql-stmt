package esql.data;

public class ValueNULL extends Value {

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

