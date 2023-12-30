package tfc.smallerunits.plat.itf;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public interface IContextAwareLadder {
	static boolean isBlockALadder(BlockState state, LevelReader pLevel, BlockPos pPos, LivingEntity entity) {
		return state.isLadder(pLevel, pPos, entity);
	}
}
