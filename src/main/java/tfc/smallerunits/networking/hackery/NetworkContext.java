package tfc.smallerunits.networking.hackery;

import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Player;

public class NetworkContext {
	public final PacketListener listener;
	public final Player player;
	public final Packet pkt;
	
	public NetworkContext(PacketListener listener, Player player, Packet pkt) {
		this.listener = listener;
		this.player = player;
		this.pkt = pkt;
	}
}
