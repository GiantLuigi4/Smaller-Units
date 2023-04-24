package tfc.smallerunits.networking.platform;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.world.entity.player.Player;
import tfc.smallerunits.networking.Packet;
import tfc.smallerunits.networking.SUNetworkRegistry;

import java.util.ArrayList;

public class NetCtx {
	Player sender;
	PacketListener handler;
	PacketSender responseSender;
	NetworkDirection direction;
	
	public NetCtx(PacketListener handler, PacketSender responseSender, Player player, NetworkDirection direction) {
		this.handler = handler;
		this.responseSender = responseSender;
		this.sender = player;
		this.direction = direction;
	}
	
	public Player getSender() {
		return sender;
	}
	
	public void respond(Packet packet) {
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeInt(SUNetworkRegistry.NETWORK_INSTANCE.getId(packet));
		packet.write(buf);
		responseSender.sendPacket(SUNetworkRegistry.NAME, buf);
	}
	
	public PacketListener getHandler() {
		return handler;
	}
	
	public void setPacketHandled(boolean b) {
	}
	
	public NetworkDirection getDirection() {
		return direction;
	}
	
	static ArrayList<Runnable> enqueued = new ArrayList<>();
	
	public void enqueueWork(Runnable r) {
		enqueued.add(r);
	}
	
	public static void tick() {
		if (Minecraft.getInstance().level != null) {
			for (Runnable runnable : enqueued)
				runnable.run();
		}
		enqueued.clear();
	}
}
