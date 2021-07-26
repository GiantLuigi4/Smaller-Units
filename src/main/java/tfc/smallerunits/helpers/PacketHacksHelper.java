package tfc.smallerunits.helpers;

import net.minecraft.util.math.BlockPos;

import java.util.HashMap;

public class PacketHacksHelper {
	public static BlockPos unitPos = null;
	
	private static final HashMap<Object, BlockPos> map = new HashMap<>();
	
	public static <MSG> void setPosForPacket(MSG packet, BlockPos pos) {
		map.put(packet, pos);
	}
	
	public static <MSG> BlockPos getPosForPacket(MSG packet) {
		return map.get(packet);
	}
	
	public static <MSG> void markPacketDone(MSG msg) {
		if (map.isEmpty()) return;
		if (map.containsKey(msg)) map.remove(msg);
	}
}
