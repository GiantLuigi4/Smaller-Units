package tfc.smallerunits.networking.util;

import net.minecraft.network.INetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import tfc.smallerunits.Smallerunits;

import java.util.function.Supplier;

public class Packet implements IPacket {
	public Packet(PacketBuffer buffer) {
		readPacketData(buffer);
	}
	
	public Packet() {
	}
	
	@Override
	public final void processPacket(INetHandler iNetHandler) {
	
	}
	
	public void readPacketData(PacketBuffer buf) {
	}
	
	public void writePacketData(PacketBuffer buf) {
	}
	
	public void handle(Supplier<NetworkEvent.Context> ctx) {
	}
	
	public boolean checkClient(NetworkEvent.Context ctx) {
		return ctx.getDirection().getReceptionSide().isClient();
	}
	
	public boolean checkServer(NetworkEvent.Context ctx) {
		return ctx.getDirection().getReceptionSide().isServer();
	}
	
	public void respond(NetworkEvent.Context ctx, Packet packet) {
		ctx.enqueueWork(() -> {
			if (checkClient(ctx)) Smallerunits.NETWORK_INSTANCE.sendToServer(packet);
			else Smallerunits.NETWORK_INSTANCE.send(PacketDistributor.PLAYER.with(() -> ctx.getSender()), packet);
		});
	}
}