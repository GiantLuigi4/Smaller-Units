package tfc.smallerunits.utils.platform.hooks;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public interface IScaffoldBlock {
	static boolean isBlockAScaffold(BlockState state, LevelReader pLevel, BlockPos pPos, LivingEntity entity) {
		return state.is(Blocks.SCAFFOLDING);
	}
}
