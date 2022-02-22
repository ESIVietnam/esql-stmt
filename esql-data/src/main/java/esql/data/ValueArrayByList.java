package esql.data;

import java.util.*;
import java.util.stream.Collectors;

public class ValueArrayByList extends ValueArray {
    private final Types type;
    private final List<Value> list;

    ValueArrayByList(Types type, List<Value> list) {
        this.type = type;
        this.list = list;
    }

    public static ValueArray createUnmodifiableVersion(ValueArrayByList val) {
        return new ValueArrayByList(val.type, Collections.unmodifiableList(val.list));
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    @Override
    public Iterator<Value> iterator() {
        return list.iterator();
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return list.toArray(a);
    }

    @Override
    public boolean add(Value value) {
        if(value == null)
            value = Value.nullOf(getType());
        if(!is(value.getType()))
            throw new IllegalArgumentException("Array of \""+getType()+"\" does not allow add other type of \""
                    +value.getType()+"\"");
        return list.add(value);
    }

    @Override
    public boolean remove(Object o) {
        return list.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return list.contains(c);
    }

    @Override
    public boolean addAll(Collection<? extends Value> c) {
        int i = 0;
        for (Value e:c) {
            if(!is(e.getType()))
                throw new IllegalArgumentException("Array of \""+getType()+"\" does not allow add other type of \""
                        +e.getType()+"\" at index "+i);
            i++;
        }
        return list.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Value> c) {
        int i = 0;
        for (Value e:c) {
            if(!is(e.getType()))
                throw new IllegalArgumentException("Array of \""+getType()+"\" does not allow add other type of \""
                        +e.getType()+"\" at index "+i);
            i++;
        }
        return list.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return list.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return list.retainAll(c);
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public Value get(int index) {
        return list.get(index);
    }

    @Override
    public Value set(int index, Value value) {
        if(value == null)
            value = Value.nullOf(getType());
        if(!is(value.getType()))
            throw new IllegalArgumentException("Array of \""+getType()+"\" does not allow add other type of \""
                    +value.getType()+"\"");
        return list.set(index, value);
    }

    @Override
    public void add(int index, Value value) {
        if(value == null)
            value = Value.nullOf(getType());
        if(!is(value.getType()))
            throw new IllegalArgumentException("Array of \""+getType()+"\" does not allow add other type of \""
                    +value.getType()+"\"");
        list.add(index, value);
    }

    @Override
    public Value remove(int index) {
        return list.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    @Override
    public ListIterator<Value> listIterator() {
        return list.listIterator();
    }

    @Override
    public ListIterator<Value> listIterator(int index) {
        return list.listIterator(index);
    }

    @Override
    public List<Value> subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }

    @Override
    public boolean isTrue() {
        if(list.isEmpty())
            return false;
        return list.stream().allMatch(v -> v.isTrue());
    }

    @Override
    public Value convertTo(Types type) {
        return new ValueArrayByList(type,
                list.stream().map(value -> value.convertTo(type))
                        .collect(Collectors.toList())
        );
    }

    @Override
    public Types getType() {
        return type;
    }

    @Override
    public String stringValue() {
        return ARRAY_PREFIX+list.stream()
                .map(v -> v.stringValue())
                .collect(Collectors.joining(ARRAY_SEPARATOR))+ARRAY_POSTFIX;
    }

    @Override
    public int compareTo(Value o) {
        if(o == null || o.isNull())
            return 1;
        if(!o.isArray())
            throw new IllegalArgumentException("Not an Array");
        if(o.isEmpty())
            return isEmpty() ? 0 : -1;

        Iterator<Value> targetIt = ((ValueArrayByList) o).iterator();
        for (Value v:list) {
            if(targetIt.hasNext()) {
                int c = v.compareTo(targetIt.next());
                if (c != 0)
                    return c;
            }
            else
                return 1;//source larger than target array
        }
        return targetIt.hasNext() ? -1 : 0; //target has next, or identical.
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null)
            return false;
        if(obj == this)
            return true;
        if (obj instanceof ValueArray) {
            if(isEmpty())
                return !((ValueArrayNULLEmpty) obj).isNull();
            if (obj instanceof ValueArrayByList)
                return list.equals(((ValueArrayByList) obj).list);
            return Arrays.equals(list.toArray(), ((ValueArray) obj).toArray());
        }

        //compare primitive
        if (obj instanceof Value[])
            return Arrays.equals(list.toArray(), ((Value[]) obj));
        if (obj instanceof List)
            return list.equals(obj);
        //other never equal
        return false;
    }

    @Override
    public Object[] toObjectArray() {
        List<Object> data = list.stream().map(new ConvertValueToObject())
                .collect(Collectors.toUnmodifiableList());
        return data.toArray();
    }

    @Override
    public List<Object> toObjectList() {
        List<Object> data = list.stream().map(new ConvertValueToObject())
                .collect(Collectors.toUnmodifiableList());
        return data;
    }
}
