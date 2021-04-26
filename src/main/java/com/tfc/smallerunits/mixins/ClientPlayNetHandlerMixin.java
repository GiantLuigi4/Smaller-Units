package com.tfc.smallerunits.mixins;

import com.tfc.smallerunits.utils.world.client.FakeClientWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetHandler.class)
public class ClientPlayNetHandlerMixin {
	@Shadow
	private ClientWorld world;
	
	@Shadow
	private Minecraft client;
	
	@Inject(at = @At("HEAD"), method = "handleUpdateTileEntity(Lnet/minecraft/network/play/server/SUpdateTileEntityPacket;)V", cancellable = true)
	public void preUpdateTileEntity(SUpdateTileEntityPacket packetIn, CallbackInfo ci) {
		if (this.client.world instanceof FakeClientWorld) {
			this.world = this.client.world;
			if (this.client.world.getTileEntity(packetIn.getPos()) == null) ci.cancel();
		}
	}
}
