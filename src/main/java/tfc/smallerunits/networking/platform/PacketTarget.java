package tfc.smallerunits.networking.platform;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import tfc.smallerunits.networking.Packet;

//#if FORGE==1
//$$import net.minecraftforge.network.PacketDistributor;
//#endif

import java.util.function.BiConsumer;

public class PacketTarget {
	public static final PacketTarget SERVER = new PacketTarget((pkt, register) -> {
		//#if FABRIC==1
		net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(register.channel, register.encode(pkt));
		//#else
		//$$register.NETWORK_INSTANCE.sendToServer(pkt);
		//#endif
	});
	
	BiConsumer<Packet, PacketRegister> sender;
	
	public PacketTarget(BiConsumer<Packet, PacketRegister> sender) {
		this.sender = sender;
	}
	
	public static PacketTarget trackingChunk(LevelChunk chunk) {
		return new PacketTarget((pkt, register) -> {
			//#if FABRIC==1
			((net.minecraft.server.level.ServerChunkCache) chunk.getLevel().getChunkSource()).chunkMap.getPlayers(chunk.getPos(), false).forEach(
					e -> net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(e, register.channel, register.encode(pkt))
			);
			//#else
			//$$register.NETWORK_INSTANCE.send(net.minecraftforge.network.PacketDistributor.TRACKING_CHUNK.with(() -> chunk), pkt);
			//#endif
		});
	}
	
	public static PacketTarget player(ServerPlayer player) {
		return new PacketTarget((pkt, register) -> {
			//#if FABRIC==1
			net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, register.channel, register.encode(pkt));
			//#else
			//$$register.NETWORK_INSTANCE.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player), pkt);
			//#endif
		});
	}
	
	public void send(Packet pkt, PacketRegister register) {
		sender.accept(pkt, register);
	}
}
