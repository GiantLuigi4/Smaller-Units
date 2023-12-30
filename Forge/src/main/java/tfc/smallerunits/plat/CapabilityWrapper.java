package tfc.smallerunits.plat;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;

public class CapabilityWrapper {
	CapabilityDispatcher dispatcher;
	
	public CapabilityWrapper(CapabilityDispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}
	
	public void deserializeNBT(CompoundTag capabilities) {
		dispatcher.deserializeNBT(capabilities);
	}
	
	public CompoundTag serializeNBT() {
		return dispatcher.serializeNBT();
	}
}
