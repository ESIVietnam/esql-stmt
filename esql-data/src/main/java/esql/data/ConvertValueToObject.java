package esql.data;

import java.util.function.Function;

/**
 * This Function class is to convert Value to Object (boxer or real object) that backed of the Value.
 * This is to use in ValueArray Stream.
 */
public class ConvertValueToObject implements Function<Value, Object> {

    @Override
    public Object apply(Value value) {
        if(value.isNull()) //the NullValue as null.
            return null;
        switch (value.getType()) {
            case TYPE_DATE:
            case TYPE_TIME:
            case TYPE_DATETIME:
            case TYPE_TIMESTAMP:
                return ((ValueDateTime)value).toTemporal();
            case TYPE_BYTE:
                return ((ValueNumber)value).byteValue(); //auto-boxer
            case TYPE_UBYTE:
            case TYPE_SHORT:
                return ((ValueNumber)value).shortValue(); //auto-boxer
            case TYPE_USHORT:
            case TYPE_INT:
                return ((ValueNumber)value).intValue(); //auto-boxer
            case TYPE_UINT:
            case TYPE_LONG:
                return ((ValueNumber)value).longValue(); //auto-boxer
            case TYPE_ULONG:
                return ((ValueNumber)value).bigIntValue(); //real object
            case TYPE_DECIMAL:
                return ((ValueNumber)value).decimalValue(); //real object
            case TYPE_FLOAT:
                return ((ValueNumber)value).floatValue(); //auto-boxer
            case TYPE_DOUBLE:
                return ((ValueNumber)value).doubleValue(); //auto-boxer
            case TYPE_BOOLEAN:
                return ((ValueBoolean)value).booleanValue() ? Boolean.TRUE : Boolean.FALSE; //auto-boxer
            case TYPE_STRING:
            case TYPE_NSTRING:
                return value.stringValue(); //real object
            case TYPE_DATA_TREE:
                break;
            case TYPE_XML:
                break;
            case TYPE_JSON:
                break;
            case TYPE_CLOB:
                break;
            case TYPE_NCLOB:
                break;
            case TYPE_BLOB:
                break;
            case TYPE_BYTES:
                return ((ValueBytes)value).bytesArray(); //real object
        }
        throw new UnsupportedOperationException("Type \""+value.getType()+"\" can not be converted to Java Object.");
    }
}
