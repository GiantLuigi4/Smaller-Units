package tfc.smallerunits.utils;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class AddOnlyList<T> extends ArrayList<T> {
    @Override
    public boolean add(T t) {
        return super.add(t);
    }

    @Override
    public T set(int index, T element) {
        throw new RuntimeException();
    }

    @Override
    public void add(int index, T element) {
        throw new RuntimeException();
    }

    @Override
    public T remove(int index) {
        return super.remove(index);
    }

    @Override
    public boolean remove(Object o) {
        return super.remove(o);
    }

    @Override
    public void clear() {
        throw new RuntimeException("");
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new RuntimeException("");
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        throw new RuntimeException("");
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        throw new RuntimeException("");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new RuntimeException("");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new RuntimeException("");
    }

    @NotNull
    @Override
    public ListIterator<T> listIterator(int index) {
        return super.listIterator(index);
    }

    @NotNull
    @Override
    public ListIterator<T> listIterator() {
        return super.listIterator();
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return super.iterator();
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        throw new RuntimeException("");
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        throw new RuntimeException("");
    }

    @Override
    public Spliterator<T> spliterator() {
        throw new RuntimeException("");
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        throw new RuntimeException("");
    }

    @Override
    public void replaceAll(UnaryOperator<T> operator) {
        throw new RuntimeException("");
    }

    @Override
    public void sort(Comparator<? super T> c) {
        throw new RuntimeException("");
    }

    @Override
    public Stream<T> stream() {
        throw new RuntimeException("");
    }

    @Override
    public Stream<T> parallelStream() {
        throw new RuntimeException("");
    }
}
