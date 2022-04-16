package tfc.smallerunits.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import tfc.smallerunits.simulation.world.server.TickerServerWorld;

public class TileRendererHelper {
	
	public static void setupStack(PoseStack stk, BlockEntity tile, BlockPos origin) {
		stk.pushPose();
		Level lvl = tile.getLevel();
		Vec3 cam = Minecraft.getInstance().getEntityRenderDispatcher().camera.getPosition();
		stk.translate(origin.getX() - cam.x, origin.getY() - cam.y, origin.getZ() - cam.z);
		if (lvl instanceof TickerServerWorld) {
			int upb = ((TickerServerWorld) lvl).getUnitsPerBlock();
			float scl = 1f / upb;
			stk.scale(scl, scl, scl);
		}
		stk.translate(
				tile.getBlockPos().getX(),
				tile.getBlockPos().getY(),
				tile.getBlockPos().getZ()
		);
	}
}
