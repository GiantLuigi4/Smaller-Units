package tfc.smallerunits.utils.vr.player;

import net.minecraft.world.InteractionHand;

public class SUVRPlayer {
	public final float worldScale;
	VRController[] controllers = new VRController[3];
	
	public SUVRPlayer(float worldScale, VRController head, VRController mainHand, VRController offHand) {
		this.worldScale = worldScale;
		controllers[0] = head;
		controllers[1] = mainHand;
		controllers[2] = offHand;
	}
	
	public VRController getHand(InteractionHand hand) {
		if (hand == InteractionHand.MAIN_HAND) return controllers[1];
		return controllers[2];
	}
}
