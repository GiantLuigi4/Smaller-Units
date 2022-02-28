package tfc.smallerunits;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import tfc.smallerunits.block.SmallerUnitBlock;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.client.RenderingHandler;
import tfc.smallerunits.registry.Deferred;
import tfc.smallerunits.utils.UnitRaytraceContext;
import tfc.smallerunits.utils.UnitRaytraceHelper;
import tfc.smallerunits.utils.compat.RaytraceUtils;
import tfc.smallerunits.utils.compat.vr.SUVRPlayer;
import tfc.smallerunits.utils.data.SUCapabilityManager;

import java.util.Optional;

public class ClientEventHandler {
	public static void doStuff() {
		RenderTypeLookup.setRenderLayer(Deferred.UNIT.get(), RenderType.getCutout());

//		if (ModList.get().isLoaded("optifine"))
//		if (!SmallerUnitsConfig.CLIENT.useExperimentalRenderer.get())
		TileEntityRendererDispatcher.instance.setSpecialRendererInternal(Deferred.UNIT_TE.get(), new SmallerUnitsTESR(TileEntityRendererDispatcher.instance));
//		else
//			new SmallerUnitsTESR(TileEntityRendererDispatcher.instance);

//		if (SmallerUnitsConfig.CLIENT.useExperimentalRenderer.get())
		MinecraftForge.EVENT_BUS.addListener(RenderingHandler::onRenderWorldLastNew);
		MinecraftForge.EVENT_BUS.addListener(ClientEventHandler::onClickBlock);
		MinecraftForge.EVENT_BUS.addListener(ClientEventHandler::onClickEmpty);
		MinecraftForge.EVENT_BUS.addListener(ClientEventHandler::onLeftClickBlock);
	}
	
	public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
		if (true) return;
		CommonEventHandler.BreakEvent eventBreak = new CommonEventHandler.BreakEvent(
				event.getPlayer(), event.getPlayer().isSneaking(), event.getPos()
		);
		CommonEventHandler.preBreakEvent(eventBreak);
		if (eventBreak.cancel) {
			event.setCanceled(true);
		}
	}
	
	public static void onClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
