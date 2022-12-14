package tfc.smallerunits.utils.threading;

import java.util.HashSet;
import java.util.function.Supplier;

public class LimitedLocalityThreadLocal<T> {
	ThreadLocal<T> local;
	HashSet<Long> validIds = new HashSet<>();
	Supplier<T> defaultValue = () -> null;
	
	public LimitedLocalityThreadLocal() {
		local = new ThreadLocal<>();
	}
	
	public LimitedLocalityThreadLocal(Supplier<T> defaultValue) {
		local = new ThreadLocal<>();
		this.defaultValue = defaultValue;
	}
	
	public void enableForThread() {
		validIds.add(Thread.currentThread().getId());
	}
	
	public T get() {
		if (validIds.contains(Thread.currentThread().getId())) {
			T t = local.get();
			if (t == null) {
				local.set(t = defaultValue.get());
			}
			return t;
		}
		return defaultValue.get();
	}
}
