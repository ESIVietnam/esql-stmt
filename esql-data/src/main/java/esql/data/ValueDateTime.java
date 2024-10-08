package esql.data;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.*;
import java.time.chrono.*;
import java.time.temporal.Temporal;

public abstract class ValueDateTime  extends Value {

    public static final long MILLISECONDS_PER_DAY = 86400*1000;
    public static final ValueDateTime NULL_DATE = new ValueNULLDateTime(Types.TYPE_DATE);
    public static final ValueDateTime NULL_TIME = new ValueNULLDateTime(Types.TYPE_TIME);
    public static final ValueDateTime NULL_DATETIME = new ValueNULLDateTime(Types.TYPE_DATETIME);
    public static final ValueDateTime NULL_TIMESTAMP = new ValueNULLDateTime(Types.TYPE_TIMESTAMP);

    private final Types type; //for Date, DateTime, Time (non-instant) or Timestamp (instant value)

    protected ValueDateTime(Types type) {
        this.type = type;
    }

    @Override
    public Types getType() {
        return type;
    }

    static Value buildDateTime(Temporal val) {
        if(val instanceof Instant)
            return buildDateTime((Instant) val);
        if(val instanceof ZonedDateTime)
            return buildDateTime((ZonedDateTime) val);
        if(val instanceof LocalDateTime)
            return buildDateTime((LocalDateTime) val);
        if(val instanceof LocalDate)
            return buildDateTime((LocalDate) val);
        if(val instanceof LocalTime)
            return buildDateTime((LocalTime) val);
        throw new IllegalArgumentException("Unsupported");
    }

    static Value buildDateTime(Date val) {
        return new ValueLocalDate(val.toLocalDate());
    }

    static Value buildDateTime(Time val) {
        return new ValueLocalTime(val.toLocalTime());
    }

    static Value buildDateTime(Timestamp val) {
        return new ValueTimestamp(val.toInstant());
    }

    static Value buildDateTime(LocalDate val) {
        return new ValueLocalDate(val);
    }

    static Value buildDateTime(LocalDateTime val) {
        return new ValueLocalDateTime(val);
    }

    static Value buildDateTime(Instant val) {
        return new ValueTimestamp(val);
    }

    static Value buildDateTime(ZonedDateTime val) {
        return new ValueTimestampWithTZ(val);
    }

    static Value buildDateTime(LocalTime val) {
        return new ValueLocalTime(val);
    }

    /**
     * build date or datetime from miliseconds from epoch.
     *
     * @param type
     * @param value
     * @return
     */
    static Value buildDateTime(Types type, long value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isTrue() {
        return true;
    }

    /**
     * this method for using by Java, or comparing value.
     *
     * @return temporal object of date time
     */
    public abstract Temporal toTemporal();

    @Override
    public boolean equals(Object obj) {
        if(obj == null)
            return false;
        if(obj == this)
            return true;
        if (obj instanceof ValueDateTime)
            return this.toTemporal().equals(((ValueDateTime) obj).toTemporal());
        if (obj instanceof Value)
            return !((Value) obj).isNull() && this.toTemporal().toString().equals(((Value) obj).stringValue());
        //compare primitive
        return this.toTemporal().equals(obj);
    }
}

final class ValueNULLDateTime extends ValueDateTime {

    ValueNULLDateTime(Types type) {
        super(type);
    }

    @Override
    public boolean isTrue() {
        return false;
    }

    @Override
    public Temporal toTemporal() {
        return null;
    }

    @Override
    public Value convertTo(Types type) {
        return Value.nullOf(type);
    }

    @Override
    public boolean isNull() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public String stringValue() {
        return Value.STRING_OF_NULL;
    }



    @Override
    public boolean equals(Object obj) {
        if(obj == this)
            return true;
        return false;
    }

    @Override
    public int compareTo(Value o) {
        if(o != null && o.isNull())
            return 0;
        return -1;
    }
}

/**
 * wrapper of LocalDate
 */
class ValueLocalDate extends ValueDateTime {
    private final LocalDate value;

    ValueLocalDate(LocalDate value) {
        super(Types.TYPE_DATE);
        this.value = value;
    }

    @Override
    public Temporal toTemporal() {
        return value;
    }

