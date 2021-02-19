package com.tfc.smallerunits.mixins;

import com.tfc.smallerunits.SmallerUnitsTESR;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(Minecraft.class)
public class MinecraftMixin {
	@Inject(at = @At("RETURN"), method = "reloadResources()Ljava/util/concurrent/CompletableFuture;")
	public void onReload(CallbackInfoReturnable<CompletableFuture<Void>> cir) {
		SmallerUnitsTESR.bufferCache.clear();
	}
}
