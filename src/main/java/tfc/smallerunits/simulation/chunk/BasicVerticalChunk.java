package tfc.smallerunits.simulation.chunk;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;
import tfc.smallerunits.Registry;
import tfc.smallerunits.UnitEdge;
import tfc.smallerunits.UnitSpace;
import tfc.smallerunits.UnitSpaceBlock;
import tfc.smallerunits.api.PositionUtils;
import tfc.smallerunits.client.access.tracking.FastCapabilityHandler;
import tfc.smallerunits.client.access.tracking.SUCapableChunk;
import tfc.smallerunits.data.access.ChunkAccessor;
import tfc.smallerunits.data.capability.ISUCapability;
import tfc.smallerunits.data.capability.SUCapabilityManager;
import tfc.smallerunits.logging.Loggers;
import tfc.smallerunits.networking.PacketTarget;
import tfc.smallerunits.networking.hackery.NetworkingHacks;
import tfc.smallerunits.simulation.block.ParentLookup;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.simulation.level.UnitChunkHolder;
import tfc.smallerunits.simulation.level.server.TickerServerLevel;
import tfc.smallerunits.utils.math.Math1D;
import tfc.smallerunits.utils.platform.PlatformUtils;
import tfc.smallerunits.utils.threading.ThreadLocals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static tfc.smallerunits.simulation.WorldStitcher.chunkRelative;

public class BasicVerticalChunk extends LevelChunk {
	public final int yPos;
	// holds the functional chunk and a method which gets the corresponding BasicVerticalChunk from an integer representing which vertical chunk
	// quite basic... weird to word however
	private final VChunkLookup verticalLookup;
	public final ArrayList<BlockPos> updated = new ArrayList<>();
	public final ArrayList<BlockPos> besRemoved = new ArrayList<>();
	public ArrayList<BlockEntity> beChanges = new ArrayList<>();
	ParentLookup lookup;
	private final int upb;
	public UnitChunkHolder holder = null;
	
	LevelChunkSection section;
	
	public boolean isLoaded() {
		return !section.hasOnlyAir();
	}
	
	public BlockState getBlockState$(BlockPos pos) {
		// locals would be redundant, this is an internal method
		// this method assumes that pos.y will always be in bounds of the specific BasicVerticalChunk
		if (section.hasOnlyAir()) return Blocks.AIR.defaultBlockState(); // simple optimization, can do a fair amount
		return section.getBlockState(pos.getX() & 15, pos.getY(), pos.getZ() & 15);
	}
	
	public BasicVerticalChunk(Level pLevel, ChunkPos pPos, int y, VChunkLookup verticalLookup, ParentLookup lookup, int upb) {
		super(pLevel, pPos);
		this.yPos = y;
		this.verticalLookup = verticalLookup;
		this.lookup = lookup;
		this.upb = upb;
		setLoaded(true);
		
		section = super.getSection(0);
		((ChunkAccessor) this).setSectionArray(new LevelChunkSection[]{section});
		// TODO: use mixin to null out unnecessary fields, maybe
	}
	
	@Override
	public boolean isTicking(BlockPos pPos) {
		// TODO:
		BlockPos origin = new BlockPos(chunkPos.getMinBlockX(), yPos * 16, chunkPos.getMinBlockZ());
		BlockPos.MutableBlockPos pos = ThreadLocals.posLocal.get();
		Level parent = ((ITickerLevel) level).getParent();
		
		if (parent == null) return false;
		
		PositionUtils.getParentPos(pPos, this, pos);
		return parent.isLoaded(pos);
	}
	
	@Override
	public LevelChunkSection getSection(int p_187657_) {
		if (p_187657_ == yPos) return section;
		int yO = chunkRelative(p_187657_, upb) + p_187657_;
		return verticalLookup.applyAbs(p_187657_).getSection(yO);
	}
	
	public LevelChunkSection getSectionNullable(int sectionIndex) {
		if (sectionIndex == yPos) return section;
		int yO = chunkRelative(sectionIndex, upb) + sectionIndex;
		LevelChunk chunk = verticalLookup.applyAbsNoLoad(sectionIndex);
		if (chunk == null) return null;
		return chunk.getSection(yO);
	}
	
