package tfc.smallerunits.utils.platform.registry;

import java.util.function.Supplier;

public abstract class RegistryWrapper<T> {
	String namespace;
	
	public RegistryWrapper(String namespace) {
		this.namespace = namespace;
	}
	
	public abstract void register();
	
	public abstract <E extends T> Supplier<E> register(String regName, Supplier<E> obj);
}
