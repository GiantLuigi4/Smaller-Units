package tfc.smallerunits.simulation.world;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import org.jetbrains.annotations.Nullable;
import tfc.smallerunits.UnitSpace;

import java.io.IOException;
import java.util.List;

public class TickerServerWorld extends ServerLevel {
	private static final NoStorageSource src = NoStorageSource.make();
	private static final LevelStorageSource.LevelStorageAccess noAccess;
	
	static {
		try {
			noAccess = src.createAccess("no");
		} catch (IOException e) {
			RuntimeException ex = new RuntimeException(e.getMessage(), e);
			ex.setStackTrace(e.getStackTrace());
			throw ex;
		}
	}
	
	final UnitSpace parent;
	
	public TickerServerWorld(MinecraftServer p_8571_, ServerLevelData p_8574_, ResourceKey<Level> p_8575_, DimensionType p_8576_, ChunkProgressListener p_8577_, ChunkGenerator p_8578_, boolean p_8579_, long p_8580_, List<CustomSpawner> p_8581_, boolean p_8582_, UnitSpace parent) throws IOException {
		super(
				p_8571_,
				Util.backgroundExecutor(),
				noAccess,
				p_8574_,
				p_8575_,
				p_8576_,
				p_8577_,
				p_8578_,
				p_8579_,
				p_8580_,
				p_8581_,
				p_8582_
		);
		this.parent = parent;
		this.chunkSource = new TickerChunkCache(
				this, noAccess,
				null, getStructureManager(),
				Util.backgroundExecutor(),
				p_8578_,
				0, 0,
				true,
				p_8577_, (pPos, pStatus) -> {
		}, () -> null
		);
	}
	
	@Override
	public float getShade(Direction pDirection, boolean pShade) {
		return parent.level.getShade(pDirection, pShade);
	}
	
	@Override
	public boolean isOutsideBuildHeight(int pY) {
		return false;
	}
	
	@Override
	public LevelChunk getChunkAt(BlockPos pPos) {
		int pX = SectionPos.blockToSectionCoord(pPos.getX());
//		int pY = SectionPos.blockToSectionCoord(pPos.getY());
		int pY = 0;
		int pZ = SectionPos.blockToSectionCoord(pPos.getZ());
		ChunkAccess chunkaccess = ((TickerChunkCache) this.getChunkSource()).getChunk(pX, pY, pZ, ChunkStatus.FULL, true);
		return (LevelChunk) chunkaccess;
	}
	
	@Override
	public BlockState getBlockState(BlockPos pPos) {
		return getChunkAt(pPos).getBlockState(new BlockPos(pPos.getX(), pPos.getY(), pPos.getZ()));
	}
	
	@Override
	public LevelChunk getChunk(int pChunkX, int pChunkZ) {
		return super.getChunk(pChunkX, pChunkZ);
	}
	
	@Nullable
	@Override
	public ChunkAccess getChunk(int pX, int pZ, ChunkStatus pRequiredStatus, boolean pNonnull) {
		return super.getChunk(pX, pZ, pRequiredStatus, pNonnull);
	}
	
	public boolean setBlock(BlockPos pPos, BlockState pNewState, int pFlags) {
		return this.setBlock(pPos, pNewState, pFlags, 512);
	}
	
	public boolean setBlock(BlockPos pPos, BlockState pState, int pFlags, int pRecursionLeft) {
		if (this.isOutsideBuildHeight(pPos)) {
			return false;
		} else if (!this.isClientSide && this.isDebug()) {
			return false;
		} else {
			LevelChunk levelchunk = this.getChunkAt(pPos);
			Block block = pState.getBlock();
			
			pPos = pPos.immutable(); // Forge - prevent mutable BlockPos leaks
			net.minecraftforge.common.util.BlockSnapshot blockSnapshot = null;
			if (this.captureBlockSnapshots && !this.isClientSide) {
				blockSnapshot = net.minecraftforge.common.util.BlockSnapshot.create(this.dimension(), this, pPos, pFlags);
				this.capturedBlockSnapshots.add(blockSnapshot);
			}
			
			BlockState old = getBlockState(pPos);
			int oldLight = old.getLightEmission(this, pPos);
			int oldOpacity = old.getLightBlock(this, pPos);
			
			BlockState blockstate = levelchunk.setBlockState(pPos, pState, (pFlags & 64) != 0);
			if (blockstate == null) {
				if (blockSnapshot != null) this.capturedBlockSnapshots.remove(blockSnapshot);
				return false;
			} else {
				BlockState blockstate1 = this.getBlockState(pPos);
				if ((pFlags & 128) == 0 && blockstate1 != blockstate && (blockstate1.getLightBlock(this, pPos) != oldOpacity || blockstate1.getLightEmission(this, pPos) != oldLight || blockstate1.useShapeForLightOcclusion() || blockstate.useShapeForLightOcclusion())) {
					this.getProfiler().push("queueCheckLight");
					this.getChunkSource().getLightEngine().checkBlock(pPos);
					this.getProfiler().pop();
				}
				
				if (blockSnapshot == null) { // Don't notify clients or update physics while capturing blockstates
					this.markAndNotifyBlock(pPos, levelchunk, blockstate, pState, pFlags, pRecursionLeft);
				}
				
				return true;
			}
		}
	}
}