	@Override
	public int getSectionsCount() {
		return 3;
	}
	
	@Override
	public int getMinSection() {
		return yPos - 1;
	}
	
	@Override
	public int getMaxSection() {
		return yPos + 1;
	}
	
	@Override
	public int getMinBuildHeight() {
		if (yPos == 0) return 0;
		return (yPos - 1) * 16;
	}
	
	@Override
	public int getMaxBuildHeight() {
		return (yPos + 1) * 16;
	}
	
	@Override
	public int getSectionIndex(int pY) {
//		return this.getSectionIndexFromSectionY(SectionPos.blockToSectionCoord(pY));
		return pY >> 4;
	}
	
	@Override
	public int getSectionIndexFromSectionY(int pSectionIndex) {
		return level.getSectionIndexFromSectionY(pSectionIndex);
//		return pSectionIndex >> 4;
	}
	
	@Override
	public int getSectionYFromSectionIndex(int pSectionIndex) {
//		return super.getSectionYFromSectionIndex(pSectionIndex);
		return pSectionIndex << 4;
	}
	
	@Nullable
	@Override
	public BlockState setBlockState(BlockPos pos, BlockState pState, boolean pIsMoving) {
		int yO = Math1D.getChunkOffset(pos.getY(), 16);
		if (yO != 0 || pos.getX() < 0 || pos.getZ() < 0 || pos.getX() >= (upb * 32) || pos.getZ() >= (upb * 32)) {
			// TODO: non-grid aligned world stitching?
			
			BasicVerticalChunk chunk = verticalLookup.apply(yPos + yO);
			if (chunk == null) {
				return Blocks.VOID_AIR.defaultBlockState();
			}
			if (chunk.holder != null)
				chunk.holder.setBlockDirty(new BlockPos(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15));
			return chunk.setBlockState$(new BlockPos(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15), pState, pIsMoving);
		}
		if (holder != null)
			holder.setBlockDirty(pos);
		return setBlockState$(pos, pState, pIsMoving);
	}
	
	@Override
	public BlockEntity getBlockEntity(BlockPos pos, LevelChunk.EntityCreationType pCreationType) {
		int yO = Math1D.getChunkOffset(pos.getY(), 16);
		if (yO != 0 || pos.getX() < 0 || pos.getZ() < 0 || pos.getX() >= (upb * 32) || pos.getZ() >= (upb * 32)) {
			// TODO: non-grid aligned world stitching?
			
			BasicVerticalChunk chunk = verticalLookup.applyNoLoad(yPos + yO);
			if (chunk == null)
				return null;
			return chunk.getBlockEntity$(new BlockPos(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15), pCreationType);
		}
		return getBlockEntity$(new BlockPos(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15), pCreationType);
	}
	
	@Override
	public void setBlockEntity(BlockEntity pBlockEntity) {
		BlockPos pos = pBlockEntity.getBlockPos();
		pos = new BlockPos(pos.getX(), pos.getY() - (yPos * 16), pos.getZ());
		int yO = Math1D.getChunkOffset(pos.getY(), 16);
//		pos = new BlockPos(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
		if (yO != 0 || pos.getX() < 0 || pos.getZ() < 0 || pos.getX() >= (upb * 32) || pos.getZ() >= (upb * 32)) {
			// TODO: non-grid aligned world stitching?
			
			BasicVerticalChunk chunk = verticalLookup.apply(yPos + yO);
			if (chunk == null)
				return;
			chunk.setBlockEntity$(pBlockEntity);
			return;
		}
		setBlockEntity$(pBlockEntity);
	}
	
	@Override
	public void removeBlockEntity(BlockPos pos) {
		int yO = Math1D.getChunkOffset(pos.getY(), 16);
		if (yO != 0 || pos.getX() < 0 || pos.getZ() < 0 || pos.getX() >= (upb * 32) || pos.getZ() >= (upb * 32)) {
			// TODO: non-grid aligned world stitching?
			
			BasicVerticalChunk chunk = verticalLookup.apply(yPos + yO);
			if (chunk == null)
				return;
			chunk.removeBlockEntity$(new BlockPos(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15));
			return;
		}
		removeBlockEntity$(pos);
	}
	
