package tfc.smallerunits;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import tfc.smallerunits.client.render.util.RenderWorld;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.data.storage.RegionPos;
import tfc.smallerunits.data.storage.UnitPallet;
import tfc.smallerunits.data.tracking.RegionalAttachments;
import tfc.smallerunits.logging.Loggers;
import tfc.smallerunits.simulation.chunk.BasicVerticalChunk;
import tfc.smallerunits.simulation.world.ITickerWorld;
import tfc.smallerunits.utils.math.Math1D;

public class UnitSpace {
	// TODO: migrate to chunk class
	public final BlockPos pos;
	public int unitsPerBlock = 16;
	
	public final Level level;
	protected Level myLevel;
	CompoundTag tag;
	private BlockPos myPosInTheLevel;
	public boolean isNatural;
	RenderWorld wld;
	
	public UnitSpace(BlockPos pos, Level level) {
		this.pos = pos;
		this.level = level;

//		for (int x = 0; x < 8; x++) {
//			for (int z = 0; z < 8; z++) {
//				int y = 0;
//				int indx = (((x * 16) + y) * 16) + z;
//				states[indx] = Blocks.COBBLESTONE.defaultBlockState();
//			}
//		}
//		for (int x = 2; x < 6; x++) {
//			for (int y = 1; y < 5; y++) {
//				int z = 2;
//				int indx = (((x * 16) + y) * 16) + z;
//				states[indx] = Blocks.OAK_PLANKS.defaultBlockState();
//			}
//		}
//		for (int x = 2; x < 6; x++) {
//			for (int y = 1; y < 5; y++) {
//				int z = 5;
//				if (x >= 3 && x <= 4 && y >= 2 && y <= 3) continue;
//				int indx = (((x * 16) + y) * 16) + z;
//				states[indx] = Blocks.OAK_PLANKS.defaultBlockState();
//			}
//		}
//		for (int z = 2; z < 6; z++) {
//			for (int y = 1; y < 5; y++) {
//				int x = 2;
//				int indx = (((x * 16) + y) * 16) + z;
//				states[indx] = Blocks.OAK_PLANKS.defaultBlockState();
//			}
//		}
//		for (int z = 2; z < 6; z++) {
//			for (int y = 1; y < 5; y++) {
//				int x = 5;
//				int indx = (((x * 16) + y) * 16) + z;
//				states[indx] = Blocks.OAK_PLANKS.defaultBlockState();
//			}
//		}
//		for (int z = 3; z < 5; z++) {
//			for (int x = 3; x < 5; x++) {
//				int y = 1;
//				int indx = (((x * 16) + y) * 16) + z;
//				states[indx] = Blocks.POLISHED_ANDESITE.defaultBlockState();
//			}
//		}
//		for (int z = 1; z < 7; z++) {
//			if (z <= 5 && z >= 2) continue;
//			for (int x = 1; x < 7; x++) {
//				if (x <= 5 && x >= 2) continue;
//				for (int y = 0; y < 6; y++) {
//					int indx = (((x * 16) + y) * 16) + z;
//					states[indx] = Blocks.OAK_LOG.defaultBlockState();
//				}
//			}
//		}
		unitsPerBlock = 1;
		setUpb(16);
		isNatural = true;
	}
	
	public Level getMyLevel() {
		return myLevel;
	}
	
