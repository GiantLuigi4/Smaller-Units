package com.tfc.smallerunits.mixins;

import com.tfc.smallerunits.helpers.ContainerMixinHelper;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.client.CCloseWindowPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetHandler.class)
public class ServerPlayNetHandlerMixin {
	@Shadow
	public ServerPlayerEntity player;
	
	@Inject(at = @At("HEAD"), cancellable = true, method = "processCloseWindow(Lnet/minecraft/network/play/client/CCloseWindowPacket;)V")
	public void preClose(CCloseWindowPacket packetIn, CallbackInfo ci) {
		if (!ContainerMixinHelper.getNaturallyClosable(player.openContainer)) {
			ContainerMixinHelper.setNaturallyClosable(player.openContainer, true);
			player.closeContainer();
		}
	}
}
