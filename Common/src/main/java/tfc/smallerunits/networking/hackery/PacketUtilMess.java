package tfc.smallerunits.networking.hackery;

import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Player;
import tfc.smallerunits.data.access.PacketListenerAccessor;
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
				Player player = ((PacketListenerAccessor) listener).getPlayer();
				if (player == null) return;
				
				NetworkingHacks.setPos(pos);
				PositionalInfo info = new PositionalInfo(player);
				info.scalePlayerReach(player, pos.upb());
				info.adjust(player, player.level, pos, !player.getLevel().isClientSide);
				
				pkts.put(packet, info);
			}
		}
	}
	
	public static <T extends PacketListener> void postHandlePacket(PacketListener listener, Packet packet) {
		if (pkts.containsKey(packet)) {
			PositionalInfo lvl = pkts.remove(packet);
			Player player = ((PacketListenerAccessor) listener).getPlayer();
			lvl.reset(player);
			((PacketListenerAccessor) listener).setWorld(lvl.lvl);
			NetworkingHacks.unitPos.remove();
			synchronizationLock = false;
		}
	}
}
