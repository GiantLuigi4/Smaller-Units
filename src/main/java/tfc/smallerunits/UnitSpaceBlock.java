package tfc.smallerunits;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import tfc.smallerunits.data.capability.ISUCapability;
import tfc.smallerunits.data.capability.SUCapabilityManager;
import tfc.smallerunits.networking.SUNetworkRegistry;
import tfc.smallerunits.networking.hackery.NetworkingHacks;
import tfc.smallerunits.networking.sync.RemoveUnitPacket;
import tfc.smallerunits.simulation.world.ITickerWorld;
import tfc.smallerunits.utils.IHateTheDistCleaner;
import tfc.smallerunits.utils.PositionalInfo;
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
			
			//// I'm stupid, why did I do it any way other than this?
			//// lol
			//// ah right, server side
			Vec3 startVec;
			Vec3 lookVec;
			// this should work
			if (FMLEnvironment.dist.isClient() && IHateTheDistCleaner.isClientLevel((Level) pLevel)) {
				IHateTheDistCleaner.updateCamera();
				if (IHateTheDistCleaner.isCameraPresent()) {
					startVec = IHateTheDistCleaner.getCameraPos();
					lookVec = new Vec3(IHateTheDistCleaner.getCameraLook());
				} else {
					startVec = ((EntityCollisionContext) pContext).getEntity().getEyePosition(Minecraft.getInstance().getFrameTime());
					lookVec = ((EntityCollisionContext) pContext).getEntity().getViewVector(Minecraft.getInstance().getFrameTime());
				}
			} else {
				startVec = ((EntityCollisionContext) pContext).getEntity().getEyePosition(Minecraft.getInstance().getFrameTime());
				lookVec = ((EntityCollisionContext) pContext).getEntity().getViewVector(Minecraft.getInstance().getFrameTime());
			}
			double reach;
			if (entity instanceof LivingEntity) {
				AttributeInstance instance = ((LivingEntity) entity).getAttribute(ForgeMod.REACH_DISTANCE.get());
				if (instance == null) reach = 6;
				else reach = instance.getValue();
			} else reach = 6;
			lookVec = lookVec.scale(reach);
			startVec = startVec.subtract(pPos.getX(), pPos.getY(), pPos.getZ());
			Vec3 endVec = startVec.add(lookVec);
			UnitShape shape = new UnitShape(space, false);
			
			double upbDouble = space.unitsPerBlock;
			final Vec3 fStartVec = startVec;
			
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
			if (space == null || space.myLevel == null) return super.getShape(pState, pLevel, pPos, pContext);
			double upbDouble = space.unitsPerBlock;
			UnitShape shape = new UnitShape(space, true);
