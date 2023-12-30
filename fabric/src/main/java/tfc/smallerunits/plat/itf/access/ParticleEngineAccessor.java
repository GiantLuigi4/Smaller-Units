package tfc.smallerunits.plat.itf.access;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public interface ParticleEngineAccessor {
	void copyProviders(ParticleEngine source);
	
	Int2ObjectMap<ParticleProvider<?>> getProviders();
}
