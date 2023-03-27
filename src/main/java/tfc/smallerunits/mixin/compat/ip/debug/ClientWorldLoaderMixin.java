package tfc.smallerunits.mixin.compat.ip.debug;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.commons.lang3.Validate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import qouteall.imm_ptl.core.ClientWorldLoader;
import qouteall.imm_ptl.core.ducks.IEClientPlayNetworkHandler;
import qouteall.imm_ptl.core.ducks.IEMinecraftClient;
import qouteall.imm_ptl.core.ducks.IEParticleManager;
import qouteall.q_misc_util.Helper;
import tfc.smallerunits.logging.Loggers;

import java.util.function.Supplier;

@Mixin(value = ClientWorldLoader.class, remap = false)
public abstract class ClientWorldLoaderMixin {
	@Shadow
	@Final
	private static Minecraft client;
	
	@Shadow
	public static native LevelRenderer getWorldRenderer(ResourceKey<Level> dimension);
	
	/**
	 * @author GiantLuigi4
	 */
	@Overwrite
	public static <T> T withSwitchedWorld(ClientLevel newWorld, Supplier<T> supplier) {
		Validate.isTrue(client.isSameThread());// 472
		Validate.isTrue(client.player != null);// 473
		ClientPacketListener networkHandler = client.getConnection();// 475
		ClientLevel originalWorld = client.level;// 477
		LevelRenderer originalWorldRenderer = client.levelRenderer;// 478
		ClientLevel originalNetHandlerWorld = networkHandler.getLevel();// 479
		LevelRenderer newWorldRenderer = getWorldRenderer(newWorld.dimension());// 481
		Validate.notNull(newWorldRenderer);// 483
		client.level = newWorld;// 485
		((IEParticleManager) client.particleEngine).ip_setWorld(newWorld);// 486
		((IEMinecraftClient) client).setWorldRenderer(newWorldRenderer);// 487
		((IEClientPlayNetworkHandler) networkHandler).ip_setWorld(newWorld);// 488
		
		Object var7 = null;
		try {
			var7 = supplier.get();
		} catch (Throwable err) {
			Loggers.SU_LOGGER.error("An exception was thrown during IP's withSwitchedWorldMethod", err);
			if (!FMLEnvironment.production) {
				throw new RuntimeException("Forcibly crash the game because it's going to crash anyway");
			}
		} finally {
			if (client.level != newWorld) {// 494
				Helper.err("Task ended in the wrong world: " + client.level + "; expected " + newWorld);
				originalWorld = client.level;// 496
				originalWorldRenderer = client.levelRenderer;// 497
			}
			
			client.level = originalWorld;// 500
			((IEMinecraftClient) client).setWorldRenderer(originalWorldRenderer);// 501
			((IEParticleManager) client.particleEngine).ip_setWorld(originalWorld);// 502
			((IEClientPlayNetworkHandler) networkHandler).ip_setWorld(originalNetHandlerWorld);// 503
		}
		
		return (T) var7;// 491
	}
}
