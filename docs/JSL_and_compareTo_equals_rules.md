The rule of equals and compareTo in this implementation.
======

Java Language Specification (JLS) requires that the `compareTo` method must be consistent with the `equals` method. This means that if two objects are considered equal by the `equals` method, they must also return zero when compared using the `compareTo` method.

## Java Standard `compareTo()` Contract

For a class implementing `Comparable<T>`, the `compareTo()` method must follow:

### 1. **Antisymmetry**

```java
Integer a, b;
int result = a.compareTo(b);

// Then:
a.compareTo(b) == -b.compareTo(a)
```

This behavior is **required** to ensure correct behavior in sorting, binary search, and data structures like `TreeSet`, `TreeMap`.

### 2. Consistency with `equals()`

If `compareTo(a, b) == 0`, it **should be consistent** with `a.equals(b)`, though it's **not strictly required**. But Java APIs (e.g., `TreeSet`) often assume it.

## How the order of Value and CompareTo is defined

The Value object is multi-poly-type, which means it can be compared to other values of the same type or different types. The `compareTo` method is defined to handle these comparisons in a way that respects the natural ordering of the values.

```java
public enum Types {
    TYPE_DATE("date"),
    TYPE_TIME("time"),
    TYPE_DATETIME("datetime"),
    TYPE_TIMESTAMP("timestamp"),
    TYPE_BYTE("byte"), //1 byte
    TYPE_UBYTE("ubyte"), //1 byte unsigned integer
    TYPE_SHORT("short"), //2 bytes
    TYPE_USHORT("ushort"), //2 bytes unsigned integer
    TYPE_INT("int"), //4 bytes
    TYPE_UINT("uint"), //4 bytes unsigned integer
    TYPE_LONG("long"), //8 bytes
    TYPE_ULONG("ulong"), //8 bytes unsigned integer (BigInteger)
    TYPE_DECIMAL("decimal"),//big decimal
    TYPE_FLOAT("float"), //or real, but not float in database. 4 bytes
    TYPE_DOUBLE("double"), //8 bytes
    TYPE_BOOLEAN("boolean"), //1 bit or 1 byte
    TYPE_STRING("string"),
    TYPE_NSTRING("nstring"),

    TYPE_DATA_TREE("data-tree"),
    TYPE_XML("xml"), //sub-type of DATA_TREE
    TYPE_JSON("json"),//sub-type of DATA_TREE

    TYPE_CLOB("clob"),
    TYPE_NCLOB("nclob"),
    TYPE_BLOB("blob"),
    TYPE_BYTES("bytes"); //bytes array or binary column, read for rowID data.
}
```

The order of Types and its value for comparing is defined as follows (from lowest to highest):

1. NULL: `null` values of any type are considered less than any non-null value. But the result of comparing between null to other types is dependent on the target value's type.
  * To other null values: 0 always.
  * To String or Data Tree: -1 always.
  * To Number and Boolean and Date Time: Integer.MIN_VALUE always.
  * To LOB types: Integer.MIN_VALUE always.
2. Strings: `TYPE_STRING` and `TYPE_NSTRING` are compared lexicographically themself. They are large than NULL and less than any numeric type. For empty string (treated as `false` of BooleanValue), it is considered less than any non-empty string.
  * For example, "a" < "b", "abc" < "abcd", "abc" < "", and "" < "a", it gives the 1 or -1 result.
  * To null: 1 always.
  * To LOB types: using SHA-256 lower-case-hexa hash value with lexicographical order.
  * To Data Tree types: using string representation (JSON or XML) with lexicographical order.
  * To other types: converted to String and compared lexicographically.
5. Numeric Type and Boolean Type (treated as number 0 or 1): Numeric types are compared based on their value. For example, Boolean(true) is considered greater than Boolean(false) and Number(0) or Number(-100). Boolean(false) is same as Number(0).
  * Compare to String or NString: they are converted to their numeric representation (floating point value of 1200 are 1.2E3) and compared as lexicographically.
  * Compare to Null: Integer.MAX_VALUE always.
  * Compare to other numeric types: they are compared based on their numeric value (the boundary of result is integer 32 bits range).
  * Compare to Data Tree: often -1 because JSON starting with "{" or "<" is greater than any numeric value (ASCII value of '{' is 123 and '<' is 60, which is greater than any digit '0'-'9').
