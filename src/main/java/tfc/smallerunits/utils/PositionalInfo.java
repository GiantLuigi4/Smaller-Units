package tfc.smallerunits.utils;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class PositionalInfo {
	public final Vec3 pos;
	public final Level lvl;
	public final AABB box;
	public final float eyeHeight;
	
	public PositionalInfo(Player pPlayer) {
		pos = new Vec3(pPlayer.getX(), pPlayer.getY(), pPlayer.getZ());
		lvl = pPlayer.level;
		box = pPlayer.getBoundingBox();
		eyeHeight = pPlayer.eyeHeight;
	}
	
	public void reset(Player pPlayer) {
		pPlayer.level = lvl;
		if (pPlayer.level.isClientSide)
			if (pPlayer instanceof LocalPlayer)
				((LocalPlayer) pPlayer).clientLevel = (ClientLevel) lvl;
		pPlayer.setBoundingBox(box);
		pPlayer.setPosRaw(pos.x, pos.y, pos.z);
		pPlayer.eyeHeight = eyeHeight;
	}
}
