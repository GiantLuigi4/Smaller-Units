package tfc.smallerunits;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import tfc.smallerunits.data.capability.ISUCapability;
import tfc.smallerunits.data.capability.SUCapabilityManager;
import tfc.smallerunits.networking.platform.PacketTarget;
import tfc.smallerunits.networking.SUNetworkRegistry;
import tfc.smallerunits.networking.sync.RemoveUnitPacketS2C;
import tfc.smallerunits.simulation.chunk.BasicVerticalChunk;
import tfc.smallerunits.utils.PositionalInfo;
import tfc.smallerunits.utils.platform.hooks.ILadderBlock;
import tfc.smallerunits.utils.platform.hooks.IScaffoldBlock;
import tfc.smallerunits.utils.selection.UnitHitResult;
import tfc.smallerunits.utils.selection.UnitShape;

public class UnitSpaceBlock extends Block implements ILadderBlock {
	public UnitSpaceBlock() {
		super(
				Properties.of(Material.STONE, MaterialColor.COLOR_BLACK)
						.isSuffocating((a, b, c) -> false)
						.isViewBlocking((a, b, c) -> false)
						.dynamicShape()
						.destroyTime(0)
						.strength(-1.0F, 3600000.0F)
		);
	}
	
	@Override
	public float getSpeedFactor() {
		return super.getSpeedFactor();
	}
	
	@Override
	public void destroy(LevelAccessor pLevel, BlockPos pPos, BlockState pState) {
		ChunkAccess chunk = pLevel.getChunk(pPos);
		if (chunk instanceof LevelChunk) {
			ISUCapability capability = SUCapabilityManager.getCapability((LevelChunk) chunk);
			capability.removeUnit(pPos);
			chunk.setUnsaved(true);
		}
	}
	
