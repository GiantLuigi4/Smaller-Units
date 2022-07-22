package tfc.smallerunits.simulation.world;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.simulation.block.ParentLookup;
import tfc.smallerunits.utils.BreakData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public interface ITickerWorld {
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
	
	void setFromSync(ChunkPos cp, int cy, int x, int y, int z, BlockState state, HashMap<ChunkPos, ChunkAccess> accessHashMap, ArrayList<BlockPos> placesBlocks);
	
	default HashMap<Integer, BreakData> getBreakData() {
		return null;
	}
	
	void invalidateCache(BlockPos pos);
}
