package tfc.smallerunits.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;

import java.util.Random;
import java.util.UUID;

public class PositionalInfo {
	public final Vec3 pos;
	public final Level lvl;
	public final AABB box;
	public final float eyeHeight;
	private static final UUID SU_REACH_UUID = new UUID(new Random(847329).nextLong(), new Random(426324).nextLong());
	private boolean isReachSet = false;
	
	public PositionalInfo(Player pPlayer) {
		pos = new Vec3(pPlayer.getX(), pPlayer.getY(), pPlayer.getZ());
		lvl = pPlayer.level;
		box = pPlayer.getBoundingBox();
		eyeHeight = pPlayer.eyeHeight;
		
	}
	
	public void scalePlayerReach(Player pPlayer, int upb) {
		AttributeInstance instance = pPlayer.getAttribute(ForgeMod.REACH_DISTANCE.get());
		instance.addPermanentModifier(
				new AttributeModifier(SU_REACH_UUID, "su:reach", upb, AttributeModifier.Operation.MULTIPLY_TOTAL)
		);
		isReachSet = true;
	}
	
	public void reset(Player pPlayer) {
		if (isReachSet) {
			AttributeInstance instance = pPlayer.getAttribute(ForgeMod.REACH_DISTANCE.get());
			instance.removeModifier(SU_REACH_UUID);
			isReachSet = false;
		}
		pPlayer.level = lvl;
		if (pPlayer.level.isClientSide) {
			if (pPlayer instanceof LocalPlayer) {
				((LocalPlayer) pPlayer).clientLevel = (ClientLevel) lvl;
				Minecraft.getInstance().level = ((LocalPlayer) pPlayer).clientLevel;
			}
		}
		pPlayer.setBoundingBox(box);
		pPlayer.setPosRaw(pos.x, pos.y, pos.z);
		pPlayer.eyeHeight = eyeHeight;
	}
}
