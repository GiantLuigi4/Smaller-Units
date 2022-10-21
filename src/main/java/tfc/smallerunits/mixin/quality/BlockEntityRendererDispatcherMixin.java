package tfc.smallerunits.mixin.quality;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tfc.smallerunits.simulation.world.ITickerWorld;

@Mixin(BlockEntityRenderer.class)
public interface BlockEntityRendererDispatcherMixin<T extends BlockEntity> {
	@Shadow
	int getViewDistance();
	
	/**
	 * @author GiantLuigi4
	 */
	@Overwrite
	default boolean shouldRender(T pBlockEntity, Vec3 pCameraPos) {
		if (pBlockEntity.getLevel() instanceof ITickerWorld tickerWorld) {
			double upb = tickerWorld.getUPB();
			double px = pBlockEntity.getBlockPos().getX() / upb + tickerWorld.getRegion().pos.toBlockPos().getX();
			double py = pBlockEntity.getBlockPos().getY() / upb + tickerWorld.getRegion().pos.toBlockPos().getX();
			double pz = (pBlockEntity.getBlockPos().getZ() / upb) + tickerWorld.getRegion().pos.toBlockPos().getX();
			px += 0.5;
			py += 0.5;
			pz += 0.5;
			double divisor = tickerWorld.getUPB();
			AABB renderBox = pBlockEntity.getRenderBoundingBox();
			double sz = renderBox.getSize();
			if (sz < 1) sz = 1;
			divisor /= sz;
			if (divisor < 1) divisor = 1;
			double dist = (double) this.getViewDistance() / divisor;
			return new Vec3(px, py, pz).closerThan(pCameraPos, dist);
		}
		return Vec3.atCenterOf(pBlockEntity.getBlockPos()).closerThan(pCameraPos, this.getViewDistance());
	}
}
