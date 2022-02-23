package esql.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.jupiter.api.Test;


public class ValueNumberTest {
	
	@Test
	public void valueNumberTest() {
		//Các kiểu UBYTE , USHORT , ULONG , UINT chưa được định nghĩa trong phương thức isNumber
		//
		
		assertEquals(ValueNumber.buildNumber(Types.TYPE_INT, (double) 3000), new ValueNumberInt(Types.TYPE_INT,(int) 3000));
//		assertEquals(ValueNumber.buildNumber(Types.TYPE_UBYTE, (double) 3000), new ValueNumberInt(Types.TYPE_UBYTE,(int) 3000));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_SHORT, (double) 3000), new ValueNumberInt(Types.TYPE_SHORT,(int) 3000));
//		assertEquals(ValueNumber.buildNumber(Types.TYPE_USHORT, (double) 3000), new ValueNumberInt(Types.TYPE_USHORT,(int) 3000));
		
		
//		assertEquals(ValueNumber.buildNumber(Types.TYPE_UINT, (double) 3000), new ValueNumberUInt((long) 3000));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_LONG, (double) 3000), new ValueNumberLong((long) 3000));
//		assertEquals(ValueNumber.buildNumber(Types.TYPE_ULONG, (double) 3000), new ValueNumberObject(Types.TYPE_ULONG, new BigInteger(String.valueOf(3000))));
//		assertEquals(ValueNumber.buildNumber(Types.TYPE_DECIMAL, (double) 3000), new ValueNumberObject(Types.TYPE_DECIMAL, BigDecimal.valueOf(3000)));
		
		assertEquals(ValueNumber.buildNumber(Types.TYPE_FLOAT, (double) 3000),new ValueNumberObject(Types.TYPE_FLOAT, Float.valueOf((float) 3000)));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_DOUBLE, (double) 3000),new ValueNumberObject(Types.TYPE_DOUBLE, Double.valueOf(3000)));
//		assertEquals(ValueNumber.buildNumber(Types.TYPE_BOOLEAN, (double) 3000), ValueBoolean.buildBoolean(true));
//		assertEquals(ValueNumber.buildNumber(Types.TYPE_BOOLEAN, (double) 0), ValueBoolean.buildBoolean(false));
		
		
		//buildNumber(Types type, long num)
		assertEquals(ValueNumber.buildNumber(Types.TYPE_INT, (long) 3000), new ValueNumberInt(Types.TYPE_INT,(int) 3000));
//		assertEquals(ValueNumber.buildNumber(Types.TYPE_UBYTE, (long) 3000), new ValueNumberInt(Types.TYPE_UBYTE,(int) 3000));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_SHORT, (long) 3000), new ValueNumberInt(Types.TYPE_SHORT,(int) 3000));
//		assertEquals(ValueNumber.buildNumber(Types.TYPE_USHORT, (long) 3000), new ValueNumberInt(Types.TYPE_USHORT,(int) 3000));
		
		
//		assertEquals(ValueNumber.buildNumber(Types.TYPE_UINT, (long) 3000), new ValueNumberUInt(3000));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_LONG, (long) 3000), new ValueNumberLong(3000));
//		assertEquals(ValueNumber.buildNumber(Types.TYPE_ULONG, (long) 3000), new ValueNumberObject(Types.TYPE_ULONG, BigInteger.valueOf(3000)));
//		assertEquals(ValueNumber.buildNumber(Types.TYPE_DECIMAL, (long) 3000), new ValueNumberObject(Types.TYPE_DECIMAL, BigDecimal.valueOf(3000)));
		
		assertEquals(ValueNumber.buildNumber(Types.TYPE_FLOAT, (long) 3000),new ValueNumberObject(Types.TYPE_FLOAT, Float.valueOf(3000)));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_DOUBLE, (long) 3000),new ValueNumberObject(Types.TYPE_DOUBLE, Double.valueOf(3000)));
//		assertEquals(ValueNumber.buildNumber(Types.TYPE_BOOLEAN, (long) 3000), ValueBoolean.buildBoolean(true));
//		assertEquals(ValueNumber.buildNumber(Types.TYPE_BOOLEAN, (long) 0), ValueBoolean.buildBoolean(false));
		
		
		//buildNumber(Types type, BigDecimal num)
		assertEquals(ValueNumber.buildNumber(Types.TYPE_INT, (BigDecimal) null), Value.nullOf(Types.TYPE_INT));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_ULONG, new BigDecimal("30000")),  new ValueNumberObject(Types.TYPE_ULONG, new BigDecimal("30000").unscaledValue()));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_DECIMAL, new BigDecimal("30000")),  new ValueNumberObject(Types.TYPE_DECIMAL, new BigDecimal("30000")));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_BOOLEAN, new BigDecimal("30000")), ValueBoolean.buildBoolean(!new BigDecimal("30000").equals(BigDecimal.ZERO)));

		
		//buildNumber(Types type, BigInteger num)
		assertEquals(ValueNumber.buildNumber(Types.TYPE_INT, (BigInteger) null), Value.nullOf(Types.TYPE_INT));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_ULONG, new BigInteger("30000")),  new ValueNumberObject(Types.TYPE_ULONG, new BigInteger("30000")));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_DECIMAL, new BigInteger("30000")),  new ValueNumberObject(Types.TYPE_DECIMAL, new BigInteger("30000")));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_BOOLEAN, new BigInteger("30000")), ValueBoolean.buildBoolean(!new BigInteger("30000").equals(BigInteger.ZERO)));

		
		
		
		assertTrue(ValueNumber.isIntegerString("30000"));
		assertTrue(ValueNumber.isDecimalString("50000.0"));
		
	}
	
	@Test
	public void valueNumberObjectTest() {
		BigDecimal value1 = new BigDecimal("50000");
		BigDecimal value2 = new BigDecimal("40000");
		ValueNumberObject valObj = new ValueNumberObject(Types.TYPE_DECIMAL,value1);
		ValueNumberObject valObj2 = new ValueNumberObject(Types.TYPE_DECIMAL,value2);
		
//		assertEquals(valObj.compareTo(null), 1);
		assertEquals(valObj.compareTo(valObj2), ((BigDecimal)value1).compareTo(((ValueNumberObject) valObj2).decimalValue()));

	}
}
