package tfc.smallerunits.plat.itf;

import net.minecraft.nbt.CompoundTag;

public interface CapabilityLike {
	
	CompoundTag serializeNBT(CompoundTag to);
	
	void deserializeNBT(CompoundTag nbt);
	
	void deserializeNBT(int index, CompoundTag nbt);
}
