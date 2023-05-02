package tfc.smallerunits.networking.platform;

import tfc.smallerunits.networking.Packet;

import java.util.function.Consumer;

public class PacketSender {
	PacketRegister registry;
	Consumer<Packet> sender;
	
	public PacketSender(PacketRegister registry, Consumer<Packet> sender) {
		this.registry = registry;
		this.sender = sender;
	}
	
	public void send(Packet packet) {
		//#if FABRIC==1
		//$$FriendlyByteBuf buf = PlatformUtils.newByteBuf();
		//$$buf.writeInt(registry.getId(packet));
		//$$packet.write(buf);
		//$$responseSender.sendPacket(registry.NAME, buf);
		//#else
		sender.accept(packet);
		//#endif
	}
}
