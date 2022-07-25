package tfc.smallerunits.utils.selection;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class UnitHitResult extends BlockHitResult {
	BlockPos unit;
	AABB specificBox;
	
	public UnitHitResult(Vec3 pLocation, Direction pDirection, BlockPos pBlockPos, boolean pInside, BlockPos unit, AABB specificBox) {
		super(pLocation, pDirection, pBlockPos, pInside);
		this.unit = unit;
		this.specificBox = specificBox;
	}
	
	public BlockPos geetBlockPos() {
		return unit;
	}
	
	public AABB getSpecificBox() {
		return specificBox;
	}
}
