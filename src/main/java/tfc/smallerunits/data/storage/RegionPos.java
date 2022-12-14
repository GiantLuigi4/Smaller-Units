package tfc.smallerunits.data.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

import java.util.Objects;

public class RegionPos {
	public final int x, y, z;
	
	BlockPos pos;
	
	public RegionPos(BlockPos pPos) {
		this(pPos.getX() >> 9, pPos.getY() >> 9, pPos.getZ() >> 9);
	}
	
	public RegionPos(ChunkPos chunkPos) {
		this(chunkPos.getWorldPosition());
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		RegionPos regionPos = (RegionPos) o;
		return x == regionPos.x && y == regionPos.y && z == regionPos.z;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(x, y, z);
	}
	
	@Override
	public String toString() {
		return "RegionPos{" +
				"x=" + x +
				", y=" + y +
				", z=" + z +
				'}';
	}
	
	public boolean regionContains(ChunkPos pos, int minBuildHeight) {
		if (minBuildHeight >= y && minBuildHeight < (y + 512))
			if (pos.getMinBlockX() >= x && pos.getMinBlockZ() >= z)
				return pos.getMinBlockX() < (x + 512) && pos.getMinBlockZ() < (z + 512);
		return false;
	}
	
	public RegionPos(int pX, int pY, int pZ) {
		this.x = pX;
		this.y = pY;
		this.z = pZ;
		pos = new BlockPos(x << 9, y << 9, z << 9);
	}
	
	public BlockPos toBlockPos() {
		return pos;
	}
}
