package tfc.smallerunits.utils.config.annoconfg.handle;

import sun.misc.Unsafe;
import tfc.smallerunits.utils.config.annoconfg.util.EnumType;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class UnsafeHandle {
	private static final Unsafe theUnsafe;
	
	static {
		try {
			Field f = Unsafe.class.getDeclaredField("theUnsafe");
			f.setAccessible(true);
			theUnsafe = (Unsafe) f.get(null);
		} catch (Throwable ignored) {
			throw new RuntimeException("AnnoConfg: Failed to acquire an instance of the unsafe.");
		}
	}
	
	private final long offset;
	
	private final Consumer<Object> uploader;
	private final Supplier<Object> getter;
	
	public UnsafeHandle(Field f) {
		this(null, f);
	}
	
	public UnsafeHandle(Object relative, Field f) {
		offset = theUnsafe.staticFieldOffset(f);
		if (relative == null) relative = theUnsafe.staticFieldBase(f);
		Object finalRelative = relative;
		if (f.getType().isPrimitive()) {
			switch (EnumType.forClass(f.getType())) {
				case BYTE -> {
					uploader = (v) -> theUnsafe.putByte(finalRelative, offset, (byte) v);
					getter = () -> theUnsafe.getByte(finalRelative, offset);
				}
				case SHORT -> {
					uploader = (v) -> theUnsafe.putShort(finalRelative, offset, (short) v);
					getter = () -> theUnsafe.getShort(finalRelative, offset);
				}
				case INT -> {
					uploader = (v) -> theUnsafe.putInt(finalRelative, offset, (int) v);
					getter = () -> theUnsafe.getInt(finalRelative, offset);
				}
				case LONG -> {
					uploader = (v) -> theUnsafe.putLong(finalRelative, offset, (long) v);
					getter = () -> theUnsafe.getLong(finalRelative, offset);
				}
				case FLOAT -> {
					uploader = (v) -> theUnsafe.putFloat(finalRelative, offset, (float) v);
					getter = () -> theUnsafe.getFloat(finalRelative, offset);
				}
				case DOUBLE -> {
					uploader = (v) -> theUnsafe.putDouble(finalRelative, offset, (double) v);
					getter = () -> theUnsafe.getDouble(finalRelative, offset);
				}
				case BOOLEAN -> {
					uploader = (v) -> theUnsafe.putBoolean(finalRelative, offset, (boolean) v);
					getter = () -> theUnsafe.getBoolean(finalRelative, offset);
				}
				default -> {
					// TODO: check that I have all primitives?
					uploader = null;
					getter = () -> null;
				}
			}
		} else {
			uploader = (v) -> theUnsafe.putObject(finalRelative, offset, v);
			getter = () -> theUnsafe.getObject(finalRelative, offset);
		}
	}
	
	public void set(Object o) {
		uploader.accept(o);
//		System.out.println(getter.get());
	}
	
	public Object get() {
		return getter.get();
	}
}
