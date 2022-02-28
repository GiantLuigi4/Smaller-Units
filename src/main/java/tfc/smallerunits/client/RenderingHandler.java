package tfc.smallerunits.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.LightType;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.DrawHighlightEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import tfc.smallerunits.SmallerUnitsTESR;
import tfc.smallerunits.block.SmallerUnitBlock;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.config.SmallerUnitsConfig;
import tfc.smallerunits.helpers.BufferCacheHelper;
import tfc.smallerunits.mixins.WorldRendererAccessor;
import tfc.smallerunits.utils.UnitRaytraceContext;
import tfc.smallerunits.utils.UnitRaytraceHelper;
import tfc.smallerunits.utils.compat.RaytraceUtils;
import tfc.smallerunits.utils.compat.vr.SUVRPlayer;
import tfc.smallerunits.utils.data.SUCapabilityManager;
import tfc.smallerunits.utils.rendering.BufferCache;
import tfc.smallerunits.utils.world.client.FakeClientWorld;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Optional;

public class RenderingHandler {
	static boolean hasChecked = false;
	
	public static void onRenderTick(TickEvent.RenderTickEvent event) {
		hasChecked = false;
	}
	
	public static void onRenderWorldLastNew(RenderWorldLastEvent event) {
		if (!((WorldRendererAccessor) event.getContext()).getWorld().equals(Minecraft.getInstance().world) || hasChecked)
			return;
		hasChecked = true;
		//TODO: force vanilla renderer to work in fake world
		if (Minecraft.getInstance().world instanceof FakeClientWorld) {
			MatrixStack matrixStack = event.getMatrixStack();
			matrixStack.push();
			Vector3d projectedView = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
			matrixStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);
			matrixStack.translate(0, 64, 0);
			matrixStack.scale(
					((FakeClientWorld) Minecraft.getInstance().world).owner.unitsPerBlock,
					((FakeClientWorld) Minecraft.getInstance().world).owner.unitsPerBlock,
					((FakeClientWorld) Minecraft.getInstance().world).owner.unitsPerBlock
			);
			SmallerUnitsTESR.render(
					((FakeClientWorld) Minecraft.getInstance().world).owner, event.getPartialTicks(),
					matrixStack, new BufferCache(Minecraft.getInstance().getRenderTypeBuffers().getBufferSource(), matrixStack),
					0, 0
			);
			matrixStack.pop();
		}
		
		MatrixStack matrixStack = event.getMatrixStack();
		ClippingHelper clippinghelper = new ClippingHelper(matrixStack.getLast().getMatrix(), event.getProjectionMatrix());
//		clippinghelper.setCameraPosition(Minecraft.getInstance().renderViewEntity.getEyePosition(event.getPartialTicks()).getX(), Minecraft.getInstance().renderViewEntity.getEyePosition(event.getPartialTicks()).getY(), Minecraft.getInstance().renderViewEntity.getEyePosition(event.getPartialTicks()).getZ());
		clippinghelper.setCameraPosition(
				Minecraft.getInstance().getRenderManager().info.getProjectedView().x,
				Minecraft.getInstance().getRenderManager().info.getProjectedView().y,
				Minecraft.getInstance().getRenderManager().info.getProjectedView().z
		);
		{
			ArrayList<BlockPos> toFree = new ArrayList<>();
			SmallerUnitsTESR.vertexBufferCacheUsed.forEach((pos, buffer) -> {
//				TileEntity tileEntity = Minecraft.getInstance().world.getTileEntity(pos);
				TileEntity tileEntity = SUCapabilityManager.getUnitAtBlock(((WorldRendererAccessor) event.getContext()).getWorld(), pos);
				if (tileEntity == null || !clippinghelper.isBoundingBoxInFrustum(tileEntity.getRenderBoundingBox())) {
					toFree.add(pos);
				}
			});
			for (BlockPos pos : toFree) {
				SmallerUnitsTESR.vertexBufferCacheFree.put(pos, SmallerUnitsTESR.vertexBufferCacheUsed.get(pos));
				SmallerUnitsTESR.vertexBufferCacheUsed.remove(pos);
			}
		}
		{
			ArrayList<BlockPos> toSetInUse = new ArrayList<>();
			SmallerUnitsTESR.vertexBufferCacheFree.forEach((pos, buffer) -> {
//				TileEntity tileEntity = Minecraft.getInstance().world.getTileEntity(pos);
				TileEntity tileEntity = SUCapabilityManager.getUnitAtBlock(((WorldRendererAccessor) event.getContext()).getWorld(), pos);
				if (tileEntity != null && clippinghelper.isBoundingBoxInFrustum(tileEntity.getRenderBoundingBox())) {
					toSetInUse.add(pos);
				}
			});
			for (BlockPos pos : toSetInUse) {
				SmallerUnitsTESR.vertexBufferCacheUsed.put(pos, SmallerUnitsTESR.vertexBufferCacheUsed.get(pos));
				SmallerUnitsTESR.vertexBufferCacheFree.remove(pos);
			}
		}
		
