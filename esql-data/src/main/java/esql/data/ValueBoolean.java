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
    public static final ValueNULLBoolean NULL_BOOLEAN = new ValueNULLBoolean();

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

    /**
     * CompareTo implementation follows Java Language Specification (JLS).
     * For ValueBoolean, it compares to other ValueBoolean, ValueNumber, and ValueString.
     * ValueNumber is compared by differ between boolean (1:0) to its long integer value.
     *  eg. TRUE (1) is greater than 0, and FALSE (0) is less than 1.
     *     TRUE (1) is less than any number greater than 1, and FALSE (0) is greater than any number less than 0.
     *  This allows lexical ordering of boolean values with numbers.
     *
     * @param o the object to be compared.
     * @return 0 if both are equal, -1 or less if this is less than o, 1 or more if this is greater than o.
     */
    @Override
    public int compareTo(Value o) {
        if(o == this)
            return 0;
        if(o == null || o.isNull())
            return 1;
        if(o instanceof ValueBoolean) {
            if (this.val == ((ValueBoolean) o).val)
                return 0;
            return this.val ? 1 : -1;
        }
        if(o instanceof ValueNumber) {
            long theLongValue;
            if (Types.isLongInteger(o.getType()) || Types.isDecimal(o.getType())) {
                theLongValue = ((ValueNumber) o).longValue();
            }
            else
                theLongValue = ((ValueNumber) o).longValue();
            //assuming TRUE = 1, FALSE = 0

            if (theLongValue < Integer.MAX_VALUE
                && theLongValue > Integer.MIN_VALUE) {
                return (val ? 1 : 0) - (int) theLongValue;
            }
            else if (theLongValue >= Integer.MAX_VALUE)
                return Integer.MIN_VALUE;
            else //if (theLongValue <= Integer.MIN_VALUE)
                return Integer.MAX_VALUE;
        }

        return this.val == o.isTrue() ? 0 : (this.val ? 1 : -1);
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
        if (obj instanceof ValueString)
            return this.val == isTrueString(((ValueString) obj).stringValue());
        return !((Value) obj).isNull() && this.val == ((Value) obj).isTrue();
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
            return ValueNumber.buildNumber(type, (val ? 1 : 0));
        if(Types.isNumber(type))
            return ValueNumber.buildNumber(type,(double)(val ? 1 : 0));
        if(Types.isString(type))
            return ValueString.buildString(type, val ? STR_TRUE : STR_FALSE);
        throw new IllegalArgumentException("can not convert boolean to "+type);
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

        /**
         * equals implementation follows Java Language Specification (JLS).
         * This is not compatible with compareTo, as NULL is not equal to any other value.
         *
         * @param obj the object to be compared.
         * @return true if both are null, false otherwise.
         */
        @Override
        public boolean equals(Object obj) {
            if(obj == this || obj == null)
                return true;
            if(obj instanceof Value)
                return ((Value) obj).isNull();
            return false;
        }

        /**
         * CompareTo implementation follows Java Language Specification (JLS).
         *
         * This is compatible with equals, as NULL is equal only to other NULL values.
         * Otherwise it is less than any other value.
         *
         * @param o the object to be compared.
         * @return 0 if both are null
         */
        @Override
        public int compareTo(Value o) {
            //NULL compareTo NULL is 0, as per JLS.
            if(o == this)
                return 0;
            //this is NULL Boolean, only comparing to other null are equal.
            if(o == null || o.isNull())
                return 0; //same as false.
            if (o instanceof ValueNumber) {
                return Integer.MIN_VALUE; //NULL is much less than any boolean or number value.
            }

            return -1; //less than any other value, but only -1 if it is a ValueString or other non-null value.
        }

        @Override
        public Value convertTo(Types type) {
            return Value.nullOf(type);
        }
    }
}
