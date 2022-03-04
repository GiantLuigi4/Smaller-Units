package tfc.smallerunits.utils.tracking.data;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.PacketDistributor;
import tfc.smallerunits.Smallerunits;
import tfc.smallerunits.networking.tracking.SSyncSUData;
import tfc.smallerunits.utils.accessor.SUTracked;

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
	
	private boolean isDirty = false;
	
	public void sync() {
		CompoundNBT nbt = serialize();
		for (PlayerEntity player : entity.getEntityWorld().getPlayers()) {
			if (player.getDistanceSq(entity) <= 128) {
				if (player instanceof ServerPlayerEntity) {
					if (!((SUTracked) player).SmallerUnits_setTracking(entity.getUniqueID())) {
						Smallerunits.NETWORK_INSTANCE.send(
								PacketDistributor.TRACKING_ENTITY.with(() -> entity),
								new SSyncSUData(serializeFully(), player.getUniqueID())
						);
					} else if (isDirty) {
						Smallerunits.NETWORK_INSTANCE.send(
								PacketDistributor.TRACKING_ENTITY.with(() -> entity),
								new SSyncSUData(nbt, player.getUniqueID())
						);
					}
				}
			}
		}
	}
	
	private boolean isDirty() {
		return isDirty;
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
				isDirty = true;
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
	
	public void markClean() {
		parameters.forEach((param, data) -> {
			data.isDirty = false;
		});
		isDirty = false;
	}
	
	public CompoundNBT serializeFully() {
		CompoundNBT nbt = new CompoundNBT();
		parameters.forEach((param, data) -> {
			byte[] bytes = param.serialize$(data.value);
			// TODO;
			nbt.putByteArray(param.getName(), bytes);
		});
		return nbt;
	}
	
	public void deserialize(CompoundNBT nbt) {
		for (SUDataParameter<?> suDataParameter : parameters.keySet()) {
			if (nbt.contains(suDataParameter.getName())) {
				byte[] bytes = nbt.getByteArray(suDataParameter.getName());
				(((DataInfo<Object>) parameters.get(suDataParameter)).value) = suDataParameter.deserialize(bytes);
			}
		}
	}
}
