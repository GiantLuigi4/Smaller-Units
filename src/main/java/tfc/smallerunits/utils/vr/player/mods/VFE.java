package tfc.smallerunits.utils.vr.player.mods;

import com.mojang.math.Quaternion;
import com.techjar.vivecraftforge.util.PlayerTracker;
import com.techjar.vivecraftforge.util.VRPlayerData;
import net.minecraft.world.entity.player.Player;
import tfc.smallerunits.utils.vr.player.SUVRPlayer;
import tfc.smallerunits.utils.vr.player.VRController;

public class VFE {
	private static Quaternion mojQuat(com.techjar.vivecraftforge.util.Quaternion quaternion) {
		return new Quaternion(quaternion.x, quaternion.y, quaternion.z, quaternion.w);
	}
	
	// TODO: this is untested
	public static SUVRPlayer getPlayer(Player player) {
		VRPlayerData data = PlayerTracker.getPlayerDataAbsolute(player);
		VRController head = new VRController(data.head.getPos(), mojQuat(data.head.getRot()));
		VRController mArm = new VRController(data.controller0.getPos(), mojQuat(data.controller0.getRot()));
		VRController oArm = new VRController(data.controller1.getPos(), mojQuat(data.controller1.getRot()));
		return new SUVRPlayer(data.worldScale, head, mArm, oArm);
	}
}
