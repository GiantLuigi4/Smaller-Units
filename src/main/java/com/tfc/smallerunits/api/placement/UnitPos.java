package com.tfc.smallerunits.api.placement;

import com.tfc.smallerunits.utils.MathUtils;
import net.minecraft.dispenser.IPosition;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;

public class UnitPos extends BlockPos {
	public final BlockPos realPos;
	public final int scale;
	
	public UnitPos(int x, int y, int z, BlockPos realPos, int scale) {
		super(x, y, z);
		this.realPos = realPos;
		this.scale = scale;
	}
	
	public UnitPos(double x, double y, double z, BlockPos realPos, int scale) {
		super(x, y, z);
		this.realPos = realPos;
		this.scale = scale;
	}
	
	public UnitPos(Vector3d vec, BlockPos realPos, int scale) {
		super(vec);
		this.realPos = realPos;
		this.scale = scale;
	}
	
	public UnitPos(IPosition position, BlockPos realPos, int scale) {
		super(position);
		this.realPos = realPos;
		this.scale = scale;
	}
	
	public UnitPos(Vector3i source, BlockPos realPos, int scale) {
		super(source);
		this.realPos = realPos;
		this.scale = scale;
	}
	
	public UnitPos getRelativePos(UnitPos other) {
		BlockPos offset = other.realPos.subtract(realPos);
		offset = new BlockPos(offset.getX() * scale, offset.getY() * scale, offset.getZ() * scale);
		return (UnitPos) this.add(offset);
	}
	
	@Override
	public BlockPos add(double x, double y, double z) {
		return x == 0.0D && y == 0.0D && z == 0.0D ? this : new UnitPos((double) this.getX() + x, (double) this.getY() + y, (double) this.getZ() + z, realPos, scale);
	}
	
	@Override
	public BlockPos add(int x, int y, int z) {
		return x == 0 && y == 0 && z == 0 ? this : new UnitPos(this.getX() + x, this.getY() + y, this.getZ() + z, realPos, scale);
	}
	
	public BlockPos offset(Direction facing) {
		return new UnitPos(this.getX() + facing.getXOffset(), this.getY() + facing.getYOffset(), this.getZ() + facing.getZOffset(), realPos, scale);
	}
	
	public UnitPos adjustRealPosition() {
		float offX = MathUtils.getChunkOffset(getX(), scale);
		float offY = MathUtils.getChunkOffset(getY() - 64, scale);
		float offZ = MathUtils.getChunkOffset(getZ(), scale);
		BlockPos newRealPos = realPos.add(offX, offY, offZ);
		float posX = MathUtils.chunkMod(getX(), scale);
		float posY = MathUtils.chunkMod(getY() - 64, scale);
		float posZ = MathUtils.chunkMod(getZ(), scale);
		return new UnitPos(posX, posY + 64, posZ, newRealPos, scale);
	}
	
	@Override
	public String toString() {
		return super.toString() + "{" + realPos.getX() + ", " + realPos.getY() + ", " + realPos.getZ() + "}";
	}
}
