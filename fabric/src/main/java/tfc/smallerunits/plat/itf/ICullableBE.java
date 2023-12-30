package tfc.smallerunits.plat.itf;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

public interface ICullableBE {
	AABB getRenderBoundingBox();
	
	static AABB getCullingBB(BlockEntity be) {
		if (be instanceof ICullableBE) return ((ICullableBE) be).getRenderBoundingBox();
		else return new AABB(be.getBlockPos());
	}
}
