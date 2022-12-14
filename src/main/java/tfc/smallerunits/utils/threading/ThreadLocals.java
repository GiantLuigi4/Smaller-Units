package tfc.smallerunits.utils.threading;

import net.minecraft.core.BlockPos;

public class ThreadLocals {
	public static LimitedLocalityThreadLocal<BlockPos.MutableBlockPos> posLocal = new LimitedLocalityThreadLocal<>(BlockPos.MutableBlockPos::new);
}
