package tfc.smallerunits.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
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
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.SmallerUnits;
import tfc.smallerunits.client.abstraction.IFrustum;
import tfc.smallerunits.client.render.util.SUTesselator;
import tfc.smallerunits.client.render.util.TextureScalingVertexBuilder;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.data.storage.RegionPos;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.simulation.level.client.TickerClientLevel;

public class TileRendererHelper {
	public static void setupStack(PoseStack stk, BlockEntity tile, BlockPos origin) {
		stk.pushPose();
		
		Level lvl = tile.getLevel();
		float scl = 1;
		if (lvl instanceof ITickerLevel tkLvl) {
			int upb = tkLvl.getUPB();
			scl = 1f / upb;
			stk.translate(
					tkLvl.getRegion().pos.toBlockPos().getX(),
					tkLvl.getRegion().pos.toBlockPos().getY(),
					tkLvl.getRegion().pos.toBlockPos().getZ()
			);
			stk.scale(scl, scl, scl);
		}
		
		if (Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes() && !FMLEnvironment.production) {
			LevelRenderer.renderLineBox(
					stk, Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.LINES),
					tile.getRenderBoundingBox(), 1, 1, 1, 1
			);
		}
		stk.translate(
				tile.getBlockPos().getX(),
				tile.getBlockPos().getY(),
				tile.getBlockPos().getZ()
		);
	}
	
	private static VertexBuffer[] buffers = new VertexBuffer[SmallerUnits.ABS_MIN + 1];
	
	private static int lastType = -1;
	private static int lastScale = -1;
	
	public static void markNewFrame() {
		lastType = -1;
		lastScale = -1;
	}
	
	private static BufferBuilder theBuilder = new BufferBuilder(128);
	
	//	public static void drawUnit(UnitSpace unit, VertexConsumer consumer, PoseStack stk, int light, int ox, int oy, int oz) {
	public static void drawUnit(IFrustum frustum, BlockPos pos, int upb, boolean natural, boolean forceIndicators, boolean isEmpty, VertexConsumer consumer, PoseStack stk, int light, int ox, int oy, int oz) {
		float r = 1;
		float g = 1;
		float b = 0;
		int type = 0;
		if (forceIndicators) {
			if (!natural) {
				type = 1;
				r = 0;
			} else {
				type = 2;
				g = 0;
			}
		} else if (!isEmpty || natural) {
			return;
		}
		
		if (lastScale == -1)
			GameRenderer.getPositionColorShader().apply();
		
		if (consumer == null) {
			if (buffers[upb - 1] != null) {
				if (lastScale != upb) {
					buffers[upb - 1].bind();
					lastScale = upb;
				}
				
				stk.pushPose();
				
				stk.translate(pos.getX() - ox, pos.getY() - oy, pos.getZ() - oz);
				
				ShaderInstance instance = RenderSystem.getShader();
				if (lastType != type) {
					if (instance.COLOR_MODULATOR != null) {
						instance.COLOR_MODULATOR.set(r, g, b, 1);
						instance.COLOR_MODULATOR.upload();
					}
					lastType = type;
				}
				if (instance.MODEL_VIEW_MATRIX != null) {
					instance.MODEL_VIEW_MATRIX.set(stk.last().pose());
					instance.MODEL_VIEW_MATRIX.upload();
				}
				
				buffers[upb - 1].draw();
				
				stk.popPose();
				
				return;
			}
		}
		
		float scl = 1f / upb;
		if (consumer == null)
			stk = new PoseStack();
		stk.pushPose();
//		stk.translate(pos.getX() - ox, pos.getY() - oy, pos.getZ() - oz);
		stk.scale(scl, scl, scl);

//		MultiBufferSource.BufferSource source = Minecraft.getInstance().renderBuffers().bufferSource();
		
		BufferBuilder builder = null;
		if (consumer == null) {
			r = 1;
			g = 1;
			b = 1;
			
			consumer = builder = theBuilder;
			builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		}
		
		light = LightTexture.pack(15, 15);
		
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
		
		if (builder != null) {
			buffers[upb - 1] = new VertexBuffer();
			buffers[upb - 1].bind();
			buffers[upb - 1].upload(builder.end());
			DefaultVertexFormat.POSITION_COLOR.setupBufferState();
			VertexBuffer.unbind();
			builder.discard();
		}
	}
	
	protected static void drawCorner(Matrix4f mat, VertexConsumer consumer, int light, float r, float g, float b, Direction[] directions) {
//		VertexConsumer consumer = source.getBuffer(RenderType.entityCutoutNoCull(new ResourceLocation("smallerunits:textures/block/white_pixel.png")));
		
		TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(new ResourceLocation("smallerunits:block/white_pixel"));
		// TODO: I want this done with a block render type
		float lscl = 1;

//		Matrix3f normal = RenderSystem.getModelViewStack().last().normal();
		
		ClientLevel level = Minecraft.getInstance().level;
		float offset = 0.00128624283327f;
		lscl = level.getShade(directions[0], true);
		vertex(consumer, mat, 0, offset, 0).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU0(), sprite.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		vertex(consumer, mat, 1, offset, 0).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU1(), sprite.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		vertex(consumer, mat, 1, offset, 1).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU1(), sprite.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		vertex(consumer, mat, 0, offset, 1).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU0(), sprite.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		
		lscl = level.getShade(directions[1], true);
		vertex(consumer, mat, 0, offset, 1).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU0(), sprite.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		vertex(consumer, mat, 1, offset, 1).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU1(), sprite.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		vertex(consumer, mat, 1, offset, 0).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU1(), sprite.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		vertex(consumer, mat, 0, offset, 0).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU0(), sprite.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		
		lscl = level.getShade(directions[2], true);
		vertex(consumer, mat, 0, 0, offset).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU0(), sprite.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		vertex(consumer, mat, 1, 0, offset).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU1(), sprite.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		vertex(consumer, mat, 1, 1, offset).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU1(), sprite.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		vertex(consumer, mat, 0, 1, offset).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU0(), sprite.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		
		lscl = level.getShade(directions[3], true);
		vertex(consumer, mat, 0, 1, offset).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU0(), sprite.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		vertex(consumer, mat, 1, 1, offset).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU1(), sprite.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		vertex(consumer, mat, 1, 0, offset).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU1(), sprite.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		vertex(consumer, mat, 0, 0, offset).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU0(), sprite.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		
		lscl = level.getShade(directions[4], true);
		vertex(consumer, mat, offset, 0, 0).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU0(), sprite.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		vertex(consumer, mat, offset, 1, 0).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU1(), sprite.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		vertex(consumer, mat, offset, 1, 1).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU1(), sprite.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		vertex(consumer, mat, offset, 0, 1).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU0(), sprite.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		
		lscl = level.getShade(directions[5], true);
		vertex(consumer, mat, offset, 0, 1).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU0(), sprite.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		vertex(consumer, mat, offset, 1, 1).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU1(), sprite.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		vertex(consumer, mat, offset, 1, 0).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU1(), sprite.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
		vertex(consumer, mat, offset, 0, 0).color(r * lscl, g * lscl, b * lscl, 1).uv(sprite.getU0(), sprite.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 0).endVertex();
	}
	
	private static VertexConsumer vertex(VertexConsumer consumer, Matrix4f mat, float x, float y, float z) {
		float w = 1.0F;
		float tx = mat.m00 * x + mat.m01 * y + mat.m02 * z + mat.m03 * w;
		float ty = mat.m10 * x + mat.m11 * y + mat.m12 * z + mat.m13 * w;
		float tz = mat.m20 * x + mat.m21 * y + mat.m22 * z + mat.m23 * w;
		
		return consumer.vertex(tx, ty, tz);
	}
	
	public static void drawParticles(PoseStack pPoseStack, float pPartialTick, long pFinishNanoTime, boolean pRenderBlockOutline, Camera pCamera, GameRenderer pGameRenderer, LightTexture pLightTexture, Matrix4f pProjectionMatrix, Region value, Level valueLevel, RenderBuffers renderBuffers, CallbackInfo ci) {
		RegionPos pos = ((TickerClientLevel) valueLevel).getRegion().pos;
		BlockPos bp = pos.toBlockPos();
		
		float scl = 1f / (((TickerClientLevel) valueLevel).getUPB());
		
		PoseStack stack = new PoseStack();
		stack.last().pose().multiply(pPoseStack.last().pose());
		
		if (!ModList.get().isLoaded("rubidium")) SmallerUnits.tesselScale = scl;
		
		ClientLevel clvl = Minecraft.getInstance().level;
		Minecraft.getInstance().level = (ClientLevel) valueLevel;
		if (Tesselator.getInstance() instanceof SUTesselator suTesselator) {
			suTesselator.setOffset(bp.getX(), bp.getY(), bp.getZ());
			// TODO: use forge method or smth
			((TickerClientLevel) valueLevel).getParticleEngine().render(
					stack, renderBuffers.bufferSource(),
					pLightTexture, pCamera, pPartialTick
			);
			SmallerUnits.tesselScale = 0;
		} else {
			SmallerUnits.tesselScale = 0;
			
			stack.translate(-pCamera.getPosition().x, -pCamera.getPosition().y, -pCamera.getPosition().z);
			stack.translate(bp.getX(), bp.getY(), bp.getZ());
			stack.scale(scl, scl, scl);
			stack.translate(pCamera.getPosition().x, pCamera.getPosition().y, pCamera.getPosition().z);
			
			((TickerClientLevel) valueLevel).getParticleEngine().render(
					stack, renderBuffers.bufferSource(),
					pLightTexture, pCamera, pPartialTick
			);
		}
		Minecraft.getInstance().level = clvl;
	}
	
	public static void drawBreakingOutline(int progr, RenderBuffers renderBuffers, PoseStack pPoseStack, Level level, BlockPos pos, BlockState state, Minecraft minecraft) {
		if (progr < 10) {
			if (level instanceof ITickerLevel) {
				PoseStack.Pose posestack$pose = pPoseStack.last();
				VertexConsumer consumer = renderBuffers.crumblingBufferSource().getBuffer(ModelBakery.DESTROY_TYPES.get(progr));
				int upb = ((ITickerLevel) level).getUPB();
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
	
	public static void renderBE(BlockEntity tile, BlockPos origin, IFrustum frustum, PoseStack stk, BlockEntityRenderDispatcher blockEntityRenderDispatcher, float pct) {
		if (tile.getLevel() == null) return; // idk how this happens, but ok?
		if (new RegionPos(origin).equals(((TickerClientLevel) tile.getLevel()).region.pos)) {
			int y = tile.getBlockPos().getY() / ((TickerClientLevel) tile.getLevel()).upb;
			BlockPos regionOrigin = new BlockPos(0, 0, 0);
			if (tile.getLevel() instanceof ITickerLevel tkLvl) regionOrigin = tkLvl.getRegion().pos.toBlockPos();
			y += regionOrigin.getY();
			
			if (y < origin.getY() + 16 &&
					y >= origin.getY()) {
				AABB renderBox = tile.getRenderBoundingBox();
				if (tile.getLevel() instanceof ITickerLevel tkLvl) {
					int upb = tkLvl.getUPB();
					float scl = 1f / upb;
					renderBox = new AABB(
							renderBox.minX * scl + regionOrigin.getX(),
							renderBox.minY * scl + regionOrigin.getY(),
							renderBox.minZ * scl + regionOrigin.getZ(),
							renderBox.maxX * scl + regionOrigin.getX(),
							renderBox.maxY * scl + regionOrigin.getY(),
							renderBox.maxZ * scl + regionOrigin.getZ()
					);
				}
//				if (Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes() && !FMLEnvironment.production) {
//					stk.pushPose();
//					LevelRenderer.renderLineBox(
//							stk, Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.LINES),
//							renderBox, 1, 1, 1, 1
//					);
//					stk.popPose();
//				}
				if (frustum.test(renderBox)) {
					TileRendererHelper.setupStack(stk, tile, origin);
					blockEntityRenderDispatcher.render(
							tile, pct,
							stk, Minecraft.getInstance().renderBuffers().bufferSource()
					);
					stk.popPose();
				}
			}
		}
	}
}
