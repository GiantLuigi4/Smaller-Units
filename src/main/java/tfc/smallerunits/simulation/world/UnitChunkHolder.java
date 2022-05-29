package tfc.smallerunits.simulation.world;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class UnitChunkHolder extends ChunkHolder {
	LevelChunk chunk;
	
	public UnitChunkHolder(ChunkPos p_142986_, int p_142987_, LevelHeightAccessor p_142988_, LevelLightEngine p_142989_, LevelChangeListener p_142990_, PlayerProvider p_142991_, LevelChunk chunk) {
		super(p_142986_, p_142987_, p_142988_, p_142989_, p_142990_, p_142991_);
		this.chunk = chunk;
	}
	
	@Override
	public CompletableFuture<Either<ChunkAccess, ChunkLoadingFailure>> getFutureIfPresentUnchecked(ChunkStatus p_140048_) {
		return getFutureIfPresent(null);
	}
	
	@Override
	public CompletableFuture<Either<ChunkAccess, ChunkLoadingFailure>> getFutureIfPresent(ChunkStatus p_140081_) {
		return new CompletableFuture<>() {
			@Override
			public boolean isDone() {
				return true;
			}
			
			@Override
			public Either<ChunkAccess, ChunkLoadingFailure> get() {
				return Either.left(chunk);
			}
		};
	}
	
	@Override
	public CompletableFuture<Either<LevelChunk, ChunkLoadingFailure>> getTickingChunkFuture() {
		return getFullChunkFuture();
	}
	
	@Override
	public CompletableFuture<Either<LevelChunk, ChunkLoadingFailure>> getEntityTickingChunkFuture() {
		return getFullChunkFuture();
	}
	
	@Override
	public CompletableFuture<Either<LevelChunk, ChunkLoadingFailure>> getFullChunkFuture() {
		return new CompletableFuture<>() {
			@Override
			public boolean isDone() {
				return true;
			}
			
			@Override
			public Either<LevelChunk, ChunkLoadingFailure> get() {
				return Either.left(chunk);
			}
		};
	}
	
	@Nullable
	@Override
	public LevelChunk getTickingChunk() {
		return chunk;
	}
	
	@Nullable
	@Override
	public LevelChunk getFullChunk() {
		return chunk;
	}
	
	public void setBlockDirty(BlockPos pos) {
		blockChanged(pos);
	}
}
