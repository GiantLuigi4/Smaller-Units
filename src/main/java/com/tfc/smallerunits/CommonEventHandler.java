package com.tfc.smallerunits;

import com.tfc.smallerunits.block.SmallerUnitBlock;
import com.tfc.smallerunits.block.UnitTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
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
	
	public static void onPlayerInteract(PlayerInteractEvent event) {
		if (!event.isCancelable() || event.getWorld() == null || event.getPlayer() == null) return;
		if (event instanceof PlayerInteractEvent.LeftClickBlock) {
			BlockState state = event.getWorld().getBlockState(event.getPos());
			if (state.getBlock() instanceof SmallerUnitBlock) {
				TileEntity te = event.getWorld().getTileEntity(event.getPos());
				if (!(te instanceof UnitTileEntity)) return;
				UnitTileEntity tileEntity = (UnitTileEntity) te;
				
				if (!((SmallerUnitBlock) state.getBlock()).canBeRemoved(event.getPlayer(), event.getWorld(), tileEntity, event.getPos())) {
					if (!event.getWorld().isRemote) {
						event.setCancellationResult(ActionResultType.SUCCESS);
						event.setCanceled(true);
					}
				}
			}
		}
	}
}
