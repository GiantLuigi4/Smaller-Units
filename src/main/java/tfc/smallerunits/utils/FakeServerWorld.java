package tfc.smallerunits.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.profiler.IProfiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerMultiWorld;
import net.minecraft.world.server.ServerTickList;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;

import javax.annotation.Nullable;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;

public class FakeServerWorld extends ServerWorld {
	protected FakeWorld owner;
	
	public FakeServerWorld(MinecraftServer serverIn, Executor executorIn, SaveHandler saveHandlerIn, WorldInfo worldInfoIn, DimensionType dimType, IProfiler profilerIn, IChunkStatusListener listenerIn) {
		super(serverIn, executorIn, saveHandlerIn, worldInfoIn, dimType, profilerIn, listenerIn);
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
	public void tick(BooleanSupplier hasTimeLeft) {
		for (SmallUnit unit : owner.unitHashMap.values()) {
			this.getPendingBlockTicks().scheduleTick(
					new BlockPos(unit.x,unit.y,unit.z),unit.heldState.getBlock(),1
			);
			this.setTileEntity(new BlockPos(unit.x,unit.y,unit.z),unit.tileEntity);
			this.setBlockState(new BlockPos(unit.x,unit.y,unit.z),unit.heldState);
			unit.heldState.tick(this,new BlockPos(unit.x,unit.y,unit.z),rand);
			if (unit.tileEntity != null) {
				TileEntity te = unit.tileEntity;
				te.setWorldAndPos(this,new BlockPos(unit.x,unit.y,unit.z));
				if (te instanceof ITickableTileEntity) {
					((ITickableTileEntity) te).tick();
				}
				unit.tileEntity=te;
			}
		}
		super.tick(hasTimeLeft);
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
