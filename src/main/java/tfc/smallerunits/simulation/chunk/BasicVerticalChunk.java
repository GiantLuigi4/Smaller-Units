package tfc.smallerunits.simulation.chunk;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;
import tfc.smallerunits.utils.math.Math1D;

public class BasicVerticalChunk extends LevelChunk {
	private final BlockState[] blocks = new BlockState[16 * 16 * 16];
	private final int yPos;
	// holds the functional chunk and a method which gets the corresponding BasicVerticalChunk from an integer representing which vertical chunk
	// quite basic... weird to word however
	private final VChunkLookup verticalLookup;
	
	public BasicVerticalChunk(Level pLevel, ChunkPos pPos, int y, VChunkLookup verticalLookup) {
		super(pLevel, pPos);
		this.yPos = y;
		this.verticalLookup = verticalLookup;
		// this actually shouldn't cause any extra memory overhead, it just removes a bunch of null check
		// afaik,
		// sizeof((long) nullptr) == 8
		// sizeof((long) Pointer<BlockState>) == 8
		// in java, you're never really dealing with objects, only pointers
		// however, it will likely slow down world loading a bit
		for (int i = 0; i < blocks.length; i++) blocks[i] = Blocks.AIR.defaultBlockState();
		// TODO: use mixin to null out unnecessary fields, maybe
	}
	
	public final int getIndx(int x, int y, int z) {
		// truthfully, this is x, z, y order, but it really does not matter at all
		return x + (((y * 16) + z) * 16);
	}
	
	protected BlockState getBlockState$(BlockPos pos) {
		// locals would be redundant, this is an internal method
		// this method assumes that pos.y will always be in bounds of the specific BasicVerticalChunk
		return blocks[getIndx(Math1D.chunkMod(pos.getX(), 16), pos.getY(), Math1D.chunkMod(pos.getZ(), 16))];
	}
	
	@Override
	public BlockState getBlockState(BlockPos pos) {
		int yO = Math1D.getChunkOffset(pos.getY(), 16);
		if (yO != 0) {
			if (yPos + yO < 0) return Blocks.VOID_AIR.defaultBlockState();
			BasicVerticalChunk chunk = verticalLookup.apply(yPos + yO);
			return chunk.getBlockState$(new BlockPos(pos.getX(), Math1D.chunkMod(pos.getY(), 16), pos.getZ()));
		}
		return getBlockState$(pos);
	}
	
	@Nullable
	@Override
	public BlockState setBlockState(BlockPos pos, BlockState pState, boolean pIsMoving) {
		int yO = Math1D.getChunkOffset(pos.getY(), 16);
		if (yO != 0) {
			if (yPos + yO < 0) return Blocks.VOID_AIR.defaultBlockState();
			BasicVerticalChunk chunk = verticalLookup.apply(yPos + yO);
			return chunk.setBlockState$(new BlockPos(pos.getX(), Math1D.chunkMod(pos.getY(), 16), pos.getZ()), pState, pIsMoving);
		}
		return setBlockState$(pos, pState, pIsMoving);
	}
	
	public BlockState setBlockState$(BlockPos pPos, BlockState pState, boolean pIsMoving) {
//		boolean flag = levelchunksection.hasOnlyAir();
		// TODO
		boolean flag = false;
		if (flag && pState.isAir()) {
			return null;
		} else {
			int j = pPos.getX() & 15;
			int k = pPos.getY();
			int l = pPos.getZ() & 15;
			int indx = getIndx(j, k, l);
//			BlockState blockstate = levelchunksection.setBlockState(j, k, l, pState);
			BlockState blockstate = blocks[indx];
			if (blockstate == pState) {
				return null;
			} else {
				blocks[indx] = pState;
				Block block = pState.getBlock();
				this.heightmaps.get(Heightmap.Types.MOTION_BLOCKING).update(j, k, l, pState);
				this.heightmaps.get(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES).update(j, k, l, pState);
				this.heightmaps.get(Heightmap.Types.OCEAN_FLOOR).update(j, k, l, pState);
				this.heightmaps.get(Heightmap.Types.WORLD_SURFACE).update(j, k, l, pState);
//				boolean flag1 = levelchunksection.hasOnlyAir();
				// TODO
				boolean flag1 = false;
				if (flag != flag1) {
					this.level.getChunkSource().getLightEngine().updateSectionStatus(pPos, flag1);
				}
				
				boolean flag2 = blockstate.hasBlockEntity();
				if (!this.level.isClientSide) {
					blockstate.onRemove(this.level, pPos, pState, pIsMoving);
				} else if ((!blockstate.is(block) || !pState.hasBlockEntity()) && flag2) {
					this.removeBlockEntity(pPos);
				}
				
				if (!blocks[indx].is(block)) {
					return null;
				} else {
					if (!this.level.isClientSide && !this.level.captureBlockSnapshots) {
						pState.onPlace(this.level, pPos, blockstate, pIsMoving);
					}
					
					if (pState.hasBlockEntity()) {
						BlockEntity blockentity = this.getBlockEntity(pPos, LevelChunk.EntityCreationType.CHECK);
						if (blockentity == null) {
							blockentity = ((EntityBlock) block).newBlockEntity(pPos, pState);
							if (blockentity != null) {
								this.addAndRegisterBlockEntity(blockentity);
							}
						} else {
							blockentity.setBlockState(pState);
							this.updateBlockEntityTicker(blockentity);
						}
					}
					
					this.unsaved = true;
					return blockstate;
				}
			}
		}
	}
	
	public void setBlockFast(BlockPos pos, BlockState state) {
		int yO = Math1D.getChunkOffset(pos.getY(), 16);
		if (yO != 0) {
			BasicVerticalChunk chunk = verticalLookup.apply(yPos + yO);
			chunk.setBlockFast(new BlockPos(pos.getX(), Math1D.chunkMod(pos.getY(), 16), pos.getZ()), state);
			return;
		}
		int j = pos.getX() & 15;
		int k = pos.getY();
		int l = pos.getZ() & 15;
		int indx = getIndx(j, k, l);
		blocks[indx] = state;
	}
	
	public void randomTick() {
		for (int k = 0; k < 3; ++k) {
			BlockPos blockpos1 = level.getBlockRandomPos(0, 0, 0, 15);
			BlockState blockstate = this.getBlockState(blockpos1);
			if (blockstate.isRandomlyTicking()) {
				blockstate.randomTick((ServerLevel) level, blockpos1, level.random);
			}
			
			FluidState fluidstate = blockstate.getFluidState();
			if (fluidstate.isRandomlyTicking()) {
				fluidstate.randomTick(level, blockpos1, level.random);
			}
		}
	}
}
