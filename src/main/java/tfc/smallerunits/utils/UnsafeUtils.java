package tfc.smallerunits.utils;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class UnsafeUtils {
	private static final Unsafe theUnsafe;
	
	static {
		try {
			Class<Unsafe> clazz = Unsafe.class;
			Field f = clazz.getDeclaredField("theUnsafe");
			f.setAccessible(true);
			theUnsafe = (Unsafe) f.get(null);
		} catch (Throwable err) {
			throw new RuntimeException("Getting theUnsafe failed");
		}
	}
	
	public static void throwError(Throwable err) {
		theUnsafe.throwException(err);
	}
}
