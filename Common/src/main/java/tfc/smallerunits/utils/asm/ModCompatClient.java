package tfc.smallerunits.utils.asm;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import tfc.smallerunits.client.abstraction.IFrustum;
import tfc.smallerunits.client.render.TileRendererHelper;

public class ModCompatClient {
    /**
     * mixin point; called after rendering a chunk layer
     */
    public static void drawBE(BlockEntity be, BlockPos origin, IFrustum frustum, PoseStack stk, float tickDelta) {
        TileRendererHelper.renderBE(be, origin, frustum, stk, Minecraft.getInstance().getBlockEntityRenderDispatcher(), tickDelta);
    }

    /**
     * mixin point; called after rendering a chunk layer
     */
    public static void postRenderLayer(RenderType type, PoseStack poseStack, double camX, double camY, double camZ, Level level) {
    }
}
