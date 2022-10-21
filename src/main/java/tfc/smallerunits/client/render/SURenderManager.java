package tfc.smallerunits.client.render;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import tfc.smallerunits.client.tracking.SUCapableChunk;
import tfc.smallerunits.client.tracking.SUCapableWorld;
import tfc.smallerunits.data.capability.ISUCapability;
import tfc.smallerunits.data.capability.SUCapabilityManager;

public class SURenderManager {
	public static void drawChunk(LevelChunk chunk, Level world, BlockPos positionRendering, RenderType type, Frustum frustum, double pCamX, double pCamY, double pCamZ, AbstractUniform uniform) {
		if (chunk instanceof EmptyLevelChunk) return;
		Minecraft.getInstance().getProfiler().push("SU");
		Minecraft.getInstance().getProfiler().push("setup");
		SUCapableChunk suCapable = ((SUCapableChunk) chunk);
		ISUCapability capability = SUCapabilityManager.getCapability(chunk);
		SUChunkRender render = suCapable.SU$getChunkRender();

//		boolean foundAny = false;
//		for (UnitSpace unit : capability.getUnits()) {
//			if (unit != null) {
//				ModCompat.postSetupMatrix(
//						Minecraft.getInstance().renderBuffers(),
//						((ClientLevel) unit.getMyLevel()), type, matrixStack,
//						Minecraft.getInstance().renderBuffers(),
//						HitboxScaling.scaleX(unit, pCamX), HitboxScaling.scaleY(unit, pCamY), HitboxScaling.scaleZ(unit, pCamZ) // TODO
//				);
//				foundAny = true;
//			}
//		}
		
		if (type.equals(RenderType.solid())) {
			SUVBOEmitter vboEmitter = ((SUCapableWorld) world).getVBOEmitter();
			Minecraft.getInstance().getProfiler().popPush("regen_dirty");
			// TODO: frustrum check
			for (BlockPos pos : suCapable.SU$dirty())
				render.addBuffers(pos, vboEmitter.genBuffers(chunk, suCapable, capability, pos));
			Minecraft.getInstance().getProfiler().popPush("remove_old");
			for (BlockPos pos : suCapable.SU$toRemove())
				render.freeBuffers(pos, vboEmitter);
			// TODO: remove only unit positions that are in the frustrum
			suCapable.SU$reset();
		}
		
		Minecraft.getInstance().getProfiler().popPush("draw");
		render.draw(positionRendering, type, frustum, uniform);
		Minecraft.getInstance().getProfiler().pop();
		Minecraft.getInstance().getProfiler().pop();
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
