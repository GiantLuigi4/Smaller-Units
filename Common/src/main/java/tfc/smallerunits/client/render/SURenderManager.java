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
import tfc.smallerunits.data.capability.ISUCapability;
import tfc.smallerunits.data.capability.SUCapabilityManager;

import java.util.ArrayList;

public class SURenderManager {
	public static void drawChunk(LevelChunk chunk, Level world, BlockPos positionRendering, RenderType type, IFrustum frustum, double pCamX, double pCamY, double pCamZ, AbstractUniform uniform) {
		if (chunk instanceof EmptyLevelChunk) return;
		SUCapableChunk suCapable = ((SUCapableChunk) chunk);
		ISUCapability capability = SUCapabilityManager.getCapability(chunk);
		SUChunkRender render = suCapable.SU$getChunkRender();

		if (type.equals(RenderType.solid())) {
			SUVBOEmitter vboEmitter = ((SUCapableWorld) world).getVBOEmitter();
			// TODO: frustrum check
			ArrayList<BlockPos> notDrawn = new ArrayList<>();
			for (BlockPos pos : suCapable.SU$dirty()) {
				if (!frustum.test(new AABB(pos)))
					notDrawn.add(pos);
				else render.addBuffers(pos, vboEmitter.genBuffers(chunk, suCapable, capability, pos));
			}
			for (BlockPos pos : suCapable.SU$toRemove())
				render.freeBuffers(pos, vboEmitter);
			// TODO: remove only unit positions that are in the frustrum
			suCapable.SU$reset(notDrawn);
		}
		
		render.draw(positionRendering, type, frustum, uniform);
	}

	public static void drawEntity(LevelRenderer renderer, Level lvl, PoseStack stk, Camera cam, float pct, MultiBufferSource buffers, Entity entity) {
		// TODO: glowing
		renderer.renderEntity(
				entity,
				0, 0, 0,
				pct, stk,
				buffers
		);
	}
}
