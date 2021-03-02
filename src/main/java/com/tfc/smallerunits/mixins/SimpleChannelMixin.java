//package com.tfc.smallerunits.mixins;
//
//import com.tfc.smallerunits.Smallerunits;
//import com.tfc.smallerunits.helpers.PacketHacksHelper;
//import com.tfc.smallerunits.networking.SUWorldDirectingPacket;
//import net.minecraft.network.NetworkManager;
//import net.minecraftforge.fml.network.NetworkDirection;
//import net.minecraftforge.fml.network.NetworkInstance;
//import net.minecraftforge.fml.network.simple.SimpleChannel;
//import org.spongepowered.asm.mixin.Final;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//@Mixin(SimpleChannel.class)
//public class SimpleChannelMixin<MSG> {
//	@Shadow @Final private NetworkInstance instance;
//
//	@Inject(at = @At("HEAD"), cancellable = true, remap = false, method = "sendTo")
//	public void onSendto(MSG message, NetworkManager manager, NetworkDirection direction, CallbackInfo ci) {
//		if (PacketHacksHelper.unitPos != null && !(message instanceof SUWorldDirectingPacket)) {
//			Smallerunits.NETWORK_INSTANCE.sendTo(
//					new SUWorldDirectingPacket<>(
//							instance.getChannelName(),
//							PacketHacksHelper.unitPos,
//							message
//					), manager, direction
//			);
//			ci.cancel();
//		}
//	}
//
////	@Shadow @Final private NetworkInstance instance;
////
////	@Inject(at = @At("HEAD"), method = "encodeMessage(Ljava/lang/Object;Lnet/minecraft/network/PacketBuffer;)I", remap = false)
////	public <MSG> void smaller_units_onEncode(MSG message, PacketBuffer target, CallbackInfoReturnable<Integer> cir) {
////		if (PacketHacksHelper.unitPos != null) {
////			target.writeBoolean(true);
////			target.writeBlockPos(PacketHacksHelper.unitPos);
////		}
////	}
//}
