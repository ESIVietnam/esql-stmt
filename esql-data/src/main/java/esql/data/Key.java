package esql.data;

import java.io.Serializable;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Key implements CharSequence, Comparable<Key>, Serializable {
    public static final int TYPE_DATA_TREE_KEY = 0;

    public static final int TYPE_DATA_TREE_KEY_CI = 1;

    public static final int TYPE_JSON_KEY = 2;

    public static final int TYPE_JSON_KEY_CI = 3;
    public static final int TYPE_XML_KEY = 4;

    public static final int TYPE_XML_KEY_CI = 5;

    public static final String EMPTY_STRING_AS_KEY = "_null";
    public static final String QUOTED_EMPTY_STRING_AS_KEY = "_null";
    public static final String DOUBLE_QUOTED_EMPTY_STRING_AS_KEY = "\"_null\"";
    static final Pattern SAFE_DATE_TREE_KEY_PATTERN = Pattern.compile("[A-Za-z_$][\\w\\-$\\.]+");
    static final Pattern UNSAFE_DATE_TREE_KEY_REPLACEMENT = Pattern.compile("[^\\w\\-$.]+");
    static final String XML_TAG_REGEX = "[A-Z_a-z\\u00C0\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02ff\\u0370-\\u037d"
            + "\\u037f-\\u1fff\\u200c\\u200d\\u2070-\\u218f\\u2c00-\\u2fef\\u3001-\\ud7ff"
            + "\\uf900-\\ufdcf\\ufdf0-\\ufffd\\x10000-\\xEFFFF]+";
    static final Pattern SAFE_XML_KEY_PATTERN = Pattern.compile(XML_TAG_REGEX+"(:"+XML_TAG_REGEX+")?");
    static final Pattern UNSAFE_XML = Pattern.compile("[^:A-Z_a-z\\u00C0\\u00D6\\u00D8-\\u00F6"
            + "\\u00F8-\\u02ff\\u0370-\\u037d\\u037f-\\u1fff\\u200c\\u200d\\u2070-\\u218f"
            + "\\u2c00-\\u2fef\\u3001-\\udfff\\uf900-\\ufdcf\\ufdf0-\\ufffd\\-\\.0-9"
            + "\\u00b7\\u0300-\\u036f\\u203f-\\u2040]+");

    private static final Key EMPTY_OR_NULL_DATA_KEY = new Key(null, TYPE_DATA_TREE_KEY);
    private static final Key EMPTY_OR_NULL_DATA_KEY_CI = new Key(null, TYPE_DATA_TREE_KEY_CI);
    private static final Key EMPTY_OR_NULL_JSON_KEY = new Key(null, TYPE_JSON_KEY);
    private static final Key EMPTY_OR_NULL_JSON_KEY_CI = new Key(null, TYPE_JSON_KEY_CI);
    private static final Key EMPTY_OR_NULL_XML_KEY = new Key(null, TYPE_XML_KEY);
    private static final Key EMPTY_OR_NULL_XML_KEY_CI = new Key(null, TYPE_XML_KEY_CI);

    private static final Map<String, Key> COMMON_KEYS;

    static {
        String[] commons = { "result", "data", "value", "col1", "col2", "col3", "col4", "col5", "col6", "col7", "col8" };
        COMMON_KEYS = new HashMap<>(commons.length * 6);
        for(String k: commons) {
            for (int i = 0; i < 6; i++) {
                COMMON_KEYS.put(k + "[" + i + "]", new Key(k, i));
            }
        }
    }
    private final String key;

    private final String key_ci;

    private final int keyType;

    private final int spaceInside; //-1 no space

    private Key(String key, int keyType) {
        if(key == null || key.isBlank()) {
            this.key = "";
            this.keyType = keyType;
            this.key_ci = this.key;
            this.spaceInside = -1;
        }
        else {
            this.key = key;
            this.keyType = keyType;
            this.key_ci = key.toLowerCase(Locale.ROOT);
            this.spaceInside = key.indexOf(' ');
        }
    }

    @Override
    public int compareTo(Key o) {
        if(o == this)
            return 0;
        if(keyType == TYPE_DATA_TREE_KEY_CI || keyType == TYPE_JSON_KEY_CI || keyType == TYPE_XML_KEY_CI)
            return key_ci.compareTo(o.key_ci);
        return key.compareTo(o.key);
    }

    @Override
    public int hashCode() {
        if(keyType == TYPE_DATA_TREE_KEY_CI || keyType == TYPE_JSON_KEY_CI || keyType == TYPE_XML_KEY_CI)
            return key_ci.hashCode();
        return key.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null)
            return false;
        if(obj == this)
            return true;
        if(obj instanceof Key) {
            if(keyType == TYPE_DATA_TREE_KEY_CI || keyType == TYPE_JSON_KEY_CI || keyType == TYPE_XML_KEY_CI)
                return key_ci.equals(((Key) obj).key_ci);
            return key.equals(((Key) obj).key);
        }
        if(keyType == TYPE_DATA_TREE_KEY_CI || keyType == TYPE_JSON_KEY_CI || keyType == TYPE_XML_KEY_CI)
            return key_ci.equalsIgnoreCase(obj.toString());
        return key.equals(obj.toString());
    }

    @Override
    public int length() {
        return key.length();
    }

    @Override
    public char charAt(int index) {
        if(keyType == TYPE_DATA_TREE_KEY_CI || keyType == TYPE_JSON_KEY_CI || keyType == TYPE_XML_KEY_CI)
            return key_ci.charAt(index);
        return key.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        if(keyType == TYPE_DATA_TREE_KEY_CI || keyType == TYPE_JSON_KEY_CI || keyType == TYPE_XML_KEY_CI)
            return key_ci.subSequence(start, end);
        return key.subSequence(start, end);
    }

    @Override
    public String toString() {
        if(keyType == TYPE_DATA_TREE_KEY_CI || keyType == TYPE_JSON_KEY_CI || keyType == TYPE_XML_KEY_CI)
            return "(ci)"+key;
        return key;
    }

    public String origString() {
        return this.key;
    }

    public String origLowerCaseString() {
        return this.key_ci;
    }

    public int keyType() {
        return this.keyType;
    }

    public boolean isSpaceInside() {
        return this.spaceInside >= 0;
    }

    public String identifier() {
        if(this.key == null || this.key.isEmpty()) {
            return EMPTY_STRING_AS_KEY; //empty string as allowed key
        }
        if(keyType == TYPE_DATA_TREE_KEY_CI || keyType == TYPE_JSON_KEY_CI || keyType == TYPE_XML_KEY_CI)
            return this.key_ci;
        return this.key;
    }

    public String quotedIdentifier() {
        return quotedIdentifier(false);
    }

    public String quotedIdentifier(boolean doubleQuote) {
        if(this.key == null || this.key.isEmpty()) {
            if(doubleQuote)
                return DOUBLE_QUOTED_EMPTY_STRING_AS_KEY;
            return QUOTED_EMPTY_STRING_AS_KEY; //empty string as allowed key
        }
        if(keyType == TYPE_DATA_TREE_KEY_CI || keyType == TYPE_JSON_KEY_CI || keyType == TYPE_XML_KEY_CI) {
            if(doubleQuote)
                return "\""+this.key_ci+"\"";
            return "'"+this.key_ci+"'";
        }
        if(doubleQuote)
            return "\""+this.key+"\"";
        return "'"+this.key+"'";
    }

    /**
     * return case sensitive data tree key type
     *
     * @param key string
     * @return key identifier
     */
    public static Key of(String key) {
        return of(key, TYPE_DATA_TREE_KEY);
    }

    /**
     * return CASE INSENSITIVE data tree key type
     *
     * @param key string
     * @return key identifier
     */
    public static Key ofIgnoreCaseString(String key) {
        return of(key, TYPE_DATA_TREE_KEY_CI);
    }

    public static Key of(String key_str, int keyType) {
        if(key_str == null || key_str.isBlank()) {
            switch (keyType) {
                case TYPE_DATA_TREE_KEY:
                    return EMPTY_OR_NULL_DATA_KEY;
                case TYPE_DATA_TREE_KEY_CI:
                    return EMPTY_OR_NULL_DATA_KEY_CI;
                case TYPE_JSON_KEY:
                    return EMPTY_OR_NULL_JSON_KEY;
                case TYPE_JSON_KEY_CI:
                    return EMPTY_OR_NULL_JSON_KEY_CI;
                case TYPE_XML_KEY:
                    return EMPTY_OR_NULL_XML_KEY;
                case TYPE_XML_KEY_CI:
                    return EMPTY_OR_NULL_XML_KEY_CI;
                default:
                    throw new IllegalArgumentException("Not a valid keyType="+keyType);
            }
        }
        switch (keyType) {
            case TYPE_DATA_TREE_KEY_CI:
                key_str = key_str.toLowerCase(Locale.ROOT);
            case TYPE_DATA_TREE_KEY:
                key_str = Normalizer.normalize(key_str, Normalizer.Form.NFKD);
                Matcher matcher1 = SAFE_DATE_TREE_KEY_PATTERN.matcher(key_str);
                if(!matcher1.matches()) {
                    UNSAFE_DATE_TREE_KEY_REPLACEMENT.matcher(key_str).replaceAll((r) ->
                                r.group().isBlank() ? "-" : "_"
                        );
                }
                return COMMON_KEYS.getOrDefault(key_str+"["+keyType+"]", new Key(key_str, keyType));
            case TYPE_JSON_KEY_CI:
                key_str = key_str.toLowerCase(Locale.ROOT);
            case TYPE_JSON_KEY:
                key_str = key_str.trim();
                return COMMON_KEYS.getOrDefault(key_str+"["+keyType+"]", new Key(key_str, keyType));

            case TYPE_XML_KEY_CI:
                //XML is spaces' insensitive
                key_str = key_str.trim().toLowerCase(Locale.ROOT);
            case TYPE_XML_KEY:
                key_str = Normalizer.normalize(key_str, Normalizer.Form.NFKD);
                Matcher matcher2 = SAFE_XML_KEY_PATTERN.matcher(key_str);
                if(matcher2.matches()) {
                    StringBuffer sb = new StringBuffer(matcher2.group(1)); //first char
                    if(!matcher2.group(2).isBlank())
                        sb.append(UNSAFE_XML.matcher(matcher2.group(2)).replaceAll((r) ->
                                r.group().startsWith(":") ? "-" : ""
                        ));

                    key_str = sb.toString();
                        return COMMON_KEYS.getOrDefault(key_str+"["+keyType+"]", new Key(key_str, keyType));
                }
                throw new IllegalArgumentException("First char of XML tag must be valid");
            default:
                throw new IllegalArgumentException("Not a valid keyType="+keyType);
        }
    }
}
