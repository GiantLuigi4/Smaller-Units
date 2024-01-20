package tfc.smallerunits.plat.mixin.core.network;

import net.minecraft.network.Connection;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import tfc.smallerunits.data.access.PacketListenerAccessor;
import tfc.smallerunits.data.access.SUScreenAttachments;
import tfc.smallerunits.logging.Loggers;
import tfc.smallerunits.networking.hackery.NetworkContext;
import tfc.smallerunits.networking.hackery.NetworkHandlingContext;
import tfc.smallerunits.networking.hackery.NetworkingHacks;
import tfc.smallerunits.plat.net.NetworkDirection;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.utils.IHateTheDistCleaner;
import tfc.smallerunits.utils.PositionalInfo;

@Mixin(BlockableEventLoop.class)
public class BlockableEventLoopMixin {
	@ModifyVariable(at = @At("HEAD"), method = "execute", argsOnly = true, ordinal = 0)
	public Runnable preSchedule(Runnable value) {
		NetworkHandlingContext nhcontext = NetworkingHacks.currentContext.get();
		if (nhcontext == null) return value;
		
		NetworkContext context = nhcontext.netContext;
		Connection networkManager = context.connection;
		PositionalInfo info = nhcontext.info;
		NetworkDirection direction = nhcontext.direction;
		
		NetworkingHacks.LevelDescriptor descriptor = NetworkingHacks.unitPos.get();
		
		Runnable r = () -> {
			NetworkingHacks.increaseBlockPosPrecision.set(true);
			NetworkingHacks.setPos(descriptor);
			NetworkingHacks.currentContext.set(nhcontext);
			
			Level preHandleLevel = context.player.level;
			
			if (context.player.level != nhcontext.targetLevel)
				info.adjust(context.player, context.player.level, descriptor, direction == NetworkDirection.TO_SERVER);
			
			Object old = null;
			boolean toServer = direction == NetworkDirection.TO_SERVER;
			if (toServer) old = context.player.containerMenu;
			else old = IHateTheDistCleaner.getScreen();
			// get level
			int upb = 0;
			if (preHandleLevel instanceof ITickerLevel tl) upb = tl.getUPB();
			// TODO: debug this garbage
			((PacketListenerAccessor) networkManager.getPacketListener()).setWorld(context.player.level);
			
			try {
				value.run(); // run deferred work
			} catch (Throwable err) {
				Loggers.PACKET_HACKS_LOGGER.error("-- A wrapped packet has encountered an error: desyncs are imminent --");
				err.printStackTrace();
			}
			
			if (toServer) {
				Object newV = context.player.containerMenu;
				if (old != newV) {
					if (newV != context.player.inventoryMenu) {
						((SUScreenAttachments) newV).setup(info, context.player.level, descriptor);
					}
				}
			} else {
				Object newV = IHateTheDistCleaner.getScreen();
				if (old != newV) {
					if (newV != null) {
						((SUScreenAttachments) newV).setup(info, context.player.level, descriptor);
					}
				}
			}
			
			info.reset(context.player);
			((PacketListenerAccessor) networkManager.getPacketListener()).setWorld(preHandleLevel);
			
			NetworkingHacks.increaseBlockPosPrecision.set(false);
			
			NetworkingHacks.setPos(null);
			NetworkingHacks.currentContext.set(null);
		};
		
		return r;
	}
}
