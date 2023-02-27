package tfc.smallerunits.networking.platform;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import tfc.smallerunits.data.access.PacketListenerAccessor;
import tfc.smallerunits.networking.Packet;
import tfc.smallerunits.networking.PacketTarget;
import tfc.smallerunits.utils.platform.PlatformUtils;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class PacketRegister {
	public final ResourceLocation channel;
	Int2ObjectOpenHashMap<PacketEntry<?>> entries = new Int2ObjectOpenHashMap<>();
	Object2IntOpenHashMap<Class<? extends Packet>> class2IdMap = new Object2IntOpenHashMap<>();
	
	public PacketRegister(
			ResourceLocation name
	) {
		this.channel = name;
		if (PlatformUtils.isClient()) {
			ClientPlayNetworking.registerGlobalReceiver(
					name,
					((client, handler, buf, responseSender) -> {
						handlePacket(handler, buf, responseSender, ((PacketListenerAccessor) handler).getPlayer(), NetworkDirection.TO_CLIENT);
					})
			);
		}
		ServerPlayNetworking.registerGlobalReceiver(
				name,
				((server, player, handler, buf, responseSender) -> {
					handlePacket(handler, buf, responseSender, player, NetworkDirection.TO_SERVER);
				})
		);
	}
	
	private void handlePacket(PacketListener handler, FriendlyByteBuf buf, PacketSender responseSender, Player player, NetworkDirection direction) {
		int id = buf.readInt();
		PacketEntry<?> entry = entries.get(id);
		Packet packet = entry.fabricator.apply(buf);
		packet.handle(new NetCtx(handler, responseSender, player, direction));
	}
	
	public <T extends Packet> void registerMessage(int indx, Class<T> clazz, BiConsumer<Packet, FriendlyByteBuf> writer, Function<FriendlyByteBuf, T> fabricator, BiConsumer<Packet, NetCtx> handler) {
		entries.put(
				indx,
				new PacketEntry<>(clazz, writer, fabricator, handler)
		);
		class2IdMap.put(clazz, indx);
	}
	
	public void send(PacketTarget target, Packet pkt) {
		target.send(pkt, this);
	}
	
	public int getId(Packet packet) {
		return class2IdMap.get(packet.getClass());
	}
	
	public FriendlyByteBuf encode(Packet pkt) {
		FriendlyByteBuf buf = PacketByteBufs.create();
		int id = getId(pkt);
		buf.writeInt(id);
		entries.get(id).writer.accept(pkt, buf);
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
