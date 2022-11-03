package tfc.smallerunits.utils.storage;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

import java.util.ArrayList;
import java.util.List;

public class VecMap<T> {
	public final int level;
	
	Int2ObjectMap<VecMap<T>> map;
	Int2ObjectMap<T> trueMap;
	
	public VecMap(int layers) {
		this.level = layers;
		if (layers == 0) trueMap = new Int2ObjectRBTreeMap<>();
		else map = new Int2ObjectRBTreeMap<>();
	}
	
	public T get(Vec3i vec) {
		if (level == 2) {
			VecMap<T> map = this.map.getOrDefault(vec.getX(), null);
			if (map == null) {
//				map = new VecMap<>(1);
//				this.map.put(vec.getX(), map);
				return null;
			}
			return map.get(vec);
		}
		if (level == 1) {
			VecMap<T> map = this.map.getOrDefault(vec.getY(), null);
			if (map == null) {
//				map = new VecMap<>(0);
//				this.map.put(vec.getY(), map);
				return null;
			}
			return map.get(vec);
		}
		return trueMap.get(vec.getZ());
	}
	
	public T get(BlockPos vec) {
		if (level == 2) {
			VecMap<T> map = this.map.getOrDefault(vec.getX(), null);
			if (map == null) {
//				map = new VecMap<>(1);
//				this.map.put(vec.getX(), map);
				return null;
			}
			return map.get(vec);
		}
		if (level == 1) {
			VecMap<T> map = this.map.getOrDefault(vec.getY(), null);
			if (map == null) {
//				map = new VecMap<>(0);
//				this.map.put(vec.getY(), map);
				return null;
			}
			return map.get(vec);
		}
		return trueMap.get(vec.getZ());
	}
	
	public T getOrDefault(BlockPos vec, T defaultVal) {
		if (level == 2) {
			VecMap<T> map = this.map.getOrDefault(vec.getX(), null);
			if (map == null) {
//				map = new VecMap<>(1);
//				this.map.put(vec.getX(), map);
				return defaultVal;
			}
			return map.getOrDefault(vec, defaultVal);
		}
		if (level == 1) {
			VecMap<T> map = this.map.getOrDefault(vec.getY(), null);
			if (map == null) {
//				map = new VecMap<>(0);
//				this.map.put(vec.getY(), map);
				return defaultVal;
			}
			return map.getOrDefault(vec, defaultVal);
		}
		return trueMap.getOrDefault(vec.getZ(), defaultVal);
	}
	
	public T put(Vec3i vec, T renderChunk) {
		if (level == 2) {
			VecMap<T> map = this.map.getOrDefault(vec.getX(), null);
			if (map == null) {
				map = new VecMap<>(1);
				this.map.put(vec.getX(), map);
			}
			return map.put(vec, renderChunk);
		}
		if (level == 1) {
			VecMap<T> map = this.map.getOrDefault(vec.getY(), null);
			if (map == null) {
				map = new VecMap<>(0);
				this.map.put(vec.getY(), map);
			}
			return map.put(vec, renderChunk);
		}
		return trueMap.put(vec.getZ(), renderChunk);
	}
	
	public T put(BlockPos vec, T renderChunk) {
		if (level == 2) {
			VecMap<T> map = this.map.getOrDefault(vec.getX(), null);
			if (map == null) {
				map = new VecMap<>(1);
				this.map.put(vec.getX(), map);
			}
			return map.put(vec, renderChunk);
		}
		if (level == 1) {
			VecMap<T> map = this.map.getOrDefault(vec.getY(), null);
			if (map == null) {
				map = new VecMap<>(0);
				this.map.put(vec.getY(), map);
			}
			return map.put(vec, renderChunk);
		}
		return trueMap.put(vec.getZ(), renderChunk);
	}
	
	public Iterable<? extends Vec3i> keySet() {
		List<Vec3i> vecs = new ArrayList<>();
		for (int integer : map.keySet()) {
			VecMap<T> mp = map.get(integer);
			for (int integer1 : mp.map.keySet()) {
				VecMap<T> mp1 = mp.map.get(integer1);
				for (Integer integer2 : mp1.trueMap.keySet()) {
					vecs.add(new Vec3i(integer, integer1, integer2));
				}
			}
		}
		return vecs;
	}
	
	public Iterable<T> values() {
		if (level == 0) {
			return trueMap.values();
		}
		List<T> out = new ArrayList<>();
		for (VecMap<T> value : map.values()) {
			for (T t : value.values()) {
				out.add(t);
			}
		}
		return out;
	}
	
	public void clear() {
		if (level == 0) trueMap.clear();
		else
			for (VecMap<T> value : map.values())
				value.clear();
	}
	
	public boolean containsKey(BlockPos vec) {
		if (level == 2) {
			VecMap<T> map = this.map.getOrDefault(vec.getX(), null);
			if (map == null) {
//				map = new VecMap<>(1);
//				this.map.put(vec.getX(), map);
				return false;
			}
			return map.containsKey(vec);
		}
		if (level == 1) {
			VecMap<T> map = this.map.getOrDefault(vec.getY(), null);
			if (map == null) {
//				map = new VecMap<>(0);
//				this.map.put(vec.getY(), map);
				return false;
			}
			return map.containsKey(vec);
		}
		return trueMap.containsKey(vec.getZ());
	}
	
	public boolean containsKey(Vec3i vec) {
		if (level == 2) {
			VecMap<T> map = this.map.getOrDefault(vec.getX(), null);
			if (map == null) {
//				map = new VecMap<>(1);
//				this.map.put(vec.getX(), map);
				return false;
			}
			return map.containsKey(vec);
		}
		if (level == 1) {
			VecMap<T> map = this.map.getOrDefault(vec.getY(), null);
			if (map == null) {
//				map = new VecMap<>(0);
//				this.map.put(vec.getY(), map);
				return false;
			}
			return map.containsKey(vec);
		}
		return trueMap.containsKey(vec.getZ());
	}
	
	public boolean remove(BlockPos pos) {
		if (level == 2) {
			VecMap<T> map = this.map.getOrDefault(pos.getX(), null);
			if (map == null) {
//				map = new VecMap<>(1);
//				this.map.put(pos.getX(), map);
				return false;
			}
			return map.remove(pos);
		}
		if (level == 1) {
			VecMap<T> map = this.map.getOrDefault(pos.getY(), null);
			if (map == null) {
//				map = new VecMap<>(0);
//				this.map.put(pos.getY(), map);
				return false;
			}
			return map.remove(pos);
		}
		return trueMap.remove(pos.getZ()) != null;
	}
}