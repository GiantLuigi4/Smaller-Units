package com.tfc.smallerunits.utils;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Optional;

public class UnitRaytraceContext {
	public VoxelShape shapeHit;
	public BlockPos posHit;
	public Vector3d vecHit;
	public Optional<Direction> hitFace = Optional.empty();
	
	public UnitRaytraceContext(VoxelShape shapeHit, BlockPos posHit, Vector3d vecHit) {
		this.shapeHit = shapeHit;
		this.posHit = posHit;
		this.vecHit = vecHit;
	}
}
