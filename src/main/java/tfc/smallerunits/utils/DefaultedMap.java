package tfc.smallerunits.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class DefaultedMap<T, V> {
	private final Map<T, V> map;
	private Supplier<V> defaultVal;
	
	public DefaultedMap(Map<T, V> map) {
		this.map = map;
	}
	
	public DefaultedMap() {
		map = new HashMap<>();
	}
	
	public DefaultedMap<T, V> setDefaultVal(Supplier<V> defaultVal) {
		this.defaultVal = defaultVal;
		return this;
	}
	
	public V put(T key, V value) {
		if (map.containsKey(key)) return map.get(key);
		map.put(key, value);
		return value;
	}
	
	public V replace(T key, V value) {
		map.replace(key, value);
		return value;
	}
	
	public V get(T key) {
		if (!map.containsKey(key)) map.put(key, defaultVal.get());
		return map.get(key);
	}
	
	public Collection<V> values() {
		return map.values();
	}
	
	public void forEach(BiConsumer<T, V> consumer) {
		map.forEach(consumer);
	}
}
