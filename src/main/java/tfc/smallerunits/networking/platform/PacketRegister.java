package tfc.smallerunits.networking.platform;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import tfc.smallerunits.networking.Packet;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import tfc.smallerunits.utils.platform.PlatformUtils;

//#if FORGE==1
//$$import net.minecraftforge.network.NetworkRegistry;
//$$import net.minecraftforge.network.PacketDistributor;
//$$import net.minecraftforge.network.simple.SimpleChannel;
//#else
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import tfc.smallerunits.data.access.PacketListenerAccessor;
//#endif

public class PacketRegister {
	public final ResourceLocation channel;
	Int2ObjectOpenHashMap<PacketEntry<?>> entries = new Int2ObjectOpenHashMap<>();
	Object2IntOpenHashMap<Class<? extends Packet>> class2IdMap = new Object2IntOpenHashMap<>();
	
	//#if FORGE==1
	//$$final SimpleChannel NETWORK_INSTANCE;
	//#endif
	
	public PacketRegister(
			ResourceLocation name,
			String networkVersion,
			Predicate<String> clientChecker,
			Predicate<String> serverChecker
	) {
		this.channel = name;
		
		//#if FORGE==1
		//$$NETWORK_INSTANCE = NetworkRegistry.newSimpleChannel(
		//$$		new ResourceLocation("smaller_units", "main"),
		//$$		() -> networkVersion,
		//$$		clientChecker, serverChecker
		//$$);
		//#else
		if (PlatformUtils.isClient()) {
			ClientPlayNetworking.registerGlobalReceiver(
					name,
					((client, handler, buf, responseSender) -> {
						handlePacket(handler, buf, new PacketSender(this, (pkt) -> responseSender.sendPacket(name, encode(pkt))), ((PacketListenerAccessor) handler).getPlayer(), NetworkDirection.TO_CLIENT);
					})
			);
		}
		ServerPlayNetworking.registerGlobalReceiver(
				name,
				((server, player, handler, buf, responseSender) -> {
					handlePacket(handler, buf, new PacketSender(this, (pkt) -> responseSender.sendPacket(name, encode(pkt))), player, NetworkDirection.TO_SERVER);
				})
		);
		//#endif
	}
	
	public net.minecraft.network.protocol.Packet<?> toVanillaPacket(Packet wrapperPacket, NetworkDirection toClient) {
		//#if FORGE==1
		//$$return switch (toClient) {
		//$$	case TO_CLIENT -> NETWORK_INSTANCE.toVanillaPacket(wrapperPacket, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT);
		//$$	case TO_SERVER -> NETWORK_INSTANCE.toVanillaPacket(wrapperPacket, net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER);
		//$$};
		//#else
		FriendlyByteBuf buf = encode(wrapperPacket);
		return switch (toClient) {
			case TO_CLIENT -> ServerPlayNetworking.createS2CPacket(channel, buf);
			case TO_SERVER -> ClientPlayNetworking.createC2SPacket(channel, buf);
		};
		//#endif
	}
	
	private void handlePacket(PacketListener handler, FriendlyByteBuf buf, PacketSender responseSender, Player player, NetworkDirection direction) {
		int id = buf.readByte();
		PacketEntry<?> entry = entries.get(id);
		Packet packet = entry.fabricator.apply(buf);
		packet.handle(new NetCtx(handler, responseSender, player, direction));
	}
	
	public <T extends Packet> void registerMessage(int indx, Class<T> clazz, BiConsumer<Packet, FriendlyByteBuf> writer, Function<FriendlyByteBuf, T> fabricator, BiConsumer<Packet, NetCtx> handler) {
		entries.put(
				indx,
				new PacketEntry<>(clazz, writer, fabricator, handler)
		);
		//#if FORGE==1
		//$$NETWORK_INSTANCE.registerMessage(
		//$$		indx,
		//$$		clazz,
		//$$		writer::accept,
		//$$		fabricator,
		//$$		(pkt, ctx) -> {
		//$$			NetCtx ctx1 = new NetCtx(
		//$$					ctx.get().getNetworkManager().getPacketListener(),
		//$$					new PacketSender(this, (pkt1) -> {
		//$$						if (ctx.get().getDirection().getReceptionSide().isServer()) {
		//$$							NETWORK_INSTANCE.send(
		//$$									PacketDistributor.PLAYER.with(() -> ctx.get().getSender()),
		//$$									pkt1
		//$$							);
		//$$						} else {
		//$$							NETWORK_INSTANCE.sendToServer(pkt1);
		//$$						}
		//$$					}),
		//$$					ctx.get().getSender(),
		//$$					switch (ctx.get().getDirection()) {
		//$$						case PLAY_TO_CLIENT, LOGIN_TO_CLIENT -> NetworkDirection.TO_CLIENT;
		//$$						case PLAY_TO_SERVER, LOGIN_TO_SERVER -> NetworkDirection.TO_SERVER;
		//$$					}
		//$$			);
		//$$			handler.accept(pkt, ctx1);
		//$$		}
		//$$);
		//#endif
		class2IdMap.put(clazz, indx);
	}
	
	public void send(PacketTarget target, Packet pkt) {
		target.send(pkt, this);
	}
	
	public int getId(Packet packet) {
		return class2IdMap.get(packet.getClass());
	}
	
	public FriendlyByteBuf encode(Packet pkt) {
		FriendlyByteBuf buf = PlatformUtils.newByteBuf();
		//#if FORGE==1
		//$$NETWORK_INSTANCE.encodeMessage(pkt, buf);
		//#else
		int id = getId(pkt);
		buf.writeByte(id & 255);
		entries.get(id).writer.accept(pkt, buf);
		//#endif
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
}
