//package tfc.smallerunits.mixin.compat.sodium;
//
//import me.jellysquid.mods.sodium.client.render.chunk.ChunkCameraContext;
//import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderList;
//import me.jellysquid.mods.sodium.client.render.chunk.RegionChunkRenderer;
//import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
//import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
//import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//import tfc.smallerunits.client.access.tracking.sodium.RenderChunkManagerAccessor;
//
//import java.util.List;
//import java.util.Map;
//
//@Mixin(value = RegionChunkRenderer.class, remap = false)
//public abstract class RenderChunkManagerMixin implements RenderChunkManagerAccessor {
//	@Shadow
//	private static Iterable<Map.Entry<RenderRegion, List<RenderSection>>> sortedRegions(ChunkRenderList list, boolean translucent) {
//		return null;
//	}
//
//	@Inject(at = @At("TAIL"), method = "buildDrawBatches")
//	public void postRender(List<RenderSection> sections, BlockRenderPass pass, ChunkCameraContext camera, CallbackInfoReturnable<Boolean> cir) {
//	}
//
//	@Override
//	public Iterable<Map.Entry<RenderRegion, List<RenderSection>>> getSortedRegions(ChunkRenderList list, boolean translucent) {
//		return sortedRegions(list, translucent);
//	}
//}
