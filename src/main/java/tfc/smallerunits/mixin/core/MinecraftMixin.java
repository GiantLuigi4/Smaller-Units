package tfc.smallerunits.mixin.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.ForgeMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.UnitSpace;
import tfc.smallerunits.data.capability.ISUCapability;
import tfc.smallerunits.data.capability.SUCapabilityManager;
import tfc.smallerunits.data.storage.RegionPos;
import tfc.smallerunits.data.tracking.RaytraceData;
import tfc.smallerunits.networking.hackery.NetworkingHacks;
import tfc.smallerunits.utils.PositionalInfo;
import tfc.smallerunits.utils.selection.UnitHitResult;

import javax.annotation.Nullable;
import java.util.ArrayList;

@Mixin(Minecraft.class)
public class MinecraftMixin {
	@Shadow
	@Nullable
	public HitResult hitResult;
	@Shadow
	@Nullable
	public LocalPlayer player;
	@Shadow
	@Nullable
	public ClientLevel level;
	@Unique
	ArrayList<RaytraceData> datas = new ArrayList<>();
	
	@Inject(at = @At("HEAD"), method = "startAttack")
	public void preAttack(CallbackInfoReturnable<Boolean> cir) {
		movePlayerTo();
	}
	
	@Inject(at = @At("HEAD"), method = "startUseItem")
	public void preUseItem(CallbackInfo cir) {
		// TODO
//		movePlayerTo();
	}
	
	@Inject(at = @At("HEAD"), method = "continueAttack")
	public void preContinueAttack(boolean direction, CallbackInfo ci) {
		movePlayerTo();
	}
	
	@Unique
	private void movePlayerTo() {
		while (hitResult instanceof UnitHitResult) {
			ISUCapability capability = SUCapabilityManager.getCapability(
					level, level.getChunk(((UnitHitResult) hitResult).getBlockPos())
			);
			PositionalInfo info = new PositionalInfo(player);
			UnitSpace space = capability.getUnit(((UnitHitResult) hitResult).getBlockPos());
			if (space == null) return;
			info.scalePlayerReach(player, space.unitsPerBlock);
			info.adjust(player, space);
			level = (ClientLevel) player.level;
			NetworkingHacks.unitPos.set(new NetworkingHacks.LevelDescriptor(new RegionPos(((UnitHitResult) hitResult).geetBlockPos()), space.unitsPerBlock));
			RaytraceData data = new RaytraceData(hitResult, info);
			datas.add(data);
			
			double reach = player.getAttribute((Attribute) ForgeMod.REACH_DISTANCE.get()).getValue();// 154
			hitResult = player.pick(reach, 1, true);
//			if (hitResult.getType() == HitResult.Type.MISS)
//				hitResult = player.pick(reach, 1, false);
		}
	}
	
	@Inject(at = @At("RETURN"), method = "startAttack")
	public void postAttack(CallbackInfoReturnable<Boolean> cir) {
		movePlayerBack();
	}
	
	@Inject(at = @At("RETURN"), method = "startUseItem")
	public void postUseItem(CallbackInfo ci) {
		//  TODO
//		movePlayerBack();
	}
	
	@Inject(at = @At("RETURN"), method = "continueAttack")
	public void postContinueAttack(boolean direction, CallbackInfo ci) {
		movePlayerBack();
	}
	
	@Unique
	private void movePlayerBack() {
		while (!datas.isEmpty()) {
			NetworkingHacks.unitPos.remove();
			RaytraceData data = datas.remove(datas.size() - 1);
			data.info.reset(player);
			hitResult = data.result;
		}
	}
}
