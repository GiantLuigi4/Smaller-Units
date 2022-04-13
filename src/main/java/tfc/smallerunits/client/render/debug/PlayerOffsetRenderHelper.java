package tfc.smallerunits.client.render.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.UnitSpace;
import tfc.smallerunits.data.capability.SUCapabilityManager;
import tfc.smallerunits.utils.math.HitboxScaling;
import tfc.smallerunits.utils.selection.UnitHitResult;

public class PlayerOffsetRenderHelper {
	public static void render(PoseStack stack, float pct, long tick, boolean idk, Camera camera, GameRenderer renderer, LightTexture lightTexture, Matrix4f matrix, CallbackInfo ci) {
		if (Minecraft.getInstance().hitResult instanceof UnitHitResult) {
			UnitHitResult result = (UnitHitResult) Minecraft.getInstance().hitResult;
			LevelChunk chnk = Minecraft.getInstance().level.getChunkAt(result.getBlockPos());
			UnitSpace space = SUCapabilityManager.getCapability(chnk).getUnit(result.getBlockPos());
			if (space == null) return;
			
			stack.pushPose();
			stack.translate(
					-camera.getPosition().x,
					-camera.getPosition().y,
					-camera.getPosition().z
			);
//			Vec3 pos = Minecraft.getInstance().cameraEntity.getPosition(pct);
//			pos = pos.subtract(
//					((TickerServerWorld) space.getMyLevel()).region.pos.toBlockPos().getX(),
//					((TickerServerWorld) space.getMyLevel()).region.pos.toBlockPos().getY(),
//					((TickerServerWorld) space.getMyLevel()).region.pos.toBlockPos().getZ()
//			);
//			pos = pos.scale(space.unitsPerBlock);
			stack.scale(1f / space.unitsPerBlock, 1f / space.unitsPerBlock, 1f / space.unitsPerBlock);
			AABB box = HitboxScaling.getOffsetAndScaledBox(Minecraft.getInstance().cameraEntity.getBoundingBox(), Minecraft.getInstance().cameraEntity.getPosition(1), space);
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
			LevelRenderer.renderVoxelShape(
					stack,
					Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.LINES),
					Shapes.create(box),
					0, 0, 0, 0, 0, 0, 0
			);
			box = box.move(0, Minecraft.getInstance().cameraEntity.getEyeHeight() * space.unitsPerBlock, 0);
			box = new AABB(
					box.minX, box.minY - (0.01 * space.unitsPerBlock), box.minZ,
					box.maxX, box.minY + (0.01 * space.unitsPerBlock), box.maxZ
			);
			LevelRenderer.renderVoxelShape(
					stack,
					Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.LINES),
					Shapes.create(box),
					0, 0, 0, 1, 0, 1, 1
			);
			stack.popPose();
		}
	}
}
