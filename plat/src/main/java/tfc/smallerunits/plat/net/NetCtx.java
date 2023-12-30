package tfc.smallerunits.plat.net;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.world.entity.player.Player;
import tfc.smallerunits.plat.util.PlatformUtils;

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
		responseSender.send(packet);
	}
	
	public PacketListener getHandler() {
		return handler;
	}
	
	public void setPacketHandled(boolean b) {
	}
	
	public NetworkDirection getDirection() {
		return direction;
	}
	
	static final ArrayList<Runnable> enqueued = new ArrayList<>();
	
	public void enqueueWork(Runnable r) {
		synchronized (enqueued) {
			enqueued.add(r);
		}
	}
	
	public static void tick() {
		if (!PlatformUtils.isClient()/* || IHateTheDistCleaner.getConnection() != null || IHateTheDistCleaner.getClientLevel() != null*/) {
			if (!enqueued.isEmpty()) {
				synchronized (enqueued) {
					for (Runnable runnable : enqueued)
						runnable.run();
				}
			}
		}
		enqueued.clear();
	}
}