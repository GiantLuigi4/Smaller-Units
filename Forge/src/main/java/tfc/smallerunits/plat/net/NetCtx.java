package tfc.smallerunits.plat.net;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketListener;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import tfc.smallerunits.plat.util.PlatformUtils;
import tfc.smallerunits.utils.IHateTheDistCleaner;

import java.util.ArrayList;

public class NetCtx {
	Player sender;
	PacketListener handler;
	PacketSender responseSender;
	NetworkDirection direction;
	
	NetworkEvent.Context context;
	
	public NetCtx(PacketListener handler, PacketSender responseSender, Player player, NetworkDirection direction, NetworkEvent.Context context) {
		this.handler = handler;
		this.responseSender = responseSender;
		this.sender = player;
		this.direction = direction;
		this.context = context;
	}
	
	public Player getSender() {
		return sender;
	}
	
	public void respond(Packet packet) {
		responseSender.send(packet);
	}
	
	public PacketListener getHandler() {
		return handler;
	}
	
	public void setPacketHandled(boolean b) {
		context.setPacketHandled(b);
	}
	
	public NetworkDirection getDirection() {
		return direction;
	}
	
	public void enqueueWork(Runnable r) {
		context.enqueueWork(r);
	}
}
