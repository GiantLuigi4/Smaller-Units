package tfc.smallerunits.mixins;

import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CapabilityProvider.class)
public interface CapabilityProviderAccessor {
	@Accessor
	@Mutable
	void setBaseClass(Class<?> baseClass);

	@Accessor
	void setCapabilities(CapabilityDispatcher capabilities);

	@Accessor
	void setValid(boolean valid);
}
