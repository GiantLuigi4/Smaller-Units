package tfc.smallerunits.utils.asm;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import tfc.smallerunits.data.storage.RegionPos;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.utils.math.HitboxScaling;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public class EntityQol {
	protected static void forEachBlock(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Level level, BiConsumer<BlockPos, ChunkAccess> function) {
		BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
		for (int l1 = minX; l1 < maxX; ++l1) {
			for (int j2 = minZ; j2 < maxZ; ++j2) {
				int pX = SectionPos.blockToSectionCoord(l1);
				int pZ = SectionPos.blockToSectionCoord(j2);
				ChunkAccess chunk = level.getChunk(pX, pZ, ChunkStatus.FULL, false);
				if (chunk == null) continue;
				
				for (int i2 = minY; i2 < maxY; ++i2) {
					blockpos$mutableblockpos.set(l1, i2, j2);
					function.accept(blockpos$mutableblockpos, chunk);
				}
			}
		}
	}
	
	public static boolean inAnyFluid(AABB box, Level level, RegionPos regionPos) {
		box = box.move(0, -0.6, 0);
		Vec3 center = box.getCenter();
		AABB aabb = HitboxScaling.getOffsetAndScaledBox(box, new Vec3(center.x, box.minY, center.z), ((ITickerLevel) level).getUPB(), regionPos);
		
		int i = Mth.floor(aabb.minX);
		int j = Mth.ceil(aabb.maxX);
		int k = Mth.floor(aabb.minY);
		int l = Mth.ceil(aabb.maxY);
		int i1 = Mth.floor(aabb.minZ);
		int j1 = Mth.ceil(aabb.maxZ);
		
		BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
		for (int l1 = i; l1 < j; ++l1) {
			for (int j2 = i1; j2 < j1; ++j2) {
				int pX = SectionPos.blockToSectionCoord(l1);
				int pZ = SectionPos.blockToSectionCoord(j2);
				ChunkAccess chunk = level.getChunk(pX, pZ, ChunkStatus.FULL, false);
				if (chunk == null) continue;
				
				for (int i2 = k; i2 < l; ++i2) {
					blockpos$mutableblockpos.set(l1, i2, j2);
					if (!chunk.getBlockState(blockpos$mutableblockpos).getFluidState().isEmpty())
						return true;
				}
			}
		}
		
		return false;
	}
	
	public static void runSUFluidEyeCheck(Entity entity, Set<TagKey<Fluid>> fluidOnEyes, Level level, RegionPos regionPos) {
		fluidOnEyes.clear();
		// TODO: whatever the heck this is
//		Entity mount = entity.getVehicle();
//		if (mount instanceof Boat) {
//			Boat boat = (Boat) mount;
//			if (!boat.isUnderWater() && boat.getBoundingBox().maxY >= d0 && boat.getBoundingBox().minY <= d0) {
//				return;
//			}
//		}
		
		// TODO: don't scale the whole box
		AABB box = HitboxScaling.getOffsetAndScaledBox(entity.getBoundingBox(), entity.getPosition(0), ((ITickerLevel) level).getUPB(), regionPos);
		double d0 = box.minY + (entity.getEyeHeight() * ((ITickerLevel) level).getUPB());
		Vec3 vec = box.getCenter();
		BlockPos blockpos = new BlockPos(vec.x, d0, vec.z);
		FluidState fluidstate = level.getFluidState(blockpos);
		double d1 = (float) blockpos.getY() + fluidstate.getHeight(level, blockpos);
		if (d1 > d0) {
			fluidstate.getTags().forEach(fluidOnEyes::add);
		}
	}
	
	public static BlockState getSUBlockAtFeet(Entity entity, Level level, RegionPos regionPos) {
		// TODO: don't scale the whole bounding box
		AABB box = HitboxScaling.getOffsetAndScaledBox(entity.getBoundingBox(), entity.getPosition(0), ((ITickerLevel) level).getUPB(), regionPos);
		double d0 = box.minY;
		Vec3 vec = box.getCenter();
		BlockPos blockpos = new BlockPos(vec.x, d0, vec.z);
		return level.getBlockState(blockpos);
	}
}
