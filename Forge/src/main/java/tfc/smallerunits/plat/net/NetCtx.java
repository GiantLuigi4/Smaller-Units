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
	
	static final ArrayList<Runnable> enqueued = new ArrayList<>();
	
	public void enqueueWork(Runnable r) {
		context.enqueueWork(r);
	}
	
	public static void tick() {
		if (!PlatformUtils.isClient() || IHateTheDistCleaner.getConnection() != null || IHateTheDistCleaner.getClientLevel() != null) {
			if (IHateTheDistCleaner.getClientLevel() != null) {
				if (!enqueued.isEmpty()) {
					synchronized (enqueued) {
						for (Runnable runnable : enqueued)
							runnable.run();
					}
				}
				enqueued.clear();
			}
		} else if (IHateTheDistCleaner.getPlayer() == null)
			enqueued.clear();
	}
}
