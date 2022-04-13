package tfc.smallerunits;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.*;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import tfc.smallerunits.client.tracking.SUCapableChunk;
import tfc.smallerunits.data.capability.ISUCapability;
import tfc.smallerunits.data.capability.SUCapabilityManager;
import tfc.smallerunits.networking.SUNetworkRegistry;
import tfc.smallerunits.networking.sync.RemoveUnitPacket;
import tfc.smallerunits.utils.selection.UnitBox;
import tfc.smallerunits.utils.selection.UnitHitResult;
import tfc.smallerunits.utils.selection.UnitShape;

import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class UnitSpaceBlock extends Block implements EntityBlock {
	public UnitSpaceBlock() {
		super(
				Properties.of(Material.STONE, MaterialColor.COLOR_BLACK)
						.isSuffocating((a, b, c) -> false)
						.isViewBlocking((a, b, c) -> false)
						.explosionResistance(0)
						.dynamicShape()
		);
	}
	
	@Override
	public float getSpeedFactor() {
		return super.getSpeedFactor();
	}
	
	@Override
	public void destroy(LevelAccessor pLevel, BlockPos pPos, BlockState pState) {
//		super.destroy(pLevel, pPos, pState);
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
//		if (!(pLevel instanceof Level)) return super.getShape(pState, pLevel, pPos, pContext);
		if (!(pLevel instanceof Level)) return Shapes.empty();
		if (pContext instanceof EntityCollisionContext) {
			Entity entity = ((EntityCollisionContext) pContext).getEntity();
//			if (entity == null) return super.getShape(pState, pLevel, pPos, pContext);
			if (entity == null) return Shapes.empty();
			
			ChunkAccess access = ((Level) pLevel).getChunk(pPos);
			ISUCapability capability = SUCapabilityManager.getCapability((Level) pLevel, access);
			UnitSpace space = capability.getUnit(pPos);
			// if unit space is null, assume syncing is still occurring
			if (space == null) return super.getShape(pState, pLevel, pPos, pContext);
			
			Camera camera = Minecraft.getInstance().getEntityRenderDispatcher().camera;
			
			//// I'm stupid, why did I do it any way other than this?
			//// lol
			//// ah right, server side
			// Vec3 startVec = camera.getPosition();
			// Vec3 lookVec = new Vec3(camera.getLookVector());
			Vec3 startVec = ((EntityCollisionContext) pContext).getEntity().getEyePosition(1);
			Vec3 lookVec = ((EntityCollisionContext) pContext).getEntity().getLookAngle();
			double reach;
			if (entity instanceof LivingEntity) {
				AttributeInstance instance = ((LivingEntity) entity).getAttribute(ForgeMod.REACH_DISTANCE.get());
				if (instance == null) reach = 6;
				else reach = instance.getValue();
			} else reach = 6;
			lookVec = lookVec.scale(reach);
			startVec = startVec.subtract(pPos.getX(), pPos.getY(), pPos.getZ());
			Vec3 endVec = startVec.add(lookVec);
			UnitShape shape = new UnitShape();
			
			final Vec3 fStartVec = startVec;
			
			double upbDouble = space.unitsPerBlock;
			collectShape((pos) -> {
				int x = pos.getX();
				int y = pos.getY();
				int z = pos.getZ();
				AABB box = new AABB(
						x / upbDouble, y / upbDouble, z / upbDouble,
						(x + 1) / upbDouble, (y + 1) / upbDouble, (z + 1) / upbDouble
				);
				return box.contains(fStartVec) || box.clip(fStartVec, endVec).isPresent();
			}, (pos, state) -> {
				int x = pos.getX();
				int y = pos.getY();
				int z = pos.getZ();
				VoxelShape sp = state.getShape(space.myLevel, space.getOffsetPos(pos));
				for (AABB toAabb : sp.toAabbs()) {
					toAabb = toAabb.move(x, y, z);
					UnitBox b = new UnitBox(
							toAabb.minX / upbDouble,
							toAabb.minY / upbDouble,
							toAabb.minZ / upbDouble,
							toAabb.maxX / upbDouble,
							toAabb.maxY / upbDouble,
							toAabb.maxZ / upbDouble,
							new BlockPos(x, y, z)
					);
					shape.addBox(b);
				}
			}, space);

//			float yLook = entity.xRotO;
//			VoxelShape shape = Shapes.empty();
//			if (yLook > 0) shape = Shapes.or(shape, Shapes.box(0, -0.0001, 0, 1, 0, 1));
//			else shape = Shapes.or(shape, Shapes.box(0, 1, 0, 1, 1.0001, 1));
//			float xLook = entity.yRotO;
//			xLook %= 360;
//			xLook -= 180;
//			if (xLook < 0 && xLook > -180) shape = Shapes.or(shape, Shapes.box(-0.0001, 0, 0, 0, 1, 1));
//			else shape = Shapes.or(shape, Shapes.box(1, 0, 0, 1.0001, 1, 1));
//			if (xLook < 90 && xLook > -90) shape = Shapes.or(shape, Shapes.box(0, 0, -0.0001, 1, 1, 0));
//			else shape = Shapes.or(shape, Shapes.box(0, 0, 1, 1, 1, 1.0001));
//			float yLook = entity.xRotO;
			
			for (Direction value : Direction.values()) {
				BlockState offset = pLevel.getBlockState(pPos.relative(value));
				if (!offset.isAir()) {
					if (offset.getBlock() instanceof UnitSpaceBlock) continue;
					VoxelShape shape1 = offset.getShape(pLevel, pPos.relative(value), pContext);
					if (shape1.isEmpty()) continue;
					shape1 = shape1.move(value.getStepX(), value.getStepY(), value.getStepZ());
					for (int xo = 0; xo < space.unitsPerBlock; xo++) {
						for (int zo = 0; zo < space.unitsPerBlock; zo++) {
							double x;
							double y;
							double z;
							if (value.equals(Direction.WEST) || value.equals(Direction.EAST)) {
								x = value.equals(Direction.EAST) ? (space.unitsPerBlock - 0.999) : -0.001;
								y = xo;
								z = zo;
							} else if (value.equals(Direction.UP) || value.equals(Direction.DOWN)) {
								x = xo;
								y = value.equals(Direction.UP) ? (space.unitsPerBlock - 0.999) : -0.001;
								z = zo;
							} else {
								x = xo;
								y = zo;
								z = value.equals(Direction.SOUTH) ? (space.unitsPerBlock - 0.999) : -0.001;
							}
							AABB box = new AABB(
									x / upbDouble, y / upbDouble, z / upbDouble,
									(x + 1) / upbDouble, (y + 1) / upbDouble, (z + 1) / upbDouble
							);
							// less expensive than voxel shape computations
							if (box.contains(fStartVec) || box.clip(fStartVec, endVec).isPresent()) {
								if (value.getStepX() == 1) x += 1;
								else if (value.getStepY() == 1) y += 1;
								else if (value.getStepZ() == 1) z += 1;
								BlockPos pos = new BlockPos(x, y, z);
								VoxelShape shape2 = Shapes.joinUnoptimized(shape1, Shapes.create(box), BooleanOp.AND);
								if (shape2.isEmpty()) continue;
								for (AABB toAabb : shape2.toAabbs()) {
									shape.addBox(new UnitBox(
											toAabb.minX, toAabb.minY, toAabb.minZ,
											toAabb.maxX, toAabb.maxY, toAabb.maxZ,
											pos
									));
								}
							}
						}
					}
				}
			}
			
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
			if (space == null) return super.getShape(pState, pLevel, pPos, pContext);
			double upbDouble = space.unitsPerBlock;
			UnitShape shape = new UnitShape();
			collectShape((pos) -> {
				// TODO:
				return true;
			}, (pos, state) -> {
				int x = pos.getX();
				int y = pos.getY();
				int z = pos.getZ();
				VoxelShape sp = state.getCollisionShape(space.myLevel, space.getOffsetPos(pos));
				for (AABB toAabb : sp.toAabbs()) {
					toAabb = toAabb.move(x, y, z);
					UnitBox b = new UnitBox(
							toAabb.minX / upbDouble,
							toAabb.minY / upbDouble,
							toAabb.minZ / upbDouble,
							toAabb.maxX / upbDouble,
							toAabb.maxY / upbDouble,
							toAabb.maxZ / upbDouble,
							new BlockPos(x, y, z)
					);
					shape.addBox(b);
				}
			}, space);
			return shape;
		}
		return Shapes.empty();
	}
	
	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
		return super.getCloneItemStack(state, target, level, pos, player);
	}
	
	public void collectShape(Function<BlockPos, Boolean> simpleChecker, BiConsumer<BlockPos, BlockState> boxFiller, UnitSpace space) {
		int upbInt = space.unitsPerBlock;
		
		for (int x = 0; x < upbInt; x++) {
			for (int y = 0; y < upbInt; y++) {
				for (int z = 0; z < upbInt; z++) {
					BlockState state = space.getBlock(x, y, z);
					if (state.isAir()) continue;
//					if (state == null) continue;
					if (simpleChecker.apply(new BlockPos(x, y, z))) {
						boxFiller.accept(new BlockPos(x, y, z), state);
					}
					// TODO: raytrace simple box
				}
			}
		}
	}
	
	@Override
	public VoxelShape getOcclusionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
		return Shapes.empty();
	}
	
	@Override
	public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
