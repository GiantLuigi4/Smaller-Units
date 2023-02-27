package tfc.smallerunits.client.render.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.TextureManager;
import org.jetbrains.annotations.Nullable;
import tfc.smallerunits.data.access.ParticleEngineAccessor;

public class UnitParticleEngine extends ParticleEngine {
	public UnitParticleEngine(ClientLevel pLevel, TextureManager pTextureManager) {
		super(pLevel, pTextureManager);
		((ParticleEngineAccessor) this).copyProviders(Minecraft.getInstance().particleEngine);
	}
}
