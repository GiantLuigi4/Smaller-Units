package tfc.smallerunits.utils.math;

// from: https://github.com/GiantLuigi4/Smaller-Units/blob/1.16.4/src/main/java/tfc/smallerunits/utils/MathUtils.java
public class Math1D {
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
}
