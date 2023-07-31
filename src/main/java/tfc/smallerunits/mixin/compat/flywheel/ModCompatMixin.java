package tfc.smallerunits.mixin.compat.flywheel;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;
import com.jozufozu.flywheel.event.RenderLayerEvent;
import com.jozufozu.flywheel.fabric.event.FlywheelEvents;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.data.tracking.RegionalAttachments;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.utils.asm.ModCompat;

@Mixin(value = ModCompat.class, remap = false)
public class ModCompatMixin {
	@Inject(at = @At("HEAD"), method = "onAddBE")
	private static void preAddBE(BlockEntity be, CallbackInfo ci) {
		if (Backend.canUseInstancing(be.getLevel())) {// 40
			if (InstancedRenderRegistry.canInstance(be.getType())) {// 47
				InstancedRenderDispatcher.getBlockEntities(be.getLevel()).add(be);// 56
				InstancedRenderDispatcher.getBlockEntities(be.getLevel()).update(be);// 75 76
//				return false;
			}

//			if (!InstancedRenderRegistry.shouldSkipRender(be)) {// 51
//				self.add(be);// 52
//				return true;// 57
//			}
		}
//		else return true
	}

	@Inject(at = @At("HEAD"), method = "onRemoveBE")
	private static void preRemoveBE(BlockEntity be, CallbackInfo ci) {
		if (Backend.canUseInstancing(be.getLevel())) {// 40
			if (InstancedRenderRegistry.canInstance(be.getType())) {// 47
				InstancedRenderDispatcher.getBlockEntities(be.getLevel()).remove(be);
				InstancedRenderDispatcher.getBlockEntities(be.getLevel()).update(be);// 75 76
			}
		}
	}

	@Inject(at = @At("HEAD"), method = "postRenderLayer")
	private static void afterRenderLayer(RenderType type, PoseStack poseStack, double camX, double camY, double camZ, Level level, CallbackInfo ci) {
		for (Region value : ((RegionalAttachments) level).SU$getRegionMap().values()) {
			BlockPos rp = value.pos.toBlockPos();
			for (Level valueLevel : value.getLevels()) {
				if (valueLevel != null) {
					poseStack.pushPose();
					poseStack.scale(
							1f / ((ITickerLevel) valueLevel).getUPB(),
							1f / ((ITickerLevel) valueLevel).getUPB(),
							1f / ((ITickerLevel) valueLevel).getUPB()
					);
					int mul = ((ITickerLevel) valueLevel).getUPB();
					RenderLayerEvent event = new RenderLayerEvent(
							(ClientLevel) valueLevel,
							type, poseStack, Minecraft.getInstance().renderBuffers(),
							camX * mul, camY * mul, (camZ - rp.getZ()) * mul
					);
					FlywheelEvents.RENDER_LAYER.invoker().handleEvent(event);
					poseStack.popPose();
				}
			}
		}
	}
}
