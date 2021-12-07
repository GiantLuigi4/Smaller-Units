package tfc.smallerunits.utils.collision;

import net.minecraft.util.math.AxisAlignedBB;

public class AxisAlignedBBHelper {
	public static double calculateXOffset(AxisAlignedBB first, AxisAlignedBB other, double offsetX) {
		if (other.maxY > first.minY && other.minY < first.maxY && other.maxZ > first.minZ && other.minZ < first.maxZ) {
			if (offsetX > 0.0D && other.maxX <= first.minX) {
				double d1 = first.minX - other.maxX;
				
				if (d1 < offsetX) {
					offsetX = d1;
				}
			} else if (offsetX < 0.0D && other.minX >= first.maxX) {
				double d0 = first.maxX - other.minX;
				
				if (d0 > offsetX) {
					offsetX = d0;
				}
			}
		}
		return offsetX;
	}
	
	public static double calculateYOffset(AxisAlignedBB first, AxisAlignedBB other, double offsetY) {
		if (other.maxX > first.minX && other.minX < first.maxX && other.maxZ > first.minZ && other.minZ < first.maxZ) {
			if (offsetY > 0.0D && other.maxY <= first.minY) {
				double d1 = first.minY - other.maxY;
				
				if (d1 < offsetY) {
					offsetY = d1;
				}
			} else if (offsetY < 0.0D && other.minY >= first.maxY) {
				double d0 = first.maxY - other.minY;
				
				if (d0 > offsetY) {
					offsetY = d0;
				}
			}
		}
		return offsetY;
	}
	
	public static double calculateZOffset(AxisAlignedBB first, AxisAlignedBB other, double offsetZ) {
		if (other.maxX > first.minX && other.minX < first.maxX && other.maxY > first.minY && other.minY < first.maxY) {
			if (offsetZ > 0.0D && other.maxZ <= first.minZ) {
				double d1 = first.minZ - other.maxZ;
				
				if (d1 < offsetZ) {
					offsetZ = d1;
				}
			} else if (offsetZ < 0.0D && other.minZ >= first.maxZ) {
				double d0 = first.maxZ - other.minZ;
				
				if (d0 > offsetZ) {
					offsetZ = d0;
				}
			}
		}
		return offsetZ;
	}
}