6. Date and Time Types: they are converted to their numeric representation (the milliseconds since epoch) and compared as numbers when comparing to Number or Boolean or Null.
  * Compare to Null: Integer.MAX_VALUE always.
  * Compare to String: converted to their numeric representation (the milliseconds since epoch) and compared as numbers.
  * Compare to Number or Boolean: based on their numeric representation (milliseconds since epoch).
3. Data Tree Types: `TYPE_DATA_TREE`, `TYPE_XML`, and `TYPE_JSON` are compared together based on their string representation (JSON or XML). They treated as strings.
4. LOB Types: `TYPE_CLOB`, `TYPE_NCLOB`, `TYPE_BLOB`, and `TYPE_BYTES` are compared based on their SHA-256 hash value when comparing to Strings.
* To Null: Integer.MAX_VALUE always.
* To String or NString, Data Tree: using SHA-256 lower-case-hexa hash value with lexicographical order.
* To Number or Boolean or Date Time: always -1.
* 
> Note: String "1" is greater than Number(0) but " 1" (space before 1) is less than Number(0). This is because the String is compared lexicographically, and space character has a lower ASCII value than digit '1'.

### Example comparisons

Null values are always less than any non-null value:

```java
//first value is null, second value is not null
Value vNullString = Value.nullOf(Types.TYPE_STRING); //null value
Value vNullNumber = Value.nullOf(Types.TYPE_INT); //null value
Value vNullBoolean = Value.nullOf(Types.TYPE_BOOLEAN); //null value
//value to compare to
Value vStringEmpty = ValueString.valueOf(""); //empty string value
Value vString = ValueString.valueOf("Hello"); //string value
Value vNumber = ValueNumber.valueOf(42); //number value
Value vNumberNegative = ValueNumber.valueOf(Integer.MIN_VALUE); //number value

int result0 = vNullString.compareTo(vNullNumber); // result = 0, null is only equal to null any types
int result1 = vNullString.compareTo(vString); // result < 0 (result = -1), null is less than "Hello"
int result2 = vNullNumber.compareTo(vString); // result < 0 (result = -1), null is less than "Hello"
int result3 = vNullBoolean.compareTo(vString); // result < 0 (result = -1), null is less than "Hello"
int result4 = vNullString.compareTo(vStringEmpty); // result < 0 (result = -1), null is less than "" as well.
int result5 = vNullNumber.compareTo(vNumber); // result < 0 (result = Integer.MIN_VALUE), null is less than 42.
int result6 = vNullNumber.compareTo(vNumberNegative); // result < 0 (result = Integer.MIN_VALUE), null is even less than -2147483648
int result7 = vNullBoolean.compareTo(vNumber); // result < 0 (result = Integer.MIN_VALUE), null is less than "Hello"
```

Same Numeric types comparisons as example:

```java
Value vFalse = ValueBoolean.valueOf(false); //boolean value
Value vTrue = ValueBoolean.valueOf(true); //boolean value
Value v1 = ValueNumber.valueOf(1L); //long value
Value v2 = ValueNumber.valueOf(20); //int value

int result1 = v1.compareTo(v2); // result < 0 (result = -19), v1 is less than v2
int result2 = vTrue.compareTo(v1); // result = 0, vTrue (1) is equal to v1 (1)
int result3 = vTrue.compareTo(v2); // result < 0 (result = -19), vTrue (1) is less than v2 (20)
int result4 = v1.compareTo(vTrue); // result = 0, v1 (1) is equal to vTrue (1)
int result5 = v2.compareTo(v1); // result > 0 (result = 19), v2 (20) is greater than v1 (1)
int result6 = vFalse.compareTo(v1); // result < 0 (result = -1), vFalse (0) is less than v1 (1)
int result7 = vFalse.compareTo(vTrue); // result < 0 (result = -1), vFalse (0) is less than vTrue (1)
int result8 = vFalse.compareTo(v2); // result < 0 (result = -20), vFalse (0) is less than v2 (20)
```
