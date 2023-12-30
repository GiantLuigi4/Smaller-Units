package tfc.smallerunits.api;

import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class CollisionUtils {
	/**
	 * Checks for any collisions with small blocks in the given direction, at the given distance
	 *
	 * @param level the level of, say, an entity
	 * @param box the collision box of whatever's being checked
	 * @param axis the axis to check on
	 * @param delta the distance to look in that direction
	 * @return true if there are any collisions, elsewise, false
	 */
	public static boolean checkCollision(Level level, AABB box, Direction.Axis axis, double delta) {
		// TODO:
		return false;
	}
}
