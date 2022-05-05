package tfc.smallerunits.mixin.core;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.UnitSpace;
import tfc.smallerunits.data.capability.ISUCapability;
import tfc.smallerunits.data.capability.SUCapabilityManager;
import tfc.smallerunits.networking.hackery.NetworkingHacks;
import tfc.smallerunits.utils.IHateTheDistCleaner;
import tfc.smallerunits.utils.PositionalInfo;
import tfc.smallerunits.utils.math.HitboxScaling;

import java.util.HashMap;

@Mixin(PacketUtils.class)
public class PacketUtilsMixin {
	@Unique
	private static final HashMap<Packet, PositionalInfo> pkts = new HashMap<>();
	
	@Inject(at = @At("HEAD"), method = {"lambda$ensureRunningOnSameThread$0", "m_131356_"})
	private static <T extends PacketListener> void preHandlePacket(PacketListener listener, Packet packet, CallbackInfo ci) {
		BlockPos pos = NetworkingHacks.getPosFor(packet);
		if (pos != null) {
			Player player = null;
			if (listener instanceof ServerGamePacketListenerImpl)
				player = ((ServerGamePacketListenerImpl) listener).getPlayer();
			else if (FMLEnvironment.dist.isClient()) {
				if (IHateTheDistCleaner.isClientPacketListener(listener))
					player = IHateTheDistCleaner.getPlayer();
			}
			Level lvl = player.level;
			ISUCapability capability = SUCapabilityManager.getCapability(lvl, new ChunkPos(pos));
			UnitSpace space = capability.getOrMakeUnit(pos);
			PositionalInfo info = new PositionalInfo(player);
			AABB scaledBB;
			player.setBoundingBox(scaledBB = HitboxScaling.getOffsetAndScaledBox(info.box, info.pos, space));
			player.eyeHeight = (float) (info.eyeHeight * space.unitsPerBlock);
			player.setPosRaw(scaledBB.getCenter().x, scaledBB.minY, scaledBB.getCenter().z);
			if (player.level.isClientSide) {
				if (player.level instanceof ClientLevel) {
					((LocalPlayer) player).clientLevel = (ClientLevel) space.getMyLevel();
				}
			}
			player.level = space.getMyLevel();
			pkts.put(packet, info);
		}
	}
	
	@Inject(at = @At("RETURN"), method = {"lambda$ensureRunningOnSameThread$0", "m_131356_"})
	private static <T extends PacketListener> void postHandlePacket(PacketListener listener, Packet packet, CallbackInfo ci) {
		if (pkts.containsKey(packet)) {
			PositionalInfo lvl = pkts.remove(packet);
			Player player = null;
			if (listener instanceof ServerGamePacketListenerImpl)
				player = ((ServerGamePacketListenerImpl) listener).getPlayer();
			else if (FMLEnvironment.dist.isClient()) {
				if (IHateTheDistCleaner.isClientPacketListener(listener))
					player = IHateTheDistCleaner.getPlayer();
			}
//			player.level = lvl;
			lvl.reset(player);
		}
	}
}
