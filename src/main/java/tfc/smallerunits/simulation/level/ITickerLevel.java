package tfc.smallerunits.simulation.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.data.storage.RegionPos;
import tfc.smallerunits.data.tracking.RegionalAttachments;
import tfc.smallerunits.networking.hackery.NetworkingHacks;
import tfc.smallerunits.simulation.block.ParentLookup;
import tfc.smallerunits.utils.BreakData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public interface ITickerLevel {
	static void update(Level level, BlockPos blockPos) {
		for (int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				for (int z = -1; z <= 1; z++) {
					RegionPos pos = new RegionPos(new BlockPos(
							blockPos.getX() - x * 15,
							blockPos.getY() - y * 15,
							blockPos.getZ() - z * 15
					));
//					Region r = ((RegionalAttachments) this).SU$getRegion(new RegionPos(blockPos));
					Region r = ((RegionalAttachments) level).SU$getRegion(pos);
					if (r == null) return;
					r.updateWorlds(blockPos);
				}
			}
		}
	}
	
	//@formatter:off
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
	void addInteractingEntity(Entity e);
	void removeInteractingEntity(Entity e);
	void ungrab(Player entitiesOfClass);
	boolean chunkExists(SectionPos pos);
	//@formatter:on
	
	default NetworkingHacks.LevelDescriptor getDescriptor() {
		NetworkingHacks.LevelDescriptor parentDescriptor = null;
		if (getParent() instanceof ITickerLevel parentLevel) parentDescriptor = parentLevel.getDescriptor();
		return new NetworkingHacks.LevelDescriptor(getRegion().pos, getUPB(), parentDescriptor);
	}
}
