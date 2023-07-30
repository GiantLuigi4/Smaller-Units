package tfc.smallerunits.utils.asm;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.ModList;

public class ModCompat {
	public static final boolean isFlywheelPresent = ModList.get().isLoaded("flywheel");
	
	/**
	 * mixin point; called when a small BE is added on client
	 */
	public static void onAddBE(BlockEntity be) {
	}
	
	/**
	 * mixin point; called when a small BE is removed on client
	 */
	public static void onRemoveBE(BlockEntity be) {
	}
	
	/**
	 * mixin point; called after rendering a chunk layer
	 */
	public static void postRenderLayer(RenderType type, PoseStack poseStack, double camX, double camY, double camZ, Level level) {
	}
}