//		event.setCanceled(onClick(
		if (true) return;
		if (event.getHand() == Hand.MAIN_HAND) {
			boolean val = (onClick(
					new ClickEvent(
							null, event.getPlayer(),
							event.getPlayer().isSneaking()
					)
			));
			if (val) {
				event.setResult(Event.Result.DENY);
				event.setCancellationResult(ActionResultType.SUCCESS);
			}
		}
	}
	
	public static void onClickBlock(PlayerInteractEvent.RightClickBlock event) {
		if (true) return;
		if (event.getWorld().getBlockState(event.getPos()).getBlock() instanceof SmallerUnitBlock) return;
		if (event.getHand() == Hand.MAIN_HAND) {
			boolean val = (onClick(
					new ClickEvent(
							event.getHitVec(), event.getPlayer(),
							event.getPlayer().isSneaking()
					)
			));
			if (val) {
				event.setCanceled(true);
				event.setResult(Event.Result.DENY);
				event.setUseItem(Event.Result.DENY);
				event.setUseBlock(Event.Result.DENY);
				event.setCancellationResult(ActionResultType.SUCCESS);
			}
		}
	}
	
	public static boolean onClick(ClickEvent event) {
		RayTraceResult result = null;
		if (event.result != null) {
			PlayerEntity entity = event.entity;
			
			BlockState state = entity.getEntityWorld().getBlockState(event.result.getPos());
			VoxelShape shape = state.getBlock().getShape(state, entity.getEntityWorld(), event.result.getPos(), ISelectionContext.forEntity(entity));
			
			Vector3d start = RaytraceUtils.getStartVector(entity);
			Vector3d end = start.add(RaytraceUtils.getLookVector(entity).scale(RaytraceUtils.getReach(entity)));
			// VoxelShape$raytrace is obnoxious
			Vector3d resultHit = null;
			double bestDist = Double.POSITIVE_INFINITY;
			for (AxisAlignedBB axisAlignedBB : shape.toBoundingBoxList()) {
				Optional<Vector3d> hit = axisAlignedBB.offset(event.result.getPos()).rayTrace(start, end);
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
			if (finalResultHit != null) {
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
		}
		
		boolean isSUBlock = false;
		if (event.result == null) return false;
		UnitTileEntity tileEntity = SUCapabilityManager.getUnitAtBlock(event.entity.getEntityWorld(), event.result.getPos());
		boolean isHitMain = false;
		if (tileEntity != null) {
			if (result == null) isSUBlock = true;
			else {
				UnitRaytraceContext context = UnitRaytraceHelper.raytraceBlockWithoutShape(
						tileEntity,
						event.entity,
						true, event.result.getPos(),
						Optional.of(ISelectionContext.forEntity(event.entity)),
						Optional.of(SUVRPlayer.getPlayer$(event.entity))
				);
//				context.vecHit = new Vector3d(
//						Math.round(context.vecHit.x * 100) / 100d,
//						Math.round(context.vecHit.y * 100) / 100d,
//						Math.round(context.vecHit.z * 100) / 100d
//				);
//				Vector3d eventResultRounded = new Vector3d(
//						Math.round(event.result.getHitVec().x * 100) / 100d,
//						Math.round(event.result.getHitVec().y * 100) / 100d,
//						Math.round(event.result.getHitVec().z * 100) / 100d
//				);
				double dist0 = context.vecHit.distanceTo(RaytraceUtils.getStartVector(event.entity));
				double dist1 = result.getHitVec().distanceTo(RaytraceUtils.getStartVector(event.entity));
//				if (context.vecHit.equals(eventResultRounded)) {
				if (dist0 <= dist1) {
					isSUBlock = true;
					if (dist0 == dist1) isHitMain = true;
				} else {
					isHitMain = true;
				}
//				if (
//						context.vecHit.x == -100 &&
//								context.vecHit.y == -100 &&
//								context.vecHit.z == -100
//				) {
//					isSUBlock = true;
//				}
			}
			
			if ((!isSUBlock || isHitMain) && !event.isSneaking) {
				if (result != null) {
					BlockState state = event.entity.getEntityWorld().getBlockState(event.result.getPos());
					ActionResultType type = state.onBlockActivated(event.entity.world, event.entity, Hand.MAIN_HAND, event.result);
					
					if (type == ActionResultType.PASS) {
						type = state.onBlockActivated(
								event.entity.world, event.entity,
								Hand.OFF_HAND, event.result
						);
					}
					
					if (
							type == ActionResultType.SUCCESS ||
									type == ActionResultType.CONSUME ||
									type == ActionResultType.FAIL
					) {
						return false;
					}
					
					// TODO: check if edge
					isSUBlock = true;
				}
			} else if (event.isSneaking) isSUBlock = true;

//			if (!isSUBlock) return false;
			
			BlockState state = tileEntity.getBlockState();
			ActionResultType type = state.onBlockActivated(
					event.entity.world, event.entity,
					Hand.MAIN_HAND, event.result
			);
			
			if (type == ActionResultType.PASS) {
				type = state.onBlockActivated(
						event.entity.world, event.entity,
						Hand.OFF_HAND, event.result
				);
			}
			
			if (
					type == ActionResultType.SUCCESS ||
							type == ActionResultType.CONSUME ||
							type == ActionResultType.FAIL
			) {
				return true;
			}
		}
		return false;
	}
	
	public static class ClickEvent {
		public final BlockRayTraceResult result;
		public final PlayerEntity entity;
		public final boolean isSneaking;
		
		public ClickEvent(BlockRayTraceResult result, PlayerEntity entity, boolean isSneaking) {
			this.result = result;
			this.entity = entity;
			this.isSneaking = isSneaking;
		}
	}
}
