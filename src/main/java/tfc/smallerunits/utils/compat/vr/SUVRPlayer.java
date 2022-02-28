package tfc.smallerunits.utils.compat.vr;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import tfc.smallerunits.Smallerunits;
import tfc.smallerunits.utils.compat.RaytraceUtils;
import tfc.smallerunits.utils.compat.vr.vivecraft.JrBuddaPlayer;
import tfc.smallerunits.utils.compat.vr.vivecraft.VFEPlayer;

public abstract class SUVRPlayer {
	public static SUVRPlayer getPlayer$(Entity player) {
		if (player instanceof ServerPlayerEntity) {
			SUVRPlayer vrPlayer = getPlayer((ServerPlayerEntity) player);
			if (vrPlayer == null)
				return new UnkownVRPlayer(RaytraceUtils.getStartVector(player), RaytraceUtils.getLookVector(player));
			return vrPlayer;
		}
		return new UnkownVRPlayer(RaytraceUtils.getStartVector(player), RaytraceUtils.getLookVector(player));
	}
	
	public static SUVRPlayer getPlayer(ServerPlayerEntity playerEntity) {
		if (Smallerunits.isVivecraftPresent())
			if (Smallerunits.isVFEPresent()) {
				if (VFEPlayer.isVR(playerEntity))
					return new VFEPlayer(playerEntity);
			} else return new JrBuddaPlayer(playerEntity);
		return null;
	}
	
	public abstract Vector3d getControllerPos(int c);
	
	public abstract Vector3d getControllerAngle(int c);
	
	public String toString() {
		return "VRPlayer{" +
				"mainHandPos=" + getControllerPos(0) +
				", mainHandAngle=" + getControllerAngle(0) +
				'}';
	}
}
