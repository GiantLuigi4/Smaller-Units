package tfc.smallerunits.data.capability;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;
import tfc.smallerunits.UnitSpace;

import java.util.ArrayList;
import java.util.List;

public class SUCapability implements ISUCapability, INBTSerializable<CompoundTag> {
	final Level level;
	
	public SUCapability(Level level) {
		this.level = level;
	}
	
	private final List<UnitSpace> spaces = new ArrayList<>();
	
	@Override
	public CompoundTag serializeNBT() {
		CompoundTag tag = new CompoundTag();
		for (int i = 0; i < spaceMap.length; i++) {
			UnitSpace unitSpace = spaceMap[i];
			if (unitSpace != null) {
				CompoundTag tg = unitSpace.serialize();
				if (tg != null)
					tag.put(String.valueOf(i), tg);
			}
		}
		return tag;
	}
	
	@Override
	public void deserializeNBT(CompoundTag tag) {
		for (String allKey : tag.getAllKeys()) {
			spaces.add(spaceMap[Integer.parseInt(allKey)] = UnitSpace.fromNBT(tag.getCompound(allKey), level));
		}
	}
	
	private int countSpacesFilled = 0;
	private final UnitSpace[] spaceMap = new UnitSpace[16 * 16 * 16];
	
	@Override
	public void setUnit(BlockPos pos, UnitSpace space) {
//		int indx = ((Math1D.chunkMod(pos.getX(), 16) * 16) + Math1D.chunkMod(pos.getY(), 16)) * 16 + Math1D.chunkMod(pos.getZ(), 16);
		int indx = (((pos.getX() & 15) * 16) + (pos.getY() & 15)) * 16 + (pos.getZ() & 15);
		spaces.remove(spaceMap[indx]);
		spaces.add(spaceMap[indx] = space);
	}
	
	@Override
	public void removeUnit(BlockPos pos) {
//		int indx = ((Math1D.chunkMod(pos.getX(), 16) * 16) + Math1D.chunkMod(pos.getY(), 16)) * 16 + Math1D.chunkMod(pos.getZ(), 16);
		int indx = (((pos.getX() & 15) * 16) + (pos.getY() & 15)) * 16 + (pos.getZ() & 15);
		spaces.remove(spaceMap[indx]);
		spaceMap[indx] = null;
	}
	
	@Override
	public void makeUnit(BlockPos pos) {
//		int indx = ((Math1D.chunkMod(pos.getX(), 16) * 16) + Math1D.chunkMod(pos.getY(), 16)) * 16 + Math1D.chunkMod(pos.getZ(), 16);
		int indx = (((pos.getX() & 15) * 16) + (pos.getY() & 15)) * 16 + (pos.getZ() & 15);
		spaceMap[indx] = new UnitSpace(pos, level);
	}
	
	@Override
	public UnitSpace getOrMakeUnit(BlockPos pos) {
//		int indx = ((Math1D.chunkMod(pos.getX(), 16) * 16) + Math1D.chunkMod(pos.getY(), 16)) * 16 + Math1D.chunkMod(pos.getZ(), 16);
		int indx = (((pos.getX() & 15) * 16) + (pos.getY() & 15)) * 16 + (pos.getZ() & 15);
		if (spaceMap[indx] == null)
			spaces.add(spaceMap[indx] = new UnitSpace(pos, level));
		return spaceMap[indx];
	}
	
	@Override
	public UnitSpace[] getUnits() {
		try {
			// to avoid spamming synchronization, I'm using a try-catch
			// this forces some more null checking, and occasionally causes extended loops
			// but I believe this should be better for performance
			return spaces.toArray(new UnitSpace[0]);
		} catch (Throwable ignored) {
			return spaceMap;
		}
//		return spaceMap;
	}
	
	@Override
	public UnitSpace getUnit(BlockPos pos) {
//		int indx = (((pos.getX() & 15) * 16) + (pos.getY() & 15)) * 16 + (pos.getZ() & 15);
		int indx = (((pos.getX() & 15) * 16) + (pos.getY() & 15)) * 16 + (pos.getZ() & 15);
		return spaceMap[indx];
	}
}