	@Override
	public RenderShape getRenderShape(BlockState pState) {
		return RenderShape.INVISIBLE;
	}
	
	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		// nothing can really be done if it's not a full level implementation
		if (!(pLevel instanceof Level)) return Shapes.empty();
		if (pContext instanceof EntityCollisionContext) {
			Entity entity = ((EntityCollisionContext) pContext).getEntity();
			if (entity == null) return Shapes.empty();
			
			ChunkAccess access = ((Level) pLevel).getChunk(pPos);
			ISUCapability capability = SUCapabilityManager.getCapability((Level) pLevel, access);
			UnitSpace space = capability.getUnit(pPos);
			if (space == null || space.myLevel == null)
				return super.getShape(pState, pLevel, pPos, pContext);
			
			UnitShape shape = new UnitShape(space, false, pContext);
			shape.setupNeigbors(pLevel, pPos);
			
			return shape;
		}
		return Shapes.empty();
	}
	
	@Override
	public VoxelShape getVisualShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		// nothing can really be done if it's not a full level implementation
		if (!(pLevel instanceof Level)) return Shapes.empty();
		if (pContext instanceof EntityCollisionContext) {
			Entity entity = ((EntityCollisionContext) pContext).getEntity();
			if (entity == null) return Shapes.empty();
			
			ChunkAccess access = ((Level) pLevel).getChunk(pPos);
			ISUCapability capability = SUCapabilityManager.getCapability((Level) pLevel, access);
			UnitSpace space = capability.getUnit(pPos);
			if (space == null || space.myLevel == null)
				return super.getShape(pState, pLevel, pPos, pContext);
			
			UnitShape shape = new UnitShape(space, true, pContext);
			shape.setupNeigbors(pLevel, pPos);
			
			return shape;
		}
		return Shapes.empty();
	}
	
	// if I could template stuff, I would
	@Override
	public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		if (!(pLevel instanceof Level)) return Shapes.empty();
		if (pContext instanceof EntityCollisionContext) {
			Entity entity = ((EntityCollisionContext) pContext).getEntity();
			if (entity == null) return Shapes.empty();
			ChunkAccess access = ((Level) pLevel).getChunk(pPos);
			ISUCapability capability = SUCapabilityManager.getCapability((Level) pLevel, access);
			UnitSpace space = capability.getUnit(pPos);
			if (space == null || space.myLevel == null)
				return super.getShape(pState, pLevel, pPos, pContext);
			
			UnitShape shape = new UnitShape(space, true, pContext);
			return shape;
		}
		return Shapes.empty();
	}
	
	@Override
	public VoxelShape getOcclusionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
		return Shapes.empty();
	}
	
	@Override
	public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
		ChunkAccess chunk = pLevel.getChunk(pPos);
		if (chunk instanceof LevelChunk asLevelChunk) {
			ISUCapability capability = SUCapabilityManager.getCapability(asLevelChunk);
			UnitSpace unit = capability.getUnit(pPos);
			pLevel.scheduleTick(pPos, this, 1);
			if (unit == null) {
				unit = capability.getOrMakeUnit(pPos);
				chunk.setUnsaved(true);
				unit.sendSync(PacketTarget.trackingChunk((LevelChunk) chunk));
			}
		}
	}
	
	@Override
	public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
		LevelChunk chnk = pLevel.getChunkAt(pPos);
		UnitSpace space = SUCapabilityManager.getCapability(chnk).getUnit(pPos);
		if (space != null && !space.isEmpty())
			space.clear();
		SUCapabilityManager.getCapability(chnk).removeUnit(pPos);
		RemoveUnitPacketS2C pckt = new RemoveUnitPacketS2C(pPos, space == null ? 4 : space.unitsPerBlock);
		SUNetworkRegistry.send(PacketTarget.trackingChunk(pLevel.getChunkAt(pPos)), pckt);
	}
	
	// the *proper* parameters
	public void destroy(BlockState blockState, Level lvl, BlockPos blockPos, Player player, InteractionHand mainHand, UnitHitResult result) {
//		LevelChunk chnk = lvl.getChunkAt(blockPos);
//		UnitSpace space = SUCapabilityManager.getCapability(chnk).getUnit(blockPos);
//		BlockPos pos = result.geetBlockPos();
//		space.setState(pos, Blocks.AIR);
	}
	
	@Override
	public boolean isLadder(BlockState state, LevelReader pLevel, BlockPos pPos, LivingEntity entity) {
		if (pLevel instanceof Level level) {
			LevelChunk chnk = level.getChunkAt(pPos);
			UnitSpace space = SUCapabilityManager.getCapability(chnk).getUnit(pPos);
			if (space == null) return false;
			if (space.myLevel == null) return false;
			
			PositionalInfo info = new PositionalInfo(entity, false);
			info.adjust(entity, space);
			if (entity instanceof Player player)
				info.scalePlayerReach(player, space.unitsPerBlock);
			
			AABB scaledBox = entity.getBoundingBox();
			
			BlockPos bp = space.getOffsetPos(new BlockPos(0, 0, 0));
			
			int minX = (int) (scaledBox.minX);
			minX = Math.max(bp.getX(), minX);
			int minY = (int) (scaledBox.minY);
			minY = Math.max(bp.getY(), minY);
			int minZ = (int) (scaledBox.minZ);
			minZ = Math.max(bp.getZ(), minZ);
			int maxX = (int) Math.floor(scaledBox.maxX);
			maxX = Math.min(bp.getX() + space.unitsPerBlock, maxX);
			int maxY = (int) Math.floor(scaledBox.maxY);
			maxY = Math.min(bp.getY() + space.unitsPerBlock, maxY);
			int maxZ = (int) Math.floor(scaledBox.maxZ);
			maxZ = Math.min(bp.getZ() + space.unitsPerBlock, maxZ);
			
			Level smallWorld = space.myLevel;
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
			double motY = entity.getDeltaMovement().y;
			double d2 = Math.max(motY, -0.15F);
			BlockPos posOn = entity.getOnPos();
			BlockState blockOn = smallWorld.getBlockState(posOn);
			BlockState feetState = entity.getFeetBlockState();
			Vec3 position = entity.getPosition(0);
			BlockPos legPos = new BlockPos(position.x, position.y + 0.15, position.z);
			BlockState legState = smallWorld.getBlockState(legPos);
			boolean onScaffold = IScaffoldBlock.isBlockAScaffold(blockOn, smallWorld, posOn, entity);
			boolean inScaffold = IScaffoldBlock.isBlockAScaffold(feetState, smallWorld, entity.blockPosition(), entity);
			boolean legScaffold = IScaffoldBlock.isBlockAScaffold(legState, smallWorld, legPos, entity);
			
			Vec3 center = scaledBox.getCenter();
			
			for (int x = minX; x <= maxX; x++) {
				for (int z = minZ; z <= maxZ; z++) {
					int pX = SectionPos.blockToSectionCoord(x);
					int pZ = SectionPos.blockToSectionCoord(z);
					BasicVerticalChunk chunk = (BasicVerticalChunk) smallWorld.getChunk(pX, pZ, ChunkStatus.FULL, true);
					if (chunk == null) {
						if (z == (z >> 4) << 4) {
							z += 15;
						} else {
							z = ((z >> 4) << 4) + 15;
						}
						continue;
					}
					
					for (int y = minY; y <= maxY; y++) {
						int sectionIndex = chunk.getSectionIndex(y);
						LevelChunkSection section = chunk.getSectionNullable(sectionIndex);
						if (section == null || section.hasOnlyAir()) {
							if (y == (y >> 4) << 4) {
								y += 15;
							} else {
								y = ((y >> 4) << 4) + 15;
							}
							continue;
						}
						
						if (center.x > x + 1) continue;
						if (scaledBox.minY > y + 1) continue;
						if (center.z > z + 1) continue;
						if (center.x < x) continue;
						if (center.y < y) continue;
						if (center.z < z) continue;
						
						mutableBlockPos.set(x, y, z);
						
						BlockState state1 = chunk.getBlockState(mutableBlockPos);
						if (ILadderBlock.isBlockALadder(state1, smallWorld, mutableBlockPos.immutable(), entity)) {
							if (onScaffold) {
								if (entity.jumping) {
									info.reset(entity);
									return true;
								}
								if (!inScaffold || legScaffold) {
									if (d2 < 0 && entity.isSuppressingSlidingDownLadder() && entity instanceof Player) {
										// TODO: do this better
										Vec3 mot = entity.getDeltaMovement();
										entity.setDeltaMovement(new Vec3(mot.x, d2, mot.z));
										entity.resetFallDistance();
										continue;
									}
								}
							}
							info.reset(entity);
							return true;
						}
					}
				}
			}
			
			info.reset(entity);
		}
		return false;
	}
}
