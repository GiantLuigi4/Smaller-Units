package tfc.smallerunits.mixin.data;

import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.client.access.tracking.SUCapableWorld;
import tfc.smallerunits.client.render.SUVBOEmitter;
import tfc.smallerunits.logging.Loggers;
import tfc.smallerunits.utils.platform.PlatformUtils;

@Mixin(Level.class)
public class LevelMixin implements SUCapableWorld {
	@Unique
	private SUVBOEmitter emitter = new SUVBOEmitter();
	
	@Override
	public SUVBOEmitter getVBOEmitter() {
		return emitter;
	}
	
	@Inject(at = @At("HEAD"), method = "close")
	public void preClose(CallbackInfo ci) {
		if (emitter != null) emitter.free();
		if (PlatformUtils.isDevEnv()) Loggers.WORLD_LOGGER.info("World " + toString() + " offloaded!");
	}
}
