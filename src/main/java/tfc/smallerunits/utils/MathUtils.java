package tfc.smallerunits.utils;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class MathUtils {
	public static float chunkMod(float val, float mod) {
		return val > 0 ? val % mod : ((mod - val) % mod);
	}
	
	public static int chunkMod(int val, int mod) {
		return val > 0 ? val % mod : ((mod - val) % mod);
	}
	
	public static float getChunkOffset(float val, float size) {
		return val >= 0 ? val / size : (val / size) - 1;
	}
	
	public static int getChunkOffset(int val, int size) {
		return val >= 0 ? val / size : (val / size) - 1;
	}
	
	public static Vector3d getVectorForRotation(float pitch, float yaw) {
		float xRadians = pitch * 0.017453292F; // magic number: how many radians are in a degree
		float yRadians = -yaw * 0.017453292F;
		float cx = MathHelper.cos(xRadians);
		float sx = MathHelper.sin(xRadians);
		float cy = MathHelper.cos(yRadians);
		float sy = MathHelper.sin(yRadians);
		return new Vector3d((double) (sy * cx), (double) (-sx), (double) (cy * cx));
	}
}
