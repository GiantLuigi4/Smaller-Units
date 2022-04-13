package tfc.smallerunits.utils.math;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import tfc.smallerunits.UnitSpace;

public class HitboxScaling {
	public static AABB getOffsetAndScaledBox(AABB box, Vec3 entityPos, UnitSpace space) {
		box = box.move(-entityPos.x, -entityPos.y, -entityPos.z);
		box = new AABB(
				box.minX * space.unitsPerBlock,
				box.minY * space.unitsPerBlock,
				box.minZ * space.unitsPerBlock,
				box.maxX * space.unitsPerBlock,
				box.maxY * space.unitsPerBlock,
				box.maxZ * space.unitsPerBlock
		);
		box = box.move(entityPos.x * space.unitsPerBlock, entityPos.y * space.unitsPerBlock, entityPos.z * space.unitsPerBlock);
		return box;
	}
}
