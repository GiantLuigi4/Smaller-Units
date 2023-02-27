package tfc.smallerunits.utils.platform.registry;

import java.util.function.Supplier;

public abstract class RegistryWrapper<T> {
	String namespace;
	
	public RegistryWrapper(String namespace) {
		this.namespace = namespace;
	}
	
	public abstract void register();
	
	public abstract Supplier<T> register(String regName, Supplier<T> obj);
}
