package tfc.smallerunits.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.server.level.ServerPlayer;
import tfc.smallerunits.networking.platform.NetCtx;
import tfc.smallerunits.networking.platform.NetworkDirection;

public class Packet implements net.minecraft.network.protocol.Packet {
	public Packet() {
	}
	
	public Packet(FriendlyByteBuf buf) {
	}
	
	public void write(FriendlyByteBuf buf) {
	}
	
	public void handle(NetCtx ctx) {
	}
	
	public final void handle(PacketListener pHandler) {
	}
	
	public boolean isSkippable() {
		return net.minecraft.network.protocol.Packet.super.isSkippable();
	}
	
	public boolean checkClient(NetCtx ctx) {
		return ctx.getDirection().equals(NetworkDirection.TO_CLIENT);
	}
	
	public boolean checkServer(NetCtx ctx) {
		return ctx.getDirection().equals(NetworkDirection.TO_SERVER);
	}
	
	public void respond(NetCtx ctx, Packet packet) {
		ctx.respond(packet);
	}
}
