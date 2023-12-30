package tfc.smallerunits.networking.hackery;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Player;

public class NetworkContext {
	public final Connection connection;
	public final Player player;
	public final Packet pkt;
	
	public NetworkContext(Connection connection, Player player, Packet pkt) {
		this.connection = connection;
		this.player = player;
		this.pkt = pkt;
	}
}
