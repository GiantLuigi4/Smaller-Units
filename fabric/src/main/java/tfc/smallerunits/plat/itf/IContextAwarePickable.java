package tfc.smallerunits.plat.itf;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

public interface IContextAwarePickable {
	ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player);
	
	static ItemStack getCloneStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
		if (state.getBlock() instanceof IContextAwarePickable contextAwarePickable)
			return contextAwarePickable.getCloneItemStack(state, target, level, pos, player);
		return state.getBlock().getCloneItemStack(level, pos, state);
	}
}