//			if (pLevel instanceof ServerLevel) {
//				if (((EntityCollisionContext) pContext).getEntity() instanceof ServerPlayer) {
//					collectShape((pos) -> {
//						// TODO:
//						return true;
//					}, (pos, state) -> {
//						int x = pos.getX();
//						int y = pos.getY();
//						int z = pos.getZ();
//						VoxelShape sp = state.getCollisionShape(space.myLevel, space.getOffsetPos(pos));
//						for (AABB toAabb : sp.toAabbs()) {
//							toAabb = toAabb.move(x, y, z);
//							UnitBox b = new UnitBox(
//									toAabb.minX / upbDouble,
//									toAabb.minY / upbDouble,
//									toAabb.minZ / upbDouble,
//									toAabb.maxX / upbDouble,
//									toAabb.maxY / upbDouble,
//									toAabb.maxZ / upbDouble,
//									new BlockPos(x, y, z)
//							);
//							shape.addBox(b);
//						}
//					}, space);
//				}
//			}
			
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
		if (chunk instanceof LevelChunk asLevelChunk) {
			ISUCapability capability = SUCapabilityManager.getCapability(asLevelChunk);
			UnitSpace unit = capability.getOrMakeUnit(pPos);
			chunk.setUnsaved(true);
			pLevel.scheduleTick(pPos, this, 1);
			unit.sendSync(PacketDistributor.TRACKING_CHUNK.with(() -> asLevelChunk));
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
		if (pLevel.isClientSide) {
			if (pHit instanceof UnitHitResult) {
				BlockPos pos = ((UnitHitResult) pHit).geetBlockPos();
				
				
				LevelChunk chnk = pLevel.getChunkAt(pPos);
				UnitSpace space = SUCapabilityManager.getCapability(chnk).getUnit(pPos);
				ItemStack itm = pPlayer.getItemInHand(pHand);
				
				NetworkingHacks.unitPos.set(new NetworkingHacks.LevelDescriptor(((ITickerWorld) space.myLevel).getRegion().pos, space.unitsPerBlock));

//				AABB srcBB = pPlayer.getBoundingBox();
//				ClientLevel trueLvl = (ClientLevel) pPlayer.getLevel();
//				Vec3 trueVec = new Vec3(pPlayer.getX(), pPlayer.getY(), pPlayer.getZ());
//				double oldEyeHeight = pPlayer.getEyeHeight();
				PositionalInfo info = new PositionalInfo(pPlayer);
				info.scalePlayerReach(pPlayer, space.unitsPerBlock);
				info.adjust(pPlayer, space);
				
				HitResult mcHitResult = Minecraft.getInstance().hitResult;
				double reach = pPlayer.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue();// 154
				Minecraft.getInstance().hitResult = pPlayer.pick(reach * space.unitsPerBlock, 1, true);
//				if (Minecraft.getInstance().hitResult.getType() == HitResult.Type.MISS)
//					Minecraft.getInstance().hitResult = pPlayer.pick(reach * space.unitsPerBlock, 1, false);

//				Minecraft.getInstance().hitResult = new BlockHitResult(
//						pHit
//								.getLocation()
//								.subtract(pPos.getX(), pPos.getY(), pPos.getZ())
//								.add(pos.getX(), pos.getY(), pos.getZ())
////										.scale(space.unitsPerBlock)
//						,
//						pHit.getDirection(),
//						space.getOffsetPos(pos), pHit.isInside()
//				);
				ClientLevel lvl = Minecraft.getInstance().level;
				Minecraft.getInstance().level = (ClientLevel) space.myLevel;
				
				net.minecraftforge.client.event.InputEvent.ClickInputEvent inputEvent = net.minecraftforge.client.ForgeHooksClient.onClickInput(0, Minecraft.getInstance().options.keyUse, InteractionHand.MAIN_HAND);
				InteractionResult result = InteractionResult.FAIL;
				if (!inputEvent.isCanceled()) {
					if (Minecraft.getInstance().player.connection != null) {
						result = Minecraft.getInstance().gameMode.useItemOn(
								(LocalPlayer) pPlayer, (ClientLevel) space.myLevel, pHand,
								(BlockHitResult) Minecraft.getInstance().hitResult
						);
					}
				}
				Minecraft.getInstance().hitResult = mcHitResult;
				Minecraft.getInstance().level = lvl;
				
				info.reset(pPlayer);
//				pPlayer.eyeHeight = (float) (oldEyeHeight);
//				pPlayer.level = ((LocalPlayer) pPlayer).clientLevel = (trueLvl);
//				pPlayer.setPosRaw(trueVec.x, trueVec.y, trueVec.z);
//				pPlayer.setBoundingBox(srcBB);
				
				NetworkingHacks.unitPos.remove();

//				if (!result.consumesAction()) {
//					UnitInteractionPacket packet = new UnitInteractionPacket((UnitHitResult) pHit);
//					SUNetworkRegistry.NETWORK_INSTANCE.sendToServer(packet);
//				}
				
				if (result.shouldSwing()) return result;
				
				if (pHand == InteractionHand.MAIN_HAND && result == InteractionResult.PASS)
					return InteractionResult.PASS;
				if (result == InteractionResult.PASS) return InteractionResult.CONSUME;
//				return result.consumesAction() ? InteractionResult.CONSUME : InteractionResult.CONSUME_PARTIAL;
				return result;
			}
		}
		if (pHit instanceof UnitHitResult) {
//			BlockPos pos = ((UnitHitResult) pHit).geetBlockPos();
//			LevelChunk chnk = pLevel.getChunkAt(pPos);
//			UnitSpace space = SUCapabilityManager.getCapability(chnk).getUnit(pPos);
//			ItemStack itm = pPlayer.getItemInHand(pHand);
////			if (itm.getItem() instanceof BlockItem) {
////				itm.useOn(
////						new UseOnContext(
////								space.myLevel, pPlayer, pHand,
////								pPlayer.getItemInHand(pHand),
////								new BlockHitResult(
////										pHit.getLocation(),
////										pHit.getDirection(),
////										space.getOffsetPos(pos), pHit.isInside()
////								)
////						)
////				);
////			}
//
////			space.getBlock(pos.getX(), pos.getY(), pos.getZ()).use(
////					space.myLevel, pPlayer, pHand, new BlockHitResult(
////							pHit.getLocation(),
////							pHit.getDirection(),
////							space.getOffsetPos(pos), pHit.isInside()
////					));
//
//			if (pPlayer instanceof ServerPlayer) {
//				NetworkingHacks.unitPos.set(pPos);
//
//				AABB srcBB = pPlayer.getBoundingBox();
//				ServerLevel trueLvl = ((ServerPlayer) pPlayer).getLevel();
//				Vec3 trueVec = new Vec3(pPlayer.getX(), pPlayer.getY(), pPlayer.getZ());
//				double oldEyeHeight = pPlayer.getEyeHeight();
//
//				AABB scaledBB;
//				pPlayer.setBoundingBox(scaledBB = HitboxScaling.getOffsetAndScaledBox(
//						srcBB, trueVec, space
//				));
//				pPlayer.eyeHeight = (float) (oldEyeHeight * space.unitsPerBlock);
//				pPlayer.setPosRaw(scaledBB.getCenter().x, scaledBB.minY, scaledBB.getCenter().z);
//				((ServerPlayer) pPlayer).setLevel((ServerLevel) space.getMyLevel());
//
//				InteractionResult result = ((ServerPlayer) pPlayer).gameMode.useItemOn(
//						(ServerPlayer) pPlayer, space.myLevel, itm, InteractionHand.MAIN_HAND,
//						new BlockHitResult(
//								pHit
//										.getLocation()
//										.subtract(pPos.getX(), pPos.getY(), pPos.getZ())
//										.add(pos.getX(), pos.getY(), pos.getZ())
////										.scale(space.unitsPerBlock)
//								,
//								pHit.getDirection(),
//								space.getOffsetPos(pos), pHit.isInside()
//						)
//				);
//
//				pPlayer.eyeHeight = (float) (oldEyeHeight);
//				((ServerPlayer) pPlayer).setLevel(trueLvl);
//				pPlayer.setPosRaw(trueVec.x, trueVec.y, trueVec.z);
//				pPlayer.setBoundingBox(srcBB);
//				NetworkingHacks.unitPos.remove();
//
//				if (result == InteractionResult.PASS) return InteractionResult.FAIL;
//				return result.consumesAction() ? InteractionResult.CONSUME_PARTIAL : InteractionResult.CONSUME;
//			}
//
////			((SUCapableChunk) chnk).SU$markDirty(pPos);
////			chnk.setUnsaved(true);
			return InteractionResult.CONSUME;
		}
		return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
	}
	
	@Override
	public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRandom) {
//		pLevel.scheduleTick(pPos, this, 1);
//		LevelChunk chnk = pLevel.getChunkAt(pPos);
//		UnitSpace space = SUCapabilityManager.getCapability(chnk).getUnit(pPos);
//		if (space == null) return;
//		space.tick();
		super.tick(pState, pLevel, pPos, pRandom);
	}
	
	@Override
	public boolean onDestroyedByPlayer(BlockState state, Level pLevel, BlockPos pPos, Player player, boolean willHarvest, FluidState fluid) {
//		HitResult result = null;
//		if (FMLEnvironment.dist.isClient())
////			if (pLevel.isClientSide)
//			result = Minecraft.getInstance().hitResult;
////		super.playerDestroy(pLevel, pPlayer, pPos, pState, pBlockEntity, pTool);
//		if (result instanceof UnitHitResult) {
//			LevelChunk chnk = pLevel.getChunkAt(pPos);
//			UnitSpace space = SUCapabilityManager.getCapability(chnk).getUnit(pPos);
//			BlockPos pos = ((UnitHitResult) result).geetBlockPos();
//			space.setState(pos, Blocks.AIR);
//		}
//		// TODO: check if the unit is empty
		return false;
	}
	
	@Override
	// relative block hardness
	public float getDestroyProgress(BlockState pState, Player pPlayer, BlockGetter pLevel, BlockPos pPos) {
		return super.getDestroyProgress(pState, pPlayer, pLevel, pPos);
	}
	
	// shoot, this is forge
	@Override
	public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
		// TODO: figure this out?
		return false;
	}
	
	@Override
	public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
		LevelChunk chnk = pLevel.getChunkAt(pPos);
		UnitSpace space = SUCapabilityManager.getCapability(chnk).getUnit(pPos);
		space.clear();
		SUCapabilityManager.getCapability(chnk).removeUnit(pPos);
		RemoveUnitPacket pckt = new RemoveUnitPacket(pPos, space.unitsPerBlock);
		SUNetworkRegistry.NETWORK_INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> pLevel.getChunkAt(pPos)), pckt);
//		super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
	}
	
	// the *proper* parameters
	public void destroy(BlockState blockState, Level lvl, BlockPos blockPos, Player player, InteractionHand mainHand, UnitHitResult result) {
//		LevelChunk chnk = lvl.getChunkAt(blockPos);
//		UnitSpace space = SUCapabilityManager.getCapability(chnk).getUnit(blockPos);
//		BlockPos pos = result.geetBlockPos();
//		space.setState(pos, Blocks.AIR);
	}
}
