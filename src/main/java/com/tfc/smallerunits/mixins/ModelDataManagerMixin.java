package com.tfc.smallerunits.mixins;

import com.tfc.smallerunits.utils.world.client.FakeClientWorld;
import com.tfc.smallerunits.utils.world.server.FakeServerWorld;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelDataManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//TODO: remove this via creating FakeClientWorld
@Mixin(ModelDataManager.class)
public class ModelDataManagerMixin {
	@Inject(at = @At("HEAD"), method = "cleanCaches(Lnet/minecraft/world/World;)V", remap = false, cancellable = true)
	private static void preCleanCaches(World world, CallbackInfo ci) {
		if (world instanceof FakeServerWorld || world instanceof FakeClientWorld) ci.cancel();
	}
}
