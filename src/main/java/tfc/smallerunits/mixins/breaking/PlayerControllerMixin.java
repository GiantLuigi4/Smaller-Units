package tfc.smallerunits.mixins.breaking;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.Smallerunits;
import tfc.smallerunits.block.SmallerUnitBlock;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.networking.CBreakLittleBlockStatusPacket;
import tfc.smallerunits.utils.UnitRaytraceContext;
import tfc.smallerunits.utils.UnitRaytraceHelper;
import tfc.smallerunits.utils.data.SUCapabilityManager;

import java.util.Optional;

@Mixin(PlayerController.class)
public class PlayerControllerMixin {
	@Shadow
	@Final
	private Minecraft mc;
	
	@Unique private double breakProgress = 0;
	
	@Inject(at = @At("HEAD"), method = "clickBlock", cancellable = true)
	public void preClickBlock(BlockPos clickedPos, Direction face, CallbackInfoReturnable<Boolean> cir) {
		PlayerEntity player = this.mc.player;
		UnitTileEntity tileEntity = SUCapabilityManager.getUnitAtBlock(player.getEntityWorld(), clickedPos);
		if (tileEntity == null) return;
		BlockState state = tileEntity.getBlockState();
		if (!(state.getBlock() instanceof SmallerUnitBlock)) return;
		
		if (!((SmallerUnitBlock) state.getBlock()).canBeRemoved(player, player.world, tileEntity, clickedPos)) {
			if (!player.world.isRemote) {
				cir.setReturnValue(false);
				return;
			}
		}
		
		Object hitInfo = mc.objectMouseOver.hitInfo;
		UnitRaytraceContext uctx;
		if (hitInfo instanceof UnitRaytraceContext) uctx = (UnitRaytraceContext) hitInfo;
		else uctx = UnitRaytraceHelper.raytraceBlockWithoutShape(tileEntity, player, true, clickedPos, Optional.of(ISelectionContext.forEntity(player)));
		Smallerunits.NETWORK_INSTANCE.sendToServer(new CBreakLittleBlockStatusPacket(clickedPos, uctx.posHit, 0, face)); // 0 == start
		if (breakProgress == 0) state.onBlockClicked(player.world, clickedPos, player);
		cir.setReturnValue(true);
	}
}
a