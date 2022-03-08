package tfc.smallerunits.data.capability;

import net.minecraft.core.BlockPos;
import tfc.smallerunits.UnitSpace;

public interface ISUCapability {
	void removeUnit(BlockPos pos);
	void makeUnit(BlockPos pos);
	UnitSpace getOrMakeUnit(BlockPos pos);
}
