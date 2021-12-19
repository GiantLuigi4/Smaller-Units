package tfc.smallerunits.utils.tracking.data;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.IDataSerializer;

public class SUDataParameter<T> {
	private final SUDataSerializer<T> serializer;
	private final int id;
	
	protected SUDataParameter(SUDataSerializer<T> serializer, int id) {
		this.serializer = serializer;
		this.id = id;
	}
	
	public byte[] serialize$(Object value) {
		return serialize((T)value);
	}
	
	public byte[] serialize(T value) {
		return serializer.serialize(value);
	}
}
a