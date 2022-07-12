package tfc.smallerunits.utils.asm;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;

public class ModCompat {
	public static void postSetupMatrix(RenderBuffers renderBuffers, ClientLevel level, RenderType type, PoseStack stack, RenderBuffers buffers, double camX, double camY, double camZ) {
		// TODO: mixin connector should gut this method out of flywheel is not present
		// TODO: fix this
//		GlStateTracker.State restoreState = GlStateTracker.getRestoreState();
//		MinecraftForge.EVENT_BUS.post(new RenderLayerEvent(level, type, stack, renderBuffers, camX, camY, camZ));
//		restoreState.restore();
//		type.setupRenderState();
	}
}
