package tfc.smallerunits.networking.hackery;

import net.minecraft.world.level.Level;
import tfc.smallerunits.plat.net.NetworkDirection;
import tfc.smallerunits.utils.PositionalInfo;

public class NetworkHandlingContext {
	public final NetworkContext netContext;
	public final PositionalInfo info;
	public final NetworkDirection direction;
	public final Level targetLevel;
	
	public NetworkHandlingContext(NetworkContext netContext, PositionalInfo info, NetworkDirection direction, Level targetLevel) {
		this.netContext = netContext;
		this.info = info;
		this.direction = direction;
		this.targetLevel = targetLevel;
	}
}
