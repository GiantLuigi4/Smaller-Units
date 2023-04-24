package tfc.smallerunits.client.access.tracking.sodium;

import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderList;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;

import java.util.List;
import java.util.Map;

public interface RenderSectionManagerAccessor {
	ChunkRenderList SU$getChunkRenderList();
}
