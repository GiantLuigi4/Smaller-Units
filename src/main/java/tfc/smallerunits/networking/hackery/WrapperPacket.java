package tfc.smallerunits.networking.hackery;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.function.BiFunction;

public class WrapperPacket implements Packet {
	private static final Unsafe theUnsafe;
	public CompoundTag additionalInfo = null;
	
	static {
		try {
			Field f = Unsafe.class.getDeclaredField("theUnsafe");
			f.setAccessible(true);
			theUnsafe = (Unsafe) f.get(null);
		} catch (Throwable err) {
			throw new RuntimeException(err);
		}
	}
	
	Object wrapped;
	boolean hasRead = false;
	private HashMap<String, Object> objs = new HashMap<>();
	
	public WrapperPacket(FriendlyByteBuf pBuffer) {
		wrapped = read(pBuffer);
	}
	
	public WrapperPacket(Object wrapped) {
		for (String name : InfoRegistry.names()) {
			Tag tg = InfoRegistry.supplier(name).get();
			if (tg != null) {
				if (additionalInfo == null) additionalInfo = new CompoundTag();
				additionalInfo.put(name, tg);
			}
		}
		if (wrapped instanceof FriendlyByteBuf) wrapped = read((FriendlyByteBuf) wrapped);
		else this.wrapped = wrapped;
	}
	
	@Override
	public void write(FriendlyByteBuf pBuffer) {
		if (wrapped instanceof Packet) {
			pBuffer.writeBoolean(additionalInfo != null);
			if (additionalInfo != null) pBuffer.writeNbt(additionalInfo);
			pBuffer.writeByteArray(wrapped.getClass().toString().getBytes(StandardCharsets.UTF_8));
			((Packet<?>) wrapped).write(pBuffer);
		}
	}
	
	public Object read(FriendlyByteBuf obj) {
		try {
			preRead(obj);
			Class<?> clazz = Class.forName(new String(obj.readByteArray()));
			Object obj1 = theUnsafe.allocateInstance(clazz);
			if (obj1 instanceof Packet) {
				for (Constructor<?> constructor : obj1.getClass().getConstructors()) {
					if (constructor.getParameterCount() == 1) {
						if (constructor.getParameters()[0].getType().equals(FriendlyByteBuf.class)) {
							return constructor.newInstance(new FriendlyByteBuf(obj));
						}
					}
				}
			}
		} catch (Throwable err) {
			theUnsafe.throwException(err);
		}
		return null;
	}
	
	public void teardown(NetworkContext connection) {
		for (String s : objs.keySet()) InfoRegistry.reseter(s).accept(objs.get(s), connection);
	}
	
	public void preRead(NetworkContext connection) {
		if (hasRead) return;
		hasRead = true;
		if (additionalInfo != null) {
			for (String allKey : additionalInfo.getAllKeys()) {
				BiFunction<Tag, NetworkContext, Object> consumer = InfoRegistry.consumer(allKey);
				if (consumer != null) objs.put(allKey, consumer.apply(additionalInfo.get(allKey), connection));
			}
		}
	}
	
	private void preRead(FriendlyByteBuf obj) {
		if (obj.readBoolean()) {
			additionalInfo = obj.readNbt();
//			preRead();
		}
	}
	
	@Override
	public void handle(PacketListener pHandler) {
	}
	
	@Override
	public boolean isSkippable() {
		return false;
	}
}
