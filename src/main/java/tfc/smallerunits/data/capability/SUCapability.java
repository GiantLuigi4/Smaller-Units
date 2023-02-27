package tfc.smallerunits.data.capability;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectBigArrayBigList;
import it.unimi.dsi.fastutil.objects.ObjectBigList;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import tfc.smallerunits.UnitSpace;

public class SUCapability implements ISUCapability, ComponentV3 {
	Level level;
	final ChunkAccess access;
	
	public SUCapability(ChunkAccess access, Level level) {
		this.access = access;
		this.level = level;
	}
	
	private final Int2ObjectMap<UnitSpace[]> spaceMap = new Int2ObjectRBTreeMap<>();
	ObjectBigList<UnitSpace> spaces = new ObjectBigArrayBigList<>();
	UnitSpace[] allSpaces = new UnitSpace[0];
	boolean modified = false;
	
	@Override
	public void readFromNbt(CompoundTag tag) {
		deserializeNBT(tag);
	}
	
	@Override
	public void writeToNbt(CompoundTag tag) {
		CompoundTag nbt = serializeNBT();
		for (String allKey : nbt.getAllKeys()) {
			tag.put(allKey, nbt.get(allKey));
		}
	}
	
	@Override
	public boolean equals(Object o) {
		return o == this;
	}
	
	@Override
	public CompoundTag serializeNBT() {
		CompoundTag tag = new CompoundTag();
		for (Integer i : spaceMap.keySet()) {
			UnitSpace[] spaceMap = this.spaceMap.get((int) i);
			CompoundTag tg0 = new CompoundTag();
			tg0.putInt("version", 0);
			for (int i1 = 0; i1 < spaceMap.length; i1++) {
				UnitSpace unitSpace = spaceMap[i1];
				if (unitSpace != null) {
					CompoundTag tg = unitSpace.serialize();
					if (tg != null)
						tg0.put(String.valueOf(i1), tg);
				}
			}
			tag.put(String.valueOf(i), tg0);
		}
		return tag;
	}
	
	public void deserializeNBT(CompoundTag nbt) {
		deserializeNBT(0, nbt);
	}
	
	@Override
	public void deserializeNBT(int index, CompoundTag tag) {
		// TODO: something may be wrong with this code
		for (String allKey : tag.getAllKeys()) {
			if (allKey.equals("version")) {
				continue;
			}
			CompoundTag nbt = tag.getCompound(allKey);
			if (nbt.contains("version")) {
				deserializeNBT(Integer.parseInt(allKey), nbt);
//				for (String key : nbt.getAllKeys()) {
//					if (!key.equals("version")) {
//					}
//				}
			} else {
				spaces.add(getUnits(index)[Integer.parseInt(allKey)] = UnitSpace.fromNBT(nbt, level));
				modified = true;
//				allSpaces = spaces.toArray(new UnitSpace[0]);
			}
		}
	}
	
	private UnitSpace[] getUnits(int section) {
		if (this.level == null) {
			if (access instanceof LevelChunk lvlChk) {
				this.level = lvlChk.getLevel();
			} else if (access.levelHeightAccessor instanceof Level) {
				this.level = (Level) access.levelHeightAccessor;
			}
		}
		UnitSpace[] spaces = spaceMap.get(section);
		if (spaces == null) spaceMap.put(section, spaces = new UnitSpace[16 * 16 * 16]);
		return spaces;
	}
	
	@Override
	public void setUnit(BlockPos pos, UnitSpace space) {
//		int indx = ((Math1D.chunkMod(pos.getX(), 16) * 16) + Math1D.chunkMod(pos.getY(), 16)) * 16 + Math1D.chunkMod(pos.getZ(), 16);
		int indx = (((pos.getX() & 15) * 16) + (pos.getY() & 15)) * 16 + (pos.getZ() & 15);
		UnitSpace[] spaceMap = getUnits((pos.getY() >> 4) & 31);
		spaces.remove(spaceMap[indx]);
		spaceMap[indx] = space;
//		int relIndex = indx + (((pos.getY() >> 4 ) & 31) * 16 * 16 * 16);
		spaces.add(space);
		modified = true;
	}
	
	@Override
	public void removeUnit(BlockPos pos) {
//		int indx = ((Math1D.chunkMod(pos.getX(), 16) * 16) + Math1D.chunkMod(pos.getY(), 16)) * 16 + Math1D.chunkMod(pos.getZ(), 16);
		int indx = (((pos.getX() & 15) * 16) + (pos.getY() & 15)) * 16 + (pos.getZ() & 15);
		UnitSpace[] spaceMap = getUnits((pos.getY() >> 4) & 31);
		spaces.remove(spaceMap[indx]);
		spaceMap[indx] = null;
		modified = true;
//		int relIndex = indx + (((pos.getY() >> 4 ) & 31) * 16 * 16 * 16);
	}
	
	@Override
	public void makeUnit(BlockPos pos) {
//		int indx = ((Math1D.chunkMod(pos.getX(), 16) * 16) + Math1D.chunkMod(pos.getY(), 16)) * 16 + Math1D.chunkMod(pos.getZ(), 16);
		int indx = (((pos.getX() & 15) * 16) + (pos.getY() & 15)) * 16 + (pos.getZ() & 15);
		UnitSpace[] spaceMap = getUnits((pos.getY() >> 4) & 31);
		spaces.remove(spaceMap[indx]);
		spaceMap[indx] = new UnitSpace(pos, level);
		spaces.add(spaceMap[indx]);
		modified = true;
	}
	
	@Override
	public UnitSpace getOrMakeUnit(BlockPos pos) {
//		int indx = ((Math1D.chunkMod(pos.getX(), 16) * 16) + Math1D.chunkMod(pos.getY(), 16)) * 16 + Math1D.chunkMod(pos.getZ(), 16);
		int indx = (((pos.getX() & 15) * 16) + (pos.getY() & 15)) * 16 + (pos.getZ() & 15);
		UnitSpace[] spaceMap = getUnits((pos.getY() >> 4) & 31);
		if (spaceMap[indx] == null) {
			spaceMap[indx] = new UnitSpace(pos, level);
//			int relIndex = indx + (((pos.getY() >> 4 ) & 31) * 16 * 16 * 16);
			spaces.add(spaceMap[indx]);
			modified = true;
		}
		return spaceMap[indx];
	}
	
	@Override
	public UnitSpace[] getUnits() {
		if (modified) {
			allSpaces = spaces.toArray(new UnitSpace[0]);
			modified = false;
		}
		return allSpaces;
	}
	
	@Override
	public UnitSpace getUnit(BlockPos pos) {
//		int indx = (((pos.getX() & 15) * 16) + (pos.getY() & 15)) * 16 + (pos.getZ() & 15);
		int indx = (((pos.getX() & 15) * 16) + (pos.getY() & 15)) * 16 + (pos.getZ() & 15);
		UnitSpace[] spaceMap = getUnits((pos.getY() >> 4) & 31);
		return spaceMap[indx];
	}
}
