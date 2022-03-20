package esql.data;

import java.io.Serializable;
import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public final class Key implements CharSequence, Comparable<Key>, Serializable {
    public static final int SAFE_DATA_TREE_KEY = 0;
    public static final int SAFE_JSON_KEY = 1;
    public static final int SAFE_XML_KEY = 2;

    private static final Pattern SAFE_DATE_TREE_FIRST = Pattern.compile("^[A-Za-z_$]");
    private static final Pattern UNSAFE_DATE_TREE = Pattern.compile("[^A-Za-z0-9\\-_\\s$.]+");
    private static final Pattern SAFE_XML_FIRST = Pattern.compile("^[:A-Z_a-z\\u00C0\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02ff\\u0370-\\u037d"
            + "\\u037f-\\u1fff\\u200c\\u200d\\u2070-\\u218f\\u2c00-\\u2fef\\u3001-\\ud7ff"
            + "\\uf900-\\ufdcf\\ufdf0-\\ufffd\\x10000-\\xEFFFF]");
    private static final Pattern UNSAFE_XML = Pattern.compile("[^:A-Z_a-z\\u00C0\\u00D6\\u00D8-\\u00F6"
            + "\\u00F8-\\u02ff\\u0370-\\u037d\\u037f-\\u1fff\\u200c\\u200d\\u2070-\\u218f"
            + "\\u2c00-\\u2fef\\u3001-\\udfff\\uf900-\\ufdcf\\ufdf0-\\ufffd\\-\\.0-9"
            + "\\u00b7\\u0300-\\u036f\\u203f-\\u2040]+");

    private static final Key EMPTY_OR_NULL_KEY = new Key(null, false);
    private final String key;
    private final String key_ci;

    private Key(String key, boolean caseInsensitive) {
        if(key == null || key.isEmpty()) {
            this.key = "";
            this.key_ci = null;
        }
        else {
            this.key = key;
            if (caseInsensitive)
                this.key_ci = key.toLowerCase(Locale.ROOT);
            else
                this.key_ci = null;
        }
    }

    @Override
    public int compareTo(Key o) {
        if(o == this)
            return 0;
        if(key_ci != null && o.key_ci != null)
            return key_ci.compareTo(o.key_ci);
        return key.compareTo(o.key);
    }

    @Override
    public int hashCode() {
        if(key_ci != null)
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
            if(key_ci != null)
                return key_ci.equals(((Key) obj).key_ci);
            return key.equals(((Key) obj).key);
        }
        if(key_ci != null)
            return key_ci.equals(obj.toString());
        return key.equals(obj.toString());
    }

    @Override
    public int length() {
        return key.length();
    }

    @Override
    public char charAt(int index) {
        return key.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return key.subSequence(start, end);
    }

    @Override
    public String toString() {
        if(this.key_ci != null)
            return "(ci)"+key;
        return key;
    }

    public String origString() {
        return this.key;
    }

    public String lowerCaseString() {
        if(key_ci != null)
            return this.key_ci;
        return this.key.toLowerCase(Locale.ROOT);
    }

    public static Key ofDataTreeKey(String key_str) {
        if(key_str == null || key_str.isEmpty())
            return EMPTY_OR_NULL_KEY;
        return new Key(UNSAFE_DATE_TREE.matcher(Normalizer.normalize(key_str, Normalizer.Form.NFKD))
                .replaceAll("_"), false);
    }

    public static Key ofJSONKey(String key_str) {
        if(key_str == null || key_str.isEmpty())
            return EMPTY_OR_NULL_KEY;
        //JSON String is freely string, trim space only
        return new Key(key_str.trim(), false);
    }

    public static Key ofXMLKey(String key_str) {
        if(key_str == null || key_str.isEmpty())
            return EMPTY_OR_NULL_KEY;
        String rm = UNSAFE_XML.matcher(Normalizer.normalize(key_str, Normalizer.Form.NFKD)).replaceAll((r) ->
            r.group().equals(":") ? "-" : ""
        );
        if(rm.isEmpty())
            throw new IllegalArgumentException("No any valid characters");
        return new Key(rm, false);
    }

    public static Key of(String key) {
        return ofDataTreeKey(key);
    }

    public static Key of(int safe, String key) {
        switch (safe) {
            case SAFE_DATA_TREE_KEY:
                return ofDataTreeKey(key);
            case SAFE_JSON_KEY:
                return ofJSONKey(key);
            case SAFE_XML_KEY:
                return ofXMLKey(key);
            default:
                throw new IllegalArgumentException();
        }
    }
}
