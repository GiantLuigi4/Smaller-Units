package tfc.smallerunits.utils.storage;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GroupMap<T> {
	public final int level;
	
	Int2ObjectMap<GroupMap<T>> map;
	Int2ObjectMap<Group<T>> trueMap;
	
	public GroupMap(int layers) {
		this.level = layers;
//		if (layers == 0) trueMap = new Int2ObjectAVLTreeMap<>();
//		else map = new Int2ObjectAVLTreeMap<>();
		if (layers == 0) trueMap = new Int2ObjectOpenHashMap<>();
		else map = new Int2ObjectOpenHashMap<>();
	}
	
	public T get(Vec3i vec) {
		if (level == 2) {
			GroupMap<T> map = this.map.getOrDefault(vec.getX() >> 4, null);
			if (map == null) {
//				map = new VecMap<>(1);
//				this.map.put(vec.getX() >> 4, map);
				return null;
			}
			return map.get(vec);
		}
		if (level == 1) {
			GroupMap<T> map = this.map.getOrDefault(vec.getY() >> 4, null);
			if (map == null) {
//				map = new VecMap<>(0);
//				this.map.put(vec.getY() >> 4, map);
				return null;
			}
			return map.get(vec);
		}
		Group<T> group = trueMap.get(vec.getZ() >> 4);
		if (group != null)
			return group.get(vec.getX() & 15, vec.getY() & 15, vec.getZ() & 15);
		return null;
	}
	
	public T get(BlockPos vec) {
		if (level == 2) {
			GroupMap<T> map = this.map.getOrDefault(vec.getX() >> 4, null);
			if (map == null) {
//				map = new VecMap<>(1);
//				this.map.put(vec.getX() >> 4, map);
				return null;
			}
			return map.get(vec);
		}
		if (level == 1) {
			GroupMap<T> map = this.map.getOrDefault(vec.getY() >> 4, null);
			if (map == null) {
//				map = new VecMap<>(0);
//				this.map.put(vec.getY() >> 4, map);
				return null;
			}
			return map.get(vec);
		}
		Group<T> group = trueMap.get(vec.getZ() >> 4);
		if (group != null)
			return group.get(vec.getX() & 15, vec.getY() & 15, vec.getZ() & 15);
		return null;
	}
	
	public T getOrDefault(BlockPos vec, T defaultVal) {
		if (level == 2) {
			GroupMap<T> map = this.map.getOrDefault(vec.getX() >> 4, null);
			if (map == null) {
//				map = new VecMap<>(1);
//				this.map.put(vec.getX() >> 4, map);
				return defaultVal;
			}
			return map.getOrDefault(vec, defaultVal);
		}
		if (level == 1) {
			GroupMap<T> map = this.map.getOrDefault(vec.getY() >> 4, null);
			if (map == null) {
//				map = new VecMap<>(0);
//				this.map.put(vec.getY() >> 4, map);
				return defaultVal;
			}
			return map.getOrDefault(vec, defaultVal);
		}
		Group<T> group = trueMap.get(vec.getZ() >> 4);
		if (group != null)
			return group.get(vec.getX() & 15, vec.getY() & 15, vec.getZ() & 15);
		return null;
	}
	
	public T put(Vec3i vec, T renderChunk) {
		if (level == 2) {
			GroupMap<T> map = this.map.getOrDefault(vec.getX() >> 4, null);
			if (map == null) {
				map = new GroupMap<>(1);
				this.map.put(vec.getX() >> 4, map);
			}
			return map.put(vec, renderChunk);
		}
		if (level == 1) {
			GroupMap<T> map = this.map.getOrDefault(vec.getY() >> 4, null);
			if (map == null) {
				map = new GroupMap<>(0);
				this.map.put(vec.getY() >> 4, map);
			}
			return map.put(vec, renderChunk);
		}
		
		Group<T> group = trueMap.get(vec.getZ() >> 4);
		if (group == null)
			trueMap.put(vec.getZ() >> 4, group = new Group<>(16));
		group.set(vec.getX() & 15, vec.getY() & 15, vec.getZ() & 15, renderChunk);
		return renderChunk;
	}
	
	public T put(BlockPos vec, T renderChunk) {
		if (level == 2) {
			GroupMap<T> map = this.map.getOrDefault(vec.getX() >> 4, null);
			if (map == null) {
				map = new GroupMap<>(1);
				this.map.put(vec.getX() >> 4, map);
			}
			return map.put(vec, renderChunk);
		}
		if (level == 1) {
			GroupMap<T> map = this.map.getOrDefault(vec.getY() >> 4, null);
			if (map == null) {
				map = new GroupMap<>(0);
				this.map.put(vec.getY() >> 4, map);
			}
			return map.put(vec, renderChunk);
		}
		Group<T> group = trueMap.get(vec.getZ() >> 4);
		if (group == null)
			trueMap.put(vec.getZ() >> 4, group = new Group<>(16));
		group.set(vec.getX() & 15, vec.getY() & 15, vec.getZ() & 15, renderChunk);
		return renderChunk;
	}
	
	public Iterable<? extends Vec3i> keySet() {
		// TODO: do this
		List<Vec3i> vecs = new ArrayList<>();
		for (int integer : map.keySet()) {
			GroupMap<T> mp = map.get(integer);
			for (int integer1 : mp.map.keySet()) {
				GroupMap<T> mp1 = mp.map.get(integer1);
				for (Integer integer2 : mp1.trueMap.keySet()) {
					vecs.add(new Vec3i(integer, integer1, integer2));
				}
			}
		}
		return vecs;
	}
	
	public Iterable<T> values() {
		if (level == 0) {
			Set<T> out = new HashSet<>();
			for (Group<T> value : trueMap.values()) {
				for (T t : value.array) {
					if (t != null)
						out.add(t);
				}
			}
			return out;
		}
		Set<T> out = new HashSet<>();
		for (GroupMap<T> value : map.values()) {
			for (T t : value.values()) {
				out.add(t);
			}
		}
		return out;
	}
	
	public void clear() {
		if (level == 0) trueMap.clear();
		else
			for (GroupMap<T> value : map.values())
				value.clear();
	}
	
	public boolean containsKey(BlockPos vec) {
		if (level == 2) {
			GroupMap<T> map = this.map.getOrDefault(vec.getX() >> 4, null);
			if (map == null) {
//				map = new VecMap<>(1);
//				this.map.put(vec.getX() >> 4, map);
				return false;
			}
			return map.containsKey(vec);
		}
		if (level == 1) {
			GroupMap<T> map = this.map.getOrDefault(vec.getY() >> 4, null);
			if (map == null) {
//				map = new VecMap<>(0);
//				this.map.put(vec.getY() >> 4, map);
				return false;
			}
			return map.containsKey(vec);
		}
		Group<T> group = trueMap.get(vec.getZ() >> 4);
		if (group == null)
			trueMap.put(vec.getZ() >> 4, group = new Group<>(16));
		return group.get(vec.getX() & 15, vec.getY() & 15, vec.getZ() & 15) != null;
	}
	
	public boolean containsKey(Vec3i vec) {
		if (level == 2) {
			GroupMap<T> map = this.map.getOrDefault(vec.getX() >> 4, null);
			if (map == null) {
//				map = new VecMap<>(1);
//				this.map.put(vec.getX() >> 4, map);
				return false;
			}
			return map.containsKey(vec);
		}
		if (level == 1) {
			GroupMap<T> map = this.map.getOrDefault(vec.getY() >> 4, null);
			if (map == null) {
//				map = new VecMap<>(0);
//				this.map.put(vec.getY() >> 4, map);
				return false;
			}
			return map.containsKey(vec);
		}
		Group<T> group = trueMap.get(vec.getZ() >> 4);
		if (group == null)
			trueMap.put(vec.getZ() >> 4, group = new Group<>(16));
		return group.get(vec.getX() & 15, vec.getY() & 15, vec.getZ() & 15) != null;
	}
	
	public boolean remove(BlockPos pos) {
		if (level == 2) {
			GroupMap<T> map = this.map.getOrDefault(pos.getX() >> 4, null);
			if (map == null) {
//				map = new VecMap<>(1);
//				this.map.put(pos.getX(), map);
				return false;
			}
			return map.remove(pos);
		}
		if (level == 1) {
			GroupMap<T> map = this.map.getOrDefault(pos.getY() >> 4, null);
			if (map == null) {
//				map = new VecMap<>(0);
//				this.map.put(pos.getY(), map);
				return false;
			}
			return map.remove(pos);
		}
		Group<T> group = trueMap.get(pos.getZ() >> 4);
		if (group == null)
			trueMap.put(pos.getZ(), group = new Group<>(16));
		T value = group.get(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
		group.set(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15, null);
		return value != null;
	}
}