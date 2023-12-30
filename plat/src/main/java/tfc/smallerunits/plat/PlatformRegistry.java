package tfc.smallerunits.plat;

import java.util.function.Supplier;

public class PlatformRegistry<T> {
	public PlatformRegistry(Class<T> cls, String modid) {
		throw new RuntimeException("Unsupported platform!");
	}
	
	public void register() {
		throw new RuntimeException("Unsupported platform!");
	}
	
	public Supplier<T> register(String name, Supplier<T> value) {
		throw new RuntimeException("Unsupported platform!");
	}
}
