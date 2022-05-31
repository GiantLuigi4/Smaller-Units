package tfc.smallerunits.mixin.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.data.access.PacketListenerAccessor;
import tfc.smallerunits.data.tracking.RegionalAttachments;
import tfc.smallerunits.networking.hackery.NetworkingHacks;
import tfc.smallerunits.utils.PositionalInfo;
import tfc.smallerunits.utils.math.HitboxScaling;

import java.util.HashMap;

@Mixin(PacketUtils.class)
public class PacketUtilsMixin {
	@Unique
	private static final HashMap<Packet, PositionalInfo> pkts = new HashMap<>();
	
	@Unique
	private static final Object syncLock = new Object();
	@Unique
	private static boolean synchronizationLock = false;
	
	@Inject(at = @At("HEAD"), method = {"lambda$ensureRunningOnSameThread$0", "m_131356_"})
	private static <T extends PacketListener> void preHandlePacket(PacketListener listener, Packet packet, CallbackInfo ci) {
		NetworkingHacks.LevelDescriptor pos = NetworkingHacks.getPosFor(packet);
		if (pos != null) {
			// TODO: is this needed?
			synchronized (syncLock) {
				while (synchronizationLock) {
					try {
						Thread.sleep(1);
					} catch (Throwable ignored) {
					}
				}
				synchronizationLock = true;
//				Player player = null;
				Player player = ((PacketListenerAccessor) listener).getPlayer();
//				if (listener instanceof ServerGamePacketListenerImpl)
//					player = ((ServerGamePacketListenerImpl) listener).getPlayer();
//				else if (FMLEnvironment.dist.isClient()) {
//					if (IHateTheDistCleaner.isClientPacketListener(listener))
//						player = IHateTheDistCleaner.getPlayer();
//				}
				Level lvl = player.level;
				RegionalAttachments attachments = (RegionalAttachments) lvl;
				Level spaceLevel = attachments.SU$getRegion(pos.pos()).getLevel(listener, player, pos.upb());
//				ISUCapability capability = SUCapabilityManager.getCapability(lvl, new ChunkPos(pos));
				
				NetworkingHacks.unitPos.set(pos);
//				UnitSpace space = capability.getOrMakeUnit(pos);
				PositionalInfo info = new PositionalInfo(player);
				info.scalePlayerReach(player, pos.upb());
				
				AABB scaledBB;
				player.setBoundingBox(scaledBB = HitboxScaling.getOffsetAndScaledBox(info.box, info.pos, pos.upb()));
				player.eyeHeight = (float) (info.eyeHeight * (1d / pos.upb()));
				player.setPosRaw(scaledBB.getCenter().x, scaledBB.minY, scaledBB.getCenter().z);
				if (player.level.isClientSide) {
					if (player.level instanceof ClientLevel) {
						((LocalPlayer) player).clientLevel = (ClientLevel) spaceLevel;
						Minecraft.getInstance().level = ((LocalPlayer) player).clientLevel;
						// TODO: set particle engine
					}
				}
				((PacketListenerAccessor) listener).setWorld(player.level = spaceLevel);
				pkts.put(packet, info);
			}
		}
	}
	
	@Inject(at = @At("RETURN"), method = {"lambda$ensureRunningOnSameThread$0", "m_131356_"})
	private static <T extends PacketListener> void postHandlePacket(PacketListener listener, Packet packet, CallbackInfo ci) {
		if (pkts.containsKey(packet)) {
			PositionalInfo lvl = pkts.remove(packet);
			Player player = ((PacketListenerAccessor) listener).getPlayer();
//			if (listener instanceof ServerGamePacketListenerImpl)
//				player = ((ServerGamePacketListenerImpl) listener).getPlayer();
//			else if (FMLEnvironment.dist.isClient()) {
//				if (IHateTheDistCleaner.isClientPacketListener(listener))
//					player = IHateTheDistCleaner.getPlayer();
//			}
//			player.level = lvl;
			// TODO: set listener level
			((PacketListenerAccessor) listener).setWorld(lvl.lvl);
			lvl.reset(player);
			NetworkingHacks.unitPos.remove();
			synchronizationLock = false;
		}
	}
}
