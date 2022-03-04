package tfc.smallerunits.utils.tracking.data;

import net.minecraft.util.ResourceLocation;

public class SUDataParameter<T> {
	private final SUDataSerializer<T> serializer;
	private final ResourceLocation location;
	
	protected SUDataParameter(SUDataSerializer<T> serializer, ResourceLocation id) {
		this.serializer = serializer;
		this.location = id;
	}
	
	public byte[] serialize$(Object value) {
		return serialize((T) value);
	}
	
	public byte[] serialize(T value) {
		return serializer.serialize(value);
	}
	
	public T deserialize(byte[] value) {
		return serializer.read(value);
	}
	
	public String getName() {
		return location.toString();
	}
}
