package tfc.smallerunits.mixin.dangit;

//import com.mojang.blaze3d.systems.RenderSystem;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.multiplayer.ClientLevel;
//import net.minecraft.client.multiplayer.ClientPacketListener;
//import net.minecraft.client.renderer.LevelRenderer;
//import org.spongepowered.asm.mixin.Final;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//import qouteall.imm_ptl.core.ducks.IEClientPlayNetworkHandler;
//import qouteall.imm_ptl.core.ducks.IEMinecraftClient;
//import qouteall.imm_ptl.core.ducks.IEParticleManager;
//import qouteall.imm_ptl.core.network.IPCommonNetworkClient;
//
//import java.util.function.Supplier;

//@Mixin(IPCommonNetworkClient.class)
public class IPNetworkDebugMixin {
//	@Shadow @Final public static Minecraft client;
//
//	private static ClientPacketListener networkHandler;
//	private static ClientLevel originalWorld;
//	private static LevelRenderer originalWorldRenderer;
//	private static ClientLevel originalNetHandlerWorld;
//
//	@Inject(at = @At("HEAD"), method = "withSwitchedWorld(Lnet/minecraft/client/multiplayer/ClientLevel;Ljava/util/function/Supplier;)Ljava/lang/Object;")
//	private static <T> void preWithSwitchedWorld(ClientLevel newWorld, Supplier<T> supplier, CallbackInfoReturnable<T> cir) {
//		if (!(RenderSystem.isOnGameThread() || RenderSystem.isOnRenderThread())) {
//			throw new RuntimeException("what");
//		}
//
//		networkHandler = client.getConnection();
//		originalWorld = client.level;
//		originalWorldRenderer = client.levelRenderer;
//		originalNetHandlerWorld = networkHandler.getLevel();
//	}
//
//	@Inject(method = "withSwitchedWorld(Lnet/minecraft/client/multiplayer/ClientLevel;Ljava/util/function/Supplier;)Ljava/lang/Object;", at = @At(value = "INVOKE", target = "Lqouteall/q_misc_util/Helper;err(Ljava/lang/Object;)V"), cancellable = true)
//	private static <T> void onCrashWithSwitchedWorld(ClientLevel newWorld, Supplier<T> supplier, CallbackInfoReturnable<T> cir) {
//		client.level = originalWorld;
//		client.player.level = originalWorld;
//		((IEMinecraftClient) client).setWorldRenderer(originalWorldRenderer);
//		((IEParticleManager) client.particleEngine).ip_setWorld(originalWorld);
//		((IEClientPlayNetworkHandler) networkHandler).ip_setWorld(originalNetHandlerWorld);
//
//		cir.cancel();
//	}
}