	@Override
	public void addAndRegisterBlockEntity(BlockEntity pBlockEntity) {
		BlockPos pos = pBlockEntity.getBlockPos();
		pos = new BlockPos(pos.getX(), pos.getY() - (yPos * 16), pos.getZ());
		int yO = Math1D.getChunkOffset(pos.getY(), 16);
		if (yO != 0 || pos.getX() < 0 || pos.getZ() < 0 || pos.getX() >= (upb * 32) || pos.getZ() >= (upb * 32)) {
			// TODO: non-grid aligned world stitching?
			
			BasicVerticalChunk chunk = verticalLookup.apply(yO);
			if (chunk == null)
				return;
			chunk.addBlockEntity$(new BlockPos(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15), pBlockEntity);
			return;
		}
		addBlockEntity$(pos, pBlockEntity);
	}
	
	// TODO: I'd like to not override this actually
	public BlockEntity getBlockEntity$(BlockPos pPos, LevelChunk.EntityCreationType pCreationType) {
		BlockEntity blockentity = super.getBlockEntity(pPos, EntityCreationType.CHECK);
		
		if (blockentity == null) {
			if (pCreationType == LevelChunk.EntityCreationType.IMMEDIATE) {
				blockentity = this.createBlockEntity(pPos);
				if (blockentity != null) {
					this.addBlockEntity$(pPos, blockentity);
				}
			}
		}
		
		return blockentity;
	}
	
	public BlockEntity createBlockEntity(BlockPos pPos) {
		BlockState blockstate = this.getBlockState(pPos);
		return !blockstate.hasBlockEntity() ? null : ((EntityBlock)blockstate.getBlock()).newBlockEntity(pPos.offset(0, this.yPos * 16, 0), blockstate);
	}
	
	public void setBlockEntity$(BlockEntity pBlockEntity) {
		BlockPos blockpos = pBlockEntity.getBlockPos();
		blockpos = new BlockPos(blockpos.getX() & 15, blockpos.getY() & 15, blockpos.getZ() & 15);
		if (this.getBlockStateSmallOnly(blockpos).hasBlockEntity()) {
			pBlockEntity.worldPosition = blockpos.offset(chunkPos.getMinBlockX(), yPos * 16, chunkPos.getMinBlockZ());
			pBlockEntity.setLevel(this.level);
			pBlockEntity.clearRemoved();
			BlockEntity blockentity = this.blockEntities.put(blockpos, pBlockEntity);
			if (blockentity != null && blockentity != pBlockEntity) {
				blockentity.setRemoved();
			}
		}
		
		if (!level.isClientSide) return;
		
		ITickerLevel tickerWorld = ((ITickerLevel) level);
		
		BlockPos parentPos = PositionUtils.getParentPos(blockpos, this, ThreadLocals.posLocal.get());
		ChunkAccess ac;
		ac = tickerWorld.getParent().getChunkAt(parentPos);
		
		// TODO: check if a renderer exists, or smth?
		((SUCapableChunk) ac).addTile(pBlockEntity);
	}
	
	public void removeBlockEntity$(BlockPos pPos) {
		pPos = new BlockPos(pPos.getX() & 15, pPos.getY() & 15, pPos.getZ() & 15);
		if (level.isClientSide) {
			ITickerLevel tickerWorld = ((ITickerLevel) level);
			
			BlockPos parentPos = PositionUtils.getParentPos(pPos, this, ThreadLocals.posLocal.get());
			ChunkAccess ac;
			ac = tickerWorld.getParent().getChunkAt(parentPos);
			
			BlockPos offsetPos = pPos.offset(chunkPos.getMinBlockX(), yPos * 16, chunkPos.getMinBlockZ());
			
			ArrayList<BlockEntity> toRemove = new ArrayList<>();
			synchronized (((SUCapableChunk) ac).getTiles()) {
				for (BlockEntity tile : ((SUCapableChunk) ac).getTiles()) {
					if (tile.getBlockPos().equals(offsetPos)) {
						toRemove.add(tile);
					}
				}
				((SUCapableChunk) ac).getTiles().removeAll(toRemove);
			}
		}
		super.removeBlockEntity(pPos);
	}
	
