package tfc.smallerunits.utils.vr.player.mods;

import net.minecraft.world.entity.player.Player;
import org.vivecraft.api.CommonNetworkHelper;
import org.vivecraft.api.ServerVivePlayer;
import org.vivecraft.api.VRData;
import org.vivecraft.gameplay.VRPlayer;
import org.vivecraft.render.PlayerModelController;
import org.vivecraft.utils.math.Quaternion;
import tfc.smallerunits.utils.IHateTheDistCleaner;
import tfc.smallerunits.utils.vr.player.SUVRPlayer;
import tfc.smallerunits.utils.vr.player.VRController;

public class Vivecraft {
	public static SUVRPlayer getPlayerClient() {
		VRPlayer player = VRPlayer.get();
		if (player == null) return null;
		IHateTheDistCleaner.updateCamera();
		VRData data = player.vrdata_world_render;
		VRData.VRDevicePose headP = data.getController(2); // I think this is the head???
		VRController head = new VRController(headP.getPosition(), mojQuat(new Quaternion(headP.getMatrix())));
		VRData.VRDevicePose mArmP = data.getController(0); // main arm
		VRController mArm = new VRController(mArmP.getPosition(), mojQuat(new Quaternion(mArmP.getMatrix())));
		VRData.VRDevicePose oArmP = data.getController(1); // second arm
		VRController oArm = new VRController(oArmP.getPosition(), mojQuat(new Quaternion(oArmP.getMatrix())));
		return new SUVRPlayer(data.worldScale, head, mArm, oArm);
	}
	
	protected static com.mojang.math.Quaternion getQuatFrom(byte[] bytes) {
		// had to trial&error this
		// update: I have now looked at vivecraft's Quaternion class and this entirely makes sense now
		return new com.mojang.math.Quaternion(
				readFloat(bytes, 17),
				readFloat(bytes, 21),
				readFloat(bytes, 25),
				readFloat(bytes, 13)
		);
	}
	
	public static SUVRPlayer getPlayerJRBudda(Player player) {
		ServerVivePlayer vivePlayer = CommonNetworkHelper.vivePlayers.get(player.getUUID());
		if (vivePlayer == null) return null;
		if (!vivePlayer.isVR()) return null;
		if (vivePlayer.hmdData == null || vivePlayer.controller0data == null || vivePlayer.controller1data == null)
			return null;

		VRController head = new VRController(
				vivePlayer.getHMDPos(player),
				getQuatFrom(vivePlayer.hmdData)
		);
		VRController mArm = new VRController(
				vivePlayer.getControllerPos(0, player),
				getQuatFrom(vivePlayer.controller0data)
		);
		VRController oArm = new VRController(
				vivePlayer.getControllerPos(1, player),
				getQuatFrom(vivePlayer.controller1data)
		);
		return new SUVRPlayer(vivePlayer.worldScale, head, mArm, oArm);
	}
	
	// https://stackoverflow.com/a/7619315
	protected static float readFloat(byte[] bytes, int index) {
		return Float.intBitsToFloat(((bytes[index] & 0xFF) << 24) | ((bytes[index + 1] & 0xFF) << 16) | ((bytes[index + 2] & 0xFF) << 8) | ((bytes[index + 3] & 0xFF)));
	}
	
	private static com.mojang.math.Quaternion mojQuat(Quaternion quaternion) {
		return new com.mojang.math.Quaternion(quaternion.x, quaternion.y, quaternion.z, quaternion.w);
	}
	
	public static SUVRPlayer getOtherClient(Player player) {
		PlayerModelController.RotInfo rotInfo = PlayerModelController.getInstance().getRotationsForPlayer(player.getUUID());
		VRController head = new VRController(rotInfo.Headpos, mojQuat(rotInfo.headQuat));
		VRController mArm = new VRController(rotInfo.rightArmPos, mojQuat(rotInfo.rightArmQuat));
		VRController oArm = new VRController(rotInfo.leftArmPos, mojQuat(rotInfo.leftArmQuat));
		return new SUVRPlayer(rotInfo.worldScale, head, mArm, oArm);
	}
}
