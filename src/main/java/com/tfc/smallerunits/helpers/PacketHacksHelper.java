package com.tfc.smallerunits.helpers;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.util.math.BlockPos;

public class PacketHacksHelper {
	public static BlockPos unitPos = null;
	
	private static final Object2ObjectLinkedOpenHashMap<Object, BlockPos> map = new Object2ObjectLinkedOpenHashMap<>();
	
	public static <MSG> void setPosForPacket(MSG packet, BlockPos pos) {
		map.put(packet, pos);
	}
	
	public static <MSG> BlockPos getPosForPacket(MSG packet) {
		return map.get(packet);
	}
	
	public static <MSG> void markPacketDone(MSG msg) {
		map.remove(msg);
	}
}
