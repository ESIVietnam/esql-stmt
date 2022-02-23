package esql.data;

import static org.junit.jupiter.api.Assertions.assertEquals;


import org.junit.jupiter.api.Test;


public class ValueNumberTest {
	
	@Test
	public void valueNumberTest() {
		assertEquals(ValueNumber.buildNumber(Types.TYPE_INT, (double) 3000), new ValueNumberInt(Types.TYPE_INT,(int) 3000));
		
		
	}
}