    @Override
    public String toString() {
        return "(ValueLocalDate)"+this.value;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public int compareTo(Value o) {
        if(o == null || o.isEmpty())
            return 1; //always bigger
        if(o instanceof ValueDateTime) {
            if(o.is(Types.TYPE_TIME))
                throw new IllegalArgumentException("A date value is only comparable to date/datetime/timestamp value");
            Temporal b = ((ValueDateTime)o).toTemporal();
            if (b instanceof ChronoLocalDate) //same type
                return this.value.compareTo((ChronoLocalDate) b);
            if (b instanceof ChronoLocalDateTime) { //excluding time
                int c = this.value.compareTo(((ChronoLocalDateTime) b).toLocalDate());
                if (c == 0) //date-only always smaller datetime if same day.
                    return -1;
                return c;
            }
            if (b instanceof Instant) { //convert to timestamp at local timezone
                ZonedDateTime a = this.value.atStartOfDay(ZoneId.systemDefault());
                return a.toInstant().compareTo((Instant) b);
            }
            if (b instanceof ChronoZonedDateTime) { //convert to timestamp at local timezone
                ZonedDateTime a = this.value.atStartOfDay(ZoneId.systemDefault());
                return a.compareTo((ChronoZonedDateTime) b);
            }
            throw new AssertionError();
        }
        if(o instanceof ValueNumber) {
            long ts = ((ValueNumber)o).longValue();
            return Long.compare(this.value.toEpochDay()*MILLISECONDS_PER_DAY, ts);
        }
        if(o instanceof ValueString) {
            return this.value.toString().compareTo(o.stringValue());
        }
        throw new IllegalArgumentException("Can not compare to "+o);
    }

    @Override
    public Value convertTo(Types type) {
        switch (type) {
            case TYPE_DATE:
                return this;
            case TYPE_DATETIME:
                return ValueDateTime.buildDateTime(this.value.atStartOfDay());
            case TYPE_TIMESTAMP:
                return ValueDateTime.buildDateTime(this.value.atStartOfDay(ZoneId.systemDefault()));
            case TYPE_INT:
            case TYPE_UINT:
            case TYPE_LONG:
            case TYPE_ULONG:
            case TYPE_DECIMAL:
            case TYPE_FLOAT:
            case TYPE_DOUBLE:
                //convert epoch days to milliseconds
                return ValueNumber.buildNumber(type, this.value.toEpochDay() * ValueDateTime.MILLISECONDS_PER_DAY);
            case TYPE_STRING:
            case TYPE_NSTRING:
                return ValueString.buildString(type, this.value.toString());
            case TYPE_BYTES:
                return ValueBytes.buildBytes(this.value.toString().getBytes(StandardCharsets.ISO_8859_1));
            default:
                throw new IllegalArgumentException("the \"date\" value can not convert to \""+type+"\" value");
        }
    }

    @Override
    public String stringValue() {
        return value.toString();
    }
}

/**
 * wrapper of LocalDateTime
 */
class ValueLocalDateTime extends ValueDateTime {
    private final LocalDateTime value;

    ValueLocalDateTime(LocalDateTime value) {
        super(Types.TYPE_DATETIME);
        this.value = value;
    }

    @Override
    public Temporal toTemporal() {
        return value;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public int compareTo(Value o) {
        if(o == null || o.isEmpty())
            return 1; //always bigger
        if(o instanceof ValueDateTime) {
            if(o.is(Types.TYPE_TIME))
                throw new IllegalArgumentException("A datetime value is only comparable to date/datetime/timestamp value");
            Temporal b = ((ValueDateTime) o).toTemporal();
            if (b instanceof ChronoLocalDate) //without time
                return this.value.compareTo(((ChronoLocalDate) b).atTime(LocalTime.MIDNIGHT));
            if (b instanceof ChronoLocalDateTime) { //same type
                return this.value.compareTo(((ChronoLocalDateTime) b));
            }
            if (b instanceof Instant) { //convert to timestamp at local timezone
                ZonedDateTime a = this.value.atZone(ZoneId.systemDefault());
                return a.toInstant().compareTo((Instant) b);
            }
            if (b instanceof ChronoZonedDateTime) { //convert to timestamp at local timezone
                ZonedDateTime a = this.value.atZone(ZoneId.systemDefault());
                return a.compareTo(((ChronoZonedDateTime) b).withZoneSameInstant(ZoneId.systemDefault()));
            }
            throw new AssertionError();
        }
        if(o instanceof ValueNumber) {
            long ts = ((ValueNumber)o).longValue();
            ZoneId systemZone = ZoneId.systemDefault();
            return Long.compare(this.value.toEpochSecond(systemZone.getRules().getOffset(this.value))*1000, ts);
        }
        if(o instanceof ValueString) {
            return this.value.toString().compareTo(o.stringValue());
        }
        throw new IllegalArgumentException("Can not compare to "+o);
    }

    @Override
    public Value convertTo(Types type) {
        switch (type) {
            case TYPE_DATE:
                return ValueDateTime.buildDateTime(this.value.toLocalDate());
            case TYPE_DATETIME:
                return this;
            case TYPE_TIMESTAMP:
                return ValueDateTime.buildDateTime(this.value.atZone(ZoneId.systemDefault()));
            case TYPE_INT:
            case TYPE_UINT:
            case TYPE_LONG:
            case TYPE_ULONG:
            case TYPE_DECIMAL:
            case TYPE_FLOAT:
            case TYPE_DOUBLE:
                //convert epoch days to milliseconds
                ZoneId systemZone = ZoneId.systemDefault();
                return ValueNumber.buildNumber(type, this.value.toEpochSecond(systemZone.getRules().getOffset(this.value)) * 1000);
            case TYPE_STRING:
            case TYPE_NSTRING:
                return ValueString.buildString(type, this.value.toString());
            case TYPE_BYTES:
                return ValueBytes.buildBytes(this.value.toString().getBytes(StandardCharsets.ISO_8859_1));
            default:
                throw new IllegalArgumentException("the \"datetime\" value can not convert to \""+type+"\" value");
        }
    }

    @Override
    public String stringValue() {
        return value.toString();
    }
}

/**
 * wrapper of Instant, timestamp at UTC.
 */
class ValueTimestamp extends ValueDateTime {
    private final Instant value;

