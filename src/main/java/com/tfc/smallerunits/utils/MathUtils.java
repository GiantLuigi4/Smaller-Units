package com.tfc.smallerunits.utils;

public class MathUtils {
	public static float chunkMod(float val, float mod) {
		return val > 0 ? val % mod : ((mod - val) % mod);
	}
	
	public static float getChunkOffset(float val, float size) {
		return val >= 0 ? val / size : (val / size) - 1;
	}
}
