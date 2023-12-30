package tfc.smallerunits.mixin.quality;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tfc.smallerunits.plat.util.PlatformUtils;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.utils.asm.AssortedQol;

@Mixin(BlockEntityRenderer.class)
public interface BlockEntityRendererMixin<T extends BlockEntity> {
	@Shadow
	int getViewDistance();

//	@Redirect(method = "shouldRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;getBlockPos()Lnet/minecraft/core/BlockPos;"))
//	default BlockPos swapVD(BlockEntity instance) {
//		ThreadLocals.be.set(instance);
//		return instance.getBlockPos();
//	}
//
//	@Redirect(method = "shouldRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;closerThan(Lnet/minecraft/core/Position;D)Z"))
//	default boolean swapVD(Vec3 instance, Position pPos, double pDistance) {
//		BlockEntity pBlockEntity = ThreadLocals.be.get();
//		if (pBlockEntity.getLevel() instanceof ITickerLevel tickerWorld) {
//			double vd = this.getViewDistance();
//			double sd = ResizingUtils.getActualSize(Minecraft.getInstance().player);
//			double divisor = tickerWorld.getUPB();
//
//			if (sd > (1d / divisor)) sd = 1;
////			vd /= sd;
//
//			vd *= divisor;
//			divisor *= sd;
//			if (divisor <= 1.001)
//				return instance.closerThan(pPos, pDistance);
//
//			AABB renderBox = pBlockEntity.getRenderBoundingBox();
//			double sz = renderBox.getSize();
//
//			if (sz < 1) sz = 1;
//			divisor /= sz;
//			if (divisor < 1) divisor = 1;
//
//			return instance.closerThan(pPos, vd);
//		}
//		return instance.closerThan(pPos, pDistance);
//	}
	
	/**
	 * @author GiantLuigi4
	 */
	@Overwrite
	default boolean shouldRender(T pBlockEntity, Vec3 pCameraPos) {
		if (pBlockEntity.getLevel() instanceof ITickerLevel tickerWorld)
			return AssortedQol.scaleRender(getViewDistance(), PlatformUtils.getRenderBox(pBlockEntity), tickerWorld, pBlockEntity.getBlockPos(), pCameraPos);
		return Vec3.atCenterOf(pBlockEntity.getBlockPos()).closerThan(pCameraPos, this.getViewDistance());
	}
}