	public void setUpb(int upb) {
		this.unitsPerBlock = upb;
		myPosInTheLevel = new BlockPos(
//				Math1D.chunkMod(pos.getX(), 512),
//				Math1D.chunkMod(pos.getY(), 512),
//				Math1D.chunkMod(pos.getZ(), 512)
//				Math1D.chunkMod(pos.getX(), 512) * upb,
//				Math1D.chunkMod(pos.getY(), 512) * upb,
//				Math1D.chunkMod(pos.getZ(), 512) * upb
				Math1D.oldChunkMod(pos.getX(), 512) * upb,
				Math1D.oldChunkMod(pos.getY(), 512) * upb,
				Math1D.oldChunkMod(pos.getZ(), 512) * upb
//				Math.abs(pos.getX() % 512) * upb,
//				Math.abs(pos.getY() % 512) * upb,
//				Math.abs(pos.getZ() % 512) * upb
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
		UnitPallet pallet = UnitPallet.fromNBT(tag.getCompound("blocks"));
		loadPallet(pallet);
		if (tag.contains("ticks")) {
			if (myLevel instanceof ITickerWorld) {
				((ITickerWorld) myLevel).loadTicks(tag.getCompound("ticks"));
			}
		}
		CompoundTag tiles = tag.getCompound("tiles");
		for (String pos : tiles.getAllKeys()) {
			String[] strs = pos.split(",");
			BlockPos pos1 = new BlockPos(
					Integer.parseInt(strs[0]),
					Integer.parseInt(strs[1]),
					Integer.parseInt(strs[2])
			);
			BlockEntity be = BlockEntity.loadStatic(
					pos1,
					getBlock(pos1.getX(), pos1.getY(), pos1.getZ()),
					tiles.getCompound(pos)
			);
			if (be == null) continue;
			myLevel.setBlockEntity(be);
		}
		((ITickerWorld) myLevel).setLoaded();
//		setState(new BlockPos(0, 0, 0), Blocks.STONE);
		
		this.tag = null;
		((ITickerWorld) myLevel).setLoaded();
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
				if (r == null) return;
				if (myLevel != null)
					((ITickerWorld) myLevel).clear(myPosInTheLevel, myPosInTheLevel.offset(upb, upb, upb));
				myLevel = r.getServerWorld(level.getServer(), (ServerLevel) level, upb);
//				setState(new BlockPos(0, 0, 0), Blocks.STONE);
			} else if (level instanceof RegionalAttachments) {
				Region r = ((RegionalAttachments) level).SU$getRegion(new RegionPos(pos));
				if (r == null) return;
				if (myLevel != null)
					((ITickerWorld) myLevel).clear(myPosInTheLevel, myPosInTheLevel.offset(upb, upb, upb));
				myLevel = r.getClientWorld(level, upb);
				
				// TODO: allow for optimization?
				wld = new RenderWorld(getMyLevel(), getOffsetPos(new BlockPos(0, 0, 0)), upb);
			}
			loadWorld(tag);
		}
	}
	
	public BlockState[] getBlocks() {
		final BlockState[] states = new BlockState[unitsPerBlock * unitsPerBlock * unitsPerBlock];
		for (int x = 0; x < unitsPerBlock; x++)
			for (int y = 0; y < unitsPerBlock; y++)
				for (int z = 0; z < unitsPerBlock; z++)
					states[(((x * unitsPerBlock) + y) * unitsPerBlock) + z] = myLevel.getBlockState(myPosInTheLevel.offset(x, y, z));
		return states;
	}
	
	public UnitPallet getPallet() {
		return new UnitPallet(this);
	}
	
	public void loadPallet(UnitPallet pallet) {
		final BlockState[] states = new BlockState[unitsPerBlock * unitsPerBlock * unitsPerBlock];
//		for (int i = 0; i < states.length; i++) states[i] = Blocks.AIR.defaultBlockState();
		pallet.acceptStates(states, false);
		try {
			for (int x = 0; x < unitsPerBlock; x++) {
				for (int y = 0; y < unitsPerBlock; y++) {
					for (int z = 0; z < unitsPerBlock; z++) {
						int indx = (((x * unitsPerBlock) + y) * unitsPerBlock) + z;
						if (states[indx] == null) continue;
						if (states[indx] == Blocks.AIR.defaultBlockState()) continue;
						BlockPos pz = getOffsetPos(new BlockPos(x, y, z));
						BasicVerticalChunk vc = (BasicVerticalChunk) myLevel.getChunkAt(pz);
						vc.setBlockFast(new BlockPos(pz.getX(), pz.getY(), pz.getZ()), states[indx]);
//						((BasicCubicChunk) myLevel.getChunkAt(getOffsetPos(new BlockPos(x, y, z)))).setBlockFast(new BlockPos(x, y, z), states[indx]);
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
		return myPosInTheLevel.offset(pos);
	}
	
	public void setFast(int x, int y, int z, BlockState state) {
		BlockPos pz = getOffsetPos(new BlockPos(x, y, z));
		BasicVerticalChunk vc = (BasicVerticalChunk) myLevel.getChunkAt(pz);
		vc.setBlockFast(new BlockPos(x, pz.getY(), z), state);
	}
	
	public void clear() {
		for (int x = 0; x < unitsPerBlock; x++) {
			for (int y = 0; y < unitsPerBlock; y++) {
				for (int z = 0; z < unitsPerBlock; z++) {
					BlockPos pz = getOffsetPos(new BlockPos(x, y, z));
					BasicVerticalChunk vc = (BasicVerticalChunk) myLevel.getChunkAt(pz);
					vc.setBlockFast(new BlockPos(pz.getX() & 15, pz.getY(), pz.getZ() & 15), Blocks.AIR.defaultBlockState());
					vc.removeBlockEntity(new BlockPos(pz.getX(), pz.getY(), pz.getZ()));
				}
			}
		}
	}
	
	public BlockEntity[] getTiles() {
		final BlockEntity[] states = new BlockEntity[unitsPerBlock * unitsPerBlock * unitsPerBlock];
		for (int x = 0; x < unitsPerBlock; x++)
			for (int y = 0; y < unitsPerBlock; y++)
				for (int z = 0; z < unitsPerBlock; z++)
					states[(((x * unitsPerBlock) + y) * unitsPerBlock) + z] = myLevel.getBlockEntity(myPosInTheLevel.offset(x, y, z));
		return states;
	}
	
	public CompoundTag serialize() {
		CompoundTag tag = new CompoundTag();
		if (unitsPerBlock == 0) return null;
		if (this.myLevel == null) {
			return this.tag; // TODO: figure out why this happens
		}
		tag.putInt("x", pos.getX());
		tag.putInt("y", pos.getY());
		tag.putInt("z", pos.getZ());
		tag.putInt("upb", unitsPerBlock);
		UnitPallet pallet = new UnitPallet(this);
		tag.put("blocks", pallet.toNBT());
		if (myLevel instanceof ITickerWorld)
			tag.put("ticks", ((ITickerWorld) myLevel).getTicksIn(myPosInTheLevel, myPosInTheLevel.offset(unitsPerBlock, unitsPerBlock, unitsPerBlock)));
		CompoundTag tiles = new CompoundTag();
		for (int x = 0; x < unitsPerBlock; x++) {
			for (int y = 0; y < unitsPerBlock; y++) {
				for (int z = 0; z < unitsPerBlock; z++) {
					BlockEntity be = myLevel.getBlockEntity(getOffsetPos(new BlockPos(x, y, z)));
					if (be != null) {
						tiles.put(be.getBlockPos().toShortString().replace(" ", ""), be.saveWithFullMetadata());
					}
				}
			}
		}
		tag.put("tiles", tiles);
		tag.putBoolean("natural", isNatural);
		return tag;
	}
	
	public RenderWorld getRenderWorld() {
		return wld;
	}
}
