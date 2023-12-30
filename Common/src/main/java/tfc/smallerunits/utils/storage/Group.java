package tfc.smallerunits.utils.storage;

public class Group<T> {
	T[] array;
	int size;
	
	public Group(int size) {
		array = (T[]) new Object[size * size * size];
		this.size = size;
	}
	
	public T get(int x, int y, int z) {
		return array[(((x * size) + y) * size) + z];
	}
	
	public void set(int x, int y, int z, T t) {
		array[(((x * size) + y) * size) + z] = t;
	}
}
