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

		assertTrue(0 < valueLocalDate.compareTo(valueLocalDate2));
		assertTrue(0 > valueLocalDate2.compareTo(valueLocalDate));
		assertTrue(0 < valueLocalDateTime2.compareTo(valueLocalDate));
		assertTrue(0 > valueLocalDate.compareTo(valueLocalDateTime2));
		assertTrue(0 > valueLocalDate.compareTo(valueTimestamp));
		assertTrue(0 > valueLocalDate.compareTo(valueLocalDateTime2));
		assertTrue(0 < valueLocalDate.compareTo(valueNumber));


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
		Instant now = Instant.now();
		ValueLocalDate valueLocalDate = new ValueLocalDate(now.atZone(ZoneId.systemDefault()).toLocalDate());
		ValueLocalDate valueLocalDate2 = new ValueLocalDate(LocalDate.of(2020, 11, 20));


		ValueLocalDateTime valueLocalDateTime = new ValueLocalDateTime(now.atZone(ZoneId.systemDefault()).toLocalDateTime());
		ValueLocalDateTime valueLocalDateTime2 = new ValueLocalDateTime(LocalDateTime.of(2020, 12, 20, 10, 30));

		ValueTimestamp valueTimestamp = new ValueTimestamp(now);
		ValueTimestampWithTZ valueTimestampWithTZ = new ValueTimestampWithTZ(now.atZone(ZoneId.of("Asia/Ho_Chi_Minh")));



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
		assertTrue(0 < valueLocalDateTime.compareTo(valueLocalDate));
		assertTrue(0 < valueLocalDateTime.compareTo(valueLocalDateTime2));
		assertTrue(0 == valueLocalDateTime.compareTo(valueTimestamp));
		assertTrue(0 == valueLocalDateTime.compareTo(valueTimestampWithTZ));
		assertTrue(0 < valueLocalDateTime.compareTo(valueNumber));



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
		Instant now = Instant.now();
		ValueTimestamp valueTimestamp = new ValueTimestamp(now);
		ValueTimestamp valueTimestamp2 = new ValueTimestamp(now);

		Value valueTime = new ValueLocalTime(now.atZone(ZoneId.systemDefault()).toLocalTime());

		ValueLocalDate valueLocalDate = new ValueLocalDate(LocalDate.of(2020, 11, 20));

		ValueLocalDateTime valueLocalDateTime = new ValueLocalDateTime(LocalDateTime.of(2020,11, 28, 12, 35));

		ValueTimestampWithTZ valueTimestampWithTZ = new ValueTimestampWithTZ(now.atZone(ZoneId.of("Asia/Ho_Chi_Minh")));

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
		assertTrue(valueTimestamp.compareTo(valueLocalDate) > 0);
		assertTrue(valueTimestamp.compareTo(valueLocalDateTime) > 0);
		assertEquals(valueTimestamp.compareTo(valueTimestampWithTZ),0);
		assertTrue(valueTimestamp.compareTo(valueNumber)>0);
		assertTrue(valueTimestamp.compareTo(valueString)<0);



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
		Instant now = Instant.now();
		ValueTimestampWithTZ valueTimestampWithTZ = new ValueTimestampWithTZ(now.atZone(ZoneId.systemDefault()));
		ValueTimestampWithTZ valueTimestampWithTZ2 = new ValueTimestampWithTZ(now.atZone(ZoneId.systemDefault()));


		ValueTimestamp valueTimestamp = new ValueTimestamp(now);

		ValueLocalDateTime valueLocalDateTime = new ValueLocalDateTime(LocalDateTime.of(2020,11, 28, 12, 35));

		ValueLocalDate valueLocalDate = new ValueLocalDate(LocalDate.of(2020, 11, 20));

		Value valueNumber = new ValueNumberLong(5000l);

		Value valueString = new ValueString(false, "26/11/1995");

		/*Test
		 * compareTo() function
		 * */

		assertTrue(valueTimestampWithTZ.compareTo(null) > 0);
		assertEquals(valueTimestampWithTZ.compareTo(valueTimestampWithTZ2),0);
		assertEquals(valueTimestampWithTZ.compareTo(valueTimestamp), 0);
		assertTrue(valueTimestampWithTZ.compareTo(valueLocalDate) > 0);
		assertTrue(valueTimestampWithTZ.compareTo(valueLocalDateTime) > 0);
		assertTrue(valueTimestampWithTZ.compareTo(valueNumber) > 0);
		assertTrue(valueTimestampWithTZ.compareTo(valueString) < 0);


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
		Instant now = Instant.now();
		ValueLocalTime valueLocalTime = new ValueLocalTime(now.atZone(ZoneId.systemDefault()).toLocalTime());
		ValueLocalTime valueLocalTime2 = new ValueLocalTime(now.atZone(ZoneId.systemDefault()).toLocalTime());

		ValueLocalDate valueLocalDate = new ValueLocalDate(now.atZone(ZoneId.systemDefault()).toLocalDate());

		ValueNumber valueNumber = new ValueNumberLong(50000L);

		ValueString valueString = new ValueString(false,"23:59:50");

		assertThrows(IllegalArgumentException.class, new Executable() {

			@Override
			public void execute() throws Throwable {
				valueLocalTime.compareTo(valueLocalDate);
			}
		});

		assertEquals(valueLocalTime.compareTo(valueLocalTime2), 0);
		assertTrue(valueLocalTime.compareTo(valueNumber)>0);
		assertTrue(valueLocalTime.compareTo(valueString)<0);


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
