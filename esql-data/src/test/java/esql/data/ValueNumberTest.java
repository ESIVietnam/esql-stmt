package esql.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;


public class ValueNumberTest {

	@Test
	public void valueNumberTest() {


		//------------------------buildNumber(Types type, double num)--------------------------------------
		assertEquals(ValueNumber.buildNumber(Types.TYPE_INT, (double) 0), new ValueNumberInt(Types.TYPE_INT,(int) 0));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_LONG, (double) 0), new ValueNumberInt(Types.TYPE_LONG,(int) 0));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_INT, (long) 0), new ValueNumberInt(Types.TYPE_INT,(int) 0));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_LONG, (long) 0), new ValueNumberInt(Types.TYPE_INT,(int) 0));

		assertEquals(ValueNumber.buildNumber(Types.TYPE_INT, (double) 1), new ValueNumberInt(Types.TYPE_INT,(int) 1));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_LONG, (double) 1), new ValueNumberInt(Types.TYPE_LONG,(int) 1));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_INT, (long) 1), new ValueNumberInt(Types.TYPE_INT,(int) 1));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_LONG, (long) 1), new ValueNumberInt(Types.TYPE_INT,(int) 1));

		assertEquals(ValueNumber.buildNumber(Types.TYPE_INT, (double) 2), new ValueNumberInt(Types.TYPE_INT,(int) 2));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_LONG, (double) 2), new ValueNumberInt(Types.TYPE_LONG,(int) 2));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_INT, (long) 2), new ValueNumberInt(Types.TYPE_INT,(int) 2));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_LONG, (long) 2), new ValueNumberInt(Types.TYPE_INT,(int) 2));

		assertEquals(ValueNumber.buildNumber(Types.TYPE_INT, (double) 10), new ValueNumberInt(Types.TYPE_INT,(int) 10));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_LONG, (double) 10), new ValueNumberInt(Types.TYPE_LONG,(int) 10));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_INT, (long) 10), new ValueNumberInt(Types.TYPE_INT,(int) 10));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_LONG, (long) 10), new ValueNumberInt(Types.TYPE_INT,(int) 10));

		assertEquals(ValueNumber.buildNumber(Types.TYPE_INT, (double) 0), new ValueNumberInt(Types.TYPE_INT,(int) 0));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_LONG, (double) 0), new ValueNumberInt(Types.TYPE_LONG,(int) 0));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_INT, (long) 0), new ValueNumberInt(Types.TYPE_INT,(int) 0));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_LONG, (long) 0), new ValueNumberInt(Types.TYPE_INT,(int) 0));

		assertEquals(ValueNumber.buildNumber(Types.TYPE_INT, (double) 3000), new ValueNumberInt(Types.TYPE_INT,(int) 3000));

		assertEquals(ValueNumber.buildNumber(Types.TYPE_UBYTE, (double) 3000), new ValueNumberInt(Types.TYPE_UBYTE,(int) 3000));



		assertEquals(ValueNumber.buildNumber(Types.TYPE_SHORT, (double) 3000), new ValueNumberInt(Types.TYPE_SHORT,(int) 3000));

		assertEquals(ValueNumber.buildNumber(Types.TYPE_USHORT, (double) 3000), new ValueNumberInt(Types.TYPE_USHORT,(int) 3000));
		


		assertEquals(ValueNumber.buildNumber(Types.TYPE_UINT, (double) 3000), new ValueNumberUInt((long) 3000));

		ValueNumberUInt uintValue = new ValueNumberUInt((long) 3000);
		assertEquals(Types.TYPE_UINT, uintValue.getType());

		assertEquals(ValueNumber.buildNumber(Types.TYPE_LONG, (double) 3000), new ValueNumberLong((long) 3000));
