package tfc.smallerunits.networking.hackery;

import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Player;
import tfc.smallerunits.data.access.PacketListenerAccessor;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.utils.PositionalInfo;

import java.util.HashMap;

public class PacketUtilMess {
	private static final HashMap<Packet, PositionalInfo> pkts = new HashMap<>();
	
	private static final Object syncLock = new Object();
	private static boolean synchronizationLock = false;
	
	
	public static <T extends PacketListener> void preHandlePacket(PacketListener listener, Packet packet) {
		NetworkingHacks.LevelDescriptor pos = NetworkingHacks.getPosFor(packet);
		if (pos != null) {
			// TODO: is this needed?
			synchronized (syncLock) {
//				while (synchronizationLock) {
//					try {
//						Thread.sleep(1);
//					} catch (Throwable ignored) {
//					}
//				}
//				synchronizationLock = true;

//				Player player = null;
				Player player = ((PacketListenerAccessor) listener).getPlayer();
//				if (listener instanceof ServerGamePacketListenerImpl)
//					player = ((ServerGamePacketListenerImpl) listener).getPlayer();
//				else if (FMLEnvironment.dist.isClient()) {
//					if (IHateTheDistCleaner.isClientPacketListener(listener))
//						player = IHateTheDistCleaner.getPlayer();
//				}
				if (player == null) return;
				
				NetworkingHacks.setPos(pos);
				PositionalInfo info = new PositionalInfo(player);
				info.scalePlayerReach(player, pos.upb());
				info.adjust(player, player.level, pos, !player.getLevel().isClientSide);
				
				try {
					ITickerLevel tkLvl = (ITickerLevel) player.level;
					tkLvl.addInteractingEntity(player);
				} catch (Throwable err) {
					info.reset(player);
					throw new RuntimeException(err);
				}
				
				pkts.put(packet, info);
			}
		}
	}
	
	public static <T extends PacketListener> void postHandlePacket(PacketListener listener, Packet packet) {
		if (pkts.containsKey(packet)) {
			PositionalInfo lvl = pkts.remove(packet);
			Player player = ((PacketListenerAccessor) listener).getPlayer();
//			if (listener instanceof ServerGamePacketListenerImpl)
//				player = ((ServerGamePacketListenerImpl) listener).getPlayer();
//			else if (FMLEnvironment.dist.isClient()) {
//				if (IHateTheDistCleaner.isClientPacketListener(listener))
//					player = IHateTheDistCleaner.getPlayer();
//			}
//			player.level = lvl;
			// TODO: set listener level
			lvl.reset(player);
			((PacketListenerAccessor) listener).setWorld(lvl.lvl);
			NetworkingHacks.unitPos.remove();
			synchronizationLock = false;
		}
	}
}
