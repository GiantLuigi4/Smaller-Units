package tfc.smallerunits.data.capability;

import net.minecraft.core.BlockPos;

public interface ISUCapability {
	void removeUnit(BlockPos pos);
	void makeUnit(BlockPos pos);
}
