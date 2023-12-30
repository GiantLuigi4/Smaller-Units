package tfc.smallerunits.utils.vr.player;

import com.mojang.math.Quaternion;
import net.minecraft.world.phys.Vec3;

public class VRController {
	Vec3 position;
	Quaternion quaternion;
	
	public VRController(Vec3 position, Quaternion quaternion) {
		this.position = position;
		this.quaternion = quaternion;
	}
	
	public Vec3 getPosition() {
		return position;
	}
	
	public Quaternion getQuaternion() {
		return quaternion.copy();
	}
}
