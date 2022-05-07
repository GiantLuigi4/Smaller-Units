package tfc.smallerunits.data.capability;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;
import tfc.smallerunits.UnitSpace;

public class SUCapability implements ISUCapability, INBTSerializable<CompoundTag> {
	final Level level;
	
	public SUCapability(Level level) {
		this.level = level;
	}
	
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
			spaceMap[Integer.parseInt(allKey)] = UnitSpace.fromNBT(tag.getCompound(allKey), level);
		}
	}
	
	private int countSpacesFilled = 0;
	private final UnitSpace[] spaceMap = new UnitSpace[16 * 16 * 16];
	
	@Override
	public void setUnit(BlockPos pos, UnitSpace space) {
//		int indx = ((Math1D.chunkMod(pos.getX(), 16) * 16) + Math1D.chunkMod(pos.getY(), 16)) * 16 + Math1D.chunkMod(pos.getZ(), 16);
		int indx = (((pos.getX() & 15) * 16) + (pos.getY() & 15)) * 16 + (pos.getZ() & 15);
		spaceMap[indx] = space;
	}
	
	@Override
	public void removeUnit(BlockPos pos) {
//		int indx = ((Math1D.chunkMod(pos.getX(), 16) * 16) + Math1D.chunkMod(pos.getY(), 16)) * 16 + Math1D.chunkMod(pos.getZ(), 16);
		int indx = (((pos.getX() & 15) * 16) + (pos.getY() & 15)) * 16 + (pos.getZ() & 15);
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
		return (spaceMap[indx] == null) ? spaceMap[indx] = new UnitSpace(pos, level) : spaceMap[indx];
	}
	
	@Override
	public UnitSpace[] getUnits() {
		return spaceMap;
	}
	
	@Override
	public UnitSpace getUnit(BlockPos pos) {
//		int indx = (((pos.getX() & 15) * 16) + (pos.getY() & 15)) * 16 + (pos.getZ() & 15);
		int indx = (((pos.getX() & 15) * 16) + (pos.getY() & 15)) * 16 + (pos.getZ() & 15);
		return spaceMap[indx];
	}
}
