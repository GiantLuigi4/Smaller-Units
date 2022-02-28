package tfc.smallerunits.utils.compat.vr.vivecraft;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import org.vivecraft.api.ServerVivePlayer;
import tfc.smallerunits.utils.compat.vr.SUVRPlayer;

public class JrBuddaPlayer extends SUVRPlayer {
	ServerVivePlayer player;
	ServerPlayerEntity playerE;
	
	public JrBuddaPlayer(ServerPlayerEntity entity) {
		player = new ServerVivePlayer(playerE = entity);
	}
	
	@Override
	public Vector3d getControllerPos(int c) {
		return player.getControllerPos(c, playerE);
	}
	
	@Override
	public Vector3d getControllerAngle(int c) {
		return player.getControllerDir(c);
	}
}
