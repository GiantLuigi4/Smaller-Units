package tfc.smallerunits.simulation.level.server.saving;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.saveddata.SavedData;
import tfc.smallerunits.data.access.DimensionDataStorageAccessor;
import tfc.smallerunits.data.storage.RegionPos;
import tfc.smallerunits.data.storage.UnitPallet;
import tfc.smallerunits.simulation.chunk.BasicVerticalChunk;
import tfc.smallerunits.simulation.level.server.TickerServerLevel;

import java.io.File;
import java.util.HashSet;
import java.util.Map;

public class SUSaveWorld {
	private final File file;
	private final TickerServerLevel level;
	private final HashSet<BasicVerticalChunk> chunksToSave = new HashSet<>();
	boolean hasThingsToSave = false;
	
	public SUSaveWorld(File file, TickerServerLevel level) {
		RegionPos rp = level.getRegion().pos;
		String fl = file.toString();
		if (fl.endsWith("/")) fl = fl.substring(0, fl.length() - 1);
		this.file = new File(fl + "/smaller_units/" + "r" + rp.x + "_" + rp.y + "_" + rp.z + "/" + level.getUPB());
		this.level = level;
	}
	
	public void tick() {
		if (!chunksToSave.isEmpty()) {
			if (!file.exists()) file.mkdirs();
			
			for (BasicVerticalChunk basicVerticalChunk : chunksToSave) {
				try {
					if (basicVerticalChunk.isSaveTime(level.getGameTime())) {
						File fl = new File(file + "/" + basicVerticalChunk.getPos().x + "_" + basicVerticalChunk.yPos + "_" + basicVerticalChunk.getPos().z + ".dat");
						if (!fl.exists()) fl.createNewFile();
						
						CompoundTag tag = new CompoundTag();
						
						LevelChunkSection section = basicVerticalChunk.getSection(basicVerticalChunk.yPos);
						if (!section.hasOnlyAir()) {
							PalettedContainer<BlockState> statePalettedContainer = section.getStates();
							statePalettedContainer.acquire();
							
							UnitPallet pallet = new UnitPallet();
							for (int x = 0; x < 15; x++) {
								for (int y = 0; y < 15; y++) {
									for (int z = 0; z < 15; z++) {
										BlockState state = section.getBlockState(x, y, z);
										if (state.isAir() && state.getFluidState().isEmpty()) continue;
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
								if (be != null) tiles.put("" + indx, be);
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
						
						tag.putString("version", "1.0");
						
						NbtIo.writeCompressed(tag, fl);
					}
				} catch (Throwable ignored) {
					ignored.printStackTrace();
				}
			}
			
			chunksToSave.clear();
		}
		
		if ((level.getGameTime() % 1000) == 0) {
			try {
				boolean isUnsaved = false;
				for (Map.Entry<String, SavedData> stringSavedDataEntry : ((DimensionDataStorageAccessor) level.getChunkSource().getDataStorage()).getStorage().entrySet()) {
					if (stringSavedDataEntry.getValue().isDirty()) {
						isUnsaved = true;
						break;
					}
				}
				
				if (isUnsaved) {
					File fl = new File(file + ".dat");
					if (!fl.exists()) {
						if (fl.getParentFile().exists())
							fl.createNewFile();
						else return;
					}
					
					CompoundTag tag = new CompoundTag();
					{
						CompoundTag saveDat = new CompoundTag();
						for (Map.Entry<String, SavedData> stringSavedDataEntry : ((DimensionDataStorageAccessor) level.getChunkSource().getDataStorage()).getStorage().entrySet()) {
							// TODO: caching
							CompoundTag tg = new CompoundTag();
							CompoundTag s = stringSavedDataEntry.getValue().save(tg);
							if (s != null) {
								saveDat.put(
										stringSavedDataEntry.getKey(),
										s
								);
							}
						}
						tag.put("save_data", saveDat);
					}
					
					CompoundTag caps = level.getCaps().serializeNBT();
					if (!caps.isEmpty()) tag.put("capabilities", caps);
					
					NbtIo.writeCompressed(tag, fl);
				}
			} catch (Throwable ignored) {
				ignored.printStackTrace();
			}
		}
	}
	
	public void markForSave(BasicVerticalChunk basicVerticalChunk) {
		hasThingsToSave = true;
		if (chunksToSave.add(basicVerticalChunk)) {
			basicVerticalChunk.updateModificationTime(level.getLevelData().getGameTime());
		}
	}
}
