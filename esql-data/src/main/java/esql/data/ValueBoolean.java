package esql.data;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * ValueBoolean boolean are special number, only 0 and 1 aka false and true.
 */
public class ValueBoolean extends ValueNumber {

    public static final String STR_TRUE = "true";
    public static final String STR_TRUE_YESNO = "yes";
    public static final String STR_TRUE_ONOFF = "on";
    public static final String STR_TRUE_NUM = "1";
    public static final String STR_FALSE = "false";
    public static final String STR_FALSE_YESNO = "no";
    public static final String STR_FALSE_ONOFF = "off";
    public static final String STR_FALSE_NUM = "0";

    public static final ValueBoolean BOOL_TRUE = new ValueBoolean(true);
    public static final ValueBoolean BOOL_FALSE = new ValueBoolean(false);
    public static final ValueBoolean NULL_BOOLEAN = new ValueNULLBoolean();

    enum BooleanChoice { TRUE_FALSE, YES_NO, ON_OFF, NUM0_1 };

    private final boolean val;

    private ValueBoolean(boolean v) {
        this.val = v;
    }

    final static Value buildBoolean(boolean v) {
        return v ? BOOL_TRUE : BOOL_FALSE;
    }

    final static Value buildBoolean(long v) {
        return v != 0 ? BOOL_TRUE : BOOL_FALSE;
    }

    @Override
    public String stringValue() {
        return val ? STR_TRUE : STR_FALSE;
    }

    @Override
    public int compareTo(Value o) {
        if(o == null || o.isNull())
            return 1;
        if(o.isTrue()) {
            if (isTrue())
                return 0;
            else
                return -1;
        }
        else if(isTrue())
            return 1;
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null)
            return false;
        if(obj == this)
            return true;
        if (obj instanceof ValueBoolean)
            return this.val == ((ValueBoolean) obj).val;
        if (obj instanceof ValueNumber)
            return this.val == ((ValueNumber) obj).booleanValue();
        if (obj instanceof Value)
            return !((Value) obj).isNull() && this.val == ((ValueNumber) obj).isTrue();
        return this.val == isTrueString(obj.toString());
    }

    @Override
    public boolean isTrue() {
        return val;
    }

    public static boolean isTrueString(String val) {
        return val!=null && !val.isEmpty();
    }

    @Override
    public Value convertTo(Types type) {
        if(Types.isInteger(type))
            return ValueNumber.buildNumber(type,(long)(val ? 1 : 0));
        if(Types.isNumber(type))
            return ValueNumber.buildNumber(type,(double)(val ? 1 : 0));
        if(Types.isString(type))
            return ValueString.buildString(type, val ? STR_TRUE : STR_FALSE);
        throw new ClassCastException();
    }

    @Override
    public boolean booleanValue() {
        return val;
    }

    @Override
    public byte byteValue() {
        return val ? (byte)1:(byte)0;
    }

    @Override
    public short shortValue() {
        return (val ? (short)1:(short)0);
    }

    @Override
    public int intValue() {
        return val ? 1:0;
    }

    @Override
    public long longValue() {
        return val ? 1:0;
    }

    @Override
    public float floatValue() {
        return val ? 1:0;
    }

    @Override
    public double doubleValue() {
        return val ? 1:0;
    }

    @Override
    public BigDecimal decimalValue() {
        return val ? BigDecimal.ONE:BigDecimal.ZERO;
    }

    @Override
    public BigInteger bigIntValue() {
        return val ? BigInteger.ONE:BigInteger.ZERO;
    }

    @Override
    public Types getType() {
        return Types.TYPE_BOOLEAN;
    }

    public static class ValueNULLBoolean extends ValueBoolean {

        private ValueNULLBoolean() {
            super(false);
        }

        @Override
        public boolean isNull() {
            return true;
        }

        @Override
        public String stringValue() {
            return Value.STRING_OF_NULL;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj == this)
                return true;
            return false;
        }

        @Override
        public int compareTo(Value o) {
            if(o != null && o.isNull())
                return 0;
            return -1;
        }
    }
}
