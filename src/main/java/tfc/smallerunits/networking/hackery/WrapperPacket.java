package tfc.smallerunits.networking.hackery;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

public class WrapperPacket implements Packet {
	private static final Unsafe theUnsafe;
	
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
	
	public WrapperPacket(Object wrapped) {
		if (wrapped instanceof FriendlyByteBuf) wrapped = read((FriendlyByteBuf) wrapped);
		else this.wrapped = wrapped;
	}
	
	public WrapperPacket(FriendlyByteBuf pBuffer) {
		wrapped = read(pBuffer);
	}
	
	@Override
	public void write(FriendlyByteBuf pBuffer) {
		if (wrapped instanceof Packet) {
			pBuffer.writeByteArray(wrapped.getClass().toString().getBytes(StandardCharsets.UTF_8));
			((Packet<?>) wrapped).write(pBuffer);
		}
	}
	
	public Object read(FriendlyByteBuf obj) {
		try {
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
	
	@Override
	public void handle(PacketListener pHandler) {
	}
	
	@Override
	public boolean isSkippable() {
		return false;
	}
}
