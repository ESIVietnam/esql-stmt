package esql.data;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

public class ValueBytes extends Value {

    public static final Charset STRING_CONVERT_CHARSET =
            Charset.forName(Optional.ofNullable(System.getenv("ESQL_DEFAULT_STRING_CHARSET"))
                    .orElse(StandardCharsets.UTF_8.name()));

    private final static byte[] EMPTY_BYTES = new byte[0];
    public static final ValueBytes EMPTY_BINARY_STRING = new ValueBytes(EMPTY_BYTES);

    private final byte[] data;

    static final ValueBytes buildBytes(byte... data) {
        if(data == null)
            ValueNULL.buildNULL(Types.TYPE_BYTES);
        if(data.length==0)
            return EMPTY_BINARY_STRING;
        //deep copy.
        return new ValueBytes(Arrays.copyOf(data, data.length));
    }

    private ValueBytes(byte[] data) {
        this.data = data;
    }

    @Override
    public boolean isEmpty() {
        return data.length == 0;
    }

    @Override
    public boolean isTrue() {
        return !isEmpty();
    }

    @Override
    public Value convertTo(Types type) {
        return null;
    }

    @Override
    public Types getType() {
        return Types.TYPE_BYTES;
    }

    @Override
    public String stringValue() {
        if(isEmpty())
            return "";
        return new String(data, STRING_CONVERT_CHARSET);
    }

    @Override
    public int compareTo(Value o) {
        if(o == null || o.isNull())
            return 1;
        if(o instanceof ValueBytes)
            return Arrays.compare(data, ((ValueBytes) o).data);
        return Arrays.compare(data, o.stringValue().getBytes(STRING_CONVERT_CHARSET));
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null)
            return false;
        if(obj == this)
            return true;
        if (obj instanceof ValueBytes)
            return Arrays.equals(data, ((ValueBytes) obj).data);
        if (obj instanceof Value)
            return !((Value) obj).isNull() && Arrays.equals(data, ((ValueString) obj).stringValue().getBytes(STRING_CONVERT_CHARSET));
        //compare primitive
        if (obj instanceof byte[])
            return Arrays.equals(data, (byte[])obj);
        //other equal checking as string
        return Arrays.equals(data, obj.toString().getBytes(STRING_CONVERT_CHARSET));
    }

    /**
     * Get deep copied of binary value;
     * @return byte array
     */
    public byte[] binaryValue() {
        if(isEmpty()) //empty array
            return EMPTY_BYTES;
        return Arrays.copyOf(data, data.length);
    }

}
