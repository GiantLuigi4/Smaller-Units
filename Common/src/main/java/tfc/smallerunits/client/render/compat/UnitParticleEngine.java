package tfc.smallerunits.client.render.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.texture.TextureManager;
import tfc.smallerunits.plat.itf.access.ParticleEngineAccessor;

public class UnitParticleEngine extends ParticleEngine {
	public UnitParticleEngine(ClientLevel pLevel, TextureManager pTextureManager) {
		super(pLevel, pTextureManager);
		((ParticleEngineAccessor) this).copyProviders(Minecraft.getInstance().particleEngine);
	}
}
