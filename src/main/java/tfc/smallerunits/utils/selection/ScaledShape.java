package tfc.smallerunits.utils.selection;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.ArrayVoxelShape;
import net.minecraft.world.phys.shapes.CubeVoxelShape;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ScaledShape {
	BlockPos pos;
	VoxelShape src;
	Vec3 offset;
	double scale;
	boolean usePrecise;
	boolean cube;
	private static final Class<?> ARRAY = ArrayVoxelShape.class;
	private static final Class<?> CUBE = CubeVoxelShape.class;
	private static final BlockPos ZERO = new BlockPos(0, 0, 0);
	
	MutableAABB worker = new MutableAABB(0, 0, 0, 1, 1, 1);
	
	public ScaledShape(BlockPos pos, VoxelShape src, Vec3 offset, double scale) {
		this.pos = pos;
		this.src = src;
		this.offset = offset;
		this.scale = scale;
		Class<?> clazz = src.getClass();
		usePrecise = clazz.equals(ARRAY);
		cube = clazz.equals(CUBE);
	}
	
	public BlockHitResult clip(BlockPos actualPos, Vec3 start, Vec3 end) {
		if (cube) {
			AABB aabb = src.bounds();
			worker.set(aabb).scale(scale).move(offset).move(actualPos);
			
			double[] percent = {1};
			double d0 = end.x - start.x;
			double d1 = end.y - start.y;
			double d2 = end.z - start.z;
			
			if (worker.contains(start)) {
				return new UnitHitResult(
						start,
						Direction.getNearest(d0, d1, d2).getOpposite(),
						actualPos,
						worker.contains(start),
						pos, null
				);
			}
			
			Direction direction = AABB.getDirection(worker, start, percent, null, d0, d1, d2);
			if (direction == null) return null;
			
			double percentile = percent[0];
			
			return new UnitHitResult(
					start.add(d0 * percentile, d1 * percentile, d2 * percentile),
					direction, actualPos,
					worker.contains(start),
					pos, null
			);
		} else if (usePrecise) {
			double bestDist = Double.POSITIVE_INFINITY;
			BlockHitResult best = null;
			
			for (AABB aabb : src.toAabbs()) {
				worker.set(aabb).scale(scale).move(offset).move(actualPos);
				
				double[] percent = {1};
				double d0 = end.x - start.x;
				double d1 = end.y - start.y;
				double d2 = end.z - start.z;
				
				if (worker.contains(start)) {
					return new UnitHitResult(
							start,
							Direction.getNearest(d0, d1, d2).getOpposite(),
							actualPos,
							worker.contains(start),
							pos, null
					);
				}
				
				Direction direction = AABB.getDirection(worker, start, percent, null, d0, d1, d2);
				if (direction == null) continue;
				
				double percentile = percent[0];
				percent[0] = 1;
				
				if (percentile < bestDist) {
					best = new UnitHitResult(
							start.add(d0 * percentile, d1 * percentile, d2 * percentile),
							direction, actualPos,
							worker.contains(start),
							pos, null
					);
					bestDist = percentile;
				}
			}
			
			return best;
		} else {
			MutableVec3 modifiedStart = new MutableVec3(start.x, start.y, start.z);
			MutableVec3 modifiedEnd = new MutableVec3(end.x, end.y, end.z);
			
			modifiedStart.subtract(actualPos);
			modifiedEnd.subtract(actualPos);
			
			Vec3 tms = modifiedStart.scale(1d / scale);
			Vec3 tme = modifiedEnd.scale(1d / scale);
			
			BlockHitResult result = src.clip(tms, tme, pos);
			if (result == null || result.getType() == HitResult.Type.MISS) return null;
			
			Vec3 oset = end.subtract(start);
			oset = oset.normalize().scale(-scale);
			tms = result.getLocation().add(oset);
			tme = result.getLocation().subtract(oset);
			
			result = src.clip(tms, tme, pos);
			if (result == null || result.getType() == HitResult.Type.MISS) return null;
			
			MutableVec3 loc = new MutableVec3(result.getLocation().x, result.getLocation().y, result.getLocation().z);
			Vec3 v3 = loc.scale(scale);
			v3 = v3.add(actualPos.getX(), actualPos.getY(), actualPos.getZ());
			
			return new UnitHitResult(
					v3,
					result.getDirection(),
					actualPos,
					result.isInside(),
					pos,
					null
			);
		}
	}
}
