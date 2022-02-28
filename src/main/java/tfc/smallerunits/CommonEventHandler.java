package tfc.smallerunits;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkProvider;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import tfc.smallerunits.api.placement.UnitPos;
import tfc.smallerunits.block.SmallerUnitBlock;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.helpers.ClientUtils;
import tfc.smallerunits.mixins.ticking.ChunkArrayAccessor;
import tfc.smallerunits.utils.UnitRaytraceContext;
import tfc.smallerunits.utils.UnitRaytraceHelper;
import tfc.smallerunits.utils.compat.RaytraceUtils;
import tfc.smallerunits.utils.compat.vr.SUVRPlayer;
import tfc.smallerunits.utils.data.CapabilityProvider;
import tfc.smallerunits.utils.data.SUCapability;
import tfc.smallerunits.utils.data.SUCapabilityManager;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class CommonEventHandler {
	public static void onSneakClick(PlayerInteractEvent.RightClickBlock event) {
		PlayerEntity entity = event.getPlayer();
		if (entity.isSneaking()) {
			BlockState state1 = entity.getEntityWorld().getBlockState(event.getPos());
			if (state1.getBlock() instanceof SmallerUnitBlock) {
				event.setCancellationResult(state1.onBlockActivated(entity.world, entity, event.getHand(), event.getHitVec()));
				event.setCanceled(true);
			}
		}
	}
	
	public static HashMap<BlockPos, UnitTileEntity> tilesToAddClient = new HashMap<>();
	
	// server
	private static final HashMap<UUID, BlockPos> unitsBeingMined = new HashMap<>();
	// client
	private static UnitPos lastMiningPos = null;
	
	// TODO: request datapackets upon comming with 64 blocks
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (FMLEnvironment.dist.isClient()) {
			if (event.player == ClientUtils.getPlayer()) {
				if (!(Minecraft.getInstance().objectMouseOver instanceof BlockRayTraceResult)) return;
				World world = event.player.world;
				BlockPos destroyPos = ((BlockRayTraceResult) Minecraft.getInstance().objectMouseOver).getPos();
				BlockState state = world.getBlockState(destroyPos);
				if (state.getBlock() instanceof SmallerUnitBlock) {
					TileEntity te = world.getTileEntity(destroyPos);
					if ((te instanceof UnitTileEntity)) {
						UnitTileEntity tileEntity = (UnitTileEntity) te;
						ISelectionContext context = ISelectionContext.forEntity(event.player);
						UnitRaytraceContext raytraceContext = UnitRaytraceHelper.raytraceBlockWithoutShape(
								tileEntity,
//								Minecraft.getInstance().player.getEntity(), // why
								event.player,
								true,
								destroyPos,
								Optional.of(context),
								Optional.of(SUVRPlayer.getPlayer$(event.player))
						);
						UnitPos pos = (UnitPos) raytraceContext.posHit;
						BlockPos pos1 = lastMiningPos;
						if (pos1 != null && !pos1.equals(pos)) {
							PlayerController controller = Minecraft.getInstance().playerController;
							if (controller.isHittingBlock) {
//								BlockState blockstate = Minecraft.getInstance().world.getBlockState(this.currentBlock);
								controller.isHittingBlock = true;
								controller.curBlockDamageMP = 0.0F;
//								this.mc.world.sendBlockBreakProgress(this.mc.player.getEntityId(), this.currentBlock, -1);
								Minecraft.getInstance().player.resetCooldown();
								Minecraft.getInstance().playerController.sendDiggingPacket(CPlayerDiggingPacket.Action.START_DESTROY_BLOCK, destroyPos, Direction.UP);
//								controller.isHittingBlock = true;
								lastMiningPos = null;
							}
						}
						lastMiningPos = pos;
					}
				}
			}
		}
		if (event.player instanceof ServerPlayerEntity) {
			PlayerInteractionManager manager = ((ServerPlayerEntity) event.player).interactionManager;
			World world = event.player.world;
			BlockState state = world.getBlockState(manager.destroyPos);
			if (state.getBlock() instanceof SmallerUnitBlock) {
				TileEntity te = world.getTileEntity(manager.destroyPos);
				if ((te instanceof UnitTileEntity)) {
					UnitTileEntity tileEntity = (UnitTileEntity) te;
					ISelectionContext context = ISelectionContext.forEntity(event.player);
					UnitRaytraceContext raytraceContext = UnitRaytraceHelper.raytraceBlockWithoutShape(
							tileEntity,
							event.player,
							true,
							manager.destroyPos,
							Optional.of(context),
							Optional.of(SUVRPlayer.getPlayer$(event.player))
					);
					UnitPos pos = (UnitPos) raytraceContext.posHit;
//					if (!manager.isDestroyingBlock) {
//						UUID uuid = event.player.getUniqueID();
//						manager.initialDamage = manager.ticks + 1;
//						manager.initialBlockDamage = manager.initialDamage;
//						manager.durabilityRemainingOnBlock = 7;
//						manager.receivedFinishDiggingPacket = false;
//						unitsBeingMined.replace(uuid, pos);
////						System.out.println("a");
//						return;
//					}
					if (pos == null) return;
					UUID uuid = event.player.getUniqueID();
					if (!unitsBeingMined.containsKey(uuid)) unitsBeingMined.put(uuid, pos);
					else {
						BlockPos pos1 = unitsBeingMined.get(uuid);
						if (pos1 != null && !pos1.equals(pos)) {
							manager.initialDamage = manager.ticks + 1;
							manager.initialBlockDamage = manager.initialDamage;
							manager.durabilityRemainingOnBlock = 7;
							manager.receivedFinishDiggingPacket = false;
							unitsBeingMined.replace(uuid, pos);
						} else if (pos1 == null) {
							unitsBeingMined.replace(uuid, pos);
						}
					}
					return;
				}
			}
			if (!manager.isDestroyingBlock) {
				UUID uuid = event.player.getUniqueID();
				if (unitsBeingMined.containsKey(uuid)) unitsBeingMined.remove(uuid);
				return;
			}
		}
	}
	
	public static void onPlayerInteract(PlayerInteractEvent event) {
		if (!event.isCancelable() || event.getWorld() == null || event.getPlayer() == null) return;
		if (event instanceof PlayerInteractEvent.LeftClickBlock) {
//			BlockState state = event.getWorld().getBlockState(event.getPos());
//			if (state.getBlock() instanceof SmallerUnitBlock) {
//				TileEntity te = event.getWorld().getTileEntity(event.getPos());
//				if (!(te instanceof UnitTileEntity)) return;
//				UnitTileEntity tileEntity = (UnitTileEntity) te;
			
			UnitTileEntity tileEntity = SUCapabilityManager.getUnitAtBlock(event.getWorld(), event.getPos());
			if (tileEntity == null) return;
			BlockState state = event.getWorld().getBlockState(event.getPos());
			
			if (!((SmallerUnitBlock) state.getBlock()).canBeRemoved(event.getPlayer(), event.getWorld(), tileEntity, event.getPos())) {
				if (!event.getWorld().isRemote) {
					event.setCancellationResult(ActionResultType.SUCCESS);
					event.setCanceled(true);
				}
			}
//			}
		}
//		else if (event instanceof PlayerInteractEvent.RightClickBlock) {
//			UnitTileEntity tileEntity = SUCapabilityManager.getUnitAtBlock(event.getWorld(), event.getPos());
//			if (tileEntity == null) return;
//			BlockState state = event.getWorld().getBlockState(event.getPos());
//
//			if (!((SmallerUnitBlock) state.getBlock()).onInteract(event.getPlayer(), event.getWorld(), tileEntity, event.getPos())) {
//				if (!event.getWorld().isRemote) {
//					event.setCancellationResult(ActionResultType.SUCCESS);
//					event.setCanceled(true);
//				}
//			}
//		}
	}
	
	public static void onAttachCapabilities(final AttachCapabilitiesEvent<Chunk> event) {
		new CapabilityProvider().attach(event);
	}
	
	public static void onWorldTick(TickEvent event) {
		if (event instanceof TickEvent.WorldTickEvent || event instanceof TickEvent.ClientTickEvent) {
			if (!event.phase.equals(TickEvent.Phase.START)) {
				return;
			}
			
			World world;
			
			if (event instanceof TickEvent.WorldTickEvent) {
				world = ((TickEvent.WorldTickEvent) event).world;
			} else {
				world = ClientUtils.getWorld();
			}
			
			if (world == null) return;

//			if (event.getChunk() instanceof ICapabilityProvider) {
//				LazyOptional<SUCapability> capability = ((ICapabilityProvider) event.getChunk()).getCapability(SUCapabilityManager.SUCapability);
//				if (capability.isPresent()) {
//					SUCapability cap = capability.resolve().get();
//					cap.tick(event.getChunk().getWorldForge());
//				}
//			}
			
			if (FMLEnvironment.dist.isClient()) {
				if (ClientUtils.checkClientWorld(world)) {
					for (BlockPos pos : tilesToAddClient.keySet())
						SUCapabilityManager.setTile(world, pos, tilesToAddClient.get(pos));
					tilesToAddClient.clear();
				}
			}
			
			if (world instanceof ServerWorld) {
				for (ChunkHolder chunkHolder : ((ServerChunkProvider) world.getChunkProvider()).chunkManager.getLoadedChunksIterable()) {
					Chunk chunk = chunkHolder.getChunkIfComplete();
					if (chunk != null) {
						LazyOptional<SUCapability> capability = chunk.getCapability(SUCapabilityManager.SUCapability);
						if (capability.isPresent()) {
							SUCapability cap = capability.resolve().get();
							cap.tick(world);
//							chunk.markDirty();
						}
					}
				}
			} else {
				ClientChunkProvider.ChunkArray array = ((ClientChunkProvider) world.getChunkProvider()).array;
				AtomicReferenceArray<Chunk> chunks = ((ChunkArrayAccessor) (Object) array).getChunks();
				for (int i = 0; i < chunks.length(); i++) {
					Chunk chunk = chunks.get(i);
					if (chunk != null) {
						LazyOptional<SUCapability> capability = chunk.getCapability(SUCapabilityManager.SUCapability);
						if (capability.isPresent()) {
							SUCapability cap = capability.resolve().get();
							cap.tick(world);
//							chunk.markDirty();
						}
					}
				}
			}
		}
	}
	
	public static void onPlayerBreakBlock(BlockEvent.BreakEvent event) {
		if (true) return;
		CommonEventHandler.BreakEvent eventBreak = new CommonEventHandler.BreakEvent(
				event.getPlayer(), event.getPlayer().isSneaking(), event.getPos()
		);
		CommonEventHandler.preBreakEvent(eventBreak);
		if (eventBreak.cancel) {
			event.setCanceled(true);
		}
	}
	
	public static void preBreakEvent(BreakEvent event) {
		PlayerEntity entity = event.getEntity();
//		RayTraceResult result = entity.pick(RaytraceUtils.getReach(entity), 0, false);
		BlockState state = entity.getEntityWorld().getBlockState(event.getPos());
		VoxelShape shape = state.getShape(entity.getEntityWorld(), event.pos, ISelectionContext.forEntity(entity));
		VoxelShape shape1 = state.getBlock().getShape(state, entity.getEntityWorld(), event.pos, ISelectionContext.forEntity(entity));
		RayTraceResult result;
		RayTraceResult result1;
		
		{
			Vector3d start = RaytraceUtils.getStartVector(entity);
			Vector3d end = start.add(RaytraceUtils.getLookVector(entity).scale(RaytraceUtils.getReach(entity)));
			// VoxelShape$raytrace is obnoxious
			Vector3d resultHit = null;
			double bestDist = Double.POSITIVE_INFINITY;
			for (AxisAlignedBB axisAlignedBB : shape.toBoundingBoxList()) {
				Optional<Vector3d> hit = axisAlignedBB.offset(event.pos).rayTrace(start, end);
				if (hit.isPresent()) {
					Vector3d hitVec = hit.get();
					double dist = hitVec.distanceTo(start);
					if (dist < bestDist) {
						bestDist = hitVec.distanceTo(start);
						resultHit = hit.get();
					}
				}
			}
			Vector3d finalResultHit = resultHit;
			result = new RayTraceResult(resultHit) {
				@Override
				public Type getType() {
					return finalResultHit == null ? Type.MISS : Type.BLOCK;
				}
				
				/**
				 * Returns the hit position of the raycast, in absolute world coordinates
				 */
				@Override
				public Vector3d getHitVec() {
					return finalResultHit;
				}
			};
		}
		
		{
			Vector3d start = RaytraceUtils.getStartVector(entity);
			Vector3d end = start.add(RaytraceUtils.getLookVector(entity).scale(RaytraceUtils.getReach(entity)));
			// VoxelShape$raytrace is obnoxious
			Vector3d resultHit = null;
			double bestDist = Double.POSITIVE_INFINITY;
			for (AxisAlignedBB axisAlignedBB : shape1.toBoundingBoxList()) {
				Optional<Vector3d> hit = axisAlignedBB.offset(event.pos).rayTrace(start, end);
				if (hit.isPresent()) {
					Vector3d hitVec = hit.get();
					double dist = hitVec.distanceTo(start);
					if (dist < bestDist) {
						bestDist = hitVec.distanceTo(start);
						resultHit = hit.get();
					}
				}
			}
			Vector3d finalResultHit = resultHit;
			result1 = new RayTraceResult(resultHit) {
				@Override
				public Type getType() {
					return finalResultHit == null ? Type.MISS : Type.BLOCK;
				}
				
				/**
				 * Returns the hit position of the raycast, in absolute world coordinates
				 */
				@Override
				public Vector3d getHitVec() {
					return finalResultHit;
				}
			};
		}

//		BlockRayTraceResult result = shape.rayTrace(
//				RaytraceUtils.getStartVector(entity),
//				RaytraceUtils.getStartVector(entity).add(RaytraceUtils.getLookVector(entity).scale(RaytraceUtils.getReach(entity))),
//				event.pos
//		);
//		if (result == null) return;

//		if (!(result instanceof BlockRayTraceResult)) return;
		if (result.getType() != RayTraceResult.Type.BLOCK) return;
//		if (!((BlockRayTraceResult) result).getPos().equals(event.getPos())) return;
		
		UnitTileEntity tileEntity = SUCapabilityManager.getUnitAtBlock(entity.getEntityWorld(), event.getPos());
		if (tileEntity == null) return;
		
		UnitRaytraceContext context = UnitRaytraceHelper.raytraceBlock(
				tileEntity, entity, true,
				event.getPos(), Optional.of(ISelectionContext.forEntity(entity)),
				Optional.of(SUVRPlayer.getPlayer$(event.entity))
		);
		
		boolean isSUBlock = false;
		
		boolean canBreakRegardless = true;
		
		// intelliJ inspection is wrong for this
		//noinspection ConstantConditions
		if (result1.getHitVec() == null) {
			isSUBlock = true;
			canBreakRegardless = false;
		} else {
			double dist0 = context.vecHit.distanceTo(RaytraceUtils.getStartVector(entity));
			double dist1 = result1.getHitVec().distanceTo(RaytraceUtils.getStartVector(entity));
			if (dist0 <= dist1) {
				isSUBlock = true;
				canBreakRegardless = false;
			}
		}
		if (
				context.vecHit.x == -100 &&
						context.vecHit.y == -100 &&
						context.vecHit.z == -100
		) {
			isSUBlock = true;
			// TODO: test if the player is selecting the actual block
//			shouldBreakRegardless = true;
		}
		
		if (isSUBlock) {
			if (tileEntity.getBlockMap().isEmpty()) {
				SUCapabilityManager.removeTile(entity.world, event.pos);
				if (state.getBlock() instanceof SmallerUnitBlock) {
					tileEntity.getBlockState().removedByPlayer(
							entity.getEntityWorld(), event.getPos(),
							entity, false, Fluids.EMPTY.getDefaultState()
					);
				}
			} else {
				tileEntity.getBlockState().removedByPlayer(
						entity.getEntityWorld(), event.getPos(),
						entity, false, Fluids.EMPTY.getDefaultState()
				);
			}
			if (!canBreakRegardless)
				event.setCanceled(true);
//			if (isSUBlock || !shouldBreakRegardless) event.setCanceled(true);
		}
	}
	
	public static class BreakEvent {
		public final PlayerEntity entity;
		public final boolean isSneaking;
		public final BlockPos pos;
		
		public boolean cancel = false;
		
		public BreakEvent(PlayerEntity entity, boolean isSneaking, BlockPos pos) {
			this.entity = entity;
			this.isSneaking = isSneaking;
			this.pos = pos;
		}
		
		public void setCanceled(boolean cancel) {
			this.cancel = cancel;
		}
		
		public PlayerEntity getEntity() {
			return entity;
		}
		
		public boolean isSneaking() {
			return isSneaking;
		}
		
		public BlockPos getPos() {
			return pos;
		}
		
		public boolean isCancel() {
			return cancel;
		}
	}
}
