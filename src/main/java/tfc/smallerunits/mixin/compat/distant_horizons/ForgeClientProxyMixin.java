//package tfc.smallerunits.mixin.compat.distant_horizons;
//
//import com.seibel.lod.forge.ForgeClientProxy;
//import net.minecraftforge.event.level.BlockEvent;
//import net.minecraftforge.event.level.ChunkEvent;
//import net.minecraftforge.event.level.LevelEvent;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//import tfc.smallerunits.simulation.level.ITickerLevel;
//
///**
// * When something happens to a small world, distant horizons should not acknowledge it
// * that is what this mixin does
// */
//@Mixin(value = ForgeClientProxy.class, remap = false)
//public class ForgeClientProxyMixin {
//	@Inject(at = @At("HEAD"), method = "worldLoadEvent", cancellable = true)
//	public void preLoadWorld(LevelEvent.Load event, CallbackInfo ci) {
//		if (event.getLevel() instanceof ITickerLevel) {
//			ci.cancel();
//		}
//	}
//
//	@Inject(at = @At("HEAD"), method = "worldUnloadEvent", cancellable = true)
//	public void preLoadWorld(LevelEvent.Unload event, CallbackInfo ci) {
//		if (event.getLevel() instanceof ITickerLevel) {
//			ci.cancel();
//		}
//	}
//
//	@Inject(at = @At("HEAD"), method = "chunkLoadEvent", cancellable = true)
//	public void preLoadWorld(ChunkEvent.Load event, CallbackInfo ci) {
//		if (event.getLevel() instanceof ITickerLevel) {
//			ci.cancel();
//		}
//	}
//
//	@Inject(at = @At("HEAD"), method = "blockChangeEvent", cancellable = true)
//	public void preLoadWorld(BlockEvent event, CallbackInfo ci) {
//		if (event.getLevel() instanceof ITickerLevel) {
//			ci.cancel();
//		}
//	}
//
//	@Inject(at = @At("HEAD"), method = "worldSaveEvent", cancellable = true)
//	public void preLoadWorld(LevelEvent.Save event, CallbackInfo ci) {
//		if (event.getLevel() instanceof ITickerLevel) {
//			ci.cancel();
//		}
//	}
//}
