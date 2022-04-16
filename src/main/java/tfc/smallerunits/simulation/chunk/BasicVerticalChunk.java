package tfc.smallerunits.simulation.chunk;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;
import tfc.smallerunits.UnitSpaceBlock;
import tfc.smallerunits.client.tracking.SUCapableChunk;
import tfc.smallerunits.simulation.block.ParentLookup;
import tfc.smallerunits.simulation.world.server.TickerServerWorld;
import tfc.smallerunits.utils.math.Math1D;

import java.util.ArrayList;

public class BasicVerticalChunk extends LevelChunk {
	private final BlockState[] blocks = new BlockState[16 * 16 * 16];
	public final int yPos;
	// holds the functional chunk and a method which gets the corresponding BasicVerticalChunk from an integer representing which vertical chunk
	// quite basic... weird to word however
	private final VChunkLookup verticalLookup;
	public final ArrayList<BlockPos> updated = new ArrayList<>();
	public final ArrayList<BlockPos> besRemoved = new ArrayList<>();
	public ArrayList<BlockEntity> beChanges = new ArrayList<>();
	ParentLookup lookup;
	private int upb;
	
	public final int getIndx(int x, int y, int z) {
		// truthfully, this is x, z, y order, but it really does not matter at all
		return x + (((y * 16) + z) * 16);
	}
	
	public boolean isLoaded() {
		for (BlockState block : blocks) if (block != null) return true;
		return false;
	}
	
	protected BlockState getBlockState$(BlockPos pos) {
		// locals would be redundant, this is an internal method
		// this method assumes that pos.y will always be in bounds of the specific BasicVerticalChunk
		BlockState state = blocks[getIndx(pos.getX() & 15, pos.getY(), pos.getZ() & 15)];
		if (state == null) state = Blocks.AIR.defaultBlockState();
		return state;
	}
	
	public BasicVerticalChunk(Level pLevel, ChunkPos pPos, int y, VChunkLookup verticalLookup, ParentLookup lookup, int upb) {
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
		this.lookup = lookup;
		this.upb = upb;
		setLoaded(true);
		// TODO: use mixin to null out unnecessary fields, maybe
	}
	
	@Override
	public boolean isTicking(BlockPos pPos) {
		// TODO:
		return true;
	}
	
