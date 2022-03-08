package tfc.smallerunits.data.capability;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;
import org.lwjgl.system.MathUtil;
import tfc.smallerunits.UnitSpace;
import tfc.smallerunits.utils.math.Math1D;

public class SUCapability implements ISUCapability, INBTSerializable<CompoundTag> {
	@Override
	public CompoundTag serializeNBT() {
		CompoundTag tag = new CompoundTag();
		for (int i = 0; i < spaceMap.length; i++) {
			UnitSpace unitSpace = spaceMap[i];
			if (unitSpace != null) tag.put(String.valueOf(i), unitSpace.serialize());
		}
		return tag;
	}
	
	@Override
	public void deserializeNBT(CompoundTag nbt) {
	}
	
	private final UnitSpace[] spaceMap = new UnitSpace[16 * 16 * 16];
	
	@Override
	public void removeUnit(BlockPos pos) {
		int indx = ((Math1D.chunkMod(pos.getX(), 16) * 16) + Math1D.chunkMod(pos.getY(), 16)) * 16 + Math1D.chunkMod(pos.getZ(), 16);
		spaceMap[indx] = null;
	}
	
	@Override
	public void makeUnit(BlockPos pos) {
		int indx = ((Math1D.chunkMod(pos.getX(), 16) * 16) + Math1D.chunkMod(pos.getY(), 16)) * 16 + Math1D.chunkMod(pos.getZ(), 16);
		spaceMap[indx] = new UnitSpace();
	}
	
	@Override
	public UnitSpace getOrMakeUnit(BlockPos pos) {
		int indx = ((Math1D.chunkMod(pos.getX(), 16) * 16) + Math1D.chunkMod(pos.getY(), 16)) * 16 + Math1D.chunkMod(pos.getZ(), 16);
		return (spaceMap[indx] == null) ? spaceMap[indx] = new UnitSpace() : spaceMap[indx];
	}
}
