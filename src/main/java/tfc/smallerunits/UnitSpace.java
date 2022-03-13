package tfc.smallerunits;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.storage.ServerLevelData;
import org.jetbrains.annotations.Nullable;
import tfc.smallerunits.data.storage.UnitPallet;
import tfc.smallerunits.logging.Loggers;
import tfc.smallerunits.simulation.chunk.BasicVerticalChunk;
import tfc.smallerunits.simulation.world.TickerServerWorld;

import java.util.ArrayList;
import java.util.List;

public class UnitSpace {
	// TODO: migrate to chunk class
	public final BlockPos pos;
	public int unitsPerBlock = 16;
	
	public final Level myLevel;
	public Level level;
	BlockPos myPosInTheLevel;
	
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
		unitsPerBlock = 8;
		try {
			myLevel = new TickerServerWorld(
					Minecraft.getInstance().getSingleplayerServer(),
					(ServerLevelData) Minecraft.getInstance().getSingleplayerServer().getLevel(Level.OVERWORLD).getLevelData(),
					Level.OVERWORLD, // TODO:
					Minecraft.getInstance().getSingleplayerServer().getLevel(Level.OVERWORLD).dimensionType(),
					new ChunkProgressListener() {
						@Override
						public void updateSpawnPos(ChunkPos pCenter) {
							System.out.println("uh, ok?");
						}
						
						@Override
						public void onStatusChange(ChunkPos pChunkPosition, @Nullable ChunkStatus pNewStatus) {
							System.out.println("OHNO");
						}
						
						@Override
						public void start() {
							System.out.println("conc");
						}
						
						@Override
						public void stop() {
							System.out.println("conc");
						}
					},
					new FlatLevelSource(
							new FlatLevelGeneratorSettings(
									new StructureSettings(false),
									RegistryAccess.builtin().registryOrThrow(Registry.BIOME_REGISTRY)
							).withLayers(
									List.of(new FlatLayerInfo(0, Blocks.AIR)),
									new StructureSettings(false)
							)
					),
					false, 0, new ArrayList<>(), false,
					this
			);
		} catch (Throwable e) {
			RuntimeException ex = new RuntimeException(e.getMessage(), e);
			ex.setStackTrace(e.getStackTrace());
			Loggers.UNITSPACE_LOGGER.error("", e);
			throw ex;
		}
		// TODO: multiply by upb
		myPosInTheLevel = new BlockPos(
//				Math1D.chunkMod(pos.getX(), 512),
//				Math1D.chunkMod(pos.getY(), 512),
//				Math1D.chunkMod(pos.getZ(), 512)
				32, 32, 32
		);
		setState(new BlockPos(0, 0, 0), Blocks.STONE);
	}
	
	public static UnitSpace fromNBT(CompoundTag tag, Level lvl) {
		UnitSpace space = new UnitSpace(
				new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z")),
				lvl
		);
		space.unitsPerBlock = tag.getInt("upb");
		UnitPallet pallet = UnitPallet.fromNBT(tag.getCompound("blocks"));
		space.loadPallet(pallet);
		// TODO: multiply by upb
		return space;
	}
	
	public CompoundTag serialize() {
		CompoundTag tag = new CompoundTag();
		tag.putInt("x", pos.getX());
		tag.putInt("y", pos.getY());
		tag.putInt("z", pos.getZ());
		tag.putInt("upb", unitsPerBlock);
		UnitPallet pallet = new UnitPallet(this);
		tag.put("blocks", pallet.toNBT());
		return tag;
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
		myPosInTheLevel = new BlockPos(
//				Math1D.chunkMod(pos.getX(), 512),
//				Math1D.chunkMod(pos.getY(), 512),
//				Math1D.chunkMod(pos.getZ(), 512)
				32, 32, 32
		);
		final BlockState[] states = new BlockState[unitsPerBlock * unitsPerBlock * unitsPerBlock];
		for (int i = 0; i < states.length; i++) states[i] = Blocks.AIR.defaultBlockState();
		pallet.acceptStates(states);
		try {
			for (int x = 0; x < unitsPerBlock; x++) {
				for (int y = 0; y < unitsPerBlock; y++) {
					for (int z = 0; z < unitsPerBlock; z++) {
						int indx = (((x * unitsPerBlock) + y) * unitsPerBlock) + z;
						if (states[indx] == Blocks.AIR.defaultBlockState()) continue;
						BlockPos pz = getOffsetPos(new BlockPos(x, y, z));
						BasicVerticalChunk vc = (BasicVerticalChunk) myLevel.getChunkAt(pz);
						vc.setBlockFast(new BlockPos(x, pz.getY(), z), states[indx]);
//						((BasicCubicChunk) myLevel.getChunkAt(getOffsetPos(new BlockPos(x, y, z)))).setBlockFast(new BlockPos(x, y, z), states[indx]);
					}
				}
			}
			for (int x = 0; x < 8; x++) {
				for (int z = 0; z < 8; z++) {
					setState(new BlockPos(x, 0, z), Blocks.COBBLESTONE);
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
}
