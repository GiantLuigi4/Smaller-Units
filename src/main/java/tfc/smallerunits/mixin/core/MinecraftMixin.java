package tfc.smallerunits.mixin.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.UnitSpace;
import tfc.smallerunits.data.access.SUScreenAttachments;
import tfc.smallerunits.data.capability.ISUCapability;
import tfc.smallerunits.data.capability.SUCapabilityManager;
import tfc.smallerunits.data.tracking.ICanUseUnits;
import tfc.smallerunits.data.tracking.RaytraceData;
import tfc.smallerunits.networking.hackery.NetworkingHacks;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.utils.PositionalInfo;
import tfc.smallerunits.utils.platform.PlatformUtils;
import tfc.smallerunits.utils.selection.UnitHitResult;

import java.util.ArrayList;

@Mixin(Minecraft.class)
public class MinecraftMixin {
	@Shadow
	public HitResult hitResult;
	@Shadow
	public LocalPlayer player;
	@Shadow
	public ClientLevel level;
	@Shadow
	public Screen screen;
	@Unique
	ArrayList<RaytraceData> datas = new ArrayList<>();
	
	@Unique
	ThreadLocal<Screen> previousScreen = new ThreadLocal<>();
	
	@Inject(at = @At("HEAD"), method = "startAttack")
	public void preAttack(CallbackInfo ci) {
		movePlayerTo();
	}
	
	@Inject(at = @At("HEAD"), method = "startUseItem")
	public void preUseItem(CallbackInfo cir) {
		movePlayerTo();
	}
	
	@Inject(at = @At("RETURN"), method = "startUseItem")
	public void postUseItem(CallbackInfo ci) {
		movePlayerBack();
	}
	
	@Inject(at = @At("HEAD"), method = "continueAttack")
	public void preContinueAttack(boolean direction, CallbackInfo ci) {
		movePlayerTo();
	}
	
	@Inject(at = @At("HEAD"), method = "pickBlock")
	public void prePick(CallbackInfo ci) {
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
//			NetworkingHacks.setPos(new NetworkingHacks.LevelDescriptor(new RegionPos(((UnitHitResult) hitResult).geetBlockPos()), space.unitsPerBlock));
			NetworkingHacks.setPos(((ITickerLevel) space.getMyLevel()).getDescriptor());
			RaytraceData data = new RaytraceData(hitResult, info);
			datas.add(data);
			
			if (player instanceof ICanUseUnits unitUser) {
				unitUser.setResult(hitResult);
			}
			
			double reach = PlatformUtils.getReach(player);// 154
			hitResult = player.pick(reach, 1, false);
			if (hitResult.getType() == HitResult.Type.MISS)
				hitResult = player.pick(reach, 1, true);
		}
		previousScreen.set(this.screen);
	}
	
	@Inject(at = @At("RETURN"), method = "startAttack")
	public void postAttack(CallbackInfo ci) {
		movePlayerBack();
	}
	
	@Inject(at = @At("RETURN"), method = "continueAttack")
	public void postContinueAttack(boolean direction, CallbackInfo ci) {
		movePlayerBack();
	}
	
	@Inject(at = @At("TAIL"), method = "pickBlock")
	public void postPick(CallbackInfo ci) {
		movePlayerBack();
	}
	
	@Unique
	private void movePlayerBack() {
		if (!datas.isEmpty()) {
			if (screen != previousScreen.get()) {
				RaytraceData data = datas.get(datas.size() - 1);
				
				PositionalInfo info = data.info;
				if (screen != null) {
					if (data.result instanceof UnitHitResult uhr) {
						if (level instanceof ITickerLevel tk) {
							ISUCapability capability = SUCapabilityManager.getCapability(tk.getParent().getChunkAt(uhr.getBlockPos()));
							((SUScreenAttachments) screen).setup(info, capability.getUnit(uhr.getBlockPos()));
						}
					}
				}
			}
			
			while (!datas.isEmpty()) {
				NetworkingHacks.unitPos.remove();
				RaytraceData data = datas.remove(datas.size() - 1);
				data.info.reset(player);
				hitResult = data.result;
			}
		}
	}
}
