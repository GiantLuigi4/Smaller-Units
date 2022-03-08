package tfc.smallerunits.data.capability;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CapabilityProvider implements ICapabilityProvider {
	private final SUCapability capability = new SUCapability();
	
	@NotNull
	@Override
	public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		//noinspection unchecked
		return (LazyOptional<T>) LazyOptional.of(() -> capability);
	}
}
