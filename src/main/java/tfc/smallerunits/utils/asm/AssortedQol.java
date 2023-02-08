package tfc.smallerunits.utils.asm;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.UnitSpace;
import tfc.smallerunits.data.capability.ISUCapability;
import tfc.smallerunits.data.capability.SUCapabilityManager;
import tfc.smallerunits.data.storage.RegionPos;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.utils.selection.UnitHitResult;

import java.util.List;
import java.util.Map;

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
	
	public static void handleBlockInfo(HitResult block, CallbackInfoReturnable<List<String>> cir, List<String> strings) {
		if (block instanceof UnitHitResult result) {
			if (strings.get(strings.size() - 1).equals("smallerunits:unit_space")) {
				strings.remove(strings.size() - 1);
				strings.remove(strings.size() - 1);
			} else {
				strings.add("");
			}
			
			Level level = Minecraft.getInstance().level;
			ISUCapability capability = SUCapabilityManager.getCapability(level, new ChunkPos(result.getBlockPos()));
			UnitSpace space = capability.getUnit(result.getBlockPos());
			
//			Vec3 start = Minecraft.getInstance().cameraEntity.getEyePosition(0);
//			Vec3 end = Minecraft.getInstance().cameraEntity.getEyePosition(0).add(Minecraft.getInstance().cameraEntity.getViewVector(0).scale(20)); // TODO: figure out what exactly this should be
//			start = new Vec3(
//					HitboxScaling.scaleX((ITickerLevel) space.getMyLevel(), start.x),
//					HitboxScaling.scaleY((ITickerLevel) space.getMyLevel(), start.y),
//					HitboxScaling.scaleZ((ITickerLevel) space.getMyLevel(), start.z)
//			);
//			end = new Vec3(
//					HitboxScaling.scaleX((ITickerLevel) space.getMyLevel(), end.x),
//					HitboxScaling.scaleY((ITickerLevel) space.getMyLevel(), end.y),
//					HitboxScaling.scaleZ((ITickerLevel) space.getMyLevel(), end.z)
//			);
//			BlockHitResult result1 = space.getMyLevel().clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, Minecraft.getInstance().player));
			
			BlockPos blockpos = space.getOffsetPos(result.geetBlockPos());
//			BlockPos blockpos = result1.getBlockPos();
			BlockState state = space.getMyLevel().getBlockState(blockpos);
			List<String> list = strings;
			
			list.add(ChatFormatting.UNDERLINE + "Targeted Small Block: " + blockpos.getX() + ", " + blockpos.getY() + ", " + blockpos.getZ());
			list.add(String.valueOf((Object) Registry.BLOCK.getKey(state.getBlock())));
			
			for (Map.Entry<Property<?>, Comparable<?>> entry : state.getValues().entrySet()) {
				Property<?> property = entry.getKey();
				Comparable<?> comparable = entry.getValue();
				String s = Util.getPropertyName(property, comparable);
				if (Boolean.TRUE.equals(comparable)) {
					s = ChatFormatting.GREEN + s;
				} else if (Boolean.FALSE.equals(comparable)) {
					s = ChatFormatting.RED + s;
				}
				
				boolean isNumber = true;
				for (char c : s.toCharArray()) {
					if (!Character.isDigit(c)) {
						isNumber = false;
						break;
					}
				}
				if (isNumber) s = ChatFormatting.GOLD + s;
				
				list.add(property.getName() + ": " + s);
			}
			
			for (Object o : state.getTags().toArray()) {
				list.add("#" + ((TagKey<Block>) o).location());
			}
		}
	}
}
