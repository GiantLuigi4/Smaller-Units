package tfc.smallerunits.utils.math;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import tfc.smallerunits.UnitSpace;
import tfc.smallerunits.data.storage.RegionPos;
import tfc.smallerunits.simulation.level.ITickerLevel;

public class HitboxScaling {
	public static AABB getOffsetAndScaledBox(AABB box, Vec3 entityPos, int upb, RegionPos regionPos) {
		box = box.move(-entityPos.x, -entityPos.y, -entityPos.z);
		box = new AABB(
				box.minX * upb,
				box.minY * upb,
				box.minZ * upb,
				box.maxX * upb,
				box.maxY * upb,
				box.maxZ * upb
		);
		BlockPos bp = regionPos.toBlockPos();
		box = box.move((entityPos.x - bp.getX()) * upb, (entityPos.y - bp.getY()) * upb, (entityPos.z - bp.getZ()) * upb);
		return box;
	}
	
	// TODO?
	public static double scaleX(UnitSpace space, double coord) {
		coord -= ((ITickerLevel) space.getMyLevel()).getRegion().pos.x;
		coord *= 1d / space.unitsPerBlock;
		coord += ((ITickerLevel) space.getMyLevel()).getRegion().pos.x;
		return coord;
	}
	
	public static double scaleY(UnitSpace space, double coord) {
		coord -= ((ITickerLevel) space.getMyLevel()).getRegion().pos.y;
		coord *= 1d / space.unitsPerBlock;
		coord += ((ITickerLevel) space.getMyLevel()).getRegion().pos.y;
		return coord;
	}
	
	public static double scaleZ(UnitSpace space, double coord) {
		coord -= ((ITickerLevel) space.getMyLevel()).getRegion().pos.z;
		coord *= 1d / space.unitsPerBlock;
		coord += ((ITickerLevel) space.getMyLevel()).getRegion().pos.z;
		return coord;
	}
}
