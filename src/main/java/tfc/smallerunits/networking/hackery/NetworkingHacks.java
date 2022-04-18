package tfc.smallerunits.networking.hackery;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;

import java.util.HashMap;

public class NetworkingHacks {
	public static ThreadLocal<BlockPos> unitPos = ThreadLocal.withInitial(() -> null);
	private static HashMap<Packet, BlockPos> positions = new HashMap<>();
	
	public static BlockPos getPosFor(Packet packet) {
		BlockPos pos = positions.get(packet);
		positions.remove(packet);
		return pos;
	}
	
	public static void setPosFor(Packet pkt, BlockPos pos) {
		positions.put(pkt, pos);
	}
}
