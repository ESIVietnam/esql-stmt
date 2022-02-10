package esql.data;

public class ValueNULL extends Value {
    
    public static final String STRING_OF_NULL = "";

    public static final ValueNULL NULL_STRING = new ValueNULL(Types.TYPE_STRING);
    public static final ValueNULL NULL_NSTRING = new ValueNULL(Types.TYPE_NSTRING);
    public static final ValueNULL NULL_CLOB = new ValueNULL(Types.TYPE_CLOB);
    public static final ValueNULL NULL_NCLOB = new ValueNULL(Types.TYPE_NCLOB);
    public static final ValueNULL NULL_BLOB = new ValueNULL(Types.TYPE_BLOB);
    public static final ValueNULL NULL_BYTES = new ValueNULL(Types.TYPE_BYTES);

    public static final ValueNULL NULL_DATA_TREE = new ValueNULL(Types.TYPE_DATA_TREE);
    public static final ValueNULL NULL_XML = new ValueNULL(Types.TYPE_XML);
    public static final ValueNULL NULL_JSON = new ValueNULL(Types.TYPE_JSON);

    public static final ValueNULL NULL_BOOLEAN = new ValueNULL(Types.TYPE_BOOLEAN);
    public static final ValueNULL NULL_BYTE = new ValueNULL(Types.TYPE_BYTE);
    public static final ValueNULL NULL_SHORT = new ValueNULL(Types.TYPE_SHORT);
    public static final ValueNULL NULL_INT = new ValueNULL(Types.TYPE_INT);
    public static final ValueNULL NULL_LONG = new ValueNULL(Types.TYPE_LONG);

    public static final ValueNULL NULL_UBYTE = new ValueNULL(Types.TYPE_UBYTE);
    public static final ValueNULL NULL_USHORT = new ValueNULL(Types.TYPE_USHORT);
    public static final ValueNULL NULL_UINT = new ValueNULL(Types.TYPE_UINT);
    public static final ValueNULL NULL_ULONG = new ValueNULL(Types.TYPE_ULONG);

    public static final ValueNULL NULL_DECIMAL = new ValueNULL(Types.TYPE_DECIMAL);
    public static final ValueNULL NULL_FLOAT = new ValueNULL(Types.TYPE_FLOAT);
    public static final ValueNULL NULL_DOUBLE = new ValueNULL(Types.TYPE_DOUBLE);

    public static final ValueNULL NULL_DATE = new ValueNULL(Types.TYPE_DATE);
    public static final ValueNULL NULL_TIME = new ValueNULL(Types.TYPE_TIME);
    public static final ValueNULL NULL_DATETIME = new ValueNULL(Types.TYPE_DATETIME);
    public static final ValueNULL NULL_TIMESTAMP = new ValueNULL(Types.TYPE_TIMESTAMP);

    private final Types type;
    private ValueNULL(Types type) {
        this.type = type;
    }

    static Value buildNULL(Types type) {
        switch(type) {
            case TYPE_STRING:
                return NULL_STRING;
            case TYPE_NSTRING:
                return NULL_NSTRING;
            case TYPE_CLOB:
                return NULL_CLOB;
            case TYPE_NCLOB:
                return NULL_NCLOB;
            case TYPE_BLOB:
                return NULL_BLOB;
            case TYPE_BYTES:
                return NULL_BYTES;

            case TYPE_DATA_TREE:
                return NULL_DATA_TREE;
            case TYPE_XML:
                return NULL_XML;
            case TYPE_JSON:
                return NULL_JSON;

            case TYPE_BYTE:
                return NULL_BYTE;
            case TYPE_SHORT:
                return NULL_SHORT;
            case TYPE_INT:
                return NULL_INT;
            case TYPE_LONG:
                return NULL_LONG;
            case TYPE_UBYTE:
                return NULL_UBYTE;
            case TYPE_USHORT:
                return NULL_USHORT;
            case TYPE_UINT:
                return NULL_UINT;
            case TYPE_ULONG:
                return NULL_ULONG;

            case TYPE_DECIMAL:
                return NULL_DECIMAL;

            case TYPE_FLOAT:
                return NULL_FLOAT;
            case TYPE_DOUBLE:
                return NULL_DOUBLE;

            case TYPE_DATE:
                return NULL_DATE;
            case TYPE_TIME:
                return NULL_TIME;
            case TYPE_DATETIME:
                return NULL_DATETIME;
            case TYPE_TIMESTAMP:
                return NULL_TIMESTAMP;
            case TYPE_BOOLEAN:
                return NULL_BOOLEAN;
        }
        return null;
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
        return buildNULL(type);
    }

    @Override
    public Types getType() {
        return type;
    }

}