    ValueTimestamp(Instant value) {
        super(Types.TYPE_DATETIME);
        this.value = value;
    }

    @Override
    public Temporal toTemporal() {
        return value;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public int compareTo(Value o) {
        if(o == null || o.isEmpty())
            return 1; //always bigger
        if(o instanceof ValueDateTime) {
            if(o.is(Types.TYPE_TIME))
                throw new IllegalArgumentException("A timestamp value is only comparable to date/datetime/timestamp value");
            Temporal b = ((ValueDateTime) o).toTemporal();
            if (b instanceof Instant) //same type
                return this.value.compareTo(((Instant) b));
            if (b instanceof ChronoLocalDate) //without time
                return this.value.compareTo(((ChronoLocalDate) b).atTime(LocalTime.MIDNIGHT).toInstant(ZoneOffset.UTC));
            if (b instanceof ChronoLocalDateTime) //from datetime
                return this.value.compareTo(((ChronoLocalDateTime) b).toInstant(ZoneOffset.UTC));
            if (b instanceof ChronoZonedDateTime) //convert to timestamp at local timezone
                return value.compareTo(((ChronoZonedDateTime) b).toInstant());
            throw new AssertionError();
        }
        if(o instanceof ValueNumber) {
            long ts = ((ValueNumber)o).longValue();
            return Long.compare(this.value.toEpochMilli(), ts);
        }
        if(o instanceof ValueString) {
            return this.value.toString().compareTo(o.stringValue());
        }
        throw new IllegalArgumentException("Can not compare to "+o);
    }

    @Override
    public Value convertTo(Types type) {
        switch (type) {
            case TYPE_TIME:
                return ValueDateTime.buildDateTime(this.value.atZone(ZoneId.systemDefault()).toLocalTime());
            case TYPE_DATE:
                return ValueDateTime.buildDateTime(this.value.atZone(ZoneId.systemDefault()).toLocalDate());
            case TYPE_DATETIME:
                return ValueDateTime.buildDateTime(this.value.atZone(ZoneId.systemDefault()).toLocalDateTime());
            case TYPE_TIMESTAMP:
                return this;
            case TYPE_INT:
            case TYPE_UINT:
            case TYPE_LONG:
            case TYPE_ULONG:
            case TYPE_DECIMAL:
            case TYPE_FLOAT:
            case TYPE_DOUBLE:
                //convert epoch days to milliseconds
                return ValueNumber.buildNumber(type, this.value.getEpochSecond() * 1000);
            case TYPE_STRING:
            case TYPE_NSTRING:
                return ValueString.buildString(type, this.value.toString());
            case TYPE_BYTES:
                return ValueBytes.buildBytes(this.value.toString().getBytes(StandardCharsets.ISO_8859_1));
            default:
                throw new IllegalArgumentException("the \"timestamp\" value can not convert to \""+type+"\" value");
        }
    }

    @Override
    public String stringValue() {
        return value.toString();
    }
}

/**
 * wrapper of ZonedDateTime, timestamp with timezone
 */
class ValueTimestampWithTZ extends ValueDateTime {
    private final ZonedDateTime value;

    ValueTimestampWithTZ(ZonedDateTime value) {
        super(Types.TYPE_DATETIME);
        this.value = value;
    }

