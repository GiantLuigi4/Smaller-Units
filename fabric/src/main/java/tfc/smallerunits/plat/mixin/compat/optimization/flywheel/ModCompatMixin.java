package tfc.smallerunits.plat.mixin.compat.optimization.flywheel;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
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
}