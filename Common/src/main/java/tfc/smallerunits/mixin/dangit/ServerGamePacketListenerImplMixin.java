package tfc.smallerunits.mixin.dangit;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
	@Shadow public ServerPlayer player;
	
	@Shadow public abstract void send(Packet<?> packet);
	
//	@Unique
//	HashMap<NetworkingHacks.LevelDescriptor, AckClock> clocks = new HashMap<>();
//
//	@Inject(at = @At("HEAD"), method = "tick")
//	public void preTick(CallbackInfo ci) {
//		if (!clocks.isEmpty()) {
//			for (AckClock value : clocks.values()) {
//				NetworkingHacks.unitPos.set(value.descriptor);
//				NetworkingHacks.currentContext.set(value.netCtx);
//				this.send(new ClientboundBlockChangedAckPacket(value.upTo));
//			}
//
//			NetworkingHacks.unitPos.remove();
//			NetworkingHacks.currentContext.remove();
//			clocks.clear();
//		}
//	}
//
//	@Inject(at = @At("HEAD"), method = "ackBlockChangesUpTo", cancellable = true)
//	public void preSetAck(int p_215202_, CallbackInfo ci) {
//		NetworkingHacks.LevelDescriptor descriptor = NetworkingHacks.unitPos.get();
//		if (descriptor != null) {
//			AckClock clock = clocks.computeIfAbsent(descriptor, (e) -> new AckClock(e, NetworkingHacks.currentContext.get()));
//			clock.upTo = p_215202_;
//			ci.cancel();
//		}
//	}
}
