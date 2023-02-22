package tfc.smallerunits.mixin.quality;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.utils.selection.MutableVec3;

@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin<T extends BlockEntity> {
	@Shadow
	public Camera camera;
	
	@Unique
	BlockEntity be;
	
	@Unique
	private final MutableVec3 mv3 = new MutableVec3(0, 0, 0);
	
	@Inject(at = @At("HEAD"), method = "render")
	public void preRender(T pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, CallbackInfo ci) {
		this.be = pBlockEntity;
	}
	
	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getPosition()Lnet/minecraft/world/phys/Vec3;"))
	public Vec3 modifyCameraPos(Camera instance) {
		if (be.getLevel() instanceof ITickerLevel tkLvl) {
			Vec3 pos = camera.getPosition();
			// TODO: test
			mv3.x = (pos.x - tkLvl.getRegion().pos.toBlockPos().getX()) * (double) tkLvl.getUPB();
			mv3.y = (pos.y - tkLvl.getRegion().pos.toBlockPos().getY()) * (double) tkLvl.getUPB();
			mv3.z = (pos.z - tkLvl.getRegion().pos.toBlockPos().getZ()) * (double) tkLvl.getUPB();
			return mv3;
		}
		return camera.getPosition();
	}
}