	public void removeBlockEntityTicker(BlockPos pPos) {
		if (yPos != 0)
			verticalLookup.applyAbs(0).removeBlockEntityTicker(new BlockPos(pPos.getX(), pPos.getY() + yPos * 16, pPos.getZ()));
		else super.removeBlockEntityTicker(chunkPos.getWorldPosition().offset(pPos));
	}
	
	public void addBlockEntity$(BlockPos pos, BlockEntity pBlockEntity) {
		this.setBlockEntity$(pBlockEntity);
		if (isLoaded() || level.isClientSide) {
			super.addGameEventListener(pBlockEntity);
			this.updateBlockEntityTicker(pBlockEntity);
		}
	}
	
	@Override
	public <T extends BlockEntity> TickingBlockEntity createTicker(T pBlockEntity, BlockEntityTicker<T> pTicker) {
		if (yPos == 0)
			return super.createTicker(pBlockEntity, pTicker);
		return verticalLookup.applyAbs(0).createTicker(pBlockEntity, pTicker);
//		return super.createTicker(pBlockEntity, pTicker);
	}
	
	@Override
	public <T extends BlockEntity> void updateBlockEntityTicker(T pBlockEntity) {
		if (yPos == 0) super.updateBlockEntityTicker(pBlockEntity);
		else verticalLookup.applyAbs(0).updateBlockEntityTicker(pBlockEntity);
//		super.updateBlockEntityTicker(pBlockEntity);
	}
	
	// TODO: optimize?
	public BlockState setBlockState$(BlockPos pPos, BlockState pState, boolean pIsMoving) {
		if (level.isClientSide) return setBlockState$$(pPos, pState, pIsMoving);
		
		int j = pPos.getX() & 15;
		int k = pPos.getY();
		int l = pPos.getZ() & 15;
		
		BlockPos parentPos = PositionUtils.getParentPosPrecise(pPos, this);
		LevelChunk ac = ((ITickerLevel) level).getParent().getChunkAt(parentPos);
		UnitSpace space = null;
		BlockState oldState = section.getBlockState(j, k, l);
		if (ac instanceof FastCapabilityHandler capabilityHandler) {
			space = capabilityHandler.getSUCapability().getUnit(parentPos);
			if (space == null) {
				BlockState state = ac.getBlockState(parentPos);
				if (state.isAir()) { // TODO: do this better
					if (!pState.isAir()) {
						ac.setBlockState(parentPos, tfc.smallerunits.Registry.UNIT_SPACE.get().defaultBlockState(), false);
						ac.getLevel().sendBlockUpdated(parentPos, state, Registry.UNIT_SPACE.get().defaultBlockState(), 0);
						space = capabilityHandler.getSUCapability().getOrMakeUnit(parentPos);
						// TODO: debug why space can still be null after this or what
						space.isNatural = true;
						space.setUpb(((ITickerLevel) level).getUPB());
						space.sendSync(PacketTarget.trackingChunk(ac));
					}
				}
			}
		}
		BlockState output = setBlockState$$(pPos, pState, pIsMoving);
		if (ac instanceof FastCapabilityHandler capabilityHandler) {
			ac.setUnsaved(true);
			if (space != null) {
				space.removeState(oldState);
				space.addState(pState);
				if (space.isEmpty() && space.isNatural) {
					space.clear();
					NetworkingHacks.LevelDescriptor descriptor = NetworkingHacks.unitPos.get();
					NetworkingHacks.unitPos.remove();
					ac.setBlockState(parentPos, Blocks.AIR.defaultBlockState(), false);
					BlockState state = ac.getBlockState(parentPos);
					ac.getLevel().sendBlockUpdated(parentPos, state, Registry.UNIT_SPACE.get().defaultBlockState(), 0);
					capabilityHandler.getSUCapability().removeUnit(parentPos);
					if (descriptor != null)
						NetworkingHacks.setPos(descriptor);
				}
			}
		}
		return output;
	}
	
