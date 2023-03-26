package tfc.smallerunits.utils.selection;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class SingletonList<T> implements Collection<T> {
	T t;
	
	public SingletonList() {
	}
	
	public void set(T t) {
		this.t = t;
	}
	
	@Override
	public int size() {
		return 1;
	}
	
	@Override
	public boolean isEmpty() {
		return false;
	}
	
	@Override
	public boolean contains(Object o) {
		return o.equals(t);
	}
	
	@NotNull
	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			boolean h = true;
			
			@Override
			public boolean hasNext() {
				return h;
			}
			
			@Override
			public T next() {
				h = false;
				return t;
			}
		};
	}
	
	@NotNull
	@Override
	public Object[] toArray() {
		return new Object[]{t};
	}
	
	@NotNull
	@Override
	public <T1> T1[] toArray(@NotNull T1 @NotNull [] a) {
		a = Arrays.copyOf(a, 1);
		//noinspection unchecked
		a[0] = (T1) t;
		return a;
	}
	
	@Override
	public boolean add(T t) {
		return false;
	}
	
	@Override
	public boolean remove(Object o) {
		return false;
	}
	
	@Override
	public boolean containsAll(@NotNull Collection<?> c) {
		for (Object o : c)
			if (!contains(o)) return false;
		return true;
	}
	
	@Override
	public boolean addAll(@NotNull Collection<? extends T> c) {
		return false;
	}
	
	@Override
	public boolean removeAll(@NotNull Collection<?> c) {
		return false;
	}
	
	@Override
	public boolean retainAll(@NotNull Collection<?> c) {
		return false;
	}
	
	@Override
	public void clear() {
	}
}
