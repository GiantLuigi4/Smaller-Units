package tfc.smallerunits.plat.itf;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public interface IContextAwareLadder {
	boolean isLadder(BlockState state, LevelReader pLevel, BlockPos pPos, LivingEntity entity);
	
	static boolean isBlockALadder(BlockState state, LevelReader pLevel, BlockPos pPos, LivingEntity entity) {
		if (state.getBlock() instanceof IContextAwareLadder ladder)
			return ladder.isLadder(state, pLevel, pPos, entity);
		
		return state.is(BlockTags.CLIMBABLE);
	}
}
