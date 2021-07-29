//package com.tfc.smallerunits.mixins;
//
//import com.mojang.datafixers.util.Pair;
//import com.tfc.smallerunits.block.UnitTileEntity;
//import io.netty.buffer.ByteBuf;
//import io.netty.channel.ChannelHandlerContext;
//import net.minecraft.client.Minecraft;
//import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.network.NettyPacketDecoder;
//import net.minecraft.network.PacketBuffer;
//import net.minecraft.network.PacketDirection;
//import net.minecraft.tileentity.TileEntity;
//import net.minecraft.world.World;
//import net.minecraftforge.fml.network.NetworkDirection;
//import net.minecraftforge.fml.network.NetworkEvent;
//import org.spongepowered.asm.mixin.Final;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.UUID;
//import java.util.function.Supplier;
//
////@Mixin(targets = "net.minecraftforge.fml.network.simple.IndexedMessageCodec", remap = false)
//@Mixin(NettyPacketDecoder.class)
//public class MessageHandlerMixin {
//	@Shadow @Final private PacketDirection direction;
//	private static Pair<HashMap<UUID, World>, HashMap<UUID, World>> oldWorlds = Pair.of(new HashMap<>(), new HashMap<>());
//
////	@Inject(at = @At("HEAD"), method = "consume(Lnet/minecraft/network/PacketBuffer;ILjava/util/function/Supplier;)V", remap = false)
//	@Inject(at = @At("HEAD"), method = "decode(Lio/netty/channel/ChannelHandlerContext;Lio/netty/buffer/ByteBuf;Ljava/util/List;)V")
////	public void smaller_units_onDecodePre(PacketBuffer payload, int payloadIndex, Supplier<NetworkEvent.Context> context, CallbackInfo ci) {
//	public void smaller_units_onDecodePre(ChannelHandlerContext context, ByteBuf payload, List<Object> p_decode_3_, CallbackInfo ci) {
//		if (payload == null) return;
//		boolean val = payload.readBoolean();
//		PacketDirection direction = this.direction;
////		if (
////				context.get().getDirection().equals(NetworkDirection.LOGIN_TO_CLIENT) ||
////						context.get().getDirection().equals(NetworkDirection.LOGIN_TO_SERVER)
////		) return;
//		if (val) {
//			PlayerEntity playerEntity;
//			HashMap<UUID, World> worldHashMap;
////			if (context.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
//			if (direction.equals(PacketDirection.SERVERBOUND)) {
////				playerEntity = context.get().getSender();
//				playerEntity = ;
//				worldHashMap = oldWorlds.getFirst();
//			} else {
//				playerEntity = Minecraft.getInstance().player;
//				worldHashMap = oldWorlds.getSecond();
//			}
//			worldHashMap.put(playerEntity.getUniqueID(), playerEntity.getEntityWorld());
//			TileEntity te = playerEntity.getEntityWorld().getTileEntity(payload.readBlockPos());
//			if (te instanceof UnitTileEntity) {
//				playerEntity.setWorld(((UnitTileEntity) te).world);
//			}
//		}
//	}
//
//	@Inject(at = @At("RETURN"), method = "Lnet/minecraft/network/NettyPacketDecoder;decode(Lio/netty/channel/ChannelHandlerContext;Lio/netty/buffer/ByteBuf;Ljava/util/List;)V")
//	public void smaller_units_onDecodePost(PacketBuffer payload, int payloadIndex, Supplier<NetworkEvent.Context> context, CallbackInfo ci) {
//		if (payload == null) return;
//		if (
//				context.get().getDirection().equals(NetworkDirection.LOGIN_TO_CLIENT) ||
//						context.get().getDirection().equals(NetworkDirection.LOGIN_TO_SERVER)
//		) return;
//		PlayerEntity playerEntity;
//		HashMap<UUID, World> worldHashMap;
//		if (context.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
//			playerEntity = context.get().getSender();
//			worldHashMap = oldWorlds.getFirst();
//		} else {
//			playerEntity = Minecraft.getInstance().player;
//			worldHashMap = oldWorlds.getSecond();
//		}
//		if (worldHashMap.containsKey(playerEntity.getUniqueID())) {
//			playerEntity.setWorld(worldHashMap.get(playerEntity.getUniqueID()));
//		}
//	}
//}
