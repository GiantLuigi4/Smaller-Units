package tfc.smallerunits.plat.net;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketListener;
import net.minecraft.world.entity.player.Player;
import tfc.smallerunits.data.access.PacketListenerAccessor;
import tfc.smallerunits.logging.Loggers;
import tfc.smallerunits.plat.util.PlatformUtils;
import tfc.smallerunits.utils.IHateTheDistCleaner;

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
	
	public void enqueueWork(Runnable r) {
		if (PlatformUtils.isClient() && (sender == null || sender.getLevel().isClientSide)) {
			Minecraft.getInstance().tell(r);
		} else {
			if (sender != null) {
				sender.getLevel().getServer().execute(r);
			} else {
				r.run(); // whar
				Loggers.SU_LOGGER.warn("A null sender on server???");
			}
		}
	}
}
