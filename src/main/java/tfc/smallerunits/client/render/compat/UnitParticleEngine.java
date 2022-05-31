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
	
	@Override
	public void render(PoseStack p_107337_, MultiBufferSource.BufferSource p_107338_, LightTexture p_107339_, Camera p_107340_, float p_107341_, @Nullable Frustum clippingHelper) {
		super.render(p_107337_, p_107338_, p_107339_, p_107340_, p_107341_, clippingHelper);
	}
}
