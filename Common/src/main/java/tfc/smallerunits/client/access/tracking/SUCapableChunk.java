package tfc.smallerunits.client.access.tracking;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;

public interface SUCapableChunk {
	BlockPos[] SU$dirty();
	
	BlockPos[] SU$toRemove();
	
	BlockPos[] SU$forRemoval();
	
	void SU$markDirty(BlockPos pos);
	
	void SU$reset(ArrayList<BlockPos> notDone, ArrayList<BlockPos> notFree);
	
	void SU$markGone(BlockPos pos);
	
	ArrayList<BlockEntity> getTiles();
	
	void addTile(BlockEntity be);
}
