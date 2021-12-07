package tfc.smallerunits.utils.data;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class SUCapabilityStorage implements Capability.IStorage<SUCapability> {
	@Nullable
	@Override
	public INBT writeNBT(Capability<SUCapability> capability, SUCapability instance, Direction side) {
		return instance.serialize();
	}
	
	@Override
	public void readNBT(Capability<SUCapability> capability, SUCapability instance, Direction side, INBT nbt) {
		instance.deserialze(nbt);
	}
}
