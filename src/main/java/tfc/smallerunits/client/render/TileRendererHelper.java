package tfc.smallerunits.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.UnitSpace;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.data.storage.RegionPos;
import tfc.smallerunits.simulation.world.ITickerWorld;
import tfc.smallerunits.simulation.world.client.FakeClientWorld;
import tfc.smallerunits.utils.IHateTheDistCleaner;

public class TileRendererHelper {
	
	public static void setupStack(PoseStack stk, BlockEntity tile, BlockPos origin) {
		stk.pushPose();
		Level lvl = tile.getLevel();
		if (lvl instanceof ITickerWorld) {
			int upb = ((ITickerWorld) lvl).getUPB();
			float scl = 1f / upb;
			stk.scale(scl, scl, scl);
		}
		stk.translate(
				tile.getBlockPos().getX(),
				tile.getBlockPos().getY(),
				tile.getBlockPos().getZ()
		);
	}
	
	// TODO: this should only happen on empty units or when the hammer is out
	// TODO: natural units and non-natural units should have different colors when the hammer is held
	public static void drawUnit(UnitSpace unit, VertexConsumer consumer, PoseStack stk, int light, int ox, int oy, int oz) {
		// Minecraft.getInstance().renderBuffers().bufferSource()
		float r = 1;
		float g = 1;
		float b = 0;
		// this can be optimized for sure
		if (IHateTheDistCleaner.isHammerHeld()) {
			if (unit.isNatural) r = 0;
			else g = 0;
		}
		
		float scl = 1f / unit.unitsPerBlock;
		stk.pushPose();
		stk.translate(unit.pos.getX() - ox, unit.pos.getY() - oy, unit.pos.getZ() - oz);
		stk.scale(scl, scl, scl);
		MultiBufferSource.BufferSource source = Minecraft.getInstance().renderBuffers().bufferSource();
		
		// half
		// bottom
		drawCorner(stk.last().pose(), source, light, r, g, b);
		stk.scale(-1, 1, 1);
		stk.translate(-unit.unitsPerBlock, 0, 0);
		drawCorner(stk.last().pose(), source, light, r, g, b);
		stk.scale(1, -1, 1);
		stk.translate(0, -unit.unitsPerBlock, 0);
		// top
		drawCorner(stk.last().pose(), source, light, r, g, b);
		stk.scale(-1, 1, 1);
		stk.translate(-unit.unitsPerBlock, 0, 0);
		drawCorner(stk.last().pose(), source, light, r, g, b);
		
		stk.scale(1, 1, -1);
		stk.translate(0, 0, -unit.unitsPerBlock);
		
		// half
		// bottom
		drawCorner(stk.last().pose(), source, light, r, g, b);
		stk.scale(-1, 1, 1);
		stk.translate(-unit.unitsPerBlock, 0, 0);
		drawCorner(stk.last().pose(), source, light, r, g, b);
		stk.scale(1, -1, 1);
		stk.translate(0, -unit.unitsPerBlock, 0);
		// top
		drawCorner(stk.last().pose(), source, light, r, g, b);
		stk.scale(-1, 1, 1);
		stk.translate(-unit.unitsPerBlock, 0, 0);
		drawCorner(stk.last().pose(), source, light, r, g, b);
		
		stk.popPose();
	}
	
	protected static void drawCorner(Matrix4f mat, MultiBufferSource source, int light, float r, float g, float b) {
		VertexConsumer consumer = source.getBuffer(RenderType.leash());
		
		float lscl = 0.9f;
		
		consumer.vertex(mat, 0, 0.00128624283327f, 0).color(r * lscl, g * lscl, b * lscl, 1).uv2(light).endVertex();
		consumer.vertex(mat, 1, 0.00128624283327f, 0).color(r * lscl, g * lscl, b * lscl, 1).uv2(light).endVertex();
		consumer.vertex(mat, 1, 0.00128624283327f, 1).color(r * lscl, g * lscl, b * lscl, 1).uv2(light).endVertex();
		consumer.vertex(mat, 0, 0.00128624283327f, 1).color(r * lscl, g * lscl, b * lscl, 1).uv2(light).endVertex();
		consumer.vertex(mat, 0, 0.00128624283327f, 0).color(r * lscl, g * lscl, b * lscl, 1).uv2(light).endVertex();
		
		lscl = 0.7f;
		consumer.vertex(mat, 0.00128624283327f, 1, 1).color(r * lscl, g * lscl, b * lscl, 1).uv2(light).endVertex();
		consumer.vertex(mat, 0.00128624283327f, 0, 0).color(r * lscl, g * lscl, b * lscl, 1).uv2(light).endVertex();
		consumer.vertex(mat, 0.00128624283327f, 1, 0).color(r * lscl, g * lscl, b * lscl, 1).uv2(light).endVertex();
		
		lscl = 0.75f;
		consumer.vertex(mat, 1, 1, 0.00128624283327f).color(r * lscl, g * lscl, b * lscl, 1).uv2(light).endVertex();
		consumer.vertex(mat, 1, 0, 0.00128624283327f).color(r * lscl, g * lscl, b * lscl, 1).uv2(light).endVertex();
		consumer.vertex(mat, 0, 0, 0.00128624283327f).color(r * lscl, g * lscl, b * lscl, 1).uv2(light).endVertex();
		
		consumer = source.getBuffer(RenderType.cutout());
	}
	
	public static void drawParticles(PoseStack pPoseStack, float pPartialTick, long pFinishNanoTime, boolean pRenderBlockOutline, Camera pCamera, GameRenderer pGameRenderer, LightTexture pLightTexture, Matrix4f pProjectionMatrix, Region value, Level valueLevel, RenderBuffers renderBuffers, CallbackInfo ci) {
		RegionPos pos = ((FakeClientWorld) valueLevel).getRegion().pos;
		BlockPos bp = pos.toBlockPos();
		
		float scl = 1f / (((FakeClientWorld) valueLevel).getUPB());
		PoseStack mdlViewStk = RenderSystem.getModelViewStack();
		mdlViewStk.pushPose();
		
		mdlViewStk.last().pose().multiply(pPoseStack.last().pose());
		mdlViewStk.translate(-pCamera.getPosition().x, -pCamera.getPosition().y, -pCamera.getPosition().z);
		mdlViewStk.translate(bp.getX(), bp.getY(), bp.getZ());
		mdlViewStk.scale(scl, scl, scl);
		mdlViewStk.translate(pCamera.getPosition().x, pCamera.getPosition().y, pCamera.getPosition().z);
		
		// TODO: use forge method or smth
		((FakeClientWorld) valueLevel).getParticleEngine().render(
				new PoseStack(), renderBuffers.bufferSource(),
				pLightTexture, pCamera, pPartialTick
		);
		mdlViewStk.popPose();
	}
}
