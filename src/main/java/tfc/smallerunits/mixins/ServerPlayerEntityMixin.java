package tfc.smallerunits.mixins;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.helpers.ContainerMixinHelper;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
	@Shadow
	public int currentWindowId;
	
	@Inject(at = @At("HEAD"), cancellable = true, method = "closeContainer()V")
	public void preCloseScreen(CallbackInfo ci) {
		if (!ContainerMixinHelper.getNaturallyClosable(((PlayerEntity) (Object) this).openContainer) && ci.isCancellable()) {
			ci.cancel();
		}
	}

//	Container lastOpenContainer = null;
//	int lastWindowId = 0;

//	@Inject(at = @At("HEAD"), method = "tick()V")
//	public void preTick(CallbackInfo ci) {
//		lastOpenContainer = ((PlayerEntity) (Object) this).openContainer;
//		lastWindowId = currentWindowId;
//	}

//	@Inject(at = @At("HEAD"), method = "tick()V")
//	public void postTick(CallbackInfo ci) {
//		if (!ContainerMixinHelper.getNaturallyClosable(lastOpenContainer)) {
//			((PlayerEntity) (Object) this).openContainer = lastOpenContainer;
//			currentWindowId = lastWindowId;
//		}
//	}
}
