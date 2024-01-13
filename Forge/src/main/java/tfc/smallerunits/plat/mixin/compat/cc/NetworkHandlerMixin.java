package tfc.smallerunits.plat.mixin.compat.cc;

import dan200.computercraft.shared.network.NetworkHandler;
import dan200.computercraft.shared.network.NetworkMessage;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.networking.hackery.NetworkingHacks;
import tfc.smallerunits.simulation.chunk.BasicVerticalChunk;
import tfc.smallerunits.simulation.level.ITickerLevel;

@Mixin(value = NetworkHandler.class, remap = false)
public class NetworkHandlerMixin {
	@Shadow
	private static SimpleChannel network;
	
	@Inject(at = @At("HEAD"), method = "sendToAllTracking", cancellable = true)
	private static void preSend(NetworkMessage packet, LevelChunk chunk, CallbackInfo ci) {
		if (chunk instanceof BasicVerticalChunk) {
			NetworkingHacks.LevelDescriptor desc = NetworkingHacks.unitPos.get();
			NetworkingHacks.setPos(((ITickerLevel) chunk.getLevel()).getDescriptor());
			network.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), packet);
			NetworkingHacks.setPos(desc);
			ci.cancel();
		}
	}
	
	@Inject(at = @At("HEAD"), method = "sendToAllAround", cancellable = true)
	private static void preSend(NetworkMessage packet, Level world, Vec3 pos, double range, CallbackInfo ci) {
		if (world instanceof ITickerLevel) {
			NetworkingHacks.LevelDescriptor desc = NetworkingHacks.unitPos.get();
			NetworkingHacks.setPos(((ITickerLevel) world).getDescriptor());
			
			PacketDistributor.TargetPoint targetpoint = new PacketDistributor.TargetPoint(pos.x, pos.y, pos.z, range, world.dimension());
			network.send(PacketDistributor.NEAR.with(() -> targetpoint), packet);
			
			NetworkingHacks.setPos(desc);
			ci.cancel();
		}
	}
}
