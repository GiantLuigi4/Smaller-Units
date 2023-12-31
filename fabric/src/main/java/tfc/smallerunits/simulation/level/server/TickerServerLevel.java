package tfc.smallerunits.simulation.level.server;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.ServerLevelData;
import org.jetbrains.annotations.Nullable;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.plat.CapabilityWrapper;
import tfc.smallerunits.utils.scale.ResizingUtils;

import java.util.List;

public class TickerServerLevel extends AbstractTickerServerLevel {
	public TickerServerLevel(MinecraftServer server, ServerLevelData data, ResourceKey<Level> p_8575_, DimensionType dimType, ChunkProgressListener progressListener, ChunkGenerator generator, boolean p_8579_, long p_8580_, List<CustomSpawner> spawners, boolean p_8582_, Level parent, int upb, Region region) {
		super(server, data, p_8575_, dimType, progressListener, generator, p_8579_, p_8580_, spawners, p_8582_, parent, upb, region);
	}
	
	CapabilityWrapper wrapper;
	
	@Override
	public CapabilityWrapper getCaps() {
		if (wrapper == null) wrapper = new CapabilityWrapper(this);
		return wrapper;
	}
	
	// sounds
	@Override
	public void playSound(@Nullable Player pPlayer, Entity pEntity, SoundEvent pEvent, SoundSource pCategory, float pVolume, float pPitch) {
		this.playSound(pPlayer, pEntity.getX(), pEntity.getY(), pEntity.getZ(), pEvent, pCategory, pVolume, pPitch);
	}
	
	@Override
	public void playSound(@Nullable Player pPlayer, double pX, double pY, double pZ, SoundEvent pSound, SoundSource pCategory, float pVolume, float pPitch) {
		double scl = 1f / upb;
		BlockPos pos = getRegion().pos.toBlockPos();
		pX *= scl;
		pY *= scl;
		pZ *= scl;
		pX += pos.getX();
		pY += pos.getY();
		pZ += pos.getZ();
		double finalPX = pX;
		double finalPY = pY;
		double finalPZ = pZ;
		completeOnTick.add(() -> {
			Level lvl = parent.get();
			if (lvl == null) return;
			for (Player player : lvl.players()) {
				if (player == pPlayer) continue;
				
				double fScl = scl;
				if (ResizingUtils.isResizingModPresent())
					fScl *= 1 / ResizingUtils.getSize(player);
				if (fScl > 1) fScl = 1 / fScl;
				parent.get().playSound(
						pPlayer,
						finalPX, finalPY, finalPZ,
						pSound, pCategory, (float) (pVolume * fScl),
						pPitch
				);
			}
		});
	}
	
	@Override
	public void playLocalSound(double pX, double pY, double pZ, SoundEvent pSound, SoundSource pCategory, float pVolume, float pPitch, boolean pDistanceDelay) {
		double scl = 1f / upb;
		BlockPos pos = getRegion().pos.toBlockPos();
		pX *= scl;
		pY *= scl;
		pZ *= scl;
		pX += pos.getX();
		pY += pos.getY();
		pZ += pos.getZ();
		double finalPX = pX;
		double finalPY = pY;
		double finalPZ = pZ;
		completeOnTick.add(() -> {
			Level lvl = parent.get();
			if (lvl == null) return;
			for (Player player : lvl.players()) {
				double fScl = scl;
				if (ResizingUtils.isResizingModPresent())
					fScl *= 1 / ResizingUtils.getSize(player);
				if (fScl > 1) fScl = 1 / fScl;
				parent.get().playLocalSound(
						finalPX, finalPY, finalPZ,
						pSound, pCategory, (float) (pVolume * fScl),
						pPitch, pDistanceDelay
				);
			}
		});
	}
	
	// TODO: modify this?
	@Override
	public void playSeededSound(@javax.annotation.Nullable Player p_215027_, Entity p_215028_, SoundEvent p_215029_, SoundSource p_215030_, float p_215031_, float p_215032_, long p_215033_) {
		broadcastTo(p_215027_, p_215028_.getX(), p_215028_.getY(), p_215028_.getZ(), (double) p_215029_.getRange(p_215031_), this.dimension(), new ClientboundSoundEntityPacket(p_215029_, p_215030_, p_215028_, p_215031_, p_215032_, p_215033_));
	}
	
	// TODO: modify this?
	@Override
	public void playSeededSound(@javax.annotation.Nullable Player p_215017_, double p_215018_, double p_215019_, double p_215020_, SoundEvent p_215021_, SoundSource p_215022_, float p_215023_, float p_215024_, long p_215025_) {
		broadcastTo(p_215017_, p_215018_, p_215019_, p_215020_, (double) p_215021_.getRange(p_215023_), this.dimension(), new ClientboundSoundPacket(p_215021_, p_215022_, p_215018_, p_215019_, p_215020_, p_215023_, p_215024_, p_215025_));
	}
}
