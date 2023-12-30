package tfc.smallerunits.plat.itf;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public interface IContextAwareScaffold {
	static boolean isBlockAScaffold(BlockState state, LevelReader pLevel, BlockPos pPos, LivingEntity entity) {
		return state.is(Blocks.SCAFFOLDING);
	}
}
