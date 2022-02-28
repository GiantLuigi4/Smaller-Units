package tfc.smallerunits.networking.util;

import net.minecraft.util.math.BlockPos;
import tfc.smallerunits.utils.compat.vr.SUVRPlayer;

public class HitContext {
	public BlockPos hitPos;
	public SUVRPlayer vrPlayer;
	
	@Override
	public String toString() {
		return "HitContext{" +
				"hitPos=" + hitPos +
				", vrPlayer=" + vrPlayer +
				'}';
	}
}
