package tfc.smallerunits.utils.compat.vr;

import net.minecraft.util.math.vector.Vector3d;

public class UnkownVRPlayer extends SUVRPlayer {
	Vector3d pos, angle;
	
	public UnkownVRPlayer(Vector3d pos, Vector3d angle) {
		this.pos = pos;
		this.angle = angle;
	}
	
	@Override
	public Vector3d getControllerPos(int c) {
		return pos;
	}
	
	@Override
	public Vector3d getControllerAngle(int c) {
		return angle;
	}
}
