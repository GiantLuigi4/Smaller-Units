package tfc.smallerunits.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.client.render.util.TextureScalingVertexBuilder;
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
//	public static void drawUnit(UnitSpace unit, VertexConsumer consumer, PoseStack stk, int light, int ox, int oy, int oz) {
	public static void drawUnit(BlockPos pos, int upb, boolean natural, boolean hammerOverride, VertexConsumer consumer, PoseStack stk, int light, int ox, int oy, int oz) {
		// TODO: this needs optimization and checking
		// could probably convert this to VBOs

//		if (true) return;
		
		float r = 1;
		float g = 1;
		float b = 0;
		// this can be optimized for sure
		if (IHateTheDistCleaner.isHammerHeld() && hammerOverride) {
			if (!natural) r = 0;
			else g = 0;
		}
		
		float scl = 1f / upb;
		stk.pushPose();
		stk.translate(pos.getX() - ox, pos.getY() - oy, pos.getZ() - oz);
		stk.scale(scl, scl, scl);
//		MultiBufferSource.BufferSource source = Minecraft.getInstance().renderBuffers().bufferSource();
		
		// half
		// bottom
		Direction[] directions = new Direction[]{
				Direction.DOWN,
				Direction.UP,
				Direction.SOUTH,
				Direction.NORTH,
				Direction.EAST,
				Direction.WEST,
		};
		drawCorner(stk.last().pose(), consumer, light, r, g, b, directions);
		stk.scale(-1, 1, 1);
		stk.translate(-upb, 0, 0);
		// bottom
		directions = new Direction[]{
				Direction.UP,
				Direction.DOWN,
				Direction.SOUTH,
				Direction.NORTH,
				Direction.WEST,
				Direction.EAST,
		};
		drawCorner(stk.last().pose(), consumer, light, r, g, b, directions);
		stk.scale(1, -1, 1);
		stk.translate(0, -upb, 0);
		// top
		directions = new Direction[]{
				Direction.UP,
				Direction.DOWN,
				Direction.NORTH,
				Direction.SOUTH,
				Direction.WEST,
				Direction.EAST,
		};
		drawCorner(stk.last().pose(), consumer, light, r, g, b, directions);
		stk.scale(-1, 1, 1);
		stk.translate(-upb, 0, 0);
		directions = new Direction[]{
				Direction.DOWN,
				Direction.UP,
				Direction.SOUTH,
				Direction.NORTH,
				Direction.WEST,
				Direction.EAST,
		};
		drawCorner(stk.last().pose(), consumer, light, r, g, b, directions);
		
		stk.scale(1, 1, -1);
		stk.translate(0, 0, -upb);
		
		// half
		// bottom
		directions = new Direction[]{
				Direction.UP,
				Direction.DOWN,
				Direction.SOUTH,
				Direction.NORTH,
				Direction.EAST,
				Direction.WEST,
		};
		drawCorner(stk.last().pose(), consumer, light, r, g, b, directions);
		stk.scale(-1, 1, 1);
		stk.translate(-upb, 0, 0);
		directions = new Direction[]{
				Direction.DOWN,
				Direction.UP,
				Direction.NORTH,
				Direction.SOUTH,
				Direction.EAST,
				Direction.WEST,
		};
		drawCorner(stk.last().pose(), consumer, light, r, g, b, directions);
		stk.scale(1, -1, 1);
		stk.translate(0, -upb, 0);
		// top
		directions = new Direction[]{
				Direction.DOWN,
				Direction.UP,
				Direction.NORTH,
				Direction.SOUTH,
				Direction.WEST,
				Direction.EAST,
		};
		drawCorner(stk.last().pose(), consumer, light, r, g, b, directions);
		stk.scale(-1, 1, 1);
		stk.translate(-upb, 0, 0);
		directions = new Direction[]{
				Direction.UP,
				Direction.DOWN,
				Direction.SOUTH,
				Direction.NORTH,
				Direction.WEST,
				Direction.EAST,
		};
		drawCorner(stk.last().pose(), consumer, light, r, g, b, directions);
		stk.popPose();
	}
	
	protected static void drawCorner(Matrix4f mat, VertexConsumer consumer, int light, float r, float g, float b, Direction[] directions) {
//		VertexConsumer consumer = source.getBuffer(RenderType.entityCutoutNoCull(new ResourceLocation("smallerunits:textures/block/white_pixel.png")));
		
		TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(new ResourceLocation("smallerunits:block/white_pixel"));
		// TODO: I want this done with a block render type
		float lscl = 1;
		
		Matrix3f normal = RenderSystem.getModelViewStack().last().normal();
		
		ClientLevel level = Minecraft.getInstance().level;
		float offset = 0.00128624283327f;
		lscl = level.getShade(directions[0], true);
		consumer.vertex(mat, 0, offset, 0).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU0(), sprite.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		consumer.vertex(mat, 1, offset, 0).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU1(), sprite.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		consumer.vertex(mat, 1, offset, 1).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU1(), sprite.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		consumer.vertex(mat, 0, offset, 1).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU0(), sprite.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		
		lscl = level.getShade(directions[1], true);
		consumer.vertex(mat, 0, offset, 1).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU0(), sprite.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		consumer.vertex(mat, 1, offset, 1).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU1(), sprite.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		consumer.vertex(mat, 1, offset, 0).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU1(), sprite.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		consumer.vertex(mat, 0, offset, 0).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU0(), sprite.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		
		lscl = level.getShade(directions[2], true);
		consumer.vertex(mat, 0, 0, offset).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU0(), sprite.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		consumer.vertex(mat, 1, 0, offset).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU1(), sprite.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		consumer.vertex(mat, 1, 1, offset).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU1(), sprite.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		consumer.vertex(mat, 0, 1, offset).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU0(), sprite.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		
		lscl = level.getShade(directions[3], true);
		consumer.vertex(mat, 0, 1, offset).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU0(), sprite.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		consumer.vertex(mat, 1, 1, offset).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU1(), sprite.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		consumer.vertex(mat, 1, 0, offset).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU1(), sprite.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		consumer.vertex(mat, 0, 0, offset).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU0(), sprite.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		
		lscl = level.getShade(directions[4], true);
		consumer.vertex(mat, offset, 0, 0).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU0(), sprite.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		consumer.vertex(mat, offset, 1, 0).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU1(), sprite.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		consumer.vertex(mat, offset, 1, 1).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU1(), sprite.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		consumer.vertex(mat, offset, 0, 1).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU0(), sprite.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		
		lscl = level.getShade(directions[5], true);
		consumer.vertex(mat, offset, 0, 1).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU0(), sprite.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		consumer.vertex(mat, offset, 1, 1).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU1(), sprite.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		consumer.vertex(mat, offset, 1, 0).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU1(), sprite.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		consumer.vertex(mat, offset, 0, 0).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU0(), sprite.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
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
	
	public static void drawBreakingOutline(int progr, RenderBuffers renderBuffers, PoseStack pPoseStack, Level level, BlockPos pos, BlockState state, Minecraft minecraft) {
		if (progr < 10) {
			if (level instanceof ITickerWorld) {
				PoseStack.Pose posestack$pose = pPoseStack.last();
				VertexConsumer consumer = renderBuffers.crumblingBufferSource().getBuffer(ModelBakery.DESTROY_TYPES.get(progr));
				int upb = ((ITickerWorld) level).getUPB();
				consumer = new TextureScalingVertexBuilder(consumer, upb);
				VertexConsumer vertexconsumer1 = new SheetedDecalTextureGenerator(consumer, posestack$pose.pose(), posestack$pose.normal());
				pPoseStack.pushPose();
				float scl = 1f / upb;
				pPoseStack.scale(scl, scl, scl);
				pPoseStack.translate(pos.getX(), pos.getY(), pos.getZ());
				minecraft.getBlockRenderer().renderBreakingTexture(state, pos, level, pPoseStack, vertexconsumer1);
				pPoseStack.popPose();
			}
		}
	}
}
