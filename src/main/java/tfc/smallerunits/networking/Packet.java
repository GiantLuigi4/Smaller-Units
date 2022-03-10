package tfc.smallerunits.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class Packet {
	public Packet() {
	}
	
	public Packet(FriendlyByteBuf buf) {
	}
	
	public void write(FriendlyByteBuf buf) {
	}
	
	public void handle(NetworkEvent.Context ctx) {
	}
	
	public boolean checkClient(NetworkEvent.Context ctx) {
		return ctx.getDirection().getReceptionSide().isClient();
	}
	
	public boolean checkServer(NetworkEvent.Context ctx) {
		return ctx.getDirection().getReceptionSide().isServer();
	}
	
	public void respond(NetworkEvent.Context ctx, Packet packet) {
		ctx.enqueueWork(() -> {
			if (checkClient(ctx)) SUNetworkRegistry.NETWORK_INSTANCE.sendToServer(packet);
			else SUNetworkRegistry.NETWORK_INSTANCE.send(PacketDistributor.PLAYER.with(ctx::getSender), packet);
		});
	}
}
