package tfc.smallerunits.client.render.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.UnitSpace;
import tfc.smallerunits.data.capability.SUCapabilityManager;
import tfc.smallerunits.utils.math.HitboxScaling;
import tfc.smallerunits.utils.selection.UnitHitResult;

public class PlayerOffsetRenderHelper {
	public static void render(PoseStack stack, float pct, long tick, boolean idk, Camera camera, GameRenderer renderer, LightTexture lightTexture, Matrix4f matrix, CallbackInfo ci) {
		if (true) return;
		
		if (Minecraft.getInstance().hitResult instanceof UnitHitResult) {
			if (!Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes())
				return;
			
			UnitHitResult result = (UnitHitResult) Minecraft.getInstance().hitResult;
			LevelChunk chnk = Minecraft.getInstance().level.getChunkAt(result.getBlockPos());
			UnitSpace space = SUCapabilityManager.getCapability(chnk).getUnit(result.getBlockPos());
			if (space == null) return;
			
			stack.pushPose();
			stack.last().pose().setIdentity();
			stack.translate(
					-camera.getPosition().x,
					-camera.getPosition().y,
					-camera.getPosition().z
			);
			stack.scale(1f / space.unitsPerBlock, 1f / space.unitsPerBlock, 1f / space.unitsPerBlock);
//			Vec3 pos = Minecraft.getInstance().cameraEntity.getPosition(pct);
//			pos = pos.subtract(
//					((TickerServerWorld) space.getMyLevel()).region.pos.toBlockPos().getX(),
//					((TickerServerWorld) space.getMyLevel()).region.pos.toBlockPos().getY(),
//					((TickerServerWorld) space.getMyLevel()).region.pos.toBlockPos().getZ()
//			);
//			pos = pos.scale(space.unitsPerBlock);
			AABB box = HitboxScaling.getOffsetAndScaledBox(Minecraft.getInstance().cameraEntity.getBoundingBox(), Minecraft.getInstance().cameraEntity.getPosition(1), space.unitsPerBlock);
//			AABB box = Minecraft.getInstance().cameraEntity.getBoundingBox();
//			box = box.move(
//					-Minecraft.getInstance().cameraEntity.getPosition(1).x,
//					-Minecraft.getInstance().cameraEntity.getPosition(1).y,
//					-Minecraft.getInstance().cameraEntity.getPosition(1).z
//			);
//			box = new AABB(
//					box.minX * space.unitsPerBlock,
//					box.minY * space.unitsPerBlock,
//					box.minZ * space.unitsPerBlock,
//					box.maxX * space.unitsPerBlock,
//					box.maxY * space.unitsPerBlock,
//					box.maxZ * space.unitsPerBlock
//			);
//			box = box.move(pos.x, pos.y, pos.z);
			VertexConsumer consumer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.lines());
			renderShape(
					stack, consumer,
					Shapes.create(box),
					0, 0, 0, 1, 1, 1, 1
			);
			box = box.move(0, Minecraft.getInstance().cameraEntity.getEyeHeight() * space.unitsPerBlock, 0);
			box = new AABB(
					box.minX, box.minY - (0.01 * space.unitsPerBlock), box.minZ,
					box.maxX, box.minY + (0.01 * space.unitsPerBlock), box.maxZ
			);
			renderShape(
					stack, consumer,
					Shapes.create(box),
					0, 0, 0, 1, 0, 0, 1
			);
			Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.solid());
			stack.popPose();
		}
	}
	
	private static void renderShape(PoseStack pPoseStack, VertexConsumer pConsumer1, VoxelShape pShape, double pX, double pY, double pZ, float pRed, float pGreen, float pBlue, float pAlpha) {
		PoseStack.Pose posestack$pose = pPoseStack.last();
		pShape.forAllEdges((x0, y0, z0, x1, y1, z1) -> {
			float f = (float) (x1 - x0);
			float f1 = (float) (y1 - y0);
			float f2 = (float) (z1 - z0);
			float f3 = Mth.sqrt(f * f + f1 * f1 + f2 * f2);
			f /= f3;
			f1 /= f3;
			f2 /= f3;
			pConsumer1
					.vertex(posestack$pose.pose(), (float) (x0 + pX), (float) (y0 + pY), (float) (z0 + pZ))
					.color(pRed, pGreen, pBlue, pAlpha)
					.normal(f, f1, f2)
					.endVertex();
			pConsumer1
					.vertex(posestack$pose.pose(), (float) (x1 + pX), (float) (y1 + pY), (float) (z1 + pZ))
					.color(pRed, pGreen, pBlue, pAlpha)
					.normal(f, f1, f2)
					.endVertex();
		});
	}
}
