The rule of equals and compareTo in this implementation.
======

Java Language Specification (JLS) requires that the `compareTo` method must be consistent with the `equals` method. This means that if two objects are considered equal by the `equals` method, they must also return zero when compared using the `compareTo` method.

## Java Standard `compareTo()` Contract

For a class implementing `Comparable<T>`, the `compareTo()` method must follow:

### 1. Ordering Rules

| Property                    | What it means (informally)                                                                                                                                                                          |
| --------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Antisymmetry**            | `sgn(x.compareTo(y)) == -sgn(y.compareTo(x))` for all non-null `x`, `y`.<br>If `x` says it’s less than `y`, then `y` must say it’s greater than `x`; if one says they’re equal, the other must too. |
| **Transitivity**            | If `x.compareTo(y) > 0` **and** `y.compareTo(z) > 0`, then `x.compareTo(z) > 0` (similarly for `< 0`).                                                                                              |
| **Consistency with itself** | The sign of `x.compareTo(y)` must not change while neither `x` nor `y` is modified and no information used in the comparison is mutated.                                                            |

### 2. Relationship with `equals`

* If `x.compareTo(y) == 0`, then **it is strongly recommended** (but not strictly required) that `x.equals(y)` is `true`.
  *Why?* Collections like `TreeSet` and `TreeMap` rely on `compareTo` for uniqueness; violating this recommendation can produce odd behavior (e.g., duplicate “equal” objects).

* The converse is **not required**—two objects can be equal according to `equals` yet return a non-zero `compareTo` (though you risk breaking the contract of sorted containers).

| Case | `compareTo()` | `equals()` | Valid?           | Recommended?           |
| ---- | ------------- | ---------- | ---------------- | ---------------------- |
| 1    | `== 0`        | `== false` | ✅ Yes            | ⚠️ Not recommended     |
| 2    | `== 0`        | `== true`  | ✅ Yes            | ✅ Strongly recommended |
| 3    | `!= 0`        | `== true`  | ✅ Legal but rare | ⚠️ Can cause bugs      |
| 4    | `!= 0`        | `== false` | ✅ Yes            | ✅ Normal               |

### 3. Null Handling

* Passing `null` **must** throw `NullPointerException`. The contract explicitly says that `compareTo` should regard `null` as illegal—unlike some comparator strategies that treat `null` as first or last.

### 4. Exceptions

* Aside from `NullPointerException`, a well-behaved `compareTo` should be side-effect free and must not throw other unchecked exceptions for ordinary valid inputs.

---

## Idiomatic Implementation Checklist

1. **Use the same key fields as `equals`** (and in the same priority order).
2. **Return only -1, 0, 1 when practical**—but any negative/zero/positive int is allowed.
3. **Beware of integer overflow** when subtracting numeric fields. Prefer `Integer.compare(a, b)` or `Long.compare(...)`.
4. **Maintain immutability for ordering fields** or document clearly if mutation affects ordering.

## How the order of Value and CompareTo is defined

The Value object is multi-poly-type, which means it can be compared to other values of the same type or different types. The `compareTo` method is defined to handle these comparisons in a way that respects the natural ordering of the values.
On other side, the `equals` compares the equivalent of data type and its value, so the result may differ from `compareTo`. The Value is should never be "key" of Map but a member of a Set.

These below rules are design for best suited with multiples data types:

### The rules of compareTo

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

> Generally, in ESQL Data, the order of types' comparison is: (NULL) < Negative Number < Boolean <= Positive number <= (String or convertible to string) - (Object) - (LOB).

The order of Types and its value for comparing is defined as follows (from lowest to highest):

1. NULL: `null` values of any type are considered less than any non-null value. But the result of comparing between null to other types is dependent on the target value's type.
  * To `null` values: 0 always. But v.equals() only returns true if same type class (string vs string, number vs number, etc).
  * All other types: Integer.MIN_VALUE always.

2. Boolean Type: treated as number 0 or 1.
  * Compare to Null: Integer.MAX_VALUE always.
  * Boolean and Numeric types are compared based on their value. For example, Boolean(true) is considered greater than Boolean(false), and greater than Number(0) or Number(-100) as well. Boolean(false) is same as Number(0).
  * Compare to String or NString: False compare to empty string = 0. Boolean true is greater than empty string (1), but less than any non-empty string (-1).
  * All other types: False compare to empty LOB/Data Tree/Array = 0. otherwise -1.
  
3. Numeric Type: Numeric types are compared based on their value.
  * Compare to Null: Integer.MAX_VALUE always.
  * Compare to Boolean: Boolean false/true are treated as numbers 0 or 1.
  * Compare to String or NString: they are converted to their numeric representation (floating point value of 1200 are 1.2E3) and compared as lexicographically.
  * Compare to other numeric types: Using Integer.compare or Long.compare which convertible to Long.
  * All other types: always -1.
  
4. Strings: `TYPE_STRING` and `TYPE_NSTRING` are compared lexicographically themself. They are large than NULL and less than any numeric type. For empty string (treated as `false` of BooleanValue), it is considered less than any non-empty string.
  * Compare to Null: Integer.MAX_VALUE always.
  * For example, "a" < "b", "abc" < "abcd", "abc" < "", and "" < "a", it gives the 1 or -1 result.
  * To LOB types: using SHA-256 lower-case-hexa hash value with lexicographical order.
  * To Data Tree types: using string representation (JSON or XML) with lexicographical order.
  * To other types: converted to String and compared lexicographically.

5. Date and Time Types: they are converted to their numeric representation (the milliseconds since epoch) and compared as numbers when comparing to Number or Boolean or Null.
  * Compare to Null: Integer.MAX_VALUE always.
  * Compare to String: converted to their numeric representation (the milliseconds since epoch) and compared as numbers.
  * Compare to Number or Boolean: based on their numeric representation (milliseconds since epoch).

6. Data Tree Types: `TYPE_DATA_TREE`, `TYPE_XML`, and `TYPE_JSON` are compared together based on their string representation (JSON or XML). They treated as strings.

7. LOB Types: `TYPE_CLOB`, `TYPE_NCLOB`, `TYPE_BLOB`, and `TYPE_BYTES` are compared based on their SHA-256 hash value when comparing to Strings.
  * To Null: Integer.MAX_VALUE always.
  * To String or NString, Data Tree: using SHA-256 lower-case-hexa hash value with lexicographical order.
  * To Number or Boolean or Date Time: always -1.

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
