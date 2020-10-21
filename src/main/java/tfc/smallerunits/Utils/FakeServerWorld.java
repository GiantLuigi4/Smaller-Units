package tfc.smallerunits.Utils;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.profiler.IProfiler;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.server.ServerMultiWorld;
import net.minecraft.world.server.ServerTickList;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.SessionLockException;
import net.minecraft.world.storage.WorldInfo;

import javax.annotation.Nullable;
import java.io.File;
import java.util.concurrent.Executor;
import java.util.function.IntSupplier;

public class FakeServerWorld extends ServerMultiWorld {
	FakeWorld owner;
	
	public FakeServerWorld(ServerWorld realWorld, FakeWorld owner) {
		super(realWorld, realWorld.getServer(), (p) -> {
		}, new SaveHandler(null, "", null, realWorld.getServer().getDataFixer()), DimensionType.OVERWORLD, realWorld.getProfiler(), new IChunkStatusListener() {
			@Override
			public void start(ChunkPos center) {
			
			}
			
			@Override
			public void statusChanged(ChunkPos chunkPosition, @Nullable ChunkStatus newStatus) {
			
			}
			
			@Override
			public void stop() {
			
			}
		});
		this.owner = owner;
	}
	
	@Override
	public Chunk getChunkAt(BlockPos pos) {
		return owner.getChunkAt(pos);
	}
	
	@Override
	public Chunk getChunk(int chunkX, int chunkZ) {
		return owner.getChunkAt(new BlockPos(chunkX, 0, chunkZ));
	}
	
	@Override
	public IChunk getChunk(int x, int z, ChunkStatus requiredStatus, boolean nonnull) {
		return owner.getChunk(new BlockPos(x, 0, z));
	}
	
	@Override
	public boolean setBlockState(BlockPos pos, BlockState newState, int flags) {
		return owner.setBlockState(pos, newState, flags);
	}
	
	@Override
	public void markAndNotifyBlock(BlockPos pos, @Nullable Chunk chunk, BlockState blockstate, BlockState newState, int flags) {
		owner.markAndNotifyBlock(pos, chunk, blockstate, newState, flags);
	}
	
	@Override
	public boolean removeBlock(BlockPos pos, boolean isMoving) {
		return owner.removeBlock(pos, isMoving);
	}
	
	@Override
	public boolean destroyBlock(BlockPos p_225521_1_, boolean p_225521_2_, @Nullable Entity p_225521_3_) {
		return owner.destroyBlock(p_225521_1_, p_225521_2_, p_225521_3_);
	}
	
	@Override
	public boolean setBlockState(BlockPos pos, BlockState state) {
		return owner.setBlockState(pos, state);
	}
	
	@Override
	public void notifyNeighbors(BlockPos pos, Block blockIn) {
		owner.notifyNeighbors(pos, blockIn);
	}
	
	@Override
	public void notifyNeighborsOfStateChange(BlockPos pos, Block blockIn) {
		owner.notifyNeighborsOfStateChange(pos, blockIn);
	}
	
	@Override
	public void notifyNeighborsOfStateExcept(BlockPos pos, Block blockType, Direction skipSide) {
		owner.notifyNeighborsOfStateExcept(pos, blockType, skipSide);
	}
	
	@Override
	public void neighborChanged(BlockPos pos, Block blockIn, BlockPos fromPos) {
		owner.neighborChanged(pos, blockIn, fromPos);
	}
	
	@Override
	public BlockState getBlockState(BlockPos pos) {
		return owner.getBlockState(pos);
	}
	
	@Override
	public IFluidState getFluidState(BlockPos pos) {
		return owner.getFluidState(pos);
	}
	
	@Nullable
	@Override
	public TileEntity getTileEntity(BlockPos pos) {
		return owner.getTileEntity(pos);
	}
	
	@Override
	public void setTileEntity(BlockPos pos, @Nullable TileEntity tileEntityIn) {
		owner.setTileEntity(pos, tileEntityIn);
	}
	
	@Override
	public IChunk getChunk(BlockPos pos) {
		return owner.getChunk(pos);
	}
	
	@Override
	public IChunk getChunk(int chunkX, int chunkZ, ChunkStatus requiredStatus) {
		return owner.getChunk(chunkX, chunkZ, requiredStatus);
	}
	
	@Override
	public ServerTickList<Block> getPendingBlockTicks() {
		return super.getPendingBlockTicks();
	}
	
	@Override
	public ServerTickList<Fluid> getPendingFluidTicks() {
		return super.getPendingFluidTicks();
	}
}