	@Nullable
	@Override
	public BlockState setBlockState(BlockPos pos, BlockState pState, boolean pIsMoving) {
		int yO = Math1D.getChunkOffset(pos.getY(), 16);
		if (yO != 0) {
			if (yPos + yO < 0) return Blocks.VOID_AIR.defaultBlockState();
			BasicVerticalChunk chunk = verticalLookup.apply(yPos + yO);
			return chunk.setBlockState$(new BlockPos(pos.getX(), pos.getY() & 15, pos.getZ()), pState, pIsMoving);
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
			pPos = pPos.above(yPos * 16);
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
					updated.add(pPos.below(yPos * 16));
					return blockstate;
				}
			}
		}
	}
	
	@Override
	public BlockState getBlockState(BlockPos pos) {
		boolean lookupPass = false;
		BlockState parentState = lookup.getState(pos);
		if (parentState.isAir() || parentState.getBlock() instanceof UnitSpaceBlock) lookupPass = true;
		if (lookupPass) {
			int yO = Math1D.getChunkOffset(pos.getY(), 16);
			if (yO != 0) {
				if (yPos + yO < 0) return Blocks.VOID_AIR.defaultBlockState();
				BasicVerticalChunk chunk = verticalLookup.apply(yPos + yO);
				return chunk.getBlockState$(new BlockPos(pos.getX(), pos.getY() & 15, pos.getZ()));
			}
			return getBlockState$(pos);
		} else {
			return Blocks.BEDROCK.defaultBlockState();
		}
	}
	
	@Override
	public FluidState getFluidState(BlockPos pos) {
		return getBlockState(pos).getFluidState();
	}
	
	private void setBlockFast$(BlockPos pos, BlockState state) {
		int j = pos.getX() & 15;
		int k = pos.getY();
		int l = pos.getZ() & 15;
		int indx = getIndx(j, k, l);
		blocks[indx] = state;
		if (level.isClientSide) {
			if (state.hasBlockEntity()) {
				BlockEntity be = ((EntityBlock) state.getBlock()).newBlockEntity(new BlockPos(j, k, l).offset(this.chunkPos.getWorldPosition().getX(), this.yPos * 16, this.chunkPos.getWorldPosition().getZ()), state);
				if (be == null) return;
				setBlockEntity(be);
				
				BlockPos rp = ((TickerServerWorld) level).region.pos.toBlockPos();
				int xo = (pos.getX() / ((TickerServerWorld) level).getUnitsPerBlock());
				int yo = (pos.getY() / ((TickerServerWorld) level).getUnitsPerBlock());
				int zo = (pos.getZ() / ((TickerServerWorld) level).getUnitsPerBlock());
				BlockPos parentPos = rp.offset(xo, yo, zo);
				ChunkAccess ac = ((TickerServerWorld) level).parent.getChunkAt(parentPos);
				ac.setBlockState(parentPos, tfc.smallerunits.Registry.UNIT_SPACE.get().defaultBlockState(), false);
				((SUCapableChunk) ac).addTile(be);
			}
		}
	}
	
	public void setBlockFast(BlockPos pos, BlockState state) {
		int yO = Math1D.getChunkOffset(pos.getY(), 16);
		if (yO != 0) {
			BasicVerticalChunk chunk = verticalLookup.apply(yPos + yO);
			chunk.setBlockFast$(new BlockPos(pos.getX(), pos.getY() & 15, pos.getZ()), state);
//			if (level.isClientSide) {
//				if (state.hasBlockEntity()) {
//					BlockEntity be;
////					setBlockEntity(be = ((EntityBlock) state.getBlock()).newBlockEntity(pos, state));
//					setBlockEntity(be = ((EntityBlock) state.getBlock()).newBlockEntity(pos.offset(this.chunkPos.getWorldPosition().getX(), this.yPos * 16, this.chunkPos.getWorldPosition().getZ()), state));
//
//					BlockPos rp = ((TickerServerWorld) level).region.pos.toBlockPos();
//					int xo = (pos.getX() / ((TickerServerWorld) level).getUnitsPerBlock());
//					int yo = (pos.getY() / ((TickerServerWorld) level).getUnitsPerBlock());
//					int zo = (pos.getZ() / ((TickerServerWorld) level).getUnitsPerBlock());
//					BlockPos parentPos = rp.offset(xo, yo, zo);
//					ChunkAccess ac = ((TickerServerWorld) level).parent.getChunkAt(parentPos);
//					ac.setBlockState(parentPos, tfc.smallerunits.Registry.UNIT_SPACE.get().defaultBlockState(), false);
//					((SUCapableChunk) ac).addTile(be);
//				}
//			}
			return;
		}
		setBlockFast$(new BlockPos(pos), state);
	}
	
	@Override
	public void setBlockEntity(BlockEntity pBlockEntity) {
		if (pBlockEntity == null) return;
		if (!level.isClientSide) {
			if (besRemoved.contains(pBlockEntity.getBlockPos())) {
				besRemoved.remove(pBlockEntity.getBlockPos());
			}
			for (BlockEntity beChange : beChanges) {
				if (beChange.getBlockPos().equals(pBlockEntity.getBlockPos())) {
					beChanges.remove(beChange);
					break;
				}
			}
			beChanges.add(pBlockEntity);
//			BlockPos rp = ((TickerServerWorld) level).region.pos.toBlockPos();
//			BlockPos pos = pBlockEntity.getBlockPos();
//			int xo = (pos.getX() / upb);
//			int yo = (pos.getY() / upb);
//			int zo = (pos.getZ() / upb);
//			BlockPos parentPos = rp.offset(xo, yo, zo);
//			ChunkAccess ac = ((TickerServerWorld) level).parent.getChunkAt(parentPos);
//			ac.setBlockState(parentPos, tfc.smallerunits.Registry.UNIT_SPACE.get().defaultBlockState(), false);
//			((SUCapableChunk) ac).getTiles().add(pBlockEntity);
		}
		super.setBlockEntity(pBlockEntity);
	}
	
	@Override
	public void removeBlockEntity(BlockPos pPos) {
		if (!level.isClientSide) {
			besRemoved.add(pPos);
			for (BlockEntity beChange : beChanges) {
				if (beChange.getBlockPos().equals(pPos)) {
					beChanges.remove(beChange);
					break;
				}
			}
		}
		super.removeBlockEntity(pPos);
	}
	
	public void randomTick() {
		for (int k = 0; k < level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING); ++k) {
			BlockPos blockpos1 = level.getBlockRandomPos(0, 0, 0, 15);
			BlockState blockstate = this.getBlockState(blockpos1);
			BlockPos wp = blockpos1.offset(chunkPos.getWorldPosition()).relative(Direction.UP, yPos * 16 - 1);
			if (blockstate.isRandomlyTicking()) {
				if (!level.isClientSide() && level instanceof ServerLevel) {
					blockstate.randomTick((ServerLevel) level, wp.above(), level.random);
				}
			}
			
			FluidState fluidstate = blockstate.getFluidState();
			if (fluidstate.isRandomlyTicking()) {
				fluidstate.randomTick(level, wp, level.random);
			}
		}
	}
	
	public BasicVerticalChunk getSubChunk(int cy) {
		return verticalLookup.apply(cy);
	}
}
