package tfc.smallerunits.simulation.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.simulation.block.ParentLookup;
import tfc.smallerunits.utils.BreakData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public interface ITickerLevel {
	int getUPB();
	
	void handleRemoval();
	
	void SU$removeEntity(Entity pEntity);
	
	void SU$removeEntity(UUID uuid);
	
	Level getParent();
	
	Region getRegion();
	
	ParentLookup getLookup();
	
	Iterable<Entity> getAllEntities();
	
	Tag getTicksIn(BlockPos myPosInTheLevel, BlockPos offset);
	
	void setLoaded();
	
	void loadTicks(CompoundTag ticks);
	
	void clear(BlockPos myPosInTheLevel, BlockPos offset);
	
	void setFromSync(ChunkPos cp, int cy, int x, int y, int z, BlockState state, ArrayList<BlockPos> placesBlocks, HashMap<SectionPos, ChunkAccess> chunkCache);
	
	default HashMap<Integer, BreakData> getBreakData() {
		return null;
	}
	
	void invalidateCache(BlockPos pos);
	
	ChunkAccess getChunk(int i, int i1, int i2, ChunkStatus pRequiredStatus, boolean pLoad);
	
	void markRenderDirty(BlockPos pLevelPos);
	
	int randomTickCount();
}
