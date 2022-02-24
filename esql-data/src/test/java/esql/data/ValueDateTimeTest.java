package esql.data;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

public class ValueDateTimeTest {
	
	@Test
	public void valueLocalDateTest() {
		ValueLocalDate localDate = new ValueLocalDate(LocalDate.now());
		ValueLocalDateTime localDateTime = new ValueLocalDateTime(LocalDateTime.now());
		
		
		
	}
}
