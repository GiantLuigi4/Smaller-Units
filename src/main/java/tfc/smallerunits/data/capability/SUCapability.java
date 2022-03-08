package tfc.smallerunits.data.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public class SUCapability implements ISUCapability, INBTSerializable<CompoundTag> {
	@Override
	public CompoundTag serializeNBT() {
		System.out.println("h");
		return new CompoundTag();
	}
	
	@Override
	public void deserializeNBT(CompoundTag nbt) {
		System.out.println("h");
	}
}
