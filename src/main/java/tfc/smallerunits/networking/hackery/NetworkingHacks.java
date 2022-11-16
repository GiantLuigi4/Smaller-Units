package tfc.smallerunits.networking.hackery;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import tfc.smallerunits.data.storage.RegionPos;

import java.util.HashMap;

public class NetworkingHacks {
	public static ThreadLocal<LevelDescriptor> unitPos = ThreadLocal.withInitial(() -> null);
	public static ThreadLocal<Boolean> increaseBlockPosPrecision = ThreadLocal.withInitial(() -> false);
	private static HashMap<Packet, LevelDescriptor> positions = new HashMap<>();
	
	public static LevelDescriptor getPosFor(Packet packet) {
		LevelDescriptor pos = positions.get(packet);
		positions.remove(packet);
		return pos;
	}
	
	public static void setPosFor(Packet pkt, LevelDescriptor pos) {
		positions.put(pkt, pos);
	}
	
	public record LevelDescriptor(RegionPos pos, int upb) {
		public static LevelDescriptor read(CompoundTag tg) {
			return new LevelDescriptor(
					new RegionPos(
							tg.getInt("x"),
							tg.getInt("y"),
							tg.getInt("z")
					), tg.getInt("upb")
			);
		}
		
		public void write(CompoundTag tg) {
			tg.putInt("x", pos.x);
			tg.putInt("y", pos.y);
			tg.putInt("z", pos.z);
			tg.putInt("upb", upb);
		}
	}
}
