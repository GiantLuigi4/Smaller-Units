package tfc.smallerunits.plat;

import dev.onyxstudios.cca.api.v3.component.ComponentProvider;
import net.minecraft.nbt.CompoundTag;

public class CapabilityWrapper {
	ComponentProvider provider;
	
	public CapabilityWrapper(Object o) {
		provider = (ComponentProvider) o;
	}
	
	public void deserializeNBT(CompoundTag capabilities) {
		provider.getComponentContainer().fromTag(capabilities);
	}
	
	public CompoundTag serializeNBT() {
		return provider.getComponentContainer().toTag(new CompoundTag());
	}
}
