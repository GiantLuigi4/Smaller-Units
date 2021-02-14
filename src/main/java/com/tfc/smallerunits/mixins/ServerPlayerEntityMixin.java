package com.tfc.smallerunits.mixins;

import com.tfc.smallerunits.helpers.ContainerMixinHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
	@Inject(at = @At("HEAD"), cancellable = true, method = "closeContainer()V")
	public void preCloseScreen(CallbackInfo ci) {
		if (!ContainerMixinHelper.getNaturallyClosable(((PlayerEntity) (Object) this).openContainer) && ci.isCancellable()) {
			ci.cancel();
		}
	}
}
