package com.tfc.smallerunits.mixins;

import com.tfc.smallerunits.helpers.ContainerMixinHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({PlayerEntity.class, ServerPlayerEntity.class})
public class PlayerEntityMixin {
	@Inject(at = @At("HEAD"), cancellable = true, method = "closeScreen()V")
	public void preCloseScreen(CallbackInfo ci) {
		if (!ContainerMixinHelper.getNaturallyClosable(((PlayerEntity) (Object) this).openContainer) && ci.isCancellable()) {
			ci.cancel();
		}
	}
}
