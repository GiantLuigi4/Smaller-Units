package tfc.smallerunits.plat.asm;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import tfc.smallerunits.data.storage.RegionPos;
import tfc.smallerunits.simulation.chunk.BasicVerticalChunk;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.utils.math.HitboxScaling;
import tfc.smallerunits.utils.scale.ResizingUtils;

public class PlatformQol {
	public static boolean runSUFluidCheck(Entity entity, TagKey<Fluid> fluids, double something, Level level, RegionPos regionPos, Object2DoubleMap<TagKey<Fluid>> fluidHeight) {
		AABB aabb = HitboxScaling.getOffsetAndScaledBox(entity.getBoundingBox().deflate(0.001D), entity.getPosition(0), ((ITickerLevel) level).getUPB(), regionPos);
		
		// TODO: limit check area to only the active world
		
		int minX = Mth.floor(aabb.minX);
		int maxX = Mth.ceil(aabb.maxX);
		int minY = Mth.floor(aabb.minY);
		int maxY = Mth.ceil(aabb.maxY);
		int minZ = Mth.floor(aabb.minZ);
		int maxZ = Mth.ceil(aabb.maxZ);
		boolean flag = entity.isPushedByFluid();
		Vec3 vec3 = new Vec3(0, 0, 0);
		BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
		int countIn = 0;
		double height = 0.0D;
		
		double scale = ((ITickerLevel) level).getUPB();
		
		boolean inFluid = false;
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
							inFluid = true;
							height = Math.max((d1 - aabb.minY) / scale, height);
							if (flag) {
								Vec3 vec31 = fluidState.getFlow(level, blockpos$mutableblockpos);
								if (height < 0.4D) {
									vec31 = vec31.scale(height / ResizingUtils.getActualSize(entity));
								}
								
								vec3.x += vec31.x;
								vec3.y += vec31.y;
								vec3.z += vec31.z;
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
		
		return inFluid;
	}
}