	// TODO: I'm sure I can shrink this
	public BlockState setBlockState$$(BlockPos pPos, BlockState pState, boolean pIsMoving) {
		boolean flag = section.hasOnlyAir();
		int j = pPos.getX() & 15;
		int k = pPos.getY();
		int l = pPos.getZ() & 15;
		// TODO
		if (flag && pState.isAir()) {
			return null;
		} else {
			pPos = pPos.above(yPos * 16);
			BlockState blockstate = section.setBlockState(j, k, l, pState);
			if (blockstate == pState) {
				return null;
			} else {
				BlockPos offsetPos = chunkPos.getWorldPosition().offset(pPos.getX(), pPos.getY() & 15, pPos.getZ()).offset(0, yPos * 16, 0);
				
				Block block = pState.getBlock();
				this.heightmaps.get(Heightmap.Types.MOTION_BLOCKING).update(j, k, l, pState);
				this.heightmaps.get(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES).update(j, k, l, pState);
				this.heightmaps.get(Heightmap.Types.OCEAN_FLOOR).update(j, k, l, pState);
				this.heightmaps.get(Heightmap.Types.WORLD_SURFACE).update(j, k, l, pState);
				
				boolean flag2 = blockstate.hasBlockEntity();
				if (!this.level.isClientSide) {
					blockstate.onRemove(this.level, offsetPos, pState, pIsMoving);
				} else if ((!blockstate.is(block) || !pState.hasBlockEntity()) && flag2) {
					this.removeBlockEntity(new BlockPos(pPos.getX(), pPos.getY() & 15, pPos.getZ()));
				}
				
				if (!section.getBlockState(j, k, l).is(block)) {
					return null;
				} else {
					if (!this.level.isClientSide && !PlatformUtils.shouldCaptureBlockSnapshots(this.level)) {
						pState.onPlace(this.level, offsetPos, blockstate, pIsMoving);
					}
					
					if (pState.hasBlockEntity()) {
						BlockPos realPos = pPos.offset(chunkPos.getWorldPosition());
						BlockEntity blockentity = this.getBlockEntity(new BlockPos(pPos.getX(), pPos.getY() & 15, pPos.getZ()), LevelChunk.EntityCreationType.CHECK);
						if (blockentity == null) {
							blockentity = ((EntityBlock) block).newBlockEntity(realPos, pState);
							if (blockentity != null) {
								this.addAndRegisterBlockEntity(blockentity);
							}
						} else {
							blockentity.setBlockState(pState);
							this.updateBlockEntityTicker(blockentity);
						}
					}
					
					boolean flag1 = section.hasOnlyAir();
					if (!flag1) level.getLightEngine().checkBlock(offsetPos);
					
					updated.add(pPos.below(yPos * 16));
					setUnsaved(true);
					return blockstate;
				}
			}
		}
	}
	
	@Override
	public void setUnsaved(boolean pUnsaved) {
		if (pUnsaved) {
			if (level instanceof TickerServerLevel) {
				((TickerServerLevel) level).saveWorld.markForSave(this);
			}
		}
		super.setUnsaved(pUnsaved);
	}
	
