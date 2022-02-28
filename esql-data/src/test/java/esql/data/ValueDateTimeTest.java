package esql.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoLocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class ValueDateTimeTest {

	@Test
	public void valueLocalDateTest() {
		ValueLocalDate valueLocalDate = new ValueLocalDate(LocalDate.now());
		ValueLocalDate valueLocalDate2 = new ValueLocalDate(LocalDate.of(2020, 11, 20));


		ValueLocalDateTime valueLocalDateTime = new ValueLocalDateTime(LocalDateTime.of(2020, 12, 20, 10, 30));
		ValueLocalDateTime valueLocalDateTime2 = new ValueLocalDateTime(LocalDateTime.now());

		ValueTimestamp valueTimestamp = new ValueTimestamp(Instant.now());



		Value valueTime = new ValueLocalTime(LocalTime.now());

		Value valueNumber = new ValueNumberLong(5000l);

		/*Test
		 * comparedTo() function
		 * 
		 * */
		assertThrows(IllegalArgumentException.class, new Executable() {

			@Override
			public void execute() throws Throwable {
				valueLocalDate.compareTo(valueTime);
			}
		});

		assertEquals(valueLocalDate.compareTo(valueLocalDate2), 2);
		assertEquals(valueLocalDate2.compareTo(valueLocalDate), -2);
		assertEquals(valueLocalDateTime2.compareTo(valueLocalDate), 1);
		assertEquals(valueLocalDate.compareTo(valueLocalDateTime2), -1);
		assertEquals(valueLocalDate.compareTo(valueTimestamp), -1);
		assertEquals(valueLocalDate.compareTo(valueLocalDateTime2), -1);
		assertEquals(valueLocalDate.compareTo(valueNumber), 1);


		/*Test
		 * convertTo() function
		 * 
		 * */
		LocalDate value = (LocalDate) valueLocalDate.toTemporal();
		
		assertEquals(valueLocalDate.convertTo(Types.TYPE_DATE), valueLocalDate);
		assertEquals(valueLocalDate.convertTo(Types.TYPE_DATETIME), ValueDateTime.buildDateTime(((LocalDate) valueLocalDate.toTemporal()).atStartOfDay()));
		assertEquals(valueLocalDate.convertTo(Types.TYPE_TIMESTAMP), ValueDateTime.buildDateTime(((LocalDate) valueLocalDate.toTemporal()).atStartOfDay(ZoneId.systemDefault())));
		assertEquals(valueLocalDate.convertTo(Types.TYPE_INT),ValueNumber.buildNumber(Types.TYPE_INT,((LocalDate) valueLocalDate.toTemporal()).toEpochDay() * ValueDateTime.MILLISECONDS_PER_DAY));
		assertEquals(valueLocalDate.convertTo(Types.TYPE_UINT),ValueNumber.buildNumber(Types.TYPE_UINT,((LocalDate) valueLocalDate.toTemporal()).toEpochDay() * ValueDateTime.MILLISECONDS_PER_DAY));
		assertEquals(valueLocalDate.convertTo(Types.TYPE_LONG),ValueNumber.buildNumber(Types.TYPE_LONG,((LocalDate) valueLocalDate.toTemporal()).toEpochDay() * ValueDateTime.MILLISECONDS_PER_DAY));
		assertEquals(valueLocalDate.convertTo(Types.TYPE_ULONG),ValueNumber.buildNumber(Types.TYPE_ULONG,((LocalDate) valueLocalDate.toTemporal()).toEpochDay() * ValueDateTime.MILLISECONDS_PER_DAY));
		assertEquals(valueLocalDate.convertTo(Types.TYPE_DECIMAL),ValueNumber.buildNumber(Types.TYPE_DECIMAL,((LocalDate) valueLocalDate.toTemporal()).toEpochDay() * ValueDateTime.MILLISECONDS_PER_DAY));
		assertEquals(valueLocalDate.convertTo(Types.TYPE_FLOAT),ValueNumber.buildNumber(Types.TYPE_FLOAT,((LocalDate) valueLocalDate.toTemporal()).toEpochDay() * ValueDateTime.MILLISECONDS_PER_DAY));
		assertEquals(valueLocalDate.convertTo(Types.TYPE_DOUBLE),ValueNumber.buildNumber(Types.TYPE_DOUBLE,((LocalDate) valueLocalDate.toTemporal()).toEpochDay() * ValueDateTime.MILLISECONDS_PER_DAY));
		assertEquals(valueLocalDate.convertTo(Types.TYPE_STRING),ValueString.buildString(Types.TYPE_STRING, value.toString()));
		assertEquals(valueLocalDate.convertTo(Types.TYPE_NSTRING),ValueString.buildString(Types.TYPE_STRING, value.toString()));
		assertEquals(valueLocalDate.convertTo(Types.TYPE_BYTES), ValueBytes.buildBytes(value.toString().getBytes(StandardCharsets.ISO_8859_1)));


	}

	@Test
	public void valueLocalDateTimeTest() {
		ValueLocalDate valueLocalDate = new ValueLocalDate(LocalDate.now());
		ValueLocalDate valueLocalDate2 = new ValueLocalDate(LocalDate.of(2020, 11, 20));


		ValueLocalDateTime valueLocalDateTime = new ValueLocalDateTime(LocalDateTime.now());
		ValueLocalDateTime valueLocalDateTime2 = new ValueLocalDateTime(LocalDateTime.of(2020, 12, 20, 10, 30));

		ValueTimestamp valueTimestamp = new ValueTimestamp(Instant.now());
		ValueTimestampWithTZ valueTimestampWithTZ = new ValueTimestampWithTZ(ZonedDateTime.of(LocalDateTime.now(), ZoneId.of("Asia/Ho_Chi_Minh")));



		Value valueTime = new ValueLocalTime(LocalTime.now());

		Value valueNumber = new ValueNumberLong(5000l);


		/*Test
		 * comparedTo() function
		 * 
		 * */
		assertThrows(IllegalArgumentException.class, new Executable() {

			@Override
			public void execute() throws Throwable {
				valueLocalDateTime.compareTo(valueTime);
			}
		});
		assertEquals(valueLocalDateTime.compareTo(valueLocalDate), 1);
		assertEquals(valueLocalDateTime.compareTo(valueLocalDateTime2), 2);
		assertEquals(valueLocalDateTime.compareTo(valueTimestamp), 0);
		assertEquals(valueLocalDateTime.compareTo(valueTimestampWithTZ), -6);
		assertEquals(valueLocalDateTime.compareTo(valueNumber), 1);



		/*Test
		 * convertTo() function
		 * */
		LocalDateTime value = (LocalDateTime) valueLocalDateTime.toTemporal();
		assertEquals(valueLocalDateTime.convertTo(Types.TYPE_DATE),ValueDateTime.buildDateTime(((LocalDateTime) valueLocalDateTime.toTemporal()).toLocalDate()));
		assertEquals(valueLocalDateTime.convertTo(Types.TYPE_DATETIME),valueLocalDateTime);
		assertEquals(valueLocalDateTime.convertTo(Types.TYPE_TIMESTAMP),ValueDateTime.buildDateTime(((LocalDateTime) valueLocalDateTime.toTemporal()).atZone(ZoneId.systemDefault())));
		assertEquals(valueLocalDateTime.convertTo(Types.TYPE_INT), ValueNumber.buildNumber(Types.TYPE_INT, value.toEpochSecond(ZoneId.systemDefault().getRules().getOffset(value)) * 1000) );
		assertEquals(valueLocalDateTime.convertTo(Types.TYPE_STRING),ValueString.buildString(Types.TYPE_STRING, valueLocalDateTime.stringValue()));
		assertEquals(valueLocalDateTime.convertTo(Types.TYPE_BYTES), ValueBytes.buildBytes(value.toString().getBytes(StandardCharsets.ISO_8859_1)));

	}

	@Test
	public void valueTimestampTest() {
		ValueTimestamp valueTimestamp = new ValueTimestamp(Instant.now());
		ValueTimestamp valueTimestamp2 = new ValueTimestamp(Instant.now());

		Value valueTime = new ValueLocalTime(LocalTime.now());

		ValueLocalDate valueLocalDate = new ValueLocalDate(LocalDate.of(2020, 11, 20));

		ValueLocalDateTime valueLocalDateTime = new ValueLocalDateTime(LocalDateTime.of(2020,11, 28, 12, 35));

		ValueTimestampWithTZ valueTimestampWithTZ = new ValueTimestampWithTZ(ZonedDateTime.of(LocalDateTime.now(), ZoneId.of("Asia/Ho_Chi_Minh")));

		Value valueNumber = new ValueNumberLong(5000l);

		Value valueString = new ValueString(false, "26/11/1995");


		/*Test
		 * comparedTo() function
		 * 
		 * */
		assertThrows(IllegalArgumentException.class, new Executable() {

			@Override
			public void execute() throws Throwable {
				valueTimestamp.compareTo(valueTime);
			}
		});

		assertEquals(valueTimestamp.compareTo(valueTimestamp2),0);
		assertEquals(valueTimestamp.compareTo(valueLocalDate), 1);
		assertEquals(valueTimestamp.compareTo(valueLocalDateTime),1);
		assertEquals(valueTimestamp.compareTo(valueTimestampWithTZ),0);
		assertEquals(valueTimestamp.compareTo(valueNumber), 1);
		assertEquals(valueTimestamp.compareTo(valueString),-6);



		/*Test
		 * converTo()	function	
		 *  * */
		Instant value = (Instant) valueTimestamp.toTemporal();
		assertEquals(valueTimestamp.convertTo(Types.TYPE_TIME), ValueDateTime.buildDateTime(value.atZone(ZoneId.systemDefault()).toLocalTime()));
		assertEquals(valueTimestamp.convertTo(Types.TYPE_DATE),  ValueDateTime.buildDateTime(value.atZone(ZoneId.systemDefault()).toLocalDate()));
		assertEquals(valueTimestamp.convertTo(Types.TYPE_DATETIME),  ValueDateTime.buildDateTime(value.atZone(ZoneId.systemDefault()).toLocalDateTime()));
		assertEquals(valueTimestamp.convertTo(Types.TYPE_TIMESTAMP),  valueTimestamp);

		assertEquals(valueTimestamp.convertTo(Types.TYPE_INT), ValueNumber.buildNumber(Types.TYPE_INT, value.getEpochSecond() * 1000));
		assertEquals(valueTimestamp.convertTo(Types.TYPE_UINT), ValueNumber.buildNumber(Types.TYPE_UINT, value.getEpochSecond() * 1000));
		assertEquals(valueTimestamp.convertTo(Types.TYPE_LONG), ValueNumber.buildNumber(Types.TYPE_LONG, value.getEpochSecond() * 1000));
		assertEquals(valueTimestamp.convertTo(Types.TYPE_ULONG), ValueNumber.buildNumber(Types.TYPE_ULONG, value.getEpochSecond() * 1000));
		assertEquals(valueTimestamp.convertTo(Types.TYPE_DECIMAL), ValueNumber.buildNumber(Types.TYPE_DECIMAL, value.getEpochSecond() * 1000));
		assertEquals(valueTimestamp.convertTo(Types.TYPE_FLOAT), ValueNumber.buildNumber(Types.TYPE_FLOAT, value.getEpochSecond() * 1000));
		assertEquals(valueTimestamp.convertTo(Types.TYPE_DOUBLE), ValueNumber.buildNumber(Types.TYPE_DOUBLE, value.getEpochSecond() * 1000));

		assertEquals(valueTimestamp.convertTo(Types.TYPE_STRING), ValueString.buildString(Types.TYPE_STRING, value.toString()));
		assertEquals(valueTimestamp.convertTo(Types.TYPE_NSTRING), ValueString.buildString(Types.TYPE_NSTRING, value.toString()));

		assertEquals(valueTimestamp.convertTo(Types.TYPE_BYTES), ValueBytes.buildBytes(value.toString().getBytes(StandardCharsets.ISO_8859_1)));
	}

	@Test
	public void valueTimestampWithTZ() {
		ValueTimestampWithTZ valueTimestampWithTZ = new ValueTimestampWithTZ(ZonedDateTime.of(LocalDateTime.now(), ZoneId.systemDefault()));
		ValueTimestampWithTZ valueTimestampWithTZ2 = new ValueTimestampWithTZ(ZonedDateTime.of(LocalDateTime.now(), ZoneId.systemDefault()));


		ValueTimestamp valueTimestamp = new ValueTimestamp(Instant.now());

		ValueLocalDateTime valueLocalDateTime = new ValueLocalDateTime(LocalDateTime.of(2020,11, 28, 12, 35));

		ValueLocalDate valueLocalDate = new ValueLocalDate(LocalDate.of(2020, 11, 20));

		Value valueNumber = new ValueNumberLong(5000l);

		Value valueString = new ValueString(false, "26/11/1995");

		/*Test
		 * compareTo() function
		 * */

		assertEquals(valueTimestampWithTZ.compareTo(null), 1);
		assertEquals(valueTimestampWithTZ.compareTo(valueTimestampWithTZ2),0);
		assertEquals(valueTimestampWithTZ.compareTo(valueTimestamp), 0);
		assertEquals(valueTimestampWithTZ.compareTo(valueLocalDate), 1);
		assertEquals(valueTimestampWithTZ.compareTo(valueLocalDateTime), 1);
		assertEquals(valueTimestampWithTZ.compareTo(valueNumber), 1);
		assertEquals(valueTimestampWithTZ.compareTo(valueString), -6);


		/*Test
		 * convertTo() function
		 * 
		 * */
		ZonedDateTime value = (ZonedDateTime) valueTimestampWithTZ.toTemporal();
		assertEquals(valueTimestampWithTZ.convertTo(Types.TYPE_TIME), ValueDateTime.buildDateTime(value.toLocalTime()));
		assertEquals(valueTimestampWithTZ.convertTo(Types.TYPE_DATE), ValueDateTime.buildDateTime(value.toLocalDate()));
		assertEquals(valueTimestampWithTZ.convertTo(Types.TYPE_DATETIME), ValueDateTime.buildDateTime(value.toLocalDateTime()));
		assertEquals(valueTimestampWithTZ.convertTo(Types.TYPE_TIMESTAMP), valueTimestampWithTZ);

		assertEquals(valueTimestampWithTZ.convertTo(Types.TYPE_INT), ValueNumber.buildNumber(Types.TYPE_INT, value.toEpochSecond() * 1000));
		assertEquals(valueTimestampWithTZ.convertTo(Types.TYPE_UINT), ValueNumber.buildNumber(Types.TYPE_UINT, value.toEpochSecond() * 1000));
		assertEquals(valueTimestampWithTZ.convertTo(Types.TYPE_LONG), ValueNumber.buildNumber(Types.TYPE_LONG, value.toEpochSecond() * 1000));
		assertEquals(valueTimestampWithTZ.convertTo(Types.TYPE_ULONG), ValueNumber.buildNumber(Types.TYPE_ULONG, value.toEpochSecond() * 1000));
		assertEquals(valueTimestampWithTZ.convertTo(Types.TYPE_DECIMAL), ValueNumber.buildNumber(Types.TYPE_DECIMAL, value.toEpochSecond() * 1000));
		assertEquals(valueTimestampWithTZ.convertTo(Types.TYPE_FLOAT), ValueNumber.buildNumber(Types.TYPE_FLOAT, value.toEpochSecond() * 1000));
		assertEquals(valueTimestampWithTZ.convertTo(Types.TYPE_DOUBLE), ValueNumber.buildNumber(Types.TYPE_DOUBLE, value.toEpochSecond() * 1000));


		assertEquals(valueTimestampWithTZ.convertTo(Types.TYPE_STRING), ValueString.buildString(Types.TYPE_STRING, value.toString()));
		assertEquals(valueTimestampWithTZ.convertTo(Types.TYPE_NSTRING), ValueString.buildString(Types.TYPE_NSTRING, value.toString()));

		assertEquals(valueTimestampWithTZ.convertTo(Types.TYPE_BYTES), ValueBytes.buildBytes(value.toString().getBytes(StandardCharsets.ISO_8859_1)));

	}

	@Test
	public void valueLocalTimeTest() {
		ValueLocalTime valueLocalTime = new ValueLocalTime(LocalTime.now());
		ValueLocalTime valueLocalTime2 = new ValueLocalTime(LocalTime.now());

		ValueLocalDate valueLocalDate = new ValueLocalDate(LocalDate.now());

		ValueNumber valueNumber = new ValueNumberLong(50000L);

		ValueString valueString = new ValueString(false,"20:10:30");

		assertThrows(IllegalArgumentException.class, new Executable() {

			@Override
			public void execute() throws Throwable {
				valueLocalTime.compareTo(valueLocalDate);
			}
		});

		assertEquals(valueLocalTime.compareTo(valueLocalTime2), 0);
		assertEquals(valueLocalTime.compareTo(valueNumber), 1);
		assertEquals(valueLocalTime.compareTo(valueString),-1);


		LocalTime value = (LocalTime) valueLocalTime.toTemporal();

		assertEquals(valueLocalTime.convertTo(Types.TYPE_TIME), valueLocalTime);


		assertEquals(valueLocalTime.convertTo(Types.TYPE_INT), ValueNumber.buildNumber(Types.TYPE_INT, value.toSecondOfDay() * 1000));
		assertEquals(valueLocalTime.convertTo(Types.TYPE_UINT), ValueNumber.buildNumber(Types.TYPE_UINT, value.toSecondOfDay() * 1000));
		assertEquals(valueLocalTime.convertTo(Types.TYPE_LONG), ValueNumber.buildNumber(Types.TYPE_LONG, value.toSecondOfDay() * 1000));
		assertEquals(valueLocalTime.convertTo(Types.TYPE_ULONG), ValueNumber.buildNumber(Types.TYPE_ULONG, value.toSecondOfDay() * 1000));
		assertEquals(valueLocalTime.convertTo(Types.TYPE_DECIMAL), ValueNumber.buildNumber(Types.TYPE_DECIMAL, value.toSecondOfDay() * 1000));
		assertEquals(valueLocalTime.convertTo(Types.TYPE_FLOAT), ValueNumber.buildNumber(Types.TYPE_FLOAT, value.toSecondOfDay() * 1000));
		assertEquals(valueLocalTime.convertTo(Types.TYPE_DOUBLE), ValueNumber.buildNumber(Types.TYPE_DOUBLE, value.toSecondOfDay() * 1000));

		assertEquals(valueLocalTime.convertTo(Types.TYPE_STRING), ValueString.buildString(Types.TYPE_STRING, value.toString()));
		assertEquals(valueLocalTime.convertTo(Types.TYPE_NSTRING), ValueString.buildString(Types.TYPE_NSTRING, value.toString()));

		assertEquals(valueLocalTime.convertTo(Types.TYPE_BYTES), ValueBytes.buildBytes(value.toString().getBytes(StandardCharsets.ISO_8859_1)));


	}
}
