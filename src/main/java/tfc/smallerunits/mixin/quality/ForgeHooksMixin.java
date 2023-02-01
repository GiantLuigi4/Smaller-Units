package tfc.smallerunits.mixin.quality;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ForgeMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.UnitSpace;
import tfc.smallerunits.data.capability.ISUCapability;
import tfc.smallerunits.data.capability.SUCapabilityManager;
import tfc.smallerunits.data.tracking.ICanUseUnits;
import tfc.smallerunits.data.tracking.RaytraceData;
import tfc.smallerunits.networking.hackery.NetworkingHacks;
import tfc.smallerunits.utils.PositionalInfo;
import tfc.smallerunits.utils.selection.UnitHitResult;

import java.util.ArrayList;

@Mixin(value = ForgeHooks.class, remap = false)
public abstract class ForgeHooksMixin {
	@Shadow
	public static native boolean onPickBlock(HitResult target, Player player, Level level);
	
	@Unique
	private static final ArrayList<RaytraceData> datas = new ArrayList<>();
	@Unique
	private static HitResult res;
	
	@Inject(at = @At("HEAD"), method = "onPickBlock", cancellable = true)
	private static void prePick(HitResult target, Player player, Level level, CallbackInfoReturnable<Boolean> cir) {
		if (movePlayerTo(target, player, level)) {
			if (player instanceof ICanUseUnits unitUser) {
				cir.setReturnValue(onPickBlock(res, player, player.level));
			}
		}
	}
	
	@Inject(at = @At("RETURN"), method = "onPickBlock")
	private static void postPick(HitResult target, Player player, Level level, CallbackInfoReturnable<Boolean> cir) {
		movePlayerBack(target, player, level);
	}
	
	@Unique
	private static boolean movePlayerTo(HitResult hitResult, Player player, Level level) {
		boolean changed = hitResult instanceof UnitHitResult;
		boolean finishedAny = false;
		while (hitResult instanceof UnitHitResult) {
			ISUCapability capability = SUCapabilityManager.getCapability(
					level, level.getChunk(((UnitHitResult) hitResult).getBlockPos())
			);
			PositionalInfo info = new PositionalInfo(player, false);
			UnitSpace space = capability.getUnit(((UnitHitResult) hitResult).getBlockPos());
			if (space == null) return finishedAny;
			info.scalePlayerReach(player, space.unitsPerBlock);
			info.adjust(player, space);
			level = (ClientLevel) player.level;
//			NetworkingHacks.setPos(new NetworkingHacks.LevelDescriptor(new RegionPos(((UnitHitResult) hitResult).geetBlockPos()), space.unitsPerBlock));
			NetworkingHacks.setPos(new NetworkingHacks.LevelDescriptor(space.regionPos, space.unitsPerBlock));
			RaytraceData data = new RaytraceData(hitResult, info);
			datas.add(data);
			
			if (player instanceof ICanUseUnits unitUser) {
				unitUser.setResult(hitResult);
			}
			
			double reach = player.getAttribute((Attribute) ForgeMod.REACH_DISTANCE.get()).getValue();// 154
			hitResult = player.pick(reach, 1, false);
//			if (hitResult.getType() == HitResult.Type.MISS)
//				hitResult = player.pick(reach, 1, false);
			finishedAny = true;
		}
		res = hitResult;
		return changed;
	}
	
	@Unique
	private static void movePlayerBack(HitResult hitResult, Player player, Level level) {
		if (!datas.isEmpty()) {
			while (!datas.isEmpty()) {
				NetworkingHacks.unitPos.remove();
				RaytraceData data = datas.remove(datas.size() - 1);
				data.info.reset(player);
				hitResult = data.result;
			}
		}
	}
}