	@Override
	public BlockState getBlockState(BlockPos pos) {
		boolean lookupPass = false;
		
		BlockPos parentPos = PositionUtils.getParentPos(pos, this, ThreadLocals.posLocal.get());
		BlockState parentState = lookup.getState(parentPos);
		if (parentState.isAir()) lookupPass = true;
		
		boolean transparent = true;
		Level lvl = ((ITickerLevel) level).getParent();
		if (parentState.isCollisionShapeFullBlock(lvl, parentPos))
			transparent = false;
		if (parentState.getBlock() instanceof UnitSpaceBlock) {
			LevelChunk chunk = lvl.getChunkAt(parentPos);
			if (chunk instanceof EmptyLevelChunk) {
				lookupPass = false;
			} else {
				ISUCapability capability = SUCapabilityManager.getCapability(chunk);
				UnitSpace space = capability.getUnit(parentPos);
				if (space != null) {
					lookupPass = space.unitsPerBlock == upb;
				} else {
					lookupPass = false;
				}
			}
		}
		
		if (lookupPass) {
			int yO = Math1D.getChunkOffset(pos.getY(), 16);
			if (yO != 0 || pos.getX() < 0 || pos.getZ() < 0 || pos.getX() >= (upb * 32) || pos.getZ() >= (upb * 32)) {
				BasicVerticalChunk chunk = verticalLookup.applyAbsNoLoad(yPos + yO);
				if (chunk == null)
					return Blocks.VOID_AIR.defaultBlockState();
				return chunk.getBlockState$(new BlockPos(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15));
			}
			return getBlockState$(pos);
		} else {
			return Registry.UNIT_EDGE.get().defaultBlockState().setValue(UnitEdge.TRANSPARENT, transparent);
		}
	}
	
	public BlockState getBlockStateSmallOnly(BlockPos pos) {
		int yO = Math1D.getChunkOffset(pos.getY(), 16);
		if (yO != 0 || pos.getX() < 0 || pos.getZ() < 0 || pos.getX() >= (upb * 32) || pos.getZ() >= (upb * 32)) {
			BasicVerticalChunk chunk = verticalLookup.applyAbsNoLoad(yPos + yO);
			if (chunk == null)
				return Blocks.VOID_AIR.defaultBlockState();
			return chunk.getBlockState$(new BlockPos(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15));
		}
		return getBlockState$(pos);
	}
	
	@Override
	public FluidState getFluidState(BlockPos pos) {
		int yO = Math1D.getChunkOffset(pos.getY(), 16);
		if (yO != 0 || pos.getX() < 0 || pos.getZ() < 0 || pos.getX() >= (upb * 32) || pos.getZ() >= (upb * 32)) {
			BasicVerticalChunk chunk = verticalLookup.applyAbsNoLoad(yPos + yO);
			if (chunk == null || !chunk.isLoaded())
				return Fluids.EMPTY.defaultFluidState();
			return chunk.getBlockState$(new BlockPos(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15)).getFluidState();
		}
		if (section.hasOnlyAir()) return Fluids.EMPTY.defaultFluidState();
		return getBlockState$(pos).getFluidState();
	}
	
	private void setBlockFast$(BlockPos pos, BlockState state, HashMap<SectionPos, ChunkAccess> chunkCache) {
		int j = pos.getX() & 15;
		int k = pos.getY();
		int l = pos.getZ() & 15;
		BlockPos parentPos = PositionUtils.getParentPos(pos, this, ThreadLocals.posLocal.get());
		
		SectionPos pPosAsSectionPos = SectionPos.of(parentPos);
		ChunkAccess ac = chunkCache.get(pPosAsSectionPos);
		if (ac == null) {
			ac = ((ITickerLevel) level).getParent().getChunkAt(parentPos);
			chunkCache.put(pPosAsSectionPos, ac);
		}
		
		if (!state.isAir()) {
			ISUCapability capability = SUCapabilityManager.getCapability(((ITickerLevel) level).getParent(), ac);
			UnitSpace space = capability.getOrMakeUnit(parentPos);
			if (space != null) {
				space.removeState(section.getBlockState(j, k, l));
				space.addState(state);
				
				section.setBlockState(j, k, l, state);
			} else {
				Loggers.SU_LOGGER.warn("Unit space @" + parentPos + " did not exist");
			}
			
			level.getLightEngine().checkBlock(chunkPos.getWorldPosition().offset(pos).offset(0, yPos * 16, 0));
		} else {
			ISUCapability capability = SUCapabilityManager.getCapability(((ITickerLevel) level).getParent(), ac);
			UnitSpace space = capability.getUnit(parentPos);
			if (space != null) {
				space.removeState(section.getBlockState(j, k, l));
				
				section.setBlockState(j, k, l, state);
			} else {
				Loggers.SU_LOGGER.warn("Unit space @" + parentPos + " did not exist");
			}
		}
	}
	