//		super.onPlace(pState, pLevel, pPos, pOldState, pIsMoving);
		ChunkAccess chunk = pLevel.getChunk(pPos);
		if (chunk instanceof LevelChunk) {
			ISUCapability capability = SUCapabilityManager.getCapability((LevelChunk) chunk);
			capability.makeUnit(pPos);
			chunk.setUnsaved(true);
			pLevel.scheduleTick(pPos, this, 1);
		}
	}
	
	// I might wind up needing a tile entity for sake of setting up world capabilities
	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		return null;
	}
	
	@Override
	public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
		if (pHit instanceof UnitHitResult) {
			BlockPos pos = ((UnitHitResult) pHit).geetBlockPos();
			LevelChunk chnk = pLevel.getChunkAt(pPos);
			UnitSpace space = SUCapabilityManager.getCapability(chnk).getUnit(pPos);
			ItemStack itm = pPlayer.getItemInHand(pHand);
			if (itm.getItem() instanceof BlockItem) {
//				space.setState(pos.relative(pHit.getDirection()), ((BlockItem) itm.getItem()).getBlock());
				itm.useOn(
						new UseOnContext(
								space.myLevel, pPlayer, pHand,
								pPlayer.getItemInHand(pHand),
								new BlockHitResult(
										pHit.getLocation(),
										pHit.getDirection(),
										space.getOffsetPos(pos), pHit.isInside()
								)
						)
				);
//				((SUCapableChunk) chnk).markDirty(pPos);
//				chnk.setUnsaved(true);
			}
			space.getBlock(pos.getX(), pos.getY(), pos.getZ()).use(
					space.myLevel, pPlayer, pHand, new BlockHitResult(
							pHit.getLocation(),
							pHit.getDirection(),
							space.getOffsetPos(pos), pHit.isInside()
					));
			((SUCapableChunk) chnk).SU$markDirty(pPos);
			chnk.setUnsaved(true);
			return InteractionResult.SUCCESS;
		}
		return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
	}
	
	@Override
	public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRandom) {
		pLevel.scheduleTick(pPos, this, 1);
		LevelChunk chnk = pLevel.getChunkAt(pPos);
		UnitSpace space = SUCapabilityManager.getCapability(chnk).getUnit(pPos);
		if (space == null) return;
		space.tick();
		super.tick(pState, pLevel, pPos, pRandom);
	}
	
	@Override
	public void playerDestroy(Level pLevel, Player pPlayer, BlockPos pPos, BlockState pState, @Nullable BlockEntity pBlockEntity, ItemStack pTool) {
		HitResult result = null;
		if (FMLEnvironment.dist.isClient())
//			if (pLevel.isClientSide)
			result = Minecraft.getInstance().hitResult;
//		super.playerDestroy(pLevel, pPlayer, pPos, pState, pBlockEntity, pTool);
		if (result instanceof UnitHitResult) {
			LevelChunk chnk = pLevel.getChunkAt(pPos);
			UnitSpace space = SUCapabilityManager.getCapability(chnk).getUnit(pPos);
			BlockPos pos = ((UnitHitResult) result).geetBlockPos();
			space.setState(pos, Blocks.AIR);
		}
	}
	
	@Override
	public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
		LevelChunk chnk = pLevel.getChunkAt(pPos);
		UnitSpace space = SUCapabilityManager.getCapability(chnk).getUnit(pPos);
		space.clear();
		RemoveUnitPacket pckt = new RemoveUnitPacket(pPos, space.unitsPerBlock);
		SUNetworkRegistry.NETWORK_INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> pLevel.getChunkAt(pPos)), pckt);
		super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
	}
}
