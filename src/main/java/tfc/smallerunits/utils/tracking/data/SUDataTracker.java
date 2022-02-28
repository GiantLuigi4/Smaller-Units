package tfc.smallerunits.utils.tracking.data;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;

public class SUDataTracker {
	public static <T> SUDataParameter<T> createParameter(SUDataSerializer<T> serializer, ResourceLocation name) {
		return new SUDataParameter<>(serializer, name);
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
			return;
		}
		throw new RuntimeException("Parameter: \"" + parameter.getName() + "\" is not registered");
	}
	
	public <T> T get(SUDataParameter<T> parameter) {
		// TODO: exceptions
		return (T) parameters.getOrDefault(parameter, new DataInfo<>()).value;
	}
	
	public CompoundNBT serialize() {
		CompoundNBT nbt = new CompoundNBT();
		parameters.forEach((param, data) -> {
			if (data.isDirty) {
				byte[] bytes = param.serialize$(data.value);
				// TODO;
				nbt.putByteArray(param.getName(), bytes);
			}
		});
		return nbt;
	}
}
