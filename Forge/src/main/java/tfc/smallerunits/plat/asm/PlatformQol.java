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
import net.minecraftforge.fluids.FluidType;
import org.apache.commons.lang3.tuple.MutableTriple;
import tfc.smallerunits.data.storage.RegionPos;
import tfc.smallerunits.simulation.chunk.BasicVerticalChunk;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.utils.math.HitboxScaling;
import tfc.smallerunits.utils.scale.ResizingUtils;

import java.util.Map;

public class PlatformQol {
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
							org.apache.commons.lang3.tuple.MutableTriple<Double, Vec3, Integer> interim = interimCalcs.computeIfAbsent(fluidType, t -> org.apache.commons.lang3.tuple.MutableTriple.of(0.0D, new Vec3(0, 0, 0), 0));
							interim.setLeft(Math.max((d1 - aabb.minY) / scale, interim.getLeft()));
							if (entity.isPushedByFluid(fluidType)) {
								Vec3 vec31 = fluidstate.getFlow(level, blockpos$mutableblockpos);
								if (interim.getLeft() < 0.4D) {
									vec31 = vec31.scale(interim.getLeft() / ResizingUtils.getActualSize(entity));
								}
								
								Vec3 mid = interim.getMiddle();
								mid.x += vec31.x;
								mid.y += vec31.y;
								mid.z += vec31.z;
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
			}
			
			double d = forgeFluidTypeHeight.getOrDefault(fluidType, 0d);
			forgeFluidTypeHeight.put(fluidType, Math.max(d, interim.getLeft()));
		}
	}
}