    @Override
    public Temporal toTemporal() {
        return value;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public int compareTo(Value o) {
        if(o == null || o.isEmpty())
            return 1; //always bigger
        if(o instanceof ValueDateTime) {
            if(o.is(Types.TYPE_TIME))
                throw new IllegalArgumentException("A timestamp value is only comparable to date/datetime/timestamp value");
            Temporal b = ((ValueDateTime) o).toTemporal();
            if (b instanceof Instant) { //convert to timestamp at local timezone
                Instant a = this.value.toInstant();
                return a.compareTo((Instant) b);
            }
            if (b instanceof ChronoZonedDateTime) //same type
                return this.value.compareTo(((ChronoZonedDateTime) b));
            if (b instanceof ChronoLocalDate) //without time
                return this.value.compareTo(((ChronoLocalDate) b).atTime(LocalTime.MIDNIGHT).atZone(ZoneId.systemDefault()));
            if (b instanceof ChronoLocalDateTime) { //from datetime
                return this.value.compareTo(((ChronoLocalDateTime) b).atZone(ZoneId.systemDefault()));
            }
            throw new AssertionError();
        }
        if(o instanceof ValueNumber) {
            long ts = ((ValueNumber)o).longValue();
            return Long.compare(this.value.toEpochSecond()*1000, ts);
        }
        if(o instanceof ValueString) {
            return this.value.toString().compareTo(o.stringValue());
        }
        throw new IllegalArgumentException("Can not compare to "+o);
    }

    @Override
    public Value convertTo(Types type) {
        switch (type) {
            case TYPE_TIME:
                return ValueDateTime.buildDateTime(this.value.toLocalTime());
            case TYPE_DATE:
                return ValueDateTime.buildDateTime(this.value.toLocalDate());
            case TYPE_DATETIME:
                return ValueDateTime.buildDateTime(this.value.toLocalDateTime());
            case TYPE_TIMESTAMP:
                return this;
            case TYPE_INT:
            case TYPE_UINT:
            case TYPE_LONG:
            case TYPE_ULONG:
            case TYPE_DECIMAL:
            case TYPE_FLOAT:
            case TYPE_DOUBLE:
                //convert epoch days to milliseconds
                return ValueNumber.buildNumber(type, this.value.toEpochSecond() * 1000);
            case TYPE_STRING:
            case TYPE_NSTRING:
                return ValueString.buildString(type, this.value.toString());
            case TYPE_BYTES:
                return ValueBytes.buildBytes(this.value.toString().getBytes(StandardCharsets.ISO_8859_1));
            default:
                throw new IllegalArgumentException("the \"timestamp-with-timezone\" value can not convert to \""+type+"\" value");
        }
    }

    @Override
    public String stringValue() {
        return value.toString();
    }
}

/**
 * wrapper of LocalTime
 */
class ValueLocalTime extends ValueDateTime {
    private final LocalTime value;

    ValueLocalTime(LocalTime value) {
        super(Types.TYPE_TIME);
        this.value = value;
    }

    @Override
    public Temporal toTemporal() {
        return value;
    }

    @Override
    public int compareTo(Value o) {
        if (o == null || o.isEmpty())
            return 1; //always bigger
        if(o instanceof ValueDateTime) {
            if(!o.is(Types.TYPE_TIME))
                throw new IllegalArgumentException("A time value is only comparable to time value");
            LocalTime b = (LocalTime)((ValueDateTime) o).toTemporal();
            return this.value.compareTo(b);
        }
        if(o instanceof ValueNumber) {
            long ts = ((ValueNumber)o).longValue();
            return Long.compare(this.value.toSecondOfDay()*1000, ts);
        }
        if(o instanceof ValueString) {
            return this.value.toString().compareTo(o.stringValue());
        }
        throw new IllegalArgumentException("Can not compare to "+o);
    }

    @Override
    public String stringValue() {
        return value.toString();
    }

    @Override
    public Value convertTo(Types type) {
        switch (type) {
            case TYPE_TIME:
                return this;
            case TYPE_SHORT:
            case TYPE_USHORT:
            case TYPE_INT:
            case TYPE_UINT:
            case TYPE_LONG:
            case TYPE_ULONG:
            case TYPE_DECIMAL:
            case TYPE_FLOAT:
            case TYPE_DOUBLE:
                //convert epoch days to milliseconds
                return ValueNumber.buildNumber(type, this.value.toSecondOfDay() * 1000);
            case TYPE_STRING:
            case TYPE_NSTRING:
                return ValueString.buildString(type, this.value.toString());
            case TYPE_BYTES:
                return ValueBytes.buildBytes(this.value.toString().getBytes(StandardCharsets.ISO_8859_1));
            default:
                throw new IllegalArgumentException("the \"time\" value can not convert to \""+type+"\" value");
        }
    }
}
