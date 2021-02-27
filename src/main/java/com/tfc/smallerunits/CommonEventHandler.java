package com.tfc.smallerunits;

import com.tfc.smallerunits.block.SmallerUnitBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class CommonEventHandler {
	public static void onSneakClick(PlayerInteractEvent.RightClickBlock event) {
		PlayerEntity entity = event.getPlayer();
		if (entity.isSneaking()) {
			BlockState state1 = entity.getEntityWorld().getBlockState(event.getPos());
			if (state1.getBlock() instanceof SmallerUnitBlock) {
				event.setCancellationResult(state1.onBlockActivated(entity.world, entity, event.getHand(), event.getHitVec()));
				event.setCanceled(true);
			}
		}
	}
}
