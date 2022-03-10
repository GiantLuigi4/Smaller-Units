package tfc.smallerunits.mixin.data;

import net.minecraft.world.level.Level;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.client.render.SUVBOEmitter;
import tfc.smallerunits.client.tracking.SUCapableWorld;
import tfc.smallerunits.logging.Loggers;

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
		emitter.free();
		if (!FMLEnvironment.production && emitter != null) {
			Loggers.WORLD_LOGGER.info("World " + toString() + " offloaded!");
			emitter = null;
		}
	}
}
