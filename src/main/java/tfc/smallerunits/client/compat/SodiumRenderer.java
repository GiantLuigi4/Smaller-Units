package tfc.smallerunits.client.compat;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.client.abstraction.SodiumFrustum;
import tfc.smallerunits.client.access.tracking.SUCapableChunk;
import tfc.smallerunits.client.access.tracking.SUCompiledChunkAttachments;
import tfc.smallerunits.client.access.tracking.sodium.RenderSectionManagerAccessor;
import tfc.smallerunits.client.render.SURenderManager;

import java.util.List;
import java.util.Map;

public class SodiumRenderer {
	public static void render(RenderType renderLayer, PoseStack matrixStack, double x, double y, double z, CallbackInfo ci, SodiumFrustum frustum, Minecraft client, ClientLevel world, RenderSectionManager renderSectionManager) {
		renderLayer.setupRenderState();
		
		ShaderInstance instance = RenderSystem.getShader();
		
		RenderSystem.disableBlend();
		
		if (instance == null) {
			renderLayer.clearRenderState();
			return;
		}
		
		instance.apply();
		
		if (instance.MODEL_VIEW_MATRIX != null) {
			instance.MODEL_VIEW_MATRIX.set(matrixStack.last().pose());
			instance.MODEL_VIEW_MATRIX.upload();
		}
		if (instance.PROJECTION_MATRIX != null) {
			instance.PROJECTION_MATRIX.set(RenderSystem.getProjectionMatrix());
			instance.PROJECTION_MATRIX.upload();
		}
		if (instance.COLOR_MODULATOR != null) {
			instance.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
			instance.COLOR_MODULATOR.upload();
		}
		
		Uniform uniform = instance.CHUNK_OFFSET;
		
		BlockPos.MutableBlockPos origin = new BlockPos.MutableBlockPos();
		for (Map.Entry<RenderRegion, List<RenderSection>> renderRegionListEntry : ((RenderSectionManagerAccessor) renderSectionManager).getChunkRenderList().sorted(false)) {
			for (RenderSection renderSection : renderRegionListEntry.getValue()) {
				SUCompiledChunkAttachments attachments = ((SUCompiledChunkAttachments) renderSection);
				SUCapableChunk chunk = attachments.getSUCapable();
				if (chunk == null) {
					LevelChunk chunk1 = world.getChunkAt(renderSection.getChunkPos().center());
					if (chunk1 instanceof SUCapableChunk chk)
						attachments.setSUCapable(chunk = chk);
				}
				
				origin.set(
						-(renderSection.getChunkX() >> 4),
						-(renderSection.getChunkY() >> 4) - 1,
						-(renderSection.getChunkZ() >> 4)
				);
				uniform.set(
						(float) (-(origin.getX() * 16) - x),
						(float) (-(origin.getY() * 16) - y),
						(float) (-(origin.getZ() * 16) - z)
				);
				
				SURenderManager.drawChunk(
						(LevelChunk) chunk, world, origin, renderLayer,
						frustum, x, y, z,
						uniform
				);
			}
		}
		
		if (instance.MODEL_VIEW_MATRIX != null) {
			Matrix4f mat = new Matrix4f();
			mat.setIdentity();
			instance.MODEL_VIEW_MATRIX.set(mat);
			instance.MODEL_VIEW_MATRIX.upload();
		}
		uniform.set(0, 0, 0);
		uniform.upload();
		
		renderLayer.clearRenderState();
	}
}
