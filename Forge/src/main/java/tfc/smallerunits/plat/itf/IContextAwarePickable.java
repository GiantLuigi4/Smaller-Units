package tfc.smallerunits.plat.itf;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

public interface IContextAwarePickable {
	static ItemStack getCloneStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
		return state.getBlock().getCloneItemStack(state, target, level, pos, player);
	}
}
