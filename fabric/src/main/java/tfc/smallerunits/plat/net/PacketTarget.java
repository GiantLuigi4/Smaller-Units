package tfc.smallerunits.plat.net;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.function.BiConsumer;

public class PacketTarget {
	public static final PacketTarget SERVER = new PacketTarget((pkt, register) -> {
		net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(register.channel, register.encode(pkt));
	});
	
	BiConsumer<Packet, PacketRegister> sender;
	
	public PacketTarget(BiConsumer<Packet, PacketRegister> sender) {
		this.sender = sender;
	}
	
	public static PacketTarget trackingChunk(LevelChunk chunk) {
		return new PacketTarget((pkt, register) -> {
			((net.minecraft.server.level.ServerChunkCache) chunk.getLevel().getChunkSource()).chunkMap.getPlayers(chunk.getPos(), false).forEach(
					e -> net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(e, register.channel, register.encode(pkt))
			);
		});
	}
	
	public static PacketTarget player(ServerPlayer player) {
		return new PacketTarget((pkt, register) -> {
			net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, register.channel, register.encode(pkt));
		});
	}
	
	public void send(Packet pkt, PacketRegister register) {
		sender.accept(pkt, register);
	}
}