	public void setBlockFast(BlockPos pos, BlockState state, HashMap<SectionPos, ChunkAccess> chunkCache) {
		int yO = Math1D.getChunkOffset(pos.getY(), 16);
		if (yO != 0) {
			BasicVerticalChunk chunk = verticalLookup.apply(yPos + yO);
			chunk.setBlockFast$(new BlockPos(pos.getX(), pos.getY() & 15, pos.getZ()), state, chunkCache);
			return;
		}
		setBlockFast$(new BlockPos(pos), state, chunkCache);
	}
	
	ArrayList<ServerPlayer> oldPlayersTracking = new ArrayList<>();
	
	ArrayList<ServerPlayer> playersTracking = new ArrayList<>();
	
	public void randomTick() {
		if (section.hasOnlyAir())
			return;
		
		for (int k = 0; k < ((ITickerLevel) level).randomTickCount(); ++k) {
			BlockPos blockpos1 = level.getBlockRandomPos(0, 0, 0, 15);
			BlockState blockstate = this.getBlockState$(blockpos1);
			BlockPos wp = blockpos1.offset(chunkPos.getWorldPosition()).relative(Direction.UP, yPos * 16 - 1);
			if (blockstate.isRandomlyTicking()) {
				if (!level.isClientSide() && level instanceof ServerLevel) { // TODO: ?
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
	
	public boolean isTrackedBy(ServerPlayer player) {
		return oldPlayersTracking.contains(player);
	}
	
	public void setTracked(ServerPlayer player) {
		playersTracking.add(player);
		if (oldPlayersTracking.contains(player)) oldPlayersTracking.remove(player);
	}
	
	public void swapTracks() {
		oldPlayersTracking = playersTracking;
		playersTracking = new ArrayList<>();
	}
	
	public List<ServerPlayer> getPlayersTracking() {
		return oldPlayersTracking;
	}
	
	@Override
	// TODO: do this more properly
	public FluidState getFluidState(int pX, int pY, int pZ) {
		return getBlockState(new BlockPos(pX, pY, pZ)).getFluidState();
	}
	
	long modTime = 0;
	
	public void updateModificationTime(long gameTime) {
		if (gameTime == -1) modTime = -1;
		// staggers save time
//		this.modTime = gameTime + new Random().nextInt(700) + 300;
//		this.modTime = gameTime + new Random().nextInt(300) + 200;
		this.modTime = gameTime + 1;
	}
	
	public boolean isSaveTime(long gameTime) {
		if (modTime == -1) return false;
		// TODO:
		return gameTime >= modTime;
	}
	
	public void maybeUnload() {
		if (!level.isClientSide) {
			if (!(level instanceof TickerServerLevel))
				return;
		} else return;
		BlockPos origin = new BlockPos(chunkPos.getMinBlockX(), yPos * 16, chunkPos.getMinBlockZ());
		BlockPos.MutableBlockPos pos = ThreadLocals.posLocal.get();
		Level parent = ((ITickerLevel) level).getParent();
		try {
			if (parent == null) {
				((TickerServerLevel) level).saveWorld.saveChunk(this);
				return;
			}
			boolean anyLoaded = false;
			lx:
			for (int x = 0; x <= 1; x++) {
				for (int y = 0; y <= 1; y++) {
					for (int z = 0; z <= 1; z++) {
						// TODO: check?
						BlockPos test = origin.offset(x * 15, y * 15, z * 15);
						PositionUtils.getParentPos(test, this, pos);
						if (parent.isLoaded(pos)) {
							anyLoaded = true;
							break lx;
						}
					}
				}
			}
			
			if (!anyLoaded) {
				if (isUnsaved()) {
					((TickerServerLevel) level).saveWorld.saveChunk(this);
					((TickerServerLevel) level).unload(this);
				}
			}
		} catch (Throwable ignored) {
			ignored.printStackTrace();
		}
		// TODO:
	}
	
	public SectionPos getSectionPos() {
		return SectionPos.of(getPos(), yPos);
	}
}
