package com.tfc.smallerunits.utils;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;

public class UnitRaytraceContext {
	public VoxelShape shapeHit;
	public BlockPos posHit;
	public Vector3d vecHit;
	
	public UnitRaytraceContext(VoxelShape shapeHit, BlockPos posHit, Vector3d vecHit) {
		this.shapeHit = shapeHit;
		this.posHit = posHit;
		this.vecHit = vecHit;
	}
}
