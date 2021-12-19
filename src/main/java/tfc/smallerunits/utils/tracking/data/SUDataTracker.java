package tfc.smallerunits.utils.tracking.data;

import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.IDataSerializer;

import java.util.HashMap;

public class SUDataTracker {
	private static int id = Integer.MIN_VALUE;
	
	private static <T> SUDataParameter<T> createParameter(IDataSerializer<T> serializer) {
		return new SUDataParameter<>(serializer, id++);
	}
	
	private final HashMap<SUDataParameter<?>, DataInfo<?>> parameters = new HashMap<>();
	private final Entity entity;
	
	public SUDataTracker(Entity entity) {
		this.entity = entity;
	}
	
	public <T> void register(SUDataParameter<T> parameter, T initial) {
		DataInfo<T> info = new DataInfo<>();
		info.isDirty = false;
		info.value = initial;
		parameters.put(parameter, info);
	}
	
	public <T> void set(SUDataParameter<T> parameter, T value) {
		if (parameters.containsKey(parameter)) {
			DataInfo<T> pair = (DataInfo<T>) parameters.get(parameter);
			if (!pair.value.equals(value)) {
				pair.isDirty = true;
				pair.value = value;
			}
		}
		throw new RuntimeException("");
	}
	
	public <T> T get(SUDataParameter<T> parameter) {
		// TODO: exceptions
		return (T) parameters.getOrDefault(parameter, new DataInfo<>()).value;
	}
	
	public CompoundNBT serialize() {
		parameters.forEach((param, data) -> {
			if (data.isDirty) {
				byte[] bytes = param.serialize$(data.value);
				// TODO;
			}
		});
	}
}
a