package tfc.smallerunits.mixins.breaking;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.helpers.SendHelp;
import tfc.smallerunits.utils.data.SUCapabilityManager;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public class MinecraftMixin {
	@Shadow
	@Nullable
	public PlayerController playerController;
	
	@Shadow
	@Nullable
	public ClientPlayerEntity player;
	
	// AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
	@Inject(at = @At("HEAD"), method = "sendClickBlockToController", cancellable = true)
	public void preClick(boolean leftClick, CallbackInfo ci) {
		if (SendHelp.doStuff(leftClick, playerController, player, ci)) {
			RayTraceResult result = Minecraft.getInstance().objectMouseOver;
			if (result.getType() == RayTraceResult.Type.BLOCK) {
				if (result instanceof BlockRayTraceResult) {
					UnitTileEntity tileEntity = SUCapabilityManager.getUnitAtBlock(player.getEntityWorld(), ((BlockRayTraceResult) result).getPos());
					if (tileEntity == null) return;
					playerController.clickBlock(tileEntity.getPos(), ((BlockRayTraceResult) result).getFace());
				}
			}
		}
	}
}
