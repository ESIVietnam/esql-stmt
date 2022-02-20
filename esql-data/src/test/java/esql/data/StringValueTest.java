package esql.data;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static esql.data.Types.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class StringValueTest {

    @Test
    public void NullStringTest() {
        Value v;
        v = Value.valueOf((String) null);
        assertTrue(v.is(TYPE_STRING), "Not correct type");
        assertTrue(v.isNull(), "Must be null");
        assertTrue(!v.isTrue(), "Must not true");
        assertTrue(v.isEmpty(), "Must be empty");
    }
        
    
    @Test
    public void EmptyStringTest() {
    	
        Value stringEmpty;
      
        
        stringEmpty = Value.valueOf("");
       


        assertTrue(stringEmpty.is(TYPE_STRING), "Not correct type");
        assertTrue(!stringEmpty.isNull(), "Must be not null");
        assertTrue(!stringEmpty.isTrue(), "Must not true");
        assertTrue(stringEmpty.isEmpty(), "Must be empty");
        assertEquals(stringEmpty.getType(), TYPE_STRING);

//        assertTrue(nstringEmpty.is(TYPE_NSTRING), "Not correct type");
//        assertTrue(!nstringEmpty.isNull(), "Must be not null");
//        assertTrue(!nstringEmpty.isTrue(), "Must not true");
//        assertTrue(nstringEmpty.isEmpty(), "Must be empty");
//        assertEquals(nstringEmpty.getType(), TYPE_NSTRING);
        
    }
    
    @Test
    public void nonEmptyStringTest() {
        Value shortString , longString, vietnameseString;
        shortString = Value.valueOf("short string");
        longString = Value.valueOf("This is a very long long long long long long long long long long long String");
        vietnameseString = Value.valueOf("Chuỗi tiếng việt");

        assertTrue(shortString.is(TYPE_STRING), "Not correct type");
        assertTrue(!shortString.isNull(), "Must not be null");
        assertTrue(shortString.isTrue(), "Must true");
        assertTrue(!shortString.isEmpty(), "Must not be empty");


        assertTrue(longString.is(TYPE_STRING), "Not correct type");
        assertTrue(!longString.isNull(), "Must not be null");
        assertTrue(longString.isTrue(), "Must true");
        assertTrue(!longString.isEmpty(), "Must not be empty");

        assertTrue(vietnameseString.is(TYPE_STRING), "Not correct type");
        assertTrue(!vietnameseString.isNull(), "Must not be null");
        assertTrue(vietnameseString.isTrue(), "Must true");
        assertTrue(!vietnameseString.isEmpty(), "Must not be empty");
    }
    
  

}
