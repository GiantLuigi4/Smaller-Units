package tfc.smallerunits.utils.compat.vr.vivecraft;

import com.techjar.vivecraftforge.util.PlayerTracker;
import com.techjar.vivecraftforge.util.VRPlayerData;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import tfc.smallerunits.utils.MathUtils;
import tfc.smallerunits.utils.compat.vr.SUVRPlayer;

public class VFEPlayer extends SUVRPlayer {
	VRPlayerData data;
	ServerPlayerEntity player;
	
	public VFEPlayer(ServerPlayerEntity entity) {
		data = PlayerTracker.getPlayerDataAbsolute(entity);
		player = entity;
	}
	
	public static boolean isVR(ServerPlayerEntity playerEntity) {
		return PlayerTracker.hasPlayerData(playerEntity);
	}
	
	@Override
	public Vector3d getControllerPos(int c) {
		return data.getController(c).getPos();
	}
	
	@Override
	public Vector3d getControllerAngle(int c) {
		// https://github.com/Techjar/VivecraftForgeExtensions_110/blob/dad53be84908b2961b286dd89bac523f480fb9b6/src/main/java/com/techjar/vivecraftforge/util/AimFixHandler.java#L52-L63
		Vector3d aim = data.getController(c).getRot().multiply(new Vector3d(0, 0, -1));
		float rotationPitch = (float) Math.toDegrees(Math.asin(-aim.y));
		float rotationYaw = (float) Math.toDegrees(Math.atan2(-aim.x, aim.z));
		return MathUtils.getVectorForRotation(rotationPitch, rotationYaw);
	}
}
