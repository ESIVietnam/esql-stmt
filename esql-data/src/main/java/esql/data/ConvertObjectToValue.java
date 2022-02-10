package esql.data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.function.Function;

public class ConvertObjectToValue implements Function<Object, Value> {

    private final Types type; //type to convert or assign

    ConvertObjectToValue(Types type) {
        this.type = type;
    }

    @Override
    public Value apply(Object o) {
        if(o == null) //the NullValue as null.
            return ValueNULL.buildNULL(type);
        Value value;
        //date, time stuffs
        if(o instanceof java.sql.Date)
            value = ValueDateTime.buildDateTime((java.sql.Date) o);
        else if(o instanceof java.sql.Time)
            value = ValueDateTime.buildDateTime((java.sql.Time) o);
        else if(o instanceof java.sql.Timestamp)
            value = ValueDateTime.buildDateTime((java.sql.Timestamp) o);
        else if(o instanceof LocalDate)
            value = ValueDateTime.buildDateTime((LocalDate) o);
        else if(o instanceof LocalDateTime)
            value = ValueDateTime.buildDateTime((LocalDateTime) o);
        else if(o instanceof ZonedDateTime)
            value = ValueDateTime.buildDateTime((ZonedDateTime) o);
        else if(o instanceof Instant)
            value = ValueDateTime.buildDateTime((Instant) o);
        else if(o instanceof LocalTime)
            value = ValueDateTime.buildDateTime((LocalTime) o);
        //Number stuffs
        else if(o instanceof BigDecimal)
            value = ValueNumber.buildNumber(type, ((BigDecimal) o));
        else if(o instanceof BigInteger)
            value = ValueNumber.buildNumber(type, ((BigInteger) o));
        else if(o instanceof Float || o instanceof Double)
            value = ValueNumber.buildNumber(type, ((Float) o).doubleValue());
        else if(o instanceof Number)
            value = ValueNumber.buildNumber(type, ((Number) o).longValue());
        //Boolean
        else if(o instanceof Boolean)
            value = ValueBoolean.buildBoolean(((Boolean) o).booleanValue());
        //binary string
        else if(o instanceof byte[])
            value = ValueBytes.buildBytes((byte[])o);
        else //to String otherwise
            value = ValueString.buildString(type, o.toString());

        if(!value.is(type))
            return value.convertTo(type);
        return value;
    }
}