package tfc.smallerunits.mixin.dangit;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockChangedAckPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.networking.hackery.NetworkingHacks;
import tfc.smallerunits.utils.AckClock;

import java.util.HashMap;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
	@Shadow public ServerPlayer player;
	
	@Shadow public abstract void send(Packet<?> packet);
	
	@Unique
	HashMap<NetworkingHacks.LevelDescriptor, AckClock> clocks = new HashMap<>();
	
	@Inject(at = @At("HEAD"), method = "tick")
	public void preTick(CallbackInfo ci) {
		if (!clocks.isEmpty()) {
			for (AckClock value : clocks.values()) {
				NetworkingHacks.unitPos.set(value.descriptor);
				NetworkingHacks.currentContext.set(value.netCtx);
				this.send(new ClientboundBlockChangedAckPacket(value.upTo));
			}
			
			NetworkingHacks.unitPos.remove();
			NetworkingHacks.currentContext.remove();
			clocks.clear();
		}
	}
	
	@Inject(at = @At("HEAD"), method = "ackBlockChangesUpTo", cancellable = true)
	public void preSetAck(int p_215202_, CallbackInfo ci) {
		NetworkingHacks.LevelDescriptor descriptor = NetworkingHacks.unitPos.get();
		if (descriptor != null) {
			AckClock clock = clocks.computeIfAbsent(descriptor, (e) -> new AckClock(e, NetworkingHacks.currentContext.get()));
			clock.upTo = p_215202_;
			ci.cancel();
		}
	}
}
