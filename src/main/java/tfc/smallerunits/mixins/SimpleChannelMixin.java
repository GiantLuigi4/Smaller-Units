package tfc.smallerunits.mixins;

import net.minecraft.network.NetworkManager;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.helpers.PacketHacksHelper;

//import net.minecraft.network.PacketBuffer;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SimpleChannel.class)
public class SimpleChannelMixin<MSG> {
	//	@Inject(at = @At("HEAD"), method = "encodeMessage(Ljava/lang/Object;Lnet/minecraft/network/PacketBuffer;)I", remap = false)
//	public void smaller_units_onEncode(MSG message, PacketBuffer target, CallbackInfoReturnable<Integer> cir) {
//		if (PacketHacksHelper.unitPos != null) {
//			target.writeBoolean(true);
//			target.writeBlockPos(PacketHacksHelper.unitPos);
//		} else {
//			target.writeBoolean(false);
//		}
//	}
	@Inject(at = @At("HEAD"), method = "sendTo(Ljava/lang/Object;Lnet/minecraft/network/NetworkManager;Lnet/minecraftforge/fml/network/NetworkDirection;)V", remap = false)
	public void onSendPacket(MSG message, NetworkManager manager, NetworkDirection direction, CallbackInfo ci) {
		PacketHacksHelper.setPosForPacket(message, PacketHacksHelper.unitPos);
	}
	
	// TODO: test
	@Inject(at = @At("HEAD"), method = "send", remap = false)
	public void onSendPacket(PacketDistributor.PacketTarget target, MSG message, CallbackInfo ci) {
		PacketHacksHelper.setPosForPacket(message, PacketHacksHelper.unitPos);
	}
}
