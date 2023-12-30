package tfc.smallerunits.plat.net;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import tfc.smallerunits.plat.util.PlatformUtils;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class PacketRegister {
	public final ResourceLocation channel;
	Int2ObjectOpenHashMap<PacketEntry<?>> entries = new Int2ObjectOpenHashMap<>();
	Object2IntOpenHashMap<Class<? extends Packet>> class2IdMap = new Object2IntOpenHashMap<>();
	
	final SimpleChannel NETWORK_INSTANCE;
	
	public PacketRegister(
			ResourceLocation name,
			String networkVersion,
			Predicate<String> clientChecker,
			Predicate<String> serverChecker
	) {
		this.channel = name;
		
		NETWORK_INSTANCE = NetworkRegistry.newSimpleChannel(
				new ResourceLocation("smaller_units", "main"),
				() -> networkVersion,
				clientChecker, serverChecker
		);
	}
	
	public net.minecraft.network.protocol.Packet<?> toVanillaPacket(Packet wrapperPacket, NetworkDirection toClient) {
 		return switch (toClient) {
 			case TO_CLIENT -> NETWORK_INSTANCE.toVanillaPacket(wrapperPacket, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT);
 			case TO_SERVER -> NETWORK_INSTANCE.toVanillaPacket(wrapperPacket, net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER);
 		};
	}
	
	private void handlePacket(PacketListener handler, FriendlyByteBuf buf, PacketSender responseSender, Player player, NetworkDirection direction, NetworkEvent.Context context) {
		int id = buf.readByte();
		PacketEntry<?> entry = entries.get(id);
		Packet packet = entry.fabricator.apply(buf);
		packet.handle(new NetCtx(handler, responseSender, player, direction, context));
	}
	
	public <T extends Packet> void registerMessage(int indx, Class<T> clazz, BiConsumer<Packet, FriendlyByteBuf> writer, Function<FriendlyByteBuf, T> fabricator, BiConsumer<Packet, NetCtx> handler) {
		entries.put(
				indx,
				new PacketEntry<>(clazz, writer, fabricator, handler)
		);
		class2IdMap.put(clazz, indx);


 		NETWORK_INSTANCE.registerMessage(
 				indx,
 				clazz,
 				writer::accept,
 				fabricator,
 				(pkt, ctx) -> {
 					NetCtx ctx1 = new NetCtx(
 							ctx.get().getNetworkManager().getPacketListener(),
 							new PacketSender(this, (pkt1) -> {
 								if (ctx.get().getDirection().getReceptionSide().isServer()) {
 									NETWORK_INSTANCE.send(
 											PacketDistributor.PLAYER.with(() -> ctx.get().getSender()),
 											pkt1
 									);
 								} else {
 									NETWORK_INSTANCE.sendToServer(pkt1);
 								}
 							}),
 							ctx.get().getSender(),
 							switch (ctx.get().getDirection()) {
 								case PLAY_TO_CLIENT, LOGIN_TO_CLIENT -> NetworkDirection.TO_CLIENT;
 								case PLAY_TO_SERVER, LOGIN_TO_SERVER -> NetworkDirection.TO_SERVER;
 							},
						    ctx.get()
 					);
 					handler.accept(pkt, ctx1);
 				}
 		);
	}
	
	public void send(PacketTarget target, Packet pkt) {
		target.send(pkt, this);
	}
	
	public int getId(Packet packet) {
		return class2IdMap.get(packet.getClass());
	}
	
	public FriendlyByteBuf encode(Packet pkt) {
		FriendlyByteBuf buf = newByteBuf();
 		NETWORK_INSTANCE.encodeMessage(pkt, buf);
		return buf;
	}
	
	
	private static class PacketEntry<T extends Packet> {
		Class<T> clazz;
		BiConsumer<Packet, FriendlyByteBuf> writer;
		Function<FriendlyByteBuf, T> fabricator;
		BiConsumer<Packet, NetCtx> handler;
		
		public PacketEntry(Class<T> clazz, BiConsumer<Packet, FriendlyByteBuf> writer, Function<FriendlyByteBuf, T> fabricator, BiConsumer<Packet, NetCtx> handler) {
			this.clazz = clazz;
			this.writer = writer;
			this.fabricator = fabricator;
			this.handler = handler;
		}
	}
	
	private static FriendlyByteBuf newByteBuf() {
		return new FriendlyByteBuf(Unpooled.buffer());
	}
}
