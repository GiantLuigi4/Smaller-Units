package tfc.smallerunits.plat.net;

import java.util.function.Consumer;

public class PacketSender {
	PacketRegister registry;
	Consumer<Packet> sender;
	
	public PacketSender(PacketRegister registry, Consumer<Packet> sender) {
		this.registry = registry;
		this.sender = sender;
	}
	
	public void send(Packet packet) {
		sender.accept(packet);
	}
}
