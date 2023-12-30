package tfc.smallerunits.networking.hackery;

import net.minecraft.nbt.Tag;

import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class InfoRegistry {
	private static final HashMap<String, Supplier<Tag>> infoSuppliers = new HashMap<>();
	private static final HashMap<String, BiFunction<Tag, NetworkContext, Object>> infoUsers = new HashMap<>();
	private static final HashMap<String, BiConsumer<Object, NetworkContext>> reseters = new HashMap<>();
	
	public static void register(String name, Supplier<Tag> generator, BiFunction<Tag, NetworkContext, Object> handler, BiConsumer<Object, NetworkContext> reseter) {
		infoSuppliers.put(name, generator);
		infoUsers.put(name, handler);
		reseters.put(name, reseter);
	}
	
	public static String[] names() {
		return infoSuppliers.keySet().toArray(new String[0]);
	}
	
	public static Supplier<Tag> supplier(String name) {
		return infoSuppliers.get(name);
	}
	
	public static BiFunction<Tag, NetworkContext, Object> consumer(String name) {
		return infoUsers.get(name);
	}
	
	public static BiConsumer<Object, NetworkContext> reseter(String name) {
		return reseters.get(name);
	}
}
