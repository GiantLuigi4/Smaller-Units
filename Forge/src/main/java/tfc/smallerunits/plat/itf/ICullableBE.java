package tfc.smallerunits.plat.itf;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

public interface ICullableBE {
	AABB getRenderBoundingBox();
	
	static AABB getCullingBB(BlockEntity be) {
		return be.getRenderBoundingBox();
	}
}
