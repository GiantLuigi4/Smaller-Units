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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.ServerLevelData;
import org.jetbrains.annotations.Nullable;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.plat.CapabilityWrapper;
import tfc.smallerunits.simulation.level.server.AbstractTickerServerLevel;
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
	// TODO: look into correcting this?
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
		if (ResizingUtils.isResizingModPresent() && pPlayer != null) // TODO: I probably need to manually send sounds to each player
			scl *= 1 / ResizingUtils.getSize(pPlayer);
		if (scl > 1) scl = 1 / scl;
		double finalScl = scl;
		completeOnTick.add(() -> {
			Level lvl = parent.get();
			if (lvl == null) return;
			for (Player player : lvl.players()) {
				if (player == pPlayer) continue;
				
				double fScl = finalScl;
				fScl *= 1 / ResizingUtils.getSize(player);
				parent.get().playSound(
						pPlayer,
						finalPX, finalPY, finalPZ,
						pSound, pCategory, (float) (pVolume * finalScl),
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
//		if (ResizingUtils.isResizingModPresent() && pPlayer != null) // TODO: I probably need to manually send sounds to each player
//			scl *= 1 / ResizingUtils.getSize(pPlayer);
		if (scl > 1) scl = 1 / scl;
		double finalScl = scl;
		completeOnTick.add(() -> {
			parent.get().playLocalSound(finalPX, finalPY, finalPZ, pSound, pCategory, (float) (pVolume * finalScl), pPitch, pDistanceDelay);
		});
	}
}
