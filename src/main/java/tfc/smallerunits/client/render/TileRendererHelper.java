package tfc.smallerunits.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import tfc.smallerunits.UnitSpace;
import tfc.smallerunits.simulation.world.ITickerWorld;

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
		float scl = 1f / unit.unitsPerBlock;
		stk.pushPose();
		stk.translate(unit.pos.getX() - ox, unit.pos.getY() - oy, unit.pos.getZ() - oz);
		stk.scale(scl, scl, scl);
		MultiBufferSource.BufferSource source = Minecraft.getInstance().renderBuffers().bufferSource();
		
		// half
		// bottom
		drawCorner(stk.last().pose(), source, light);
		stk.scale(-1, 1, 1);
		stk.translate(-unit.unitsPerBlock, 0, 0);
		drawCorner(stk.last().pose(), source, light);
		stk.scale(1, -1, 1);
		stk.translate(0, -unit.unitsPerBlock, 0);
		// top
		drawCorner(stk.last().pose(), source, light);
		stk.scale(-1, 1, 1);
		stk.translate(-unit.unitsPerBlock, 0, 0);
		drawCorner(stk.last().pose(), source, light);
		
		stk.scale(1, 1, -1);
		stk.translate(0, 0, -unit.unitsPerBlock);
		
		// half
		// bottom
		drawCorner(stk.last().pose(), source, light);
		stk.scale(-1, 1, 1);
		stk.translate(-unit.unitsPerBlock, 0, 0);
		drawCorner(stk.last().pose(), source, light);
		stk.scale(1, -1, 1);
		stk.translate(0, -unit.unitsPerBlock, 0);
		// top
		drawCorner(stk.last().pose(), source, light);
		stk.scale(-1, 1, 1);
		stk.translate(-unit.unitsPerBlock, 0, 0);
		drawCorner(stk.last().pose(), source, light);
		
		stk.popPose();
	}
	
	protected static void drawCorner(Matrix4f mat, MultiBufferSource source, int light) {
		VertexConsumer consumer = source.getBuffer(RenderType.leash());
		
		float lscl = 0.9f;
		
		consumer.vertex(mat, 0, 0.00128624283327f, 0).color(lscl, lscl, 0, 1).uv2(light).endVertex();
		consumer.vertex(mat, 1, 0.00128624283327f, 0).color(lscl, lscl, 0, 1).uv2(light).endVertex();
		consumer.vertex(mat, 1, 0.00128624283327f, 1).color(lscl, lscl, 0, 1).uv2(light).endVertex();
		consumer.vertex(mat, 0, 0.00128624283327f, 1).color(lscl, lscl, 0, 1).uv2(light).endVertex();
		consumer.vertex(mat, 0, 0.00128624283327f, 0).color(lscl, lscl, 0, 1).uv2(light).endVertex();
		
		lscl = 0.7f;
		consumer.vertex(mat, 0.00128624283327f, 1, 1).color(lscl, lscl, 0, 1).uv2(light).endVertex();
		consumer.vertex(mat, 0.00128624283327f, 0, 0).color(lscl, lscl, 0, 1).uv2(light).endVertex();
		consumer.vertex(mat, 0.00128624283327f, 1, 0).color(lscl, lscl, 0, 1).uv2(light).endVertex();
		
		lscl = 0.75f;
		consumer.vertex(mat, 1, 1, 0.00128624283327f).color(lscl, lscl, 0, 1).uv2(light).endVertex();
		consumer.vertex(mat, 1, 0, 0.00128624283327f).color(lscl, lscl, 0, 1).uv2(light).endVertex();
		consumer.vertex(mat, 0, 0, 0.00128624283327f).color(lscl, lscl, 0, 1).uv2(light).endVertex();
		
		consumer = source.getBuffer(RenderType.cutout());
	}
}
