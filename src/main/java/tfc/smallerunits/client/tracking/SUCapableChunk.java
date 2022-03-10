package tfc.smallerunits.client.tracking;

import net.minecraft.core.BlockPos;
import tfc.smallerunits.client.render.SUChunkRender;

public interface SUCapableChunk {
	BlockPos[] dirty();
	
	BlockPos[] toRemove();
	
	BlockPos[] forRemoval();
	
	void markDirty(BlockPos pos);
	
	void reset();
	
	void markGone(BlockPos pos);
	
	SUChunkRender getChunkRender();
}
