package tfc.smallerunits.networking.hackery;

import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import qouteall.imm_ptl.core.network.IPCommonNetwork;
import tfc.smallerunits.data.access.PacketListenerAccessor;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.data.tracking.RegionalAttachments;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.utils.PositionalInfo;
import tfc.smallerunits.utils.math.HitboxScaling;

import java.util.HashMap;

public class PacketUtilMess {
	private static final HashMap<Packet, PositionalInfo> pkts = new HashMap<>();
	
	private static final Object syncLock = new Object();
	private static boolean synchronizationLock = false;
	
	
	public static <T extends PacketListener> void preHandlePacket(PacketListener listener, Packet packet) {
		NetworkingHacks.LevelDescriptor pos = NetworkingHacks.getPosFor(packet);
		if (pos != null) {
			// TODO: is this needed?
			synchronized (syncLock) {
//				while (synchronizationLock) {
//					try {
//						Thread.sleep(1);
//					} catch (Throwable ignored) {
//					}
//				}
//				synchronizationLock = true;

//				Player player = null;
				Player player = ((PacketListenerAccessor) listener).getPlayer();
//				if (listener instanceof ServerGamePacketListenerImpl)
//					player = ((ServerGamePacketListenerImpl) listener).getPlayer();
//				else if (FMLEnvironment.dist.isClient()) {
//					if (IHateTheDistCleaner.isClientPacketListener(listener))
//						player = IHateTheDistCleaner.getPlayer();
//				}
				if (player == null) return;
				Level lvl = player.level;
				RegionalAttachments attachments = (RegionalAttachments) lvl;
				Region region = attachments.SU$getRegion(pos.pos());
				if (region == null) return;
				Level spaceLevel = region.getLevel(listener, player, pos.upb());
//				ISUCapability capability = SUCapabilityManager.getCapability(lvl, new ChunkPos(pos));
				
				NetworkingHacks.setPos(pos);
//				UnitSpace space = capability.getOrMakeUnit(pos);
				PositionalInfo info = new PositionalInfo(player);
				info.scalePlayerReach(player, pos.upb());
				
				AABB scaledBB;
				// TODO: some form of hitbox validation, because elsewise the server can freeze pretty easily
				player.setBoundingBox(scaledBB = HitboxScaling.getOffsetAndScaledBox(info.box, info.pos, pos.upb(), pos.pos()));
				player.eyeHeight = (float) (info.eyeHeight * (pos.upb()));
				((PacketListenerAccessor) listener).setWorld(player.level = spaceLevel);
				player.setPosRaw(scaledBB.getCenter().x, scaledBB.minY, scaledBB.getCenter().z);
				info.setupClient(player, spaceLevel, true);
				// TODO: do this more properly
				player.xOld = player.xo = player.position().x;
				player.yOld = player.yo = player.position().y;
				player.zOld = player.zo = player.position().z;
				
				ITickerLevel tkLvl = (ITickerLevel) player.level;
				tkLvl.addInteractingEntity(player);
				
				pkts.put(packet, info);
			}
		}
	}
	
	public static <T extends PacketListener> void postHandlePacket(PacketListener listener, Packet packet) {
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
			lvl.reset(player);
			((PacketListenerAccessor) listener).setWorld(lvl.lvl);
			NetworkingHacks.unitPos.remove();
			synchronizationLock = false;
		}
	}
}
