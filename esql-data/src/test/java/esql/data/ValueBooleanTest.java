/*
 * Copyright (C) 2025 by ESI Tech Vietnam and associated contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esql.data;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

class ValueBooleanTest {

    @Test
    void typeTest() {
        assertEquals(Types.TYPE_BOOLEAN, ValueBoolean.BOOL_TRUE.getType());
        assertEquals(Types.TYPE_BOOLEAN, ValueBoolean.NULL_BOOLEAN.getType());
    }

    @Test
    void buildBooleanFromTrue() {
        Value result = ValueBoolean.buildBoolean(true);
        assertEquals(ValueBoolean.BOOL_TRUE, result);
    }

    @Test
    void buildBooleanFromFalse() {
        Value result = ValueBoolean.buildBoolean(false);
        assertEquals(ValueBoolean.BOOL_FALSE, result);
    }

    @Test
    void buildBooleanFromNonZeroLong() {
        Value result = ValueBoolean.buildBoolean(5L);
        assertEquals(ValueBoolean.BOOL_TRUE, result);
    }

    @Test
    void buildBooleanFromZeroLong() {
        Value result = ValueBoolean.buildBoolean(0L);
        assertEquals(ValueBoolean.BOOL_FALSE, result);
    }

    @Test
    void stringValueReturnsTrueForTrueValue() {
        ValueBoolean value = ValueBoolean.BOOL_TRUE;
        assertEquals("true", value.stringValue());
    }

    @Test
    void stringValueReturnsFalseForFalseValue() {
        ValueBoolean value = ValueBoolean.BOOL_FALSE;
        assertEquals("false", value.stringValue());
    }

    @Test
    void equalsReturnsTrueForSameInstance() {
        ValueBoolean value = ValueBoolean.BOOL_TRUE;
        assertTrue(value.equals(value));

        ValueNumber int1 = ValueNumber.INT_ONE;
        assertTrue(value.equals(int1));

        ValueString str = (ValueString) ValueString.buildString(Types.TYPE_STRING, "true");
        assertTrue(value.equals(str));

        ValueBytes bytes = ValueBytes.buildBytes((byte) 0);
        assertTrue(value.equals(bytes));
    }

    @Test
    void equalsReturnsFalseForNull() {
        ValueBoolean value = ValueBoolean.BOOL_TRUE;
        assertFalse(value.equals(null));
    }

    @Test
    void equalsReturnsTrueForEqualValueBoolean() {
        ValueBoolean value = ValueBoolean.BOOL_TRUE;
        assertTrue(value.equals(ValueBoolean.BOOL_TRUE));
    }

    @Test
    void equalsReturnsFalseForDifferentValueBoolean() {
        ValueBoolean value = ValueBoolean.BOOL_TRUE;
        assertFalse(value.equals(ValueBoolean.BOOL_FALSE));
    }

    @Test
    void trueBooleanComparing() {
        ValueBoolean value = ValueBoolean.BOOL_TRUE;
        assertTrue(value.isTrue());
        assertFalse(value.isNull());
        assertEquals(1,value.compareTo(null));
        assertEquals(0,value.compareTo(ValueBoolean.BOOL_TRUE));
        assertEquals(1,value.compareTo(ValueBoolean.BOOL_FALSE));
        assertEquals(-9,value.compareTo(ValueNumber.BYTE_TEN));
    }

    @Test
    void compareToReturnsMinValueWhenComparedToLargePositiveLong() {
        ValueBoolean value = ValueBoolean.BOOL_TRUE;
        ValueNumber largePositive = (ValueNumber) ValueNumber.buildNumber(Types.TYPE_LONG, Long.MAX_VALUE);
        assertEquals(Integer.MIN_VALUE, value.compareTo(largePositive));
    }

    @Test
    void compareToReturnsMaxValueWhenComparedToLargeNegativeLong() {
        ValueBoolean value = ValueBoolean.BOOL_FALSE;
        ValueNumber largeNegative = (ValueNumber) ValueNumber.buildNumber(Types.TYPE_LONG, Long.MIN_VALUE);
        assertEquals(Integer.MAX_VALUE, value.compareTo(largeNegative));
    }

    @Test
    void compareToReturnsCorrectDifferenceForSmallLongValues() {
        ValueBoolean value = ValueBoolean.BOOL_TRUE;
        ValueNumber one = (ValueNumber) ValueNumber.buildNumber(Types.TYPE_LONG, 1L);
        ValueNumber zero = (ValueNumber) ValueNumber.buildNumber(Types.TYPE_LONG, 0L);
        assertEquals(0, value.compareTo(one));
        assertEquals(1, value.compareTo(zero));
    }

    @Test
    void compareToReturnsNegativeOneWhenFalseComparedToOne() {
        ValueBoolean value = ValueBoolean.BOOL_FALSE;
        ValueNumber one = (ValueNumber) ValueNumber.buildNumber(Types.TYPE_LONG, 1L);
        assertEquals(-1, value.compareTo(one));
    }

    @Test
    void compareToReturnsZeroWhenTrueComparedToTrueAsDecimal() {
        ValueBoolean value = ValueBoolean.BOOL_TRUE;
        ValueNumber decimalOne = (ValueNumber) ValueNumber.buildNumber(Types.TYPE_DECIMAL, 1.0);
        assertEquals(0, value.compareTo(decimalOne));
    }

    @Test
    void compareToReturnsOneWhenTrueComparedToZeroAsDecimal() {
        ValueBoolean value = ValueBoolean.BOOL_TRUE;
        ValueNumber decimalZero = (ValueNumber) ValueNumber.buildNumber(Types.TYPE_DECIMAL, 0.0);
        assertEquals(1, value.compareTo(decimalZero));
    }

    @Test
    void isTrueReturnsTrueForTrueValue() {
        ValueBoolean value = ValueBoolean.BOOL_TRUE;
        assertTrue(value.isTrue());
    }

    @Test
    void isTrueReturnsFalseForFalseValue() {
        ValueBoolean value = ValueBoolean.BOOL_FALSE;
        assertFalse(value.isTrue());
    }

    @Test
    void isTrueStringReturnsTrueForNonEmptyString() {
        assertTrue(ValueBoolean.isTrueString("yes"));
    }

    @Test
    void isTrueStringReturnsFalseForEmptyString() {
        assertFalse(ValueBoolean.isTrueString(""));
    }

    @Test
    void isTrueStringReturnsFalseForNull() {
        assertFalse(ValueBoolean.isTrueString(null));
    }

    @Test
    void convertToIntegerTypeReturnsCorrectValue() {
        Value result = ValueBoolean.BOOL_TRUE.convertTo(Types.TYPE_INT);
        assertEquals(0, ValueBoolean.BOOL_FALSE.intValue());
        assertEquals(1, ValueBoolean.BOOL_TRUE.intValue());
        assertEquals(1, ((ValueNumber)result.convertTo(Types.TYPE_INT)).intValue());

        assertEquals(1, ValueBoolean.BOOL_TRUE.byteValue());
        assertEquals(1, ValueBoolean.BOOL_TRUE.shortValue());
        assertEquals(1, ValueBoolean.BOOL_TRUE.longValue());
        assertEquals(BigInteger.ONE, ValueBoolean.BOOL_TRUE.bigIntValue());
        assertEquals(BigDecimal.ONE, ValueBoolean.BOOL_TRUE.decimalValue());

        assertEquals(Float.parseFloat("0"), ValueBoolean.BOOL_FALSE.floatValue());
        assertEquals(Double.parseDouble("0"), ValueBoolean.BOOL_FALSE.doubleValue());

    }

    @Test
    void convertToStringTypeReturnsCorrectValue() {
        Value result = ValueBoolean.BOOL_FALSE.convertTo(Types.TYPE_STRING);
        assertEquals("false", result.stringValue());
    }

    @Test
    void booleanValueReturnsTrueForTrueValue() {
        ValueBoolean value = ValueBoolean.BOOL_TRUE;
        assertTrue(value.booleanValue());
    }

    @Test
    void booleanValueReturnsFalseForFalseValue() {
        ValueBoolean value = ValueBoolean.BOOL_FALSE;
        assertFalse(value.booleanValue());
    }

    @Test
    void nullBooleanIsNull() {
        ValueBoolean value = ValueBoolean.NULL_BOOLEAN;
        assertTrue(value.isNull());
        assertEquals(0,value.compareTo(null));
        assertEquals(0,value.compareTo(value));
        assertEquals(-1,value.compareTo(ValueString.EMPTY_STRING));
        assertEquals(Integer.MIN_VALUE,value.compareTo(ValueBoolean.BOOL_TRUE));
        assertEquals(Integer.MIN_VALUE,value.compareTo(ValueBoolean.BYTE_ZERO));
        assertEquals(Integer.MIN_VALUE,value.compareTo(ValueBoolean.BYTE_TWO));
    }

    @Test
    void nullBooleanStringValueReturnsNullString() {
        ValueBoolean value = ValueBoolean.NULL_BOOLEAN;
        assertEquals(Value.STRING_OF_NULL, value.stringValue());
    }
}