package tfc.smallerunits.networking;

import net.minecraft.network.FriendlyByteBuf;
import tfc.smallerunits.plat.net.Packet;
import tfc.smallerunits.plat.net.PacketRegister;

import java.util.function.Function;

public class NetworkEntry<T extends Packet> {
	Class<T> clazz;
	Function<FriendlyByteBuf, T> fabricator;
	
	public NetworkEntry(Class<T> clazz, Function<FriendlyByteBuf, T> fabricator) {
		this.clazz = clazz;
		this.fabricator = fabricator;
	}
	
	public void register(int indx, PacketRegister channel) {
		channel.registerMessage(
				indx, clazz,
				Packet::write,
				fabricator,
				Packet::handle
		);
	}
}
