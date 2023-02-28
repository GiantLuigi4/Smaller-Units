package tfc.smallerunits.utils.asm;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidType;
import org.apache.commons.lang3.tuple.MutableTriple;
import tfc.smallerunits.data.storage.RegionPos;
import tfc.smallerunits.simulation.chunk.BasicVerticalChunk;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.utils.math.HitboxScaling;
import tfc.smallerunits.utils.scale.ResizingUtils;

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
	
	public static double runSUFluidCheck(Entity entity, TagKey<Fluid> fluids, double something, Level level, RegionPos regionPos, Object2DoubleMap<TagKey<Fluid>> fluidHeight, Object2DoubleMap<FluidType> forgeFluidTypeHeight) {
		AABB aabb = HitboxScaling.getOffsetAndScaledBox(entity.getBoundingBox().deflate(0.001D), entity.getPosition(0), ((ITickerLevel) level).getUPB(), regionPos);
		
		// TODO: limit check area to only the active world
		
		int minX = Mth.floor(aabb.minX);
		int maxX = Mth.ceil(aabb.maxX);
		int minY = Mth.floor(aabb.minY);
		int maxY = Mth.ceil(aabb.maxY);
		int minZ = Mth.floor(aabb.minZ);
		int maxZ = Mth.ceil(aabb.maxZ);
		boolean flag = entity.isPushedByFluid();
		Vec3 vec3 = Vec3.ZERO;
		BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
		int countIn = 0;
		double height = 0.0D;
		
		double scale = ((ITickerLevel) level).getUPB();
		
		// TODO: use a more efficient loop
		for (int x = minX; x < maxX; ++x) {
			for (int z = minZ; z < maxZ; ++z) {
				{
					AABB box = new AABB(
							x, minY, z,
							(x + 1), maxY, (z + 1)
					);
					if (!box.intersects(aabb)) {
						continue;
					}
				}
				
				int pX = SectionPos.blockToSectionCoord(x);
				int pZ = SectionPos.blockToSectionCoord(z);
				BasicVerticalChunk chunk = (BasicVerticalChunk) level.getChunk(pX, pZ, ChunkStatus.FULL, false);
				if (chunk == null) {
					if (z == (z >> 4) << 4) {
						z += 15;
					} else {
						z = ((z >> 4) << 4) + 15;
					}
					continue;
				}
				
				for (int y = minY; y < maxY; ++y) {
					int sectionIndex = chunk.getSectionIndex(y);
					LevelChunkSection section = chunk.getSectionNullable(sectionIndex);
					if (section == null || section.hasOnlyAir()) {
						if (y == (y >> 4) << 4) {
							y += 15;
						} else {
							y = ((y >> 4) << 4) + 15;
						}
						continue;
					}
					
					blockpos$mutableblockpos.set(x, y, z);
					
					FluidState fluidState = chunk.getFluidState(blockpos$mutableblockpos);
					if (fluidState.is(fluids)) {
						double d1 = (float) y + fluidState.getHeight(level, blockpos$mutableblockpos);
						if (d1 >= aabb.minY) {
							height = Math.max((d1 - aabb.minY) / scale, height);
							if (flag) {
								Vec3 vec31 = fluidState.getFlow(level, blockpos$mutableblockpos);
								if (height < 0.4D) {
									vec31 = vec31.scale(height / ResizingUtils.getActualSize(entity));
								}
								
								vec3 = vec3.add(vec31);
								++countIn;
							}
						}
					}
				}
			}
		}
		fluidHeight.put(fluids, Math.max(fluidHeight.getOrDefault(fluids, 0), height));
		
		if (vec3.length() > 0.0D) {
			if (countIn > 0) {
				vec3 = vec3.scale(1.0D / (double) countIn);
			}
			
			if (!(entity instanceof Player)) {
				vec3 = vec3.normalize();
			}
			
			Vec3 vec32 = entity.getDeltaMovement();
			vec3 = vec3.scale(something);
			if (Math.abs(vec32.x) < 0.003D && Math.abs(vec32.z) < 0.003D && vec3.length() < 0.0045000000000000005D) {
				vec3 = vec3.normalize().scale(0.0045000000000000005D);
			}
			
			entity.setDeltaMovement(entity.getDeltaMovement().add(vec3));
		}
		
		return height;
	}
	
	public static void runSUFluidCheck(Entity entity, Level level, RegionPos regionPos, Object2DoubleMap<TagKey<Fluid>> fluidHeight, Object2DoubleMap<FluidType> forgeFluidTypeHeight) {
		AABB aabb = HitboxScaling.getOffsetAndScaledBox(entity.getBoundingBox().deflate(0.001D), entity.getPosition(0), ((ITickerLevel) level).getUPB(), regionPos);
		
		int minX = Mth.floor(aabb.minX);
		int maxX = Mth.ceil(aabb.maxX);
		int minY = Mth.floor(aabb.minY);
		int maxY = Mth.ceil(aabb.maxY);
		int minZ = Mth.floor(aabb.minZ);
		int maxZ = Mth.ceil(aabb.maxZ);
		
		BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
		it.unimi.dsi.fastutil.objects.Object2ObjectMap<net.minecraftforge.fluids.FluidType, org.apache.commons.lang3.tuple.MutableTriple<Double, Vec3, Integer>> interimCalcs = new it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap<>(net.minecraftforge.fluids.FluidType.SIZE.get() - 1);
		
		double scale = ((ITickerLevel) level).getUPB();
		
		for (int x = minX; x < maxX; ++x) {
			for (int z = minZ; z < maxZ; ++z) {
				{
					AABB box = new AABB(
							x, minY, z,
							(x + 1), maxY, (z + 1)
					);
					if (!box.intersects(aabb)) {
						continue;
					}
				}
				
				int pX = SectionPos.blockToSectionCoord(x);
				int pZ = SectionPos.blockToSectionCoord(z);
				BasicVerticalChunk chunk = (BasicVerticalChunk) level.getChunk(pX, pZ, ChunkStatus.FULL, false);
				if (chunk == null) {
					if (z == (z >> 4) << 4) {
						z += 15;
					} else {
						z = ((z >> 4) << 4) + 15;
					}
					continue;
				}
				
				for (int y = minY; y < maxY; ++y) {
					int sectionIndex = chunk.getSectionIndex(y);
					LevelChunkSection section = chunk.getSectionNullable(sectionIndex);
					if (section == null || section.hasOnlyAir()) {
						if (y == (y >> 4) << 4) {
							y += 15;
						} else {
							y = ((y >> 4) << 4) + 15;
						}
						continue;
					}
					
					blockpos$mutableblockpos.set(x, y, z);
					
					
					FluidState fluidstate = chunk.getFluidState(blockpos$mutableblockpos);
					net.minecraftforge.fluids.FluidType fluidType = fluidstate.getFluidType();
					if (!fluidType.isAir()) {
						double d1 = (float) y + fluidstate.getHeight(level, blockpos$mutableblockpos);
						if (d1 >= aabb.minY) {
							org.apache.commons.lang3.tuple.MutableTriple<Double, Vec3, Integer> interim = interimCalcs.computeIfAbsent(fluidType, t -> org.apache.commons.lang3.tuple.MutableTriple.of(0.0D, Vec3.ZERO, 0));
							interim.setLeft(Math.max((d1 - aabb.minY) / scale, interim.getLeft()));
							if (entity.isPushedByFluid(fluidType)) {
								Vec3 vec31 = fluidstate.getFlow(level, blockpos$mutableblockpos);
								if (interim.getLeft() < 0.4D) {
									vec31 = vec31.scale(interim.getLeft() / ResizingUtils.getActualSize(entity));
								}
								
								interim.setMiddle(interim.getMiddle().add(vec31));
								interim.setRight(interim.getRight() + 1);
							}
						}
					}
				}
			}
		}
		
		for (Map.Entry<FluidType, MutableTriple<Double, Vec3, Integer>> fluidTypeMutableTripleEntry : interimCalcs.entrySet()) {
			FluidType fluidType = fluidTypeMutableTripleEntry.getKey();
			org.apache.commons.lang3.tuple.MutableTriple<Double, Vec3, Integer> interim = fluidTypeMutableTripleEntry.getValue();
			
			if (interim.getMiddle().length() > 0.0D) {
				if (interim.getRight() > 0) {
					interim.setMiddle(interim.getMiddle().scale(1.0D / (double) interim.getRight()));
				}
				
				if (!(entity instanceof Player)) {
					interim.setMiddle(interim.getMiddle().normalize());
				}
				
				Vec3 vec32 = entity.getDeltaMovement();
				interim.setMiddle(interim.getMiddle().scale(entity.getFluidMotionScale(fluidType)));
				if (Math.abs(vec32.x) < 0.003D && Math.abs(vec32.z) < 0.003D && interim.getMiddle().length() < 0.0045000000000000005D) {
					interim.setMiddle(interim.getMiddle().normalize().scale(0.0045000000000000005D));
				}
				
				entity.push(interim.middle.x, interim.middle.y, interim.middle.z);
				
				double d = forgeFluidTypeHeight.getOrDefault(fluidType, 0d);
				forgeFluidTypeHeight.put(fluidType, Math.max(d, interim.getRight()));
			}
		}
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
