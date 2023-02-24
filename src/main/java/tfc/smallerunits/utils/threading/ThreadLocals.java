package tfc.smallerunits.utils.threading;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ThreadLocals {
	public static ThreadLocal<BlockPos.MutableBlockPos> posLocal = ThreadLocal.withInitial(BlockPos.MutableBlockPos::new);
	public static ThreadLocal<BlockEntity> be = new ThreadLocal<>();
}