		if (!SmallerUnitsConfig.CLIENT.useExperimentalRendererPt2.get()) return;
		matrixStack.push();
		Vector3d projectedView = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
		matrixStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);
		IRenderTypeBuffer.Impl buffers = Minecraft.getInstance().getRenderTypeBuffers().getCrumblingBufferSource();
		ArrayList<RenderType> types = new ArrayList<>();
		for (TileEntity tileEntity : Minecraft.getInstance().world.loadedTileEntityList) {
			if (tileEntity instanceof UnitTileEntity) {
				if (clippinghelper.isBoundingBoxInFrustum(tileEntity.getRenderBoundingBox())) {
					if (BufferCacheHelper.cache == null) {
						BufferCache cache = new BufferCache(buffers, matrixStack);
						BufferCacheHelper.cache = cache;
					}
					BufferCache cache = BufferCacheHelper.cache;
//					BufferCache cache = new BufferCache(buffers, matrixStack);
					cache.buffer = buffers;
					matrixStack.push();
					matrixStack.translate(
							tileEntity.getPos().getX(),
							tileEntity.getPos().getY(),
							tileEntity.getPos().getZ()
					);
					SmallerUnitsTESR.render(
							(UnitTileEntity) tileEntity, event.getPartialTicks(),
							matrixStack, cache,
							0, OverlayTexture.NO_OVERLAY
					);
					matrixStack.pop();
					for (RenderType type : cache.builderHashMap.keySet()) {
						if (!types.contains(type)) {
							types.add(type);
//							buffers.finish(type);
						}
					}
					cache.builderHashMap.clear();
				}
			}
		}
		for (RenderType type : types) {
			buffers.finish(type);
		}
		matrixStack.pop();
	}
	
	//	public static void onRenderWorldLast(RenderWorldLastEvent event) {
	public static void onRenderWorldLast(MatrixStack matrixStack, Matrix4f projectionIn) {
//		float partialTicks = event.getPartialTicks();
//		ActiveRenderInfo activerenderinfo = Minecraft.getInstance().getRenderManager().info;
//		MatrixStack matrixStackIn = new MatrixStack();
//
//		net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup cameraSetup = net.minecraftforge.client.ForgeHooksClient.onCameraSetup(Minecraft.getInstance().gameRenderer, activerenderinfo, partialTicks);
//		activerenderinfo.setAnglesInternal(cameraSetup.getYaw(), cameraSetup.getPitch());
//		matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(cameraSetup.getRoll()));
//
//		matrixStackIn.rotate(Vector3f.XP.rotationDegrees(activerenderinfo.getPitch()));
//		matrixStackIn.rotate(Vector3f.YP.rotationDegrees(activerenderinfo.getYaw() + 180.0F));
		
		MatrixStack.Entry entry = matrixStack.getLast();
		matrixStack.stack.removeLast();
		MatrixStack stack = new MatrixStack();
//		stack.getLast().getMatrix().mul(event.getProjectionMatrix());
		stack.stack.add(entry);
//		stack.rotate(Minecraft.getInstance().getRenderManager().info.getRotation());
		stack.translate(
				-Minecraft.getInstance().getRenderManager().info.getProjectedView().getX(),
				-Minecraft.getInstance().getRenderManager().info.getProjectedView().getY(),
				-Minecraft.getInstance().getRenderManager().info.getProjectedView().getZ()
		);
		ClippingHelper clippinghelper = new ClippingHelper(stack.getLast().getMatrix(), projectionIn);
		clippinghelper.setCameraPosition(Minecraft.getInstance().renderViewEntity.getPosX(), Minecraft.getInstance().renderViewEntity.getPosY(), Minecraft.getInstance().renderViewEntity.getPosZ());
		for (TileEntity tileEntity : Minecraft.getInstance().world.loadedTileEntityList) {
			if (tileEntity instanceof UnitTileEntity) {
//				if (clippinghelper.isBoundingBoxInFrustum(tileEntity.getRenderBoundingBox())) {
				stack.push();
				MatrixStack stack1 = new MatrixStack();
				stack.translate(tileEntity.getPos().getX(), tileEntity.getPos().getY(), tileEntity.getPos().getZ());
				BufferCache cache = new BufferCache(Minecraft.getInstance().getRenderTypeBuffers().getBufferSource(), stack1);
				stack1.stack.add(stack.getLast());
				SmallerUnitsTESR.render(
						(UnitTileEntity) tileEntity, Minecraft.getInstance().getRenderPartialTicks(),
						stack1, cache,
						LightTexture.packLight(
								Minecraft.getInstance().world.getLightFor(LightType.BLOCK, tileEntity.getPos()),
								Minecraft.getInstance().world.getLightFor(LightType.SKY, tileEntity.getPos())
						),
						OverlayTexture.NO_OVERLAY
				);
				stack.pop();
//				}
			}
		}
		matrixStack.stack.add(entry);
	}
	
	public static void onChangeDimensions(PlayerEvent.PlayerChangedDimensionEvent event) {
		SmallerUnitsTESR.bufferCache.forEach((pos, pair) -> {
			pair.getSecond().dispose();
		});
		SmallerUnitsTESR.bufferCache.clear();
	}
	
	public static void onLeaveWorld(ClientPlayerNetworkEvent.LoggedOutEvent event) {
		SmallerUnitsTESR.bufferCache.forEach((pos, pair) -> {
			pair.getSecond().dispose();
		});
		SmallerUnitsTESR.bufferCache.clear();
	}
	
	public static void onDrawSelectionBox(DrawHighlightEvent event) {
		if (!SmallerUnitsConfig.CLIENT.useExperimentalSelection.get()) return;
		if (!(event.getTarget() instanceof BlockRayTraceResult)) return;
//		BlockState state = Minecraft.getInstance().world.getBlockState(((BlockRayTraceResult) event.getTarget()).getPos());
//		if (!(state.getBlock() instanceof SmallerUnitBlock)) return;
		
		UnitTileEntity tileEntity = SUCapabilityManager.getUnitAtBlock(Minecraft.getInstance().world, ((BlockRayTraceResult) event.getTarget()).getPos());
		if (tileEntity == null) return;
		BlockState state = tileEntity.getBlockState();
		
		event.getMatrix().push();

//		UnitTileEntity tileEntity = (UnitTileEntity) Minecraft.getInstance().player.getEntityWorld().getTileEntity(((BlockRayTraceResult) event.getTarget()).getPos());
//		UnitRaytraceContext raytraceContext = UnitRaytraceHelper.raytraceBlock(tileEntity, Minecraft.getInstance().player, true, ((BlockRayTraceResult) event.getTarget()).getPos(), Optional.empty());
//		raytraceContext.posHit = raytraceContext.posHit.offset(raytraceContext.hitFace.orElse(Direction.UP));
//		BlockPos worldPos = ((BlockRayTraceResult) event.getTarget()).getPos();
////		if (raytraceContext.vecHit.equals(new Vector3d(-100,-100,-100))) {
////			raytraceContext.vecHit = hit.getHitVec()
////					.subtract(worldPos.getX(), worldPos.getY(), worldPos.getZ())
////					.add(worldPos.getX(), worldPos.getY(), worldPos.getZ())
////			;
////			if (raytraceContext.hitFace.orElse(hit.getFace()).equals(Direction.UP)) {
////				raytraceContext.vecHit = raytraceContext.vecHit.subtract(0,-(1f/16)/4,0);
////			}
////			raytraceContext.posHit = raytraceContext.posHit.up();
////		}
//		raytraceContext.vecHit = raytraceContext.vecHit
//				.subtract(worldPos.getX(),worldPos.getY(),worldPos.getZ())
//				.subtract(raytraceContext.posHit.getX()/((float)tileEntity.unitsPerBlock),(raytraceContext.posHit.getY()-64)/((float)tileEntity.unitsPerBlock),raytraceContext.posHit.getZ()/((float)tileEntity.unitsPerBlock))
//		;
//		raytraceContext.vecHit = raytraceContext.vecHit.scale(tileEntity.unitsPerBlock).add(raytraceContext.posHit.getX(),raytraceContext.posHit.getY(),raytraceContext.posHit.getZ());
//		Direction face = raytraceContext.hitFace.orElse(Direction.UP);
//		raytraceContext.posHit = raytraceContext.posHit.offset(face.getOpposite());
//		tileEntity.world.result = new BlockRayTraceResult(
//				raytraceContext.vecHit,
//				face,
//				raytraceContext.posHit,
//				true
//		);
//
//		MinecraftForge.EVENT_BUS.post(new DrawHighlightEvent.HighlightBlock(
//				event.getContext(),
//				event.getInfo(),
//				tileEntity.world.result,
//				event.getPartialTicks(),
//				event.getMatrix(),
//				event.getBuffers()
//		));

//		event.getMatrix().translate(
//				-Minecraft.getInstance().getRenderManager().info.getProjectedView().getX(),
//				-Minecraft.getInstance().getRenderManager().info.getProjectedView().getY(),
//				-Minecraft.getInstance().getRenderManager().info.getProjectedView().getZ()
//		);
		ISelectionContext context = new ISelectionContext() {
			@Override
			public boolean getPosY() {
				return false;
			}
			
			@Override
			public boolean func_216378_a(VoxelShape shape, BlockPos pos, boolean p_216378_3_) {
				return false;
			}
			
			@Override
			public boolean hasItem(Item itemIn) {
				return false;
			}
			
			@Nullable
			@Override
			public Entity getEntity() {
				return Minecraft.getInstance().player;
			}
			
			@Override
			public boolean func_230426_a_(FluidState p_230426_1_, FlowingFluid p_230426_2_) {
				return false;
			}
		};

//		VoxelShape shape = SmallerUnitBlock.getShapeOld(state, Minecraft.getInstance().world, ((BlockRayTraceResult) event.getTarget()).getPos(), context);
//		TileEntity te = Minecraft.getInstance().world.getTileEntity(((BlockRayTraceResult) event.getTarget()).getPos());
//		if (!(te instanceof UnitTileEntity)) return;
//		UnitTileEntity tileEntity = (UnitTileEntity) te;
		UnitRaytraceContext raytraceContext;
		if (event.getTarget().hitInfo instanceof UnitRaytraceContext)
			raytraceContext = (UnitRaytraceContext) event.getTarget().hitInfo;
		else
			raytraceContext = UnitRaytraceHelper.raytraceBlockWithoutShape(tileEntity, Minecraft.getInstance().player, true, ((BlockRayTraceResult) event.getTarget()).getPos(), Optional.of(context), Optional.of(SUVRPlayer.getPlayer$(Minecraft.getInstance().player)));
		VoxelShape shape;
		if (raytraceContext.posHit != null) {
			BlockState state1 = tileEntity.getFakeWorld().getBlockState(raytraceContext.posHit);
			shape = state1.getShape(tileEntity.getFakeWorld(), raytraceContext.posHit, context);
			if (shape.isEmpty()) {
				shape = SmallerUnitBlock.getShapeOld(state, Minecraft.getInstance().world, ((BlockRayTraceResult) event.getTarget()).getPos(), context);
				raytraceContext.posHit = null;
			}
		} else {
			shape = SmallerUnitBlock.getShapeOld(state, Minecraft.getInstance().world, ((BlockRayTraceResult) event.getTarget()).getPos(), context);
		}
		IVertexBuilder builder = event.getBuffers().getBuffer(RenderType.getLines());
		event.getMatrix().translate(
				((BlockRayTraceResult) event.getTarget()).getPos().getX() - Minecraft.getInstance().getRenderManager().info.getProjectedView().getX(),
				((BlockRayTraceResult) event.getTarget()).getPos().getY() - Minecraft.getInstance().getRenderManager().info.getProjectedView().getY(),
				((BlockRayTraceResult) event.getTarget()).getPos().getZ() - Minecraft.getInstance().getRenderManager().info.getProjectedView().getZ()
		);
		if (raytraceContext.posHit != null) {
			event.getMatrix().scale(1f / tileEntity.unitsPerBlock, 1f / tileEntity.unitsPerBlock, 1f / tileEntity.unitsPerBlock);
			event.getMatrix().translate(
					raytraceContext.posHit.getX(),
					raytraceContext.posHit.getY() - 64,
					raytraceContext.posHit.getZ()
			);
		}
		
		boolean doShow = raytraceContext.vecHit == null && false;
		{
			BlockState state1 = Minecraft.getInstance().world.getBlockState(((BlockRayTraceResult) event.getTarget()).getPos());
			VoxelShape shape1 = state1.getBlock().getBlock().getShape(
					state1, Minecraft.getInstance().world,
					((BlockRayTraceResult) event.getTarget()).getPos(),
					ISelectionContext.forEntity(event.getInfo().getRenderViewEntity())
			);
			if (false) {
				Vector3d startVec = RaytraceUtils.getStartVector(
						event.getInfo().getRenderViewEntity()
				);
				
				if (!doShow) {
					Entity entity = event.getInfo().getRenderViewEntity();
					
					BlockState state2 = entity.getEntityWorld().getBlockState(((BlockRayTraceResult) event.getTarget()).getPos());
					VoxelShape shape2 = state2.getBlock().getShape(state2, entity.getEntityWorld(), ((BlockRayTraceResult) event.getTarget()).getPos(), ISelectionContext.forEntity(entity));
					
					Vector3d start = RaytraceUtils.getStartVector(entity);
					Vector3d end = start.add(RaytraceUtils.getLookVector(entity).scale(RaytraceUtils.getReach(entity)));
					// VoxelShape$raytrace is obnoxious
					Vector3d resultHit = null;
					double bestDist = Double.POSITIVE_INFINITY;
					for (AxisAlignedBB axisAlignedBB : shape2.toBoundingBoxList()) {
						Optional<Vector3d> hit = axisAlignedBB.offset(((BlockRayTraceResult) event.getTarget()).getPos()).rayTrace(start, end);
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
					
					double d0;
					if (finalResultHit == null) d0 = event.getTarget().getHitVec().distanceTo(startVec);
					else d0 = finalResultHit.distanceTo(startVec);
					double d1 = raytraceContext.vecHit.distanceTo(startVec);
					doShow = d0 <= d1;
				}
				
				if (doShow) {
					Matrix4f matrix4f = event.getMatrix().getLast().getMatrix();
					float red = 0;
					float green = 0;
					float blue = 0;
					float alpha = 0.5f;
					
					double sclMul = 1d / tileEntity.unitsPerBlock;
					double sclDiv = tileEntity.unitsPerBlock;
					
					Vector3d vector3d = event.getTarget().getHitVec().subtract(
							((BlockRayTraceResult) event.getTarget()).getPos().getX(),
							((BlockRayTraceResult) event.getTarget()).getPos().getY(),
							((BlockRayTraceResult) event.getTarget()).getPos().getZ()
					);
					vector3d = vector3d.mul(sclDiv, sclDiv, sclDiv);
					vector3d = new Vector3d(
							Math.floor(vector3d.x),
							Math.floor(vector3d.y),
							Math.floor(vector3d.z)
					);
					vector3d = vector3d.mul(sclMul, sclMul, sclMul);
					
					if (
							((BlockRayTraceResult) event.getTarget()).getFace() == Direction.UP ||
									((BlockRayTraceResult) event.getTarget()).getFace() == Direction.EAST ||
									((BlockRayTraceResult) event.getTarget()).getFace() == Direction.SOUTH
					) {
						vector3d = vector3d.add(
								((BlockRayTraceResult) event.getTarget()).getFace().getXOffset() * -sclMul,
								((BlockRayTraceResult) event.getTarget()).getFace().getYOffset() * -sclMul,
								((BlockRayTraceResult) event.getTarget()).getFace().getZOffset() * -sclMul
						);
					}
					
					VoxelShape shape2 = VoxelShapes.create(
							vector3d.x,
							vector3d.y,
							vector3d.z,
							vector3d.x + sclMul,
							vector3d.y + sclMul,
							vector3d.z + sclMul
					);
					
					shape2 = VoxelShapes.combine(shape1, shape2, IBooleanFunction.AND);
					
					shape2.forEachEdge((x1, y1, z1, x2, y2, z2) -> {
						builder.pos(matrix4f, (float) (x1), (float) (y1), (float) (z1)).color(red, green, blue, alpha).endVertex();
						builder.pos(matrix4f, (float) (x2), (float) (y2), (float) (z2)).color(red, green, blue, alpha).endVertex();
					});

//					if (event.isCancelable()) event.setCanceled(true);
//					event.getMatrix().pop();
//					return;
				}
			}
		}
		if (!doShow) {
			Matrix4f matrix4f = event.getMatrix().getLast().getMatrix();
			float red = 0;
			float green = 0;
			float blue = 0;
			float alpha = 0.5f;
			shape.forEachEdge((x1, y1, z1, x2, y2, z2) -> {
				builder.pos(matrix4f, (float) (x1), (float) (y1), (float) (z1)).color(red, green, blue, alpha).endVertex();
				builder.pos(matrix4f, (float) (x2), (float) (y2), (float) (z2)).color(red, green, blue, alpha).endVertex();
			});
		}
		if (event.isCancelable()) event.setCanceled(true);
		event.getMatrix().pop();
	}
}
