package tfc.smallerunits.utils.threading;

import net.minecraft.core.BlockPos;

public class ThreadLocals {
	public static ThreadLocal<BlockPos.MutableBlockPos> posLocal = ThreadLocal.withInitial(BlockPos.MutableBlockPos::new);
}
