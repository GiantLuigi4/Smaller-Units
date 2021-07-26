//package com.tfc.smallerunits.mixins;
//
//import com.tfc.smallerunits.helpers.PacketHacksHelper;
//import net.minecraft.util.ResourceLocation;
//import net.minecraftforge.fml.network.NetworkInstance;
//import net.minecraftforge.fml.network.NetworkRegistry;
//import net.minecraftforge.fml.network.simple.SimpleChannel;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Redirect;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//
//import java.util.function.Predicate;
//import java.util.function.Supplier;
//
//@Mixin(NetworkRegistry.class)
//public abstract class NetworkRegistryMixin {
//	@Shadow
//	protected static NetworkInstance createInstance(ResourceLocation name, Supplier<String> networkProtocolVersion, Predicate<String> clientAcceptedVersions, Predicate<String> serverAcceptedVersions) {
//		return null;
//	}
//
//	@Redirect(at = @At("RETURN"), remap = false, method = "newSimpleChannel(Lnet/minecraft/util/ResourceLocation;Ljava/util/function/Supplier;Ljava/util/function/Predicate;Ljava/util/function/Predicate;)Lnet/minecraftforge/fml/network/simple/SimpleChannel;")
//	private static SimpleChannel smaller_units_onCreateInstance(ResourceLocation name, Supplier<String> networkProtocolVersion, Predicate<String> clientAcceptedVersions, Predicate<String> serverAcceptedVersions, CallbackInfoReturnable<SimpleChannel> cir) {
//		return PacketHacksHelper.channelHashMap.put(name, new SimpleChannel(createInstance(name, networkProtocolVersion, clientAcceptedVersions, serverAcceptedVersions)));
//	}
//}
