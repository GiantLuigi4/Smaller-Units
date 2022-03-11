package tfc.smallerunits.utils.selection;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class UnitHitResult extends BlockHitResult {
	public UnitHitResult(Vec3 pLocation, Direction pDirection, BlockPos pBlockPos, boolean pInside, BlockPos unit) {
		super(pLocation, pDirection, pBlockPos, pInside);
	}
}
