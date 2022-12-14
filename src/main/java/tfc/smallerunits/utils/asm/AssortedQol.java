package tfc.smallerunits.utils.asm;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import tfc.smallerunits.data.storage.RegionPos;
import tfc.smallerunits.simulation.level.ITickerLevel;

public class AssortedQol {
	public static FogType getFogType(Level level, RegionPos regionPos, Vec3 position, Vec3 camPos) {
		position = position.scale(1d / ((ITickerLevel) level).getUPB()).add(camPos);
		
		BlockPos pos = regionPos.toBlockPos();
		position = position.subtract(pos.getX(), pos.getY(), pos.getZ());
		position = position.scale(((ITickerLevel) level).getUPB());
		
		BlockPos ps = new BlockPos(position);
		BlockState block = level.getBlockState(ps);
		FluidState fluid = block.getFluidState();
		if (fluid.is(FluidTags.LAVA)) {
			if (position.y <= (double) (fluid.getHeight(level, ps) + (float) ps.getY())) {
				return FogType.LAVA;
			}
		} else if (fluid.is(FluidTags.WATER)) {
			if (position.y <= (double) (fluid.getHeight(level, ps) + (float) ps.getY())) {
				return FogType.WATER;
			}
		} else if (block.is(Blocks.POWDER_SNOW)) {
			return FogType.POWDER_SNOW;
		}
		
		return FogType.NONE;
	}
}
