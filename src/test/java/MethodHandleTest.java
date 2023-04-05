import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;

public class MethodHandleTest {
	private static final MethodHandle handle;
	private static final Unsafe theUnsafe;
	private static final long currentShaderNameId;
	
	static {
		try {
			MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();
			MethodType mt = MethodType.methodType(boolean.class);
			handle = publicLookup.findStatic(Class.forName("net.optifine.Config"), "isShaders", mt);
			
			Class<Unsafe> unsafeClass = Unsafe.class;
			Field f = unsafeClass.getDeclaredField("theUnsafe");
			f.setAccessible(true);
			theUnsafe = (Unsafe) f.get(null);
			currentShaderNameId = theUnsafe.staticFieldOffset(Class.forName("net.optifine.shaders.Shaders").getDeclaredField("currentShaderName"));
		} catch (Throwable ignored) {
			throw new RuntimeException();
		}
	}
	
	public static void main(String[] args) {
		try {
			boolean shadersActive = (boolean) handle.invoke();
			String name = (String) theUnsafe.getObject(null, currentShaderNameId);
		} catch (Throwable ignored) {
		}
	}
}