//		assertEquals(ValueNumber.buildNumber(Types.TYPE_ULONG, (double) 3000),new ValueNumberObject(Types.TYPE_ULONG, new BigInteger(String.valueOf(3000))));

		assertEquals(ValueNumber.buildNumber(Types.TYPE_DECIMAL, (double) 3000), new ValueNumberObject(Types.TYPE_DECIMAL, BigDecimal.valueOf((double)3000)));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_FLOAT, (double) 3000),new ValueNumberObject(Types.TYPE_FLOAT, Float.valueOf((float) 3000)));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_DOUBLE, (double) 3000),new ValueNumberObject(Types.TYPE_DOUBLE, Double.valueOf(3000)));
		assertThrows(IllegalArgumentException.class, new Executable() {

			@Override
			public void execute() throws Throwable {
				ValueNumber.buildNumber(Types.TYPE_BYTES, (double) 3000);

			}
		});



		//------------------------buildNumber(Types type, long num)--------------------------------------
		assertEquals(ValueNumber.buildNumber(Types.TYPE_INT, (long) 3000), new ValueNumberInt(Types.TYPE_INT,(int) 3000));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_UBYTE, (long) 3000), new ValueNumberInt(Types.TYPE_UBYTE,(int) 3000));



		assertEquals(ValueNumber.buildNumber(Types.TYPE_SHORT, (long) 3000), new ValueNumberInt(Types.TYPE_SHORT,(int) 3000));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_USHORT, (long) 3000), new ValueNumberInt(Types.TYPE_USHORT,(int) 3000));


		assertEquals(ValueNumber.buildNumber(Types.TYPE_UINT, (long) 3000), new ValueNumberUInt(3000));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_LONG, (long) 3000), new ValueNumberLong(3000));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_ULONG, (long) 3000), new ValueNumberObject(Types.TYPE_ULONG, BigInteger.valueOf(3000)));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_DECIMAL, (long) 3000), new ValueNumberObject(Types.TYPE_DECIMAL, BigDecimal.valueOf(3000)));

		assertEquals(ValueNumber.buildNumber(Types.TYPE_FLOAT, (long) 3000),new ValueNumberObject(Types.TYPE_FLOAT, Float.valueOf(3000)));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_DOUBLE, (long) 3000),new ValueNumberObject(Types.TYPE_DOUBLE, Double.valueOf(3000)));


		assertThrows(IllegalArgumentException.class, new Executable() {

			@Override
			public void execute() throws Throwable {
				ValueNumber.buildNumber(Types.TYPE_DATE, (long) 3000);

			}
		});



		//--------------------------------------buildNumber(Types type, BigDecimal num)--------------------------------------------------------
		assertEquals(ValueNumber.buildNumber(Types.TYPE_INT, (BigDecimal) null), Value.nullOf(Types.TYPE_INT));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_INT, new BigDecimal("30000")),  new ValueNumberObject(Types.TYPE_INT, new BigDecimal("30000").unscaledValue()));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_ULONG, new BigDecimal("30000")),  new ValueNumberObject(Types.TYPE_ULONG, new BigDecimal("30000").unscaledValue()));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_DECIMAL, new BigDecimal("30000")),  new ValueNumberObject(Types.TYPE_DECIMAL, new BigDecimal("30000")));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_BOOLEAN, new BigDecimal("30000")), ValueBoolean.buildBoolean(!new BigDecimal("30000").equals(BigDecimal.ZERO)));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_DOUBLE, new BigDecimal("30000")),  new ValueNumberObject(Types.TYPE_DOUBLE, new BigDecimal("30000")));

		//buildNumber(Types type, BigInteger num)
		assertEquals(ValueNumber.buildNumber(Types.TYPE_INT, (BigInteger) null), Value.nullOf(Types.TYPE_INT));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_INT, new BigInteger("30000")),  new ValueNumberObject(Types.TYPE_INT, new BigInteger("30000")));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_ULONG, new BigInteger("30000")),  new ValueNumberObject(Types.TYPE_ULONG, new BigInteger("30000")));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_DECIMAL, new BigInteger("30000")),  new ValueNumberObject(Types.TYPE_DECIMAL, new BigInteger("30000")));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_BOOLEAN, new BigInteger("30000")), ValueBoolean.buildBoolean(!new BigInteger("30000").equals(BigInteger.ZERO)));


	}

	@Test
	public void valueNumberTestPool() {
		System.setProperty("ESQL_INTEGER_USING_POOL", "true");

		assertEquals(ValueNumber.buildNumber(Types.TYPE_BOOLEAN, 1), ValueBoolean.buildBoolean(true));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_BOOLEAN, (double) 0), ValueBoolean.buildBoolean(false));

		assertEquals(ValueNumber.buildNumber(Types.TYPE_BYTE, 1), ValueNumberInt.buildNumber(Types.TYPE_INT, 1));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_BYTE, 0), ValueNumberInt.buildNumber(Types.TYPE_INT, 0));

		//------------------------buildNumber(Types type, double num)--------------------------------------
		assertEquals(ValueNumber.buildNumber(Types.TYPE_INT, (double) 0), new ValueNumberInt(Types.TYPE_INT,(int) 0));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_LONG, (double) 0), new ValueNumberInt(Types.TYPE_LONG,(int) 0));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_INT, (long) 0), new ValueNumberInt(Types.TYPE_INT,(int) 0));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_LONG, (long) 0), new ValueNumberInt(Types.TYPE_INT,(int) 0));

		assertEquals(ValueNumber.buildNumber(Types.TYPE_INT, (double) 1), new ValueNumberInt(Types.TYPE_INT,(int) 1));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_LONG, (double) 1), new ValueNumberInt(Types.TYPE_LONG,(int) 1));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_INT, (long) 1), new ValueNumberInt(Types.TYPE_INT,(int) 1));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_LONG, (long) 1), new ValueNumberInt(Types.TYPE_INT,(int) 1));

		assertEquals(ValueNumber.buildNumber(Types.TYPE_INT, (double) 2), new ValueNumberInt(Types.TYPE_INT,(int) 2));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_LONG, (double) 2), new ValueNumberInt(Types.TYPE_LONG,(int) 2));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_INT, (long) 2), new ValueNumberInt(Types.TYPE_INT,(int) 2));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_LONG, (long) 2), new ValueNumberInt(Types.TYPE_INT,(int) 2));

		assertEquals(ValueNumber.buildNumber(Types.TYPE_INT, (double) 10), new ValueNumberInt(Types.TYPE_INT,(int) 10));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_LONG, (double) 10), new ValueNumberInt(Types.TYPE_LONG,(int) 10));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_INT, (long) 10), new ValueNumberInt(Types.TYPE_INT,(int) 10));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_LONG, (long) 10), new ValueNumberInt(Types.TYPE_INT,(int) 10));

		assertEquals(ValueNumber.buildNumber(Types.TYPE_DECIMAL, (double) 1), new ValueNumberInt(Types.TYPE_DECIMAL,(int) 1));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_ULONG, (double) 1), new ValueNumberInt(Types.TYPE_ULONG,(int) 1));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_DECIMAL, (long) 1), new ValueNumberInt(Types.TYPE_DECIMAL,(int) 1));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_ULONG, (long) 1), new ValueNumberInt(Types.TYPE_ULONG,(int) 1));

		assertEquals(ValueNumber.buildNumber(Types.TYPE_DECIMAL, (double) 2), new ValueNumberInt(Types.TYPE_DECIMAL,(int) 2));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_ULONG, (double) 2), new ValueNumberInt(Types.TYPE_ULONG,(int) 2));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_DECIMAL, (long) 2), new ValueNumberInt(Types.TYPE_DECIMAL,(int) 2));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_ULONG, (long) 2), new ValueNumberInt(Types.TYPE_ULONG,(int) 2));

		assertEquals(ValueNumber.buildNumber(Types.TYPE_DECIMAL, BigDecimal.valueOf(2)), new ValueNumberInt(Types.TYPE_DECIMAL,(int) 2));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_DECIMAL, BigInteger.valueOf(2)), new ValueNumberInt(Types.TYPE_DECIMAL,(int) 2));

		assertEquals(ValueNumber.buildNumber(Types.TYPE_DECIMAL, BigDecimal.valueOf(10)), new ValueNumberInt(Types.TYPE_DECIMAL,(int) 10));
		assertEquals(ValueNumber.buildNumber(Types.TYPE_DECIMAL, BigInteger.valueOf(10)), new ValueNumberInt(Types.TYPE_DECIMAL,(int) 10));

		assertEquals(ValueNumber.buildNumber(Types.TYPE_ULONG, (double) 99), new ValueNumberInt(Types.TYPE_ULONG,(int) 99));
		System.setProperty("ESQL_INTEGER_USING_POOL", "false");
	}

	@Test
	public void valueNumberObjectTest() {
		BigDecimal value1 = new BigDecimal("50000");
		BigDecimal value2 = new BigDecimal("40000");

		ValueNumber valueTypeString = new ValueNumberObject(Types.TYPE_STRING, new BigInteger("50000"));
		ValueNumber valueTypeBoolean = new ValueNumberObject(Types.TYPE_BOOLEAN, new BigInteger("50000"));
		ValueNumber valueTypeNumberBigInt = new ValueNumberObject(Types.TYPE_LONG, new BigInteger("50000"));


		/*Test
		 * compareTo() function
		 * */
		assertThrows(NullPointerException.class, new Executable() {

			@Override
			public void execute() throws Throwable {
				valueTypeNumberBigInt.compareTo(null);
			}
		});


		assertEquals(valueTypeNumberBigInt.compareTo(valueTypeNumberBigInt), 0);

		/*Test
		 * convertTo() function
		 * 
		 * */
		assertEquals(valueTypeString.convertTo(Types.TYPE_STRING), ValueString.buildString(Types.TYPE_STRING, new BigInteger("50000").toString()));
		assertEquals(valueTypeNumberBigInt.convertTo(Types.TYPE_BOOLEAN), ValueBoolean.buildBoolean(valueTypeNumberBigInt.isTrue()));
		assertEquals(valueTypeNumberBigInt.convertTo(Types.TYPE_DOUBLE), ValueNumber.buildNumber(Types.TYPE_DOUBLE, new BigInteger("50000")));
		assertThrows(UnsupportedOperationException.class, new Executable() {

			@Override
			public void execute() throws Throwable {
				valueTypeNumberBigInt.convertTo(Types.TYPE_DATETIME);
			}
		});

	}

	@Test
	public void valueNumberLongTest() {
		ValueNumberLong valueLong = new ValueNumberLong(123456L);
		ValueNumberLong valueLong2 = new ValueNumberLong(1234567L);


		ValueNumber valueNumber = new ValueNumberInt(Types.TYPE_INT, 1234567);


		Value valueToTest = new ValueNumberObject(Types.TYPE_ULONG, new BigInteger("3000"));
		Value valueToTest2 = new ValueNumberObject(Types.TYPE_LONG, new BigInteger("3000"));
		Value valueToTest3 = new ValueNumberObject(Types.TYPE_STRING, new BigInteger("3000"));

		/*Test
		 * convertTo() function
		 * */
		assertEquals(valueLong.convertTo(Types.TYPE_STRING), ValueString.buildString(Types.TYPE_STRING, String.valueOf(123456L)));
		assertEquals(valueLong.convertTo(Types.TYPE_BOOLEAN), ValueBoolean.buildBoolean(valueLong.isTrue()));
		assertEquals(valueLong.convertTo(Types.TYPE_INT), ValueNumber.buildNumber(Types.TYPE_INT, 123456L));
		assertThrows(UnsupportedOperationException.class, new Executable() {

			@Override
			public void execute() throws Throwable {
				valueLong.convertTo(Types.TYPE_DATETIME);
			}
		});

		/*Test
		 * compareTo() function
		 * */
		assertEquals(valueLong.compareTo(null), 1);
		assertEquals(valueLong.compareTo(valueToTest), -((ValueNumberObject) valueToTest).bigIntValue().compareTo(BigInteger.valueOf(123456L)));
		assertEquals(valueLong.compareTo(valueToTest2), Long.compare(123456L, ((ValueNumber)valueToTest2).longValue()));
		assertEquals(valueLong.compareTo(valueToTest3), -1);


		/*Test
		 *  equal() function
		 * */
		assertTrue(!valueLong.equals(null),"return false when obj which is compared is null");
		assertTrue(valueLong.equals(valueLong),"Two the same Object must return true");
		assertTrue(!valueLong.equals(valueLong2),"true if 2 value is equal else it is false");
		assertTrue(!valueLong.equals(valueToTest3),"true if 2 string value is equal else it is false");


	}

	@Test
	public void valueNumberIntTest() {

		ValueNumberInt valueInt = new ValueNumberInt(Types.TYPE_LONG, 50000);
		ValueNumberInt valueInt2 = new ValueNumberInt(Types.TYPE_INT, 50000);
		ValueNumber valueNumber = new ValueNumberInt(Types.TYPE_INT, 50000);
		ValueNumber valueNumber2 = new ValueNumberInt(Types.TYPE_BYTE, 50000);


		Value valueString = new ValueString(false, "50000");
		Value valueULong = new ValueNumberObject(Types.TYPE_ULONG, new BigInteger("50000"));




		/*Test
		 *  convertTo() function
		 * */
		assertEquals(valueInt.convertTo(Types.TYPE_STRING), ValueString.buildString(Types.TYPE_STRING, String.valueOf((int)50000)));
		assertEquals(valueInt.convertTo(Types.TYPE_BOOLEAN), ValueBoolean.buildBoolean(valueInt.isTrue()));
		assertEquals(valueInt.convertTo(Types.TYPE_INT), ValueNumber.buildNumber(Types.TYPE_INT, 50000));
		assertNotEquals(valueNumber2.convertTo(Types.TYPE_STRING), ValueString.buildString(Types.TYPE_STRING, String.valueOf((int)50000)));


		/*Test
		 *  compareTo() function
		 * */
		assertEquals(valueInt.compareTo(null), 1);
		assertEquals(valueInt.compareTo(valueULong), -((ValueNumberObject) valueULong).bigIntValue().compareTo(BigInteger.valueOf(50000)));


		/*Test
		 *  equal() function
		 * */
		assertTrue(!valueInt.equals(null),"return false if obj is null");
		assertTrue(valueInt.equals(valueInt),"Two the same Object must return true");
		assertTrue(valueInt.equals(valueInt2),"true if 2 value is equal else it is false");
		assertTrue(valueInt.equals(valueNumber),"true if 2 value is equal else it is false");
		assertEquals(valueInt.equals(valueString), !((Value) valueString).isNull() && String.valueOf(50000).equals(((Value) valueString).stringValue()));
		assertTrue(!valueInt.equals(valueNumber2),"true if 2 value is equal else it is false");
	}
}
