package esql.data;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * The Bytes Value contains an array of byte, convertible/comparable to ValueArray(Types.TYPE_BYTE).
 * It is always input or output to hex-string.
 */
public class ValueBytes extends Value {
    public static final Pattern HEX_STRING_MATCH = Pattern.compile("^[0-9A-Fa-f]+$");

    private final static byte[] EMPTY_BYTES = new byte[0];
    static final ValueBytes EMPTY_BINARY_STRING = new ValueBytes(EMPTY_BYTES);
    static final ValueBytes NULL_BYTES = new ValueBytes(null);

    private final byte[] data;

    static final ValueBytes buildBytes(byte... data) {
        if(data == null)
            return NULL_BYTES;
        if(data.length==0)
            return EMPTY_BINARY_STRING;
        //deep copy.
        return new ValueBytes(Arrays.copyOf(data, data.length));
    }

    static final ValueBytes buildBytes(String hex_data) {
        if(hex_data == null)
            return NULL_BYTES;
        if(hex_data.isEmpty())
            return EMPTY_BINARY_STRING;
        //hex string to bytes
        return new ValueBytes(hexToBytes(hex_data));
    }

    private ValueBytes(byte[] data) {
        this.data = data;
    }

    @Override
    public boolean isNull() {
        return data == null;
    }

    @Override
    public boolean isEmpty() {
        return data == null || data.length == 0;
    }

    @Override
    public boolean isTrue() {
        return !isEmpty();
    }

    @Override
    public Value convertTo(Types type) {
        if(this.isNull())
            return Value.nullOf(type);
        if(is(type))
            return this;
        switch (type) {
            case TYPE_BLOB:
                try {
                    return ValueBLOB.wrap(this.data, this.data.length);
                } catch (NoSuchAlgorithmException e) {
                    throw new AssertionError();
                }
            case TYPE_CLOB:
            case TYPE_NCLOB:
                try {
                    return ValueCLOB.wrap(bytesToHex(this.data), Types.TYPE_NCLOB.equals(type));
                } catch (NoSuchAlgorithmException e) {
                    throw new AssertionError();
                }
            case TYPE_BOOLEAN:
                return ValueBoolean.buildBoolean(this.data.length != 0);
            case TYPE_STRING:
            case TYPE_NSTRING:
                return ValueString.buildString(type, bytesToHex(this.data));
        }
        throw new IllegalArgumentException("can not convert from bytes to "+type.getAbbreviation());
    }

    @Override
    public Types getType() {
        return Types.TYPE_BYTES;
    }

    @Override
    public String stringValue() {
        if(data == null || data.length == 0)
            return "";
        return bytesToHex(data);
    }

    @Override
    public int compareTo(Value o) {
        if(o == null || o.isNull())
            return 1;
        if(o instanceof ValueBytes)
            return Arrays.compareUnsigned(data, ((ValueBytes) o).data);
        //compare array of bytes
        if (o instanceof ValueArray) {
            if(!o.is(Types.TYPE_BYTE)) //always less than
                return -1;
            //as byte array
            for(int i = 0;i<Math.min(data.length, ((ValueArray) o).size());i++) {
                int c = Byte.compareUnsigned(data[i], ((ValueNumber) ((ValueArray) o).get(i)).byteValue());
                if(c != 0)
                    return c;
            }
            return data.length > ((ValueArray) o).size() ? 1 : data.length < ((ValueArray) o).size() ? -1 : 0;
        }
        //compare hex string
        if (o instanceof ValueString)
            return Arrays.compare(data,
                    hexToBytes(o.stringValue()));
        //can not compare, always -1
        return -1;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null)
            return false;
        if(obj == this)
            return true;
        if (obj instanceof ValueBytes)
            return Arrays.equals(data, ((ValueBytes) obj).data);
        //compare array of byte
        if (obj instanceof ValueArray) {
            if(!((ValueArray) obj).is(Types.TYPE_BYTE))
                return false;
            if (((ValueArray) obj).size() != data.length)
                return false;
            Iterator<Value> oit = ((ValueArray) obj).iterator();
            int not_match = IntStream.range(0, Math.min(data.length,((ValueArray) obj).size()))
                    .filter(i -> oit.hasNext() ? !oit.next().equals(data[i]) : true )
                    .findFirst().orElse(-1);
            return not_match < 0 && data.length == ((ValueArray) obj).size();
        }
        //compare hexa string
        if (obj instanceof ValueString)
            return (!((Value) obj).isArray() && ((ValueString) obj).isEmpty() && this.isEmpty())
                    || (!((ValueString) obj).isEmpty() && !this.isEmpty()
                        && Arrays.equals(data,
                    hexToBytes(((ValueString) obj).stringValue())));
        if (obj instanceof Value) //other, false
            return false;
        //compare primitive
        if (obj instanceof byte[])
            return Arrays.equals(data, (byte[])obj);
        if (obj instanceof String) //hex string, too
            return !((String) obj).isEmpty() && Arrays.equals(data,
                    hexToBytes(((String) obj)));
        //other false
        return false;
    }

    /**
     * return length of byte array
     *
     * @return
     */
    public int length() {
        if(isNull())
            return 0;
        return data.length;
    }

    /**
     * Get deep copied of binary value;
     * @return byte array
     */
    public byte[] bytesArray() {
        if(data == null || data.length == 0) //null or empty array
            return EMPTY_BYTES;
        return Arrays.copyOf(data, data.length);
    }

    byte[] backedBytesArray() {
        if(data == null || data.length == 0) //empty array
            return EMPTY_BYTES;
        return data;
    }

    /**
     * get a ValueArray of TYPE_BYTES.
     * @return
     */
    public ValueArray bytesValueArray() {
        if(isNull())
            return ValueArray.ValueArrayNULLEmpty.buildNullArray(Types.TYPE_BYTE);
        if(isEmpty())
            return ValueArray.ValueArrayNULLEmpty.buildEmptyArray(Types.TYPE_BYTE);
        Value[] a = new Value[data.length];
        for(int i=0;i<a.length;i++)
            a[i] = Value.valueOf(data[i]);
        return new ValueArrayByArray(Types.TYPE_BYTE, a);
    }

    //lower char for hexa
    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

    /**
     * byte array to hex string, big endian conversion
     *
     * @param bytes
     * @return
     */
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * big endian only conversion
     */
    public static byte[] hexToBytes(String hex) {
        byte[] buff = new byte[Math.floorDiv(hex.length()+1, 2)];
        hexToBytes(hex, buff, 0);
        return buff;
    }

    /**
     * big endian only conversion
     */
    public static int hexToBytes(String hex, byte[] buf, int pos) {
        int i = pos;
        for (int j = 0; j < hex.length(); j++) {
            if(i>=buf.length)
                return i;//end of buffer
            int v = 0;
            char c = hex.charAt(j);
            if(c >= '0' && c <= '9')
                v = c - '0';
            else if(c >= 'A' && c <= 'F')
                v = c - 'A' + 10;
            else if(c >= 'a' && c <= 'f')
                v = c - 'a' + 10;
            if(j % 2 == 0) //even
                buf[i] |= v << 4;
            else
                buf[i++] |= v;
        }
        return i+1; //end of hex string
    }

}
