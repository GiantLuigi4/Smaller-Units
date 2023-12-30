package tfc.smallerunits.utils.selection;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;

public class UnitDiscreteShape extends DiscreteVoxelShape {
	UnitShape sp;
	
	public UnitDiscreteShape(int pXSize, int pYSize, int pZSize) {
		super(pXSize, pYSize, pZSize);
	}
	
	@Override
	public boolean isFull(int pX, int pY, int pZ) {
		for (AABB toAabb : sp.toAabbs()) {
			if (toAabb.contains(pX, pY, pZ)) return true;
		}
		return false;
	}
	
	@Override
	public void fill(int pX, int pY, int pZ) {
		// TODO
	}
	
	@Override
	public int firstFull(Direction.Axis pAxis) {
		// TODO
		return 0;
	}
	
	@Override
	public int lastFull(Direction.Axis pAxis) {
		// TODO
		return 0;
	}
}
