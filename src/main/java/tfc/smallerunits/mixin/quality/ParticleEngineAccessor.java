package tfc.smallerunits.mixin.quality;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(ParticleEngine.class)
public class ParticleEngineAccessor implements tfc.smallerunits.data.access.ParticleEngineAccessor {
	@Shadow
	@Mutable
	private Map<ResourceLocation, ParticleProvider<?>> providers;
	
	@Override
	public void copyProviders(ParticleEngine source) {
		providers = ((tfc.smallerunits.data.access.ParticleEngineAccessor) source).getProviders();
	}
	
	@Override
	public Map<ResourceLocation, ParticleProvider<?>> getProviders() {
		return providers;
	}
}
