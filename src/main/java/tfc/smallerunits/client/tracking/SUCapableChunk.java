package tfc.smallerunits.client.tracking;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import tfc.smallerunits.client.render.SUChunkRender;

import java.util.ArrayList;

public interface SUCapableChunk {
	BlockPos[] SU$dirty();
	
	BlockPos[] SU$toRemove();
	
	BlockPos[] SU$forRemoval();
	
	void SU$markDirty(BlockPos pos);
	
	void SU$reset();
	
	void SU$markGone(BlockPos pos);
	
	SUChunkRender SU$getChunkRender();
	
	ArrayList<BlockEntity> getTiles();
	
	void addTile(BlockEntity be);
}
