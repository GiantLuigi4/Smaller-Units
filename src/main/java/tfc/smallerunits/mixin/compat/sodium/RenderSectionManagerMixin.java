//package tfc.smallerunits.mixin.compat.sodium;
//
//import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderList;
//import me.jellysquid.mods.sodium.client.render.chunk.RegionChunkRenderer;
//import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
//import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
//import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;
//import org.spongepowered.asm.mixin.Final;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//import tfc.smallerunits.client.access.tracking.sodium.RenderChunkManagerAccessor;
//import tfc.smallerunits.client.access.tracking.sodium.RenderSectionManagerAccessor;
//
//import java.util.List;
//import java.util.Map;
//
//@Mixin(value = RenderSectionManager.class, remap = false)
//public class RenderSectionManagerMixin implements RenderSectionManagerAccessor {
//	@Shadow
//	@Final
//	private RegionChunkRenderer chunkRenderer;
//
//	@Shadow @Final private ChunkRenderList chunkRenderList;
//
//	@Override
//	public ChunkRenderList getChunkRenderList() {
//		return chunkRenderList;
//	}
//
//	@Override
//	public Iterable<Map.Entry<RenderRegion, List<RenderSection>>> getSortedRegions(ChunkRenderList list, boolean translucent) {
//		return ((RenderChunkManagerAccessor) chunkRenderer).getSortedRegions(list, translucent);
//	}
//}
