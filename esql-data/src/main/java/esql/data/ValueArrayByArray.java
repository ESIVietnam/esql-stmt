package esql.data;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Unmodifiable version of ValueArray, implemented as an internal array storage.
 */
public class ValueArrayByArray extends ValueArray {

    private final Types type;
    private final Value[] array;

    ValueArrayByArray(Types type, Value... array) {
        this.type = type;
        if(array.length == 0)
            this.array = ValueArrayNULLEmpty.EMPTY_ARRAY;
        else
            this.array = array;
    }

    ValueArrayByArray(Types type, Collection<Value> list) {
        this.type = type;
        if(list.isEmpty())
            this.array = ValueArrayNULLEmpty.EMPTY_ARRAY;
        else {
            this.array = new Value[list.size()];
            list.toArray(array);
        }
    }

    @Override
    public int size() {
        return array.length;
    }

    @Override
    public boolean isEmpty() {
        return array.length == 0;
    }

    @Override
    public boolean contains(Object o) {
        return Arrays.stream(array).anyMatch((Predicate<? super Value>) e -> e.equals(o));
    }

    @Override
    public Iterator<Value> iterator() {
        return null;
    }

    @Override
    public Object[] toArray() {
        return Arrays.copyOf(this.array, this.array.length);
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return Arrays.asList(this.array).toArray(a);
    }

    @Override
    public boolean add(Value value) {
        throw new UnsupportedOperationException("Unmodifiable array");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Unmodifiable array");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return Arrays.asList(this.array).containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Value> c) {
        throw new UnsupportedOperationException("Unmodifiable array");
    }

    @Override
    public boolean addAll(int index, Collection<? extends Value> c) {
        throw new UnsupportedOperationException("Unmodifiable array");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Unmodifiable array");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Unmodifiable array");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Unmodifiable array");
    }

    @Override
    public Value get(int index) {
        return array[index];
    }

    @Override
    public Value set(int index, Value element) {
        throw new UnsupportedOperationException("Unmodifiable array");
    }

    @Override
    public void add(int index, Value element) {
        throw new UnsupportedOperationException("Unmodifiable array");
    }

    @Override
    public Value remove(int index) {
        throw new UnsupportedOperationException("Unmodifiable array");
    }

    @Override
    public int indexOf(Object o) {
        return IntStream.range(0, array.length)
                .filter(ix -> array[ix].equals(o))
                .findFirst().orElse(-1);
    }

    @Override
    public int lastIndexOf(Object o) {
        return array.length - IntStream.range(0, array.length)
                .filter(ix -> array[array.length-ix-1].equals(o))
                .findFirst().orElse(-1) - 1;
    }

    @Override
    public ListIterator<Value> listIterator() {
        return new ArrayIterator(array);
    }

    @Override
    public ListIterator<Value> listIterator(int index) {
        if(index < 0 || index >= array.length)
            throw new IndexOutOfBoundsException();
        return new ArrayIterator(index, array);
    }

    @Override
    public List<Value> subList(int fromIndex, int toIndex) {
        return Arrays.asList(array).subList(fromIndex, toIndex);
    }

    @Override
    public boolean isTrue() {
        if(array.length == 0)
            return false;
        return IntStream.range(0, array.length)
                .allMatch(ix -> array[ix].isTrue());
    }

    @Override
    public Value convertTo(Types type) {
        if(getType().equals(type))
            return this;
        return new ValueArrayByList(type,
                Arrays.stream(array).map(value -> value.convertTo(type))
                        .collect(Collectors.toUnmodifiableList())
        );
    }

    @Override
    public Types getType() {
        return type;
    }

    @Override
    public String stringValue() {
        return ARRAY_PREFIX+Arrays.stream(array)
                .map(v -> v.stringValue())
                .collect(Collectors.joining(ARRAY_SEPARATOR))+ARRAY_POSTFIX;
    }

    @Override
    public Object[] toObjectArray() {
        List<Object> data = Arrays.stream(array).map(new ConvertValueToObject())
                .collect(Collectors.toUnmodifiableList());
        return data.toArray();
    }

    @Override
    public List<Object> toObjectList() {
        List<Object> data = Arrays.stream(array).map(new ConvertValueToObject())
                .collect(Collectors.toUnmodifiableList());
        return data;
    }

    @Override
    public int compareTo(Value o) {
        if(o == null || o.isNull())
            return 1;
        if(o instanceof ValueArrayByArray)
            return Arrays.compare(array, ((ValueArrayByArray) o).array);
        //compare array of value
        if (o instanceof ValueArray) {
            Iterator<Value> oit = ((ValueArray) o).iterator();
            for(int i = 0;i<Math.min(array.length, ((ValueArray) o).size());i++) {
                if(!oit.hasNext())
                    return 1;
                int c = array[i].compareTo(oit.next());
                if(c != 0)
                    return c;
            }
            return array.length > ((ValueArray) o).size() ? 1 : array.length < ((ValueArray) o).size() ? -1 : 0;
        }
        //can not compare, always -1
        return -1;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null)
            return false;
        if(obj == this)
            return true;
        if (obj instanceof ValueArrayByArray)
            return Arrays.equals(this.array, ((ValueArrayByArray) obj).array);
        //compare array of value
        if (obj instanceof ValueArray) {
            if (((ValueArray) obj).size() != array.length)
                return false;
            Iterator<Value> oit = ((ValueArray) obj).iterator();
            int not_match = IntStream.range(0, Math.min(array.length,((ValueArray) obj).size()))
                    .filter(i -> oit.hasNext() ? !oit.next().equals(array[i]) : true )
                    .findFirst().orElse(-1);
            return not_match < 0 && array.length == ((ValueArray) obj).size();
        }
        //compare primitive
        if (obj instanceof Value[])
            return Arrays.equals(array, (Value[])obj);
        //other false
        return false;
    }

    static class ArrayIterator implements ListIterator<Value> {

        private int i;
        private final Value[] array;

        ArrayIterator(Value[] array) {
            i = 0;
            this.array = array;
        }

        public ArrayIterator(int index, Value[] array) {
            this.array = array;
            i = index;
        }

        @Override
        public boolean hasNext() {
            return array.length > i;
        }

        @Override
        public Value next() {
            return array[i++];
        }

        @Override
        public boolean hasPrevious() {
            return i > 0;
        }

        @Override
        public Value previous() {
            return array[i--];
        }

        @Override
        public int nextIndex() {
            return i+1;
        }

        @Override
        public int previousIndex() {
            return i-1;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Unmodifiable array");
        }

        @Override
        public void set(Value value) {
            throw new UnsupportedOperationException("Unmodifiable array");
        }

        @Override
        public void add(Value value) {
            throw new UnsupportedOperationException("Unmodifiable array");
        }
    }
}

