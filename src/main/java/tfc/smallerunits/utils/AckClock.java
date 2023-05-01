package tfc.smallerunits.utils;

import tfc.smallerunits.networking.hackery.NetworkHandlingContext;
import tfc.smallerunits.networking.hackery.NetworkingHacks;

public class AckClock {
	public int upTo;
	public final NetworkingHacks.LevelDescriptor descriptor;
	public final NetworkHandlingContext netCtx;
	
	public AckClock(NetworkingHacks.LevelDescriptor descriptor, NetworkHandlingContext netCtx) {
		this.descriptor = descriptor;
		this.netCtx = netCtx;
	}
}
