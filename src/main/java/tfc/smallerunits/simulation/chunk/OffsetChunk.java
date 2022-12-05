//package tfc.smallerunits.simulation.chunk;
//
//import net.minecraft.core.BlockPos;
//import net.minecraft.core.Direction;
//import net.minecraft.world.level.ChunkPos;
//import net.minecraft.world.level.Level;
//import net.minecraft.world.level.block.Block;
//import net.minecraft.world.level.block.entity.BlockEntity;
//import net.minecraft.world.level.block.state.BlockState;
//import net.minecraft.world.level.chunk.LevelChunk;
//import net.minecraft.world.level.gameevent.GameEventDispatcher;
//import net.minecraft.world.level.material.Fluid;
//import net.minecraft.world.level.material.FluidState;
//import net.minecraft.world.ticks.TickContainerAccess;
//import net.minecraftforge.common.capabilities.Capability;
//import net.minecraftforge.common.util.LazyOptional;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//public class OffsetChunk extends LevelChunk {
//	LevelChunk actualChunk;
//
//	public OffsetChunk(Level pLevel, ChunkPos pPos, int yOff, LevelChunk actualChunk) {
//		super(pLevel, pPos);
//	}
//
//	int yOff;
//
//	@Override
//	public FluidState getFluidState(BlockPos pPos) {
//		return actualChunk.getFluidState(pPos.offset(0, yOff, 0));
//	}
//
//	@Nullable
//	@Override
//	public BlockState setBlockState(BlockPos pPos, BlockState pState, boolean pIsMoving) {
//		return actualChunk.setBlockState(pPos.offset(0, yOff, 0), pState, pIsMoving);
//	}
//
//	@Override
//	public boolean isEmpty() {
//		return actualChunk.isEmpty();
//	}
//
//	@Override
//	public void setUnsaved(boolean pUnsaved) {
//		actualChunk.setUnsaved(pUnsaved);
//	}
//
//	@Override
//	public boolean isUnsaved() {
//		return actualChunk.isUnsaved();
//	}
//
//	@Override
//	public void markPosForPostprocessing(BlockPos pPos) {
//		actualChunk.markPosForPostprocessing(pPos.offset(0, yOff, 0));
//	}
//
//	@Override
//	public BlockState getBlockState(BlockPos pPos) {
//		return actualChunk.getBlockState(pPos.offset(0, yOff, 0));
//	}
//
//	@Nullable
//	@Override
//	public BlockEntity getBlockEntity(BlockPos pPos, EntityCreationType pCreationType) {
//		return actualChunk.getBlockEntity(pPos.offset(0, yOff, 0), pCreationType);
//	}
//
//	@Override
//	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
//		return actualChunk.getCapability(cap, side);
//	}
//
//	@Override
//	public TickContainerAccess<Block> getBlockTicks() {
//		return super.getBlockTicks(); // TODO: offset tick list
//	}
//
//	@Override
//	public TickContainerAccess<Fluid> getFluidTicks() {
//		return super.getFluidTicks(); // TODO: offset tick list
//	}
//
//	@Override
//	public TicksToSave getTicksForSerialization() {
//		return super.getTicksForSerialization(); // TODO: offset tick list
//	}
//
//	@Override
//	public GameEventDispatcher getEventDispatcher(int pSectionY) {
//		return super.getEventDispatcher(pSectionY); // TODO:
//	}
//
//	@Override
//	public FluidState getFluidState(int pX, int pY, int pZ) {
//		return actualChunk.getFluidState(pX, pY - yOff, pZ);
//	}
//
//	@Override
//	public <T extends BlockEntity> void updateBlockEntityTicker(T pBlockEntity) {
//		actualChunk.updateBlockEntityTicker(pBlockEntity);
//	}
//
//	@Override
//	public void setBlockEntity(BlockEntity pBlockEntity) {
//		pBlockEntity.worldPosition = pBlockEntity.worldPosition.offset(0, yOff, 0);
//		actualChunk.setBlockEntity(pBlockEntity);
//	}
//
//
//}
