package tfc.smallerunits.networking.util;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

// TODO:
public class SimpleChannelWrapper {
	private final SimpleChannel channel;
	private int currIndex;
	
	public SimpleChannelWrapper(SimpleChannel channel) {
		this.channel = channel;
	}
	
	public <T extends Packet> void registerMsg(Class<T> clazz, Function<PacketBuffer, T> packetCreator, BiConsumer<T, Supplier<NetworkEvent.Context>> packetConsumer) {
		channel.registerMessage(currIndex++, clazz, (BiConsumer<T, PacketBuffer>) Packet::writePacketData, packetCreator, packetConsumer);
	}
	
	public <T extends Packet> void registerMsg(Class<T> clazz, Function<PacketBuffer, T> packetCreator) {
		registerMsg(clazz, packetCreator, (msg, ctx) -> {
			ctx.get().setPacketHandled(true);
			msg.handle(ctx);
		});
	}
}
