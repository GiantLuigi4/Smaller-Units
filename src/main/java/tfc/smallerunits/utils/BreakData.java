package tfc.smallerunits.utils;

import net.minecraft.core.BlockPos;

public class BreakData {
	public final BlockPos pos;
	public final int prog;
	
	public BreakData(BlockPos pos, int prog) {
		this.pos = pos;
		this.prog = prog;
	}
}
