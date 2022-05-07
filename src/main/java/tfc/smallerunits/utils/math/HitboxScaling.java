package tfc.smallerunits.utils.math;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class HitboxScaling {
	public static AABB getOffsetAndScaledBox(AABB box, Vec3 entityPos, int upb) {
		box = box.move(-entityPos.x, -entityPos.y, -entityPos.z);
		box = new AABB(
				box.minX * upb,
				box.minY * upb,
				box.minZ * upb,
				box.maxX * upb,
				box.maxY * upb,
				box.maxZ * upb
		);
		box = box.move(entityPos.x * upb, entityPos.y * upb, entityPos.z * upb);
		return box;
	}
}
