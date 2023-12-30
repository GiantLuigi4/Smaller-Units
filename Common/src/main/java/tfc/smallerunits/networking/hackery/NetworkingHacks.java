package tfc.smallerunits.networking.hackery;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import tfc.smallerunits.data.storage.RegionPos;

import java.util.HashMap;

public class NetworkingHacks {
	public static ThreadLocal<LevelDescriptor> unitPos = ThreadLocal.withInitial(() -> null);
	public static ThreadLocal<Boolean> increaseBlockPosPrecision = ThreadLocal.withInitial(() -> false);
	public static ThreadLocal<NetworkHandlingContext> currentContext = ThreadLocal.withInitial(() -> null);
	private static HashMap<Packet, LevelDescriptor> positions = new HashMap<>();
	
	public static LevelDescriptor getPosFor(Packet packet) {
		LevelDescriptor pos = positions.get(packet);
		positions.remove(packet);
		return pos;
	}
	
	public static void setPosFor(Packet pkt, LevelDescriptor pos) {
		positions.put(pkt, pos);
	}
	
	public static void setPos(LevelDescriptor descriptor) {
		unitPos.set(descriptor);
	}
	
	public record LevelDescriptor(RegionPos pos, int upb, LevelDescriptor parent) {
		public static LevelDescriptor read(CompoundTag tg) {
			LevelDescriptor parent = null;
			if (tg.contains("parent", Tag.TAG_COMPOUND)) parent = read(tg.getCompound("parent"));
			return new LevelDescriptor(
					new RegionPos(
							tg.getInt("x"),
							tg.getInt("y"),
							tg.getInt("z")
					), tg.getInt("upb"),
					parent
			);
		}
		
		public void write(CompoundTag tg) {
			tg.putInt("x", pos.x);
			tg.putInt("y", pos.y);
			tg.putInt("z", pos.z);
			tg.putInt("upb", upb);
			if (parent != null) {
				CompoundTag parentTag = new CompoundTag();
				parent.write(parentTag);
				tg.put("parent", parentTag);
			}
		}
		
		public int getReachScale() {
			int mul = 1;
			if (parent != null) mul = parent.getReachScale();
			return upb * mul;
		}
	}
}
