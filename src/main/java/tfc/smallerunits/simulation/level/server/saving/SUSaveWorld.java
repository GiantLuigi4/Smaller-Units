package tfc.smallerunits.simulation.level.server.saving;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.saveddata.SavedData;
import tfc.smallerunits.data.access.DimensionDataStorageAccessor;
import tfc.smallerunits.data.storage.RegionPos;
import tfc.smallerunits.data.storage.UnitPallet;
import tfc.smallerunits.simulation.chunk.BasicVerticalChunk;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.simulation.level.server.TickerServerLevel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class SUSaveWorld {
	public final File file;
	private final TickerServerLevel level;
	private final HashSet<BasicVerticalChunk> chunksToSave = new HashSet<>();
	boolean hasThingsToSave = false;
	private CompoundTag prevCapsNbt = new CompoundTag();
	
	public SUSaveWorld(File file, TickerServerLevel level) {
		RegionPos rp = level.getRegion().pos;
		String fl = file.toString();
		if (fl.endsWith("/")) fl = fl.substring(0, fl.length() - 1);
		this.file = new File(fl + "/smaller_units/" + "r" + rp.x + "_" + rp.y + "_" + rp.z + "/" + level.getUPB());
		this.level = level;
	}
	
	protected File getFile(ChunkPos ckPos, int y) {
		return new File(file + "/" + ckPos.x + "_" + y + "_" + ckPos.z + ".dat");
	}
	
	public void tick() {
		if (!chunksToSave.isEmpty()) {
			if (!file.exists()) file.mkdirs();
			
			ArrayList<BasicVerticalChunk> chunksSaved = new ArrayList<>();
			for (BasicVerticalChunk basicVerticalChunk : chunksToSave) {
				if (basicVerticalChunk.isSaveTime(level.getGameTime())) {
					try {
						saveChunk(basicVerticalChunk);
						chunksSaved.add(basicVerticalChunk);
					} catch (Throwable ignored) {
					}
				}
			}
			
			chunksToSave.removeAll(chunksSaved);
		}
		
		if ((level.getGameTime() % 1000) == 0) {
			saveLevel();
		}
	}
	
	public void saveAllChunks() {
		for (BasicVerticalChunk basicVerticalChunk : chunksToSave) {
			try {
				saveChunk(basicVerticalChunk);
			} catch (Throwable ignored) {
				ignored.printStackTrace();
			}
		}
		chunksToSave.clear();
	}
	
	private void saveChunk(BasicVerticalChunk basicVerticalChunk) throws IOException {
		try {
			basicVerticalChunk.updateModificationTime(-1);
			
			File fl = getFile(basicVerticalChunk.getPos(), basicVerticalChunk.yPos);
			if (!fl.exists()) fl.createNewFile();
			
			CompoundTag tag = new CompoundTag();
			
			LevelChunkSection section = basicVerticalChunk.getSection(basicVerticalChunk.yPos);
			if (!section.hasOnlyAir()) {
				PalettedContainer<BlockState> statePalettedContainer = section.getStates();
				statePalettedContainer.acquire();
				
				UnitPallet pallet = new UnitPallet();
				for (int x = 0; x < 16; x++) {
					for (int y = 0; y < 16; y++) {
						for (int z = 0; z < 16; z++) {
							BlockState state = section.getBlockState(x, y, z);
							if (state.getBlock().equals(Blocks.AIR) && state.getFluidState().isEmpty()) continue;
							pallet.put(Pair.of(new BlockPos(x, y, z), state));
						}
					}
				}
				
				tag.put("blocks", pallet.toNBT());
				
				statePalettedContainer.release();
			}
			
			{
				CompoundTag tiles = new CompoundTag();
				for (BlockPos pos : basicVerticalChunk.getBlockEntitiesPos()) {
					int indx = ((pos.getX() * 16) + pos.getY()) * 16 + pos.getZ();
					CompoundTag be = basicVerticalChunk.getBlockEntityNbtForSaving(pos);
					if (be != null) {
						be.remove("x");
						be.remove("y");
						be.remove("z");
						
						tiles.put("" + indx, be);
					}
				}
				if (!tiles.isEmpty()) tag.put("tiles", tiles);
			}
			
			{
				CompoundTag caps = basicVerticalChunk.writeCapsToNBT();
				if (!caps.isEmpty()) {
					tag.put("capabilities", caps);
				}
			}
			
			BlockPos origin = new BlockPos(
					basicVerticalChunk.getPos().getBlockX(0),
					basicVerticalChunk.yPos * 16,
					basicVerticalChunk.getPos().getBlockZ(0)
			);
			CompoundTag ticks = level.getTicksIn(origin, origin.offset(16, 16, 16));
			if (!ticks.isEmpty()) tag.put("ticks", ticks);
			
			tag.putInt("version", 0);
			
			NbtIo.writeCompressed(tag, fl);
		} catch (Throwable ignored) {
			ignored.printStackTrace();
		}
	}
	
	public BasicVerticalChunk load(BasicVerticalChunk shell, ChunkPos ckPos, int ckY) {
		File fl = getFile(ckPos, ckY);
		if (!fl.exists()) return shell;
		
		try {
			int ox = ckPos.getBlockX(0);
			int oy = ckY * 16;
			int oz = ckPos.getBlockZ(0);
			
			BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
			CompoundTag tag = NbtIo.readCompressed(fl);
			if (tag.contains("blocks", Tag.TAG_COMPOUND)) {
				UnitPallet pallet = UnitPallet.fromNBT(tag.getCompound("blocks"));
				BlockState[] states = new BlockState[16 * 16 * 16];
				pallet.acceptStates(states, true);
				
				HashMap<SectionPos, ChunkAccess> cache = new HashMap<>();
				for (int x = 0; x < 16; x++) {
					for (int y = 0; y < 16; y++) {
						for (int z = 0; z < 16; z++) {
							pos.set(x + ox, y, z + oz);
							int indx = (((x * 16) + y) * 16) + z;
							BlockState state = states[indx];
							if (
									!state.getBlock().equals(Blocks.AIR) ||
											!state.getFluidState().isEmpty()
							) shell.setBlockFast(pos, state, cache);
						}
					}
				}
			}
			
			if (tag.contains("tiles", Tag.TAG_COMPOUND)) {
				CompoundTag tiles = tag.getCompound("tiles");
				for (int x = 0; x < 16; x++) {
					for (int y = 0; y < 16; y++) {
						for (int z = 0; z < 16; z++) {
							int indx = (((x * 16) + y) * 16) + z;
							String str = String.valueOf(indx);
							if (tiles.contains(str, Tag.TAG_COMPOUND)) {
								CompoundTag tile = tiles.getCompound(str);
								tile.putInt("x", ox + x);
								tile.putInt("y", oy + y);
								tile.putInt("z", oz + z);
								
								BlockPos pz = new BlockPos(ox + x, oy + y, oz + z);
								BlockEntity be = null;
								try {
									be = BlockEntity.loadStatic(pz, shell.getBlockState(new BlockPos(x, y, z)), tile);
								} catch (Exception err) {
									err.printStackTrace();
								}
								if (be == null) continue;
								shell.addBlockEntity$(pz, be);
							}
						}
					}
				}
			}
			
			if (tag.contains("ticks", Tag.TAG_COMPOUND)) {
				((ITickerLevel)shell.level).loadTicks(tag.getCompound("ticks"));
			}
			
			return shell;
		} catch (Throwable ignored) {
			ignored.printStackTrace();
		}
		
		return shell;
	}
	
	public void saveLevel() {
		try {
			for (Map.Entry<String, SavedData> stringSavedDataEntry : ((DimensionDataStorageAccessor) level.getChunkSource().getDataStorage()).getStorage().entrySet()) {
				if (stringSavedDataEntry.getValue().isDirty()) {
					File fl1 = new File(file + "/data/" + stringSavedDataEntry.getKey() + ".dat");
					if (!fl1.exists()) {
						if (!fl1.getParentFile().exists()) fl1.getParentFile().mkdirs();
						fl1.createNewFile();
					}
					stringSavedDataEntry.getValue().save(fl1);
				}
			}
			
			File fl = new File(file + ".dat");
			if (!fl.exists()) {
				if (fl.getParentFile().exists())
					fl.createNewFile();
				else return;
			}
			
			CompoundTag caps = level.getCaps().serializeNBT();
			if (!caps.equals(prevCapsNbt)) {
				prevCapsNbt = caps;
				
				CompoundTag tag = new CompoundTag();
				if (!caps.isEmpty()) tag.put("capabilities", caps);
				NbtIo.writeCompressed(tag, fl);
			}
		} catch (Throwable ignored) {
			ignored.printStackTrace();
		}
	}
	
	public void markForSave(BasicVerticalChunk basicVerticalChunk) {
		hasThingsToSave = true;
		if (chunksToSave.add(basicVerticalChunk)) {
			basicVerticalChunk.updateModificationTime(level.getLevelData().getGameTime());
		}
	}
}
