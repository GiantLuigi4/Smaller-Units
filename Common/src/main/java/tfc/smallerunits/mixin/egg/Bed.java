package tfc.smallerunits.mixin.egg;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.simulation.level.ITickerLevel;

@Mixin(ClientPacketListener.class)
public abstract class Bed {
	@Shadow private ClientLevel level;
	
	@Shadow protected abstract void postAddEntitySoundInstance(Entity entity);
	
	@Shadow @Final private static Logger LOGGER;
	
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/util/thread/BlockableEventLoop;)V", shift = At.Shift.AFTER), method = "handleAddEntity", cancellable = true)
	public void preAddEnt(ClientboundAddEntityPacket $$0, CallbackInfo ci) {
		if (level instanceof ITickerLevel) {
			EntityType<?> entitytype = EntityType.PIG;
			Entity entity = entitytype.create(this.level);
			if (entity != null) {
				entity.recreateFromPacket($$0);
				int i = $$0.getId();
				this.level.putNonPlayerEntity(i, entity);
				this.postAddEntitySoundInstance(entity);
				ci.cancel();
			} else {
				LOGGER.warn("Skipping Entity with id {}", entitytype);
			}
		}
	}
}
