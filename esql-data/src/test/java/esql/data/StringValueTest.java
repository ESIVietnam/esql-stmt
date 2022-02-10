package esql.data;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static esql.data.Types.*;
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

}
