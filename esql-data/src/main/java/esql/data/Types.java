package esql.data;

import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

public enum Types {

    TYPE_DATE("date"),
    TYPE_TIME("time"),
    TYPE_DATETIME("datetime"),
    TYPE_TIMESTAMP("timestamp"),
    TYPE_BYTE ("byte"), //1 byte
    TYPE_UBYTE ("ubyte"), //1 byte unsigned integer
    TYPE_SHORT ("short"), //2 bytes
    TYPE_USHORT ("ushort"), //2 bytes unsigned integer
    TYPE_INT ("int"), //4 bytes
    TYPE_UINT ("uint"), //4 bytes unsigned integer
    TYPE_LONG ("long"), //8 bytes
    TYPE_ULONG ("ulong"), //8 bytes unsigned integer (BigInteger)
    TYPE_DECIMAL ("decimal"),//big decimal
    TYPE_FLOAT ("float"), //or real, but not float in database. 4 bytes
    TYPE_DOUBLE ("double"), //8 bytes
    TYPE_BOOLEAN ("boolean"), //1 bit or 1 byte
    TYPE_STRING ("string"),
    TYPE_NSTRING ("nstring"),

    TYPE_DATA_TREE ("data-tree"),
    TYPE_XML ("xml"), //sub-type of DATA_TREE
    TYPE_JSON ("json"),//sub-type of DATA_TREE

    TYPE_CLOB ("clob"),
    TYPE_NCLOB ("nclob"),
    TYPE_BLOB ("blob"),
    TYPE_BYTES ("bytes"); //bytes array or binary column, read for rowID data.

    // Reverse-lookup map for abbreviation
    private static final HashMap<String, Types> lookup = new HashMap<String, Types>(21); //21 so far
    public final static String EXT_ARRAY = "[]";
    public final static String PREF_ARRAY = "array-of-";

    static {
        for (Types t : Types.values()) {
            lookup.put(t.getAbbreviation(), t);
        }
        //alias name
        lookup.put("real", TYPE_FLOAT);
        lookup.put("bigint", TYPE_ULONG);
        lookup.put("binary", Types.TYPE_BYTES);
    }

    private final String abbreviation;

    private Types(String abbr) {
        this.abbreviation = abbr;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    @Override
    public String toString() {
        return getAbbreviation();
    }

    public static String extractAbbreviation(String type_str) {
        String abbr = null;
        type_str = type_str.trim();
        if(type_str.endsWith(EXT_ARRAY))
            abbr = type_str.substring(0,type_str.length()-2); //cat 2 ky tu cuoi
        else if(type_str.startsWith(PREF_ARRAY))
            abbr = type_str.substring(PREF_ARRAY.length()); //cat 9 ky tu dau
        if(abbr != null) {
            if (lookup.containsKey(abbr) && isDataTree(lookup.get(abbr)))
                throw new IllegalArgumentException("Invalid type array type of \"" + abbr + "\"");
            return abbr;
        }
        return type_str;
    }

    public static boolean detectArray(String type_str) {
        type_str = type_str.trim();
        return(type_str.endsWith(EXT_ARRAY) || type_str.startsWith(PREF_ARRAY));
    }

    public static boolean isValidType(String abbreviation) {
        if(abbreviation==null)
            throw new NullPointerException();
        return lookup.containsKey(abbreviation);
    }

    public static Types of(String abbreviation) {
        if(!isValidType(abbreviation))
            throw new IllegalArgumentException("Invalid type abbreviation \""+abbreviation
                    + "\", only "+ getAllowedTypes().stream().collect(Collectors.joining(", "))+" allowed.");
        return lookup.get(abbreviation);
    }

    public static Set<String> getAllowedTypes() {
        return lookup.keySet();
    }

    public static boolean isString(Types type) {
        switch(type) {
            case TYPE_NSTRING:
            case TYPE_STRING:
                return true;
            default:
                break;
        }
        return false;
    }

    public static boolean isDataTree(Types type) {
        switch(type) {
            case TYPE_DATA_TREE:
            case TYPE_JSON:
            case TYPE_XML:
                return true;
            default:
                break;
        }
        return false;
    }

    public static boolean isLOB(Types type) {
        switch(type) {
            case TYPE_BLOB:
            case TYPE_CLOB:
            case TYPE_NCLOB:
                return true;
            default:
                break;
        }
        return false;
    }

    public static boolean isNumber(Types type) {
        return isInteger(type) || isDecimal(type);
    }

    public static boolean isDecimal(Types type) {
        switch(type) {
            case TYPE_DECIMAL:
            case TYPE_FLOAT:
            case TYPE_DOUBLE:
                return true;
            default:
                break;
        }
        return false;
    }

    public static boolean isInteger(Types type) {
        switch(type) {
            case TYPE_BYTE:
            case TYPE_SHORT:
            case TYPE_INT:
            case TYPE_LONG:
            case TYPE_UBYTE:
            case TYPE_USHORT:
            case TYPE_UINT:
            case TYPE_ULONG:
                return true;
            default:
                break;
        }
        return false;
    }

    public static boolean isDateOrTime(Types type) {
        switch(type) {
            case TYPE_DATE:
            case TYPE_TIME:
            case TYPE_DATETIME:
            case TYPE_TIMESTAMP:
                return true;
            default:
                break;
        }
        return false;
    }
}
