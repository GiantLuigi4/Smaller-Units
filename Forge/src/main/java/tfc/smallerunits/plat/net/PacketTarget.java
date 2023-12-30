package tfc.smallerunits.plat.net;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.function.BiConsumer;

public class PacketTarget {
	public static final PacketTarget SERVER = new PacketTarget((pkt, register) -> {
 		register.NETWORK_INSTANCE.sendToServer(pkt);
	});
	
	BiConsumer<Packet, PacketRegister> sender;
	
	public PacketTarget(BiConsumer<Packet, PacketRegister> sender) {
		this.sender = sender;
	}
	
	public static PacketTarget trackingChunk(LevelChunk chunk) {
		return new PacketTarget((pkt, register) -> {
			register.NETWORK_INSTANCE.send(net.minecraftforge.network.PacketDistributor.TRACKING_CHUNK.with(() -> chunk), pkt);
		});
	}
	
	public static PacketTarget player(ServerPlayer player) {
		return new PacketTarget((pkt, register) -> {
			register.NETWORK_INSTANCE.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player), pkt);
		});
	}
	
	public void send(Packet pkt, PacketRegister register) {
		sender.accept(pkt, register);
	}
}
