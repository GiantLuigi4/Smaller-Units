package tfc.smallerunits;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunkSection;
import tfc.smallerunits.client.render.util.RenderWorld;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.data.storage.RegionPos;
import tfc.smallerunits.data.storage.UnitPallet;
import tfc.smallerunits.data.tracking.RegionalAttachments;
import tfc.smallerunits.logging.Loggers;
import tfc.smallerunits.networking.PacketTarget;
import tfc.smallerunits.networking.SUNetworkRegistry;
import tfc.smallerunits.networking.hackery.NetworkingHacks;
import tfc.smallerunits.networking.sync.SyncPacketS2C;
import tfc.smallerunits.simulation.chunk.BasicVerticalChunk;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.utils.config.ServerConfig;
import tfc.smallerunits.utils.math.Math1D;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class UnitSpace {
	// TODO: migrate to chunk class
	public final BlockPos pos;
	public int unitsPerBlock = 16;
	
	public final Level level;
	public RegionPos regionPos;
	protected Level myLevel;
	CompoundTag tag;
	private BlockPos myPosInTheLevel;
	public boolean isNatural;
	RenderWorld wld;
	
	int numBlocks = 0;
	
	public UnitSpace(BlockPos pos, Level level) {
		this.pos = pos;
		this.level = level;
		
		unitsPerBlock = 1;
		setUpb(ServerConfig.SizeOptions.defaultScale);
		isNatural = false;
		
		regionPos = new RegionPos(pos);
	}
	
	public Level getMyLevel() {
		return myLevel;
	}
	
	public void setUpb(int upb) {
		this.unitsPerBlock = upb;
		myPosInTheLevel = new BlockPos(
				Math1D.regionMod(pos.getX()) * upb,
				Math1D.regionMod(pos.getY()) * upb,
				Math1D.regionMod(pos.getZ()) * upb
		);
		myLevel = null;
		tick();
	}
	
	public static UnitSpace fromNBT(CompoundTag tag, Level lvl) {
		UnitSpace space = new UnitSpace(
				new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z")),
				lvl
		);
		space.tag = tag;
		space.unitsPerBlock = tag.getInt("upb");
		space.setUpb(space.unitsPerBlock);
		space.loadWorld(tag);
		if (tag.contains("natural")) space.isNatural = tag.getBoolean("natural");
		// TODO: multiply by upb
		
		if (space.unitsPerBlock == 0)
			Loggers.UNITSPACE_LOGGER.error("A unit space had a UPB of " + space.unitsPerBlock + "; this is not a good thing! Coords: " + space.pos.getX() + ", " + space.pos.getY() + ", " + space.pos.getZ());
		
		return space;
	}
	
	private void loadWorld(CompoundTag tag) {
		if (myLevel == null || tag == null) return;
		
		// ensures that chunks are loaded
		for (int x = 0; x < unitsPerBlock; x += 15) {
			for (int z = 0; z < unitsPerBlock; z += 15) {
				int pX = SectionPos.blockToSectionCoord(x + myPosInTheLevel.getX());
				int pZ = SectionPos.blockToSectionCoord(z + myPosInTheLevel.getZ());
				ChunkAccess chunk = myLevel.getChunk(pX, pZ, ChunkStatus.FULL, true);
				if (chunk == null) continue;
				BasicVerticalChunk vc = (BasicVerticalChunk) chunk;
				
				for (int y = 0; y < unitsPerBlock; y += 15) {
					vc.getSubChunk((y + myPosInTheLevel.getY()) >> 4);
				}
			}
		}
		
		if (tag.contains("blocks", Tag.TAG_COMPOUND)) {
			UnitPallet pallet = UnitPallet.fromNBT(tag.getCompound("blocks"));
			loadPallet(pallet);
		}
		
		if (tag.contains("ticks")) {
			if (myLevel instanceof ITickerLevel) {
				((ITickerLevel) myLevel).loadTicks(tag.getCompound("ticks"));
			}
		}
		
		if (tag.contains("tiles", Tag.TAG_COMPOUND)) {
			CompoundTag tiles = tag.getCompound("tiles");
			for (String pos : tiles.getAllKeys()) {
				String[] strs = pos.split(",");
				BlockPos pos1 = new BlockPos(
						Integer.parseInt(strs[0]),
						Integer.parseInt(strs[1]),
						Integer.parseInt(strs[2])
				);
				// TODO: fix
				BlockEntity be = null;
				try {
					be = BlockEntity.loadStatic(
							pos1,
							myLevel.getBlockState(pos1),
							tiles.getCompound(pos)
					);
				} catch (Exception err) {
					err.printStackTrace();
				}
				if (be == null) continue;
				myLevel.setBlockEntity(be);
			}
			((ITickerLevel) myLevel).setLoaded();
		}
		
		this.tag = null;
	}
	
	/* reason: race conditions */
	public void tick() {
		if (myLevel instanceof ServerLevel) {
//			((ServerLevel) myLevel).tick(() -> true);
		} else if (myLevel == null) {
			int upb = unitsPerBlock;
			if (level instanceof ServerLevel) {
				ChunkMap cm = ((ServerLevel) level).getChunkSource().chunkMap;
				Region r = ((RegionalAttachments) cm).SU$getRegion(new RegionPos(pos));
				if (r == null) {
//					if (level.isLoaded(pos))
//					Loggers.UNITSPACE_LOGGER.error("Region@" + new RegionPos(pos) + " was null");
					return;
				}
				if (myLevel != null)
					((ITickerLevel) myLevel).clear(myPosInTheLevel, myPosInTheLevel.offset(upb, upb, upb));
				myLevel = r.getServerWorld(level.getServer(), (ServerLevel) level, upb);
//				setState(new BlockPos(0, 0, 0), Blocks.STONE);
			} else if (level instanceof RegionalAttachments) {
				Region r = ((RegionalAttachments) level).SU$getRegion(new RegionPos(pos));
				if (r == null) {
					Loggers.UNITSPACE_LOGGER.error("Region@" + new RegionPos(pos) + " was null");
					return;
				}
				if (myLevel != null)
					((ITickerLevel) myLevel).clear(myPosInTheLevel, myPosInTheLevel.offset(upb, upb, upb));
				myLevel = r.getClientWorld(level, upb);
				
				// TODO: allow for optimization?
				wld = new RenderWorld(getMyLevel(), getOffsetPos(new BlockPos(0, 0, 0)), upb);
			}
			loadWorld(tag);
		}
	}
	
	// gets every block within the unit space
	public BlockState[] getBlocks() {
		numBlocks = 0;
		final BlockState[] states = new BlockState[unitsPerBlock * unitsPerBlock * unitsPerBlock];
		BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
		for (int x = 0; x < unitsPerBlock; x++) {
			for (int z = 0; z < unitsPerBlock; z++) {
				int pX = SectionPos.blockToSectionCoord(x + myPosInTheLevel.getX());
				int pZ = SectionPos.blockToSectionCoord(z + myPosInTheLevel.getZ());
				ChunkAccess chunk = myLevel.getChunk(pX, pZ, ChunkStatus.FULL, true);
				if (chunk == null) continue;
				BasicVerticalChunk vc = (BasicVerticalChunk) chunk;
				
				for (int y = 0; y < unitsPerBlock; y++) {
					int sectionIndex = vc.getSectionIndex(y + myPosInTheLevel.getY());
					LevelChunkSection section = vc.getSectionNullable(sectionIndex);
//					if (section == null || section.hasOnlyAir()) {
//						if (y == (y >> 4) << 4) {
//							y += 15;
//						} else {
//							y = ((y >> 4) << 4) + 15;
//						}
//						continue;
//					}
					
					blockPos.set(x + myPosInTheLevel.getX(), y + myPosInTheLevel.getY(), z + myPosInTheLevel.getZ());
					BlockState state = states[(((x * unitsPerBlock) + y) * unitsPerBlock) + z] = vc.getBlockStateSmallOnly(blockPos);
					addState(state);
				}
			}
		}
		return states;
	}
	
	// used for saving
	public UnitPallet getPallet() {
		return new UnitPallet(this);
	}
	
	public void loadPallet(UnitPallet pallet) {
		loadPallet(pallet, null);
	}
	
	public void loadPallet(UnitPallet pallet, HashSet<BlockPos> positionsWithBE) {
		HashMap<SectionPos, ChunkAccess> cache = new HashMap<>();
		final BlockState[] states = new BlockState[unitsPerBlock * unitsPerBlock * unitsPerBlock];
//		for (int i = 0; i < states.length; i++) states[i] = Blocks.AIR.defaultBlockState();
		pallet.acceptStates(states, false);
		try {
			BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
			for (int x = 0; x < unitsPerBlock; x++) {
				for (int z = 0; z < unitsPerBlock; z++) {
					int pX = SectionPos.blockToSectionCoord(x + myPosInTheLevel.getX());
					int pZ = SectionPos.blockToSectionCoord(z + myPosInTheLevel.getZ());
					ChunkAccess chunk = myLevel.getChunk(pX, pZ, ChunkStatus.FULL, true);
					if (chunk == null) continue;
					BasicVerticalChunk vc = (BasicVerticalChunk) chunk;
					
					for (int y = 0; y < unitsPerBlock; y++) {
						int indx = (((x * unitsPerBlock) + y) * unitsPerBlock) + z;
						if (states[indx] == null) continue;
						if (states[indx] == Blocks.AIR.defaultBlockState()) continue;
						pos.set(x, y, z);
						BlockPos pz = getOffsetPos(pos);
						vc.setBlockFast(new BlockPos(pz.getX(), pz.getY(), pz.getZ()), states[indx], cache);
						vc.getSubChunk(pz.getY() >> 4).setUnsaved(true);
						
						addState(states[indx]);
						if (positionsWithBE != null)
							if (states[indx].hasBlockEntity())
								positionsWithBE.add(pz);
					}
				}
			}
		} catch (Throwable e) {
			RuntimeException ex = new RuntimeException(e.getMessage(), e);
			ex.setStackTrace(e.getStackTrace());
			Loggers.UNITSPACE_LOGGER.error("", e);
			throw ex;
		}
	}
	
	public BlockState getBlock(int x, int y, int z) {
		return myLevel.getBlockState(getOffsetPos(new BlockPos(x, y, z)));
	}
	
	public void setState(BlockPos relative, Block block) {
//		int indx = (((relative.getX() * 16) + relative.getY()) * 16) + relative.getZ();
//		states[indx] = block.defaultBlockState();
		BlockState st = block.defaultBlockState();
		myLevel.setBlockAndUpdate(getOffsetPos(relative), st);
	}
	
	public BlockPos getOffsetPos(BlockPos pos) {
		return pos.offset(myPosInTheLevel);
	}
	
	public void setFast(int x, int y, int z, BlockState state) {
		BlockPos pz = getOffsetPos(new BlockPos(x, y, z));
		BasicVerticalChunk vc = (BasicVerticalChunk) myLevel.getChunkAt(pz);
		vc.setBlockFast(new BlockPos(x, pz.getY(), z), state, new HashMap<>());
	}
	
	public void clear() {
		HashMap<SectionPos, ChunkAccess> cache = new HashMap<>();
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		BlockPos.MutableBlockPos posMod = new BlockPos.MutableBlockPos();
		for (int x = 0; x < unitsPerBlock; x++) {
			for (int z = 0; z < unitsPerBlock; z++) {
				int pX = SectionPos.blockToSectionCoord(x + myPosInTheLevel.getX());
				int pZ = SectionPos.blockToSectionCoord(z + myPosInTheLevel.getZ());
				ChunkAccess chunk = myLevel.getChunk(pX, pZ, ChunkStatus.FULL, false);
				if (chunk == null) continue;
				BasicVerticalChunk vc = (BasicVerticalChunk) chunk;
				
				for (int y = 0; y < unitsPerBlock; y++) {
					pos.set(myPosInTheLevel.getX() + x, myPosInTheLevel.getY() + y, myPosInTheLevel.getZ() + z);
					posMod.set(pos.getX() & 15, pos.getY(), pos.getZ() & 15);
					vc.setBlockFast(posMod, Blocks.AIR.defaultBlockState(), cache);
					vc.removeBlockEntity(pos);
				}
			}
		}
	}
	
	public BlockEntity[] getTiles() {
		final BlockEntity[] states = new BlockEntity[unitsPerBlock * unitsPerBlock * unitsPerBlock];
		for (int x = 0; x < unitsPerBlock; x++) {
			for (int z = 0; z < unitsPerBlock; z++) {
				int pX = SectionPos.blockToSectionCoord(x + myPosInTheLevel.getX());
				int pZ = SectionPos.blockToSectionCoord(z + myPosInTheLevel.getZ());
				ChunkAccess chunk = myLevel.getChunk(pX, pZ, ChunkStatus.FULL, false);
				if (chunk == null) continue;
				
				for (int y = 0; y < unitsPerBlock; y++) {
					states[(((x * unitsPerBlock) + y) * unitsPerBlock) + z] = chunk.getBlockEntity(myPosInTheLevel.offset(x, y, z));
				}
			}
		}
		return states;
	}
	
	public CompoundTag serialize() {
		if (unitsPerBlock == 0) return null;
		if (this.myLevel == null) {
			if (level != null) tick();
			if (this.myLevel == null) return this.tag; // TODO: figure out if this still happens
		}
		CompoundTag tag = new CompoundTag();
		
		tag.putInt("x", pos.getX());
		tag.putInt("y", pos.getY());
		tag.putInt("z", pos.getZ());
		tag.putInt("upb", unitsPerBlock);
		tag.putBoolean("natural", isNatural);
		
		return tag;
	}
	
	public RenderWorld getRenderWorld() {
		return wld;
	}
	
	public void sendSync(PacketTarget target) {
		NetworkingHacks.LevelDescriptor descriptor = NetworkingHacks.unitPos.get();
		NetworkingHacks.unitPos.remove();
		SyncPacketS2C pkt = new SyncPacketS2C(this);
		SUNetworkRegistry.send(target, pkt);
		if (descriptor != null)
			NetworkingHacks.setPos(descriptor);
	}
	
	public void removeState(BlockState block) {
		if (!block.isAir()) {
			numBlocks -= 1;
			if (numBlocks < 0) {
				numBlocks = 0; // idk how this would happen
			}
		}
	}
	
	public void addState(BlockState block) {
		if (!block.isAir()) {
			numBlocks += 1;
		}
	}
	
	public boolean isEmpty() {
		// TODO: this doesn't work on client
		return numBlocks <= 0;
	}
	
	public Set<BasicVerticalChunk> getChunks() {
		Set<BasicVerticalChunk> chunks = new HashSet<>();
		if (myLevel == null) return chunks;
		for (int x = 0; x < unitsPerBlock; x += 15) {
			for (int z = 0; z < unitsPerBlock; z += 15) {
				int pX = SectionPos.blockToSectionCoord(x + myPosInTheLevel.getX());
				int pZ = SectionPos.blockToSectionCoord(z + myPosInTheLevel.getZ());
				ChunkAccess chunk = myLevel.getChunk(pX, pZ, ChunkStatus.FULL, true);
				if (chunk == null) continue;
				BasicVerticalChunk vc = (BasicVerticalChunk) chunk;
				
				for (int y = 0; y < unitsPerBlock; y += 15) {
					chunks.add(vc.getSubChunk((y + myPosInTheLevel.getY()) >> 4));
				}
			}
		}
		return chunks;
	}
}
