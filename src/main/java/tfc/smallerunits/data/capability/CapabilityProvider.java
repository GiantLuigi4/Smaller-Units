package tfc.smallerunits.data.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
	private final ISUCapability capability;
	private final LazyOptional<?> optional;
	
	public CapabilityProvider(ISUCapability capability) {
		this.capability = capability;
		optional = LazyOptional.of(() -> capability);
	}
	
	@NotNull
	@Override
	public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		//noinspection unchecked
		return (LazyOptional<T>) optional;
	}
	
	@Override
	public CompoundTag serializeNBT() {
		return capability.serializeNBT();
	}
	
	@Override
	public void deserializeNBT(CompoundTag nbt) {
		capability.deserializeNBT(0, nbt);
	}
}
