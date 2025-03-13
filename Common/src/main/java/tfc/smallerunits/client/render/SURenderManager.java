package tfc.smallerunits.client.render;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import tfc.smallerunits.client.abstraction.IFrustum;
import tfc.smallerunits.client.access.tracking.SUCapableChunk;
import tfc.smallerunits.client.access.tracking.SUCapableWorld;
import tfc.smallerunits.client.access.tracking.SUCompiledChunkAttachments;
import tfc.smallerunits.data.capability.ISUCapability;
import tfc.smallerunits.data.capability.SUCapabilityManager;
import tfc.smallerunits.mixin.client.access.LevelRendererAccessor;
import tfc.smallerunits.utils.selection.MutableAABB;

import java.util.ArrayList;

public class SURenderManager {
	public static void drawChunk(SUCompiledChunkAttachments attachments, LevelChunk chunk, Level world, BlockPos positionRendering, RenderType type, IFrustum frustum, double pCamX, double pCamY, double pCamZ, AbstractUniform uniform) {
		SUChunkRender render = attachments.SU$getChunkRender();
		drawChunk(render, attachments, chunk, world, positionRendering, type, frustum, pCamX, pCamY, pCamZ, uniform);
	}
	public static void drawChunk(SUChunkRender render, SUCompiledChunkAttachments attachments, LevelChunk chunk, Level world, BlockPos positionRendering, RenderType type, IFrustum frustum, double pCamX, double pCamY, double pCamZ, AbstractUniform uniform) {
		if (chunk instanceof EmptyLevelChunk) return;
		SUCapableChunk suCapable = ((SUCapableChunk) chunk);
		ISUCapability capability = SUCapabilityManager.getCapability(chunk);

		if (type.equals(RenderType.solid())) {
			int yRL = positionRendering.getY();
			int yRM = positionRendering.getY() + 15;

			SUVBOEmitter vboEmitter = ((SUCapableWorld) world).getVBOEmitter();
			// TODO: frustrum check
			ArrayList<BlockPos> notDrawn = new ArrayList<>();
			ArrayList<BlockPos> notFreed = new ArrayList<>();
			for (BlockPos pos : suCapable.SU$dirty()) {
				if (pos.getY() >= yRL && pos.getY() <= yRM) {
                    if (!frustum.test(new AABB(pos)))
                        notDrawn.add(pos);
                    else render.addBuffers(pos, vboEmitter.genBuffers(chunk, suCapable, capability, pos));
                } else notDrawn.add(pos);
			}
			for (BlockPos pos : suCapable.SU$toRemove()) {
				if (pos.getY() >= yRL || pos.getY() < yRM) {
                    render.freeBuffers(pos, vboEmitter);
                } else notFreed.add(pos);
			}
			suCapable.SU$reset(notDrawn, notFreed);

			if (attachments.needsCull()) {
				MutableAABB bounds = new MutableAABB(0, 0, 0, 0, 0, 0);
				render.performCull(bounds, frustum);
			}
		}

		render.draw(type, uniform);
	}

	public static void drawEntity(LevelRenderer renderer, Level lvl, PoseStack stk, Camera cam, float pct, MultiBufferSource buffers, Entity entity) {
		// TODO: glowing
		((LevelRendererAccessor)renderer).invokeRenderEntity(
				entity,
				0, 0, 0,
				pct, stk,
				buffers
		);
	}
}
