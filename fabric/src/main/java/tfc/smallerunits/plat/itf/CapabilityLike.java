package tfc.smallerunits.plat.itf;

import net.minecraft.nbt.CompoundTag;

public interface CapabilityLike extends dev.onyxstudios.cca.api.v3.component.ComponentV3 {
	
	@Override
	default void readFromNbt(CompoundTag tag) {
		deserializeNBT(tag);
	}
	
	@Override
	default void writeToNbt(CompoundTag tag) {
		serializeNBT(tag);
	}
	
	CompoundTag serializeNBT(CompoundTag to);
	
	void deserializeNBT(CompoundTag nbt);
	
	void deserializeNBT(int index, CompoundTag nbt);
}
