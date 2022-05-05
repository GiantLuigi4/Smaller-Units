package tfc.smallerunits.mixin;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.UnitSpace;
import tfc.smallerunits.UnitSpaceBlock;
import tfc.smallerunits.client.render.SURenderManager;
import tfc.smallerunits.client.render.TileRendererHelper;
import tfc.smallerunits.client.tracking.SUCapableChunk;
import tfc.smallerunits.client.tracking.SUCompiledChunkAttachments;
import tfc.smallerunits.data.capability.SUCapabilityManager;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.data.tracking.RegionalAttachments;
import tfc.smallerunits.simulation.world.ITickerWorld;
import tfc.smallerunits.utils.selection.UnitHitResult;
import tfc.smallerunits.utils.selection.UnitShape;

import javax.annotation.Nullable;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
	@Shadow
	@Nullable
	private ClientLevel level;
	@Unique
	PoseStack stk;
	@Shadow
	@Final
	private BlockEntityRenderDispatcher blockEntityRenderDispatcher;
	
	@Unique
	public ChunkRenderDispatcher.RenderChunk renderChunk;
	
	@Unique
	double pCamX, pCamY, pCamZ;
	
	@Inject(at = @At("HEAD"), method = "renderChunkLayer")
	public void preStartDraw(RenderType j, PoseStack d0, double d1, double d2, double i, Matrix4f k, CallbackInfo ci) {
		pCamX = d1;
		pCamY = d2;
		pCamZ = i;
	}
	
	// TODO: move off of redirect
	// even js coremods are better than a redirect imo
	// granted those aren't exactly able to be ported to fabric
	// and if I'm not gonna be the one porting SU to fabric, I don't wanna force someone else to port js coremods to fabric
	@Redirect(method = "renderChunkLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$RenderChunk;getCompiledChunk()Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$CompiledChunk;"))
	public ChunkRenderDispatcher.CompiledChunk preRenderLayer(ChunkRenderDispatcher.RenderChunk instance) {
		return (renderChunk = instance).getCompiledChunk();
	}

//	// TODO: move off of redirect
//	@Redirect(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$CompiledChunk;getRenderableBlockEntities()Ljava/util/List;"))
//	public List<BlockEntity> preRenderBEs(ChunkRenderDispatcher.CompiledChunk instance) {
//		return (renderChunk = instance).getCompiledChunk();
//	}
	
	@Shadow
	private static void renderShape(PoseStack pPoseStack, VertexConsumer pConsumer, VoxelShape pShape, double pX, double pY, double pZ, float pRed, float pGreen, float pBlue, float pAlpha) {
	}
	
	@Shadow
	@Final
	private EntityRenderDispatcher entityRenderDispatcher;
	@Shadow
	@Final
	private RenderBuffers renderBuffers;
	
	@Shadow
	protected abstract void renderEntity(Entity pEntity, double pCamX, double pCamY, double pCamZ, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource);
	
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;entitiesForRendering()Ljava/lang/Iterable;"), method = "renderLevel")
	public void beforeRenderEntities(PoseStack stack, float i, long j, boolean k, Camera l, GameRenderer i1, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo ci) {
		for (Region value : ((RegionalAttachments) level).SU$getRegionMap().values()) {
			if (value == null) continue;
			value.forEachLevel((lvl) -> {
				renderEntities(lvl, stack, l, i, this.renderBuffers.bufferSource());
			});
		}
	}
	
	@Unique
	public void renderEntities(Level lvl, PoseStack stk, Camera cam, float pct, MultiBufferSource buffers) {
		Iterable<Entity> entities;
		if (lvl instanceof ClientLevel) entities = ((ClientLevel) lvl).entitiesForRendering();
		else entities = ((ITickerWorld) lvl).getAllEntities();
		stk.pushPose();
		stk.translate(
				-cam.getPosition().x,
				-cam.getPosition().y,
				-cam.getPosition().z
		);
		stk.scale(1f / ((ITickerWorld) lvl).getUPB(), 1f / ((ITickerWorld) lvl).getUPB(), 1f / ((ITickerWorld) lvl).getUPB());
		for (Entity entity : entities)
			SURenderManager.drawEntity((LevelRenderer) (Object) this, lvl, stk, cam, pct, buffers, entity);
		stk.popPose();
	}
	
	@Inject(at = @At("HEAD"), method = "renderHitOutline", cancellable = true)
	public void preRenderOutline(PoseStack pPoseStack, VertexConsumer pConsumer, Entity pEntity, double pCamX, double pCamY, double pCamZ, BlockPos pPos, BlockState pState, CallbackInfo ci) {
		if (pState.getBlock() instanceof UnitSpaceBlock) {
			VoxelShape shape = pState.getShape(this.level, pPos, CollisionContext.of(pEntity));
			if (shape instanceof UnitShape) {
				ci.cancel();
				HitResult result = Minecraft.getInstance().hitResult;
				if (result instanceof UnitHitResult) {
					BlockPos pos = ((UnitHitResult) result).geetBlockPos();
					LevelChunk chnk = level.getChunkAt(pPos);
					UnitSpace space = SUCapabilityManager.getCapability(chnk).getUnit(pPos);
					BlockState state = space.getBlock(pos.getX(), pos.getY(), pos.getZ());
					
					pPoseStack.pushPose();
					pPoseStack.translate(
							(double) pPos.getX() - pCamX,
							(double) pPos.getY() - pCamY,
							(double) pPos.getZ() - pCamZ
					);
					// TODO: better handling
					VoxelShape shape1 = state.getShape(space.getMyLevel(), pos, CollisionContext.of(pEntity));
					if (shape1.isEmpty()) {
						int x = pos.getX();
						int y = pos.getY();
						int z = pos.getZ();
						double upbDouble = space.unitsPerBlock;
						AABB box = new AABB(
								x / upbDouble, y / upbDouble, z / upbDouble,
								(x + 1) / upbDouble, (y + 1) / upbDouble, (z + 1) / upbDouble
						);
						shape1 = Shapes.create(box);
						renderShape(
								pPoseStack,
								pConsumer,
								shape1,
								0, 0, 0,
								0.0F, 0.0F, 0.0F, 0.4F
						);
					} else {
						pPoseStack.scale(1f / space.unitsPerBlock, 1f / space.unitsPerBlock, 1f / space.unitsPerBlock);
						pPoseStack.translate(pos.getX(), pos.getY(), pos.getZ());
						renderShape(
								pPoseStack,
								pConsumer,
								shape1,
								0, 0, 0,
								0.0F, 0.0F, 0.0F, 0.4F
						);
					}
					pPoseStack.popPose();
				}
			}
		}
	}
	
	@Inject(at = @At("HEAD"), method = "checkPoseStack")
	public void preCheckMatrices(PoseStack pPoseStack, CallbackInfo ci) {
		stk = pPoseStack;
	}
	
	@Redirect(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$RenderChunk;getCompiledChunk()Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$CompiledChunk;"))
	public ChunkRenderDispatcher.CompiledChunk preGetCompiledChunk(ChunkRenderDispatcher.RenderChunk instance) {
		renderChunk = instance;
		BlockPos origin = renderChunk.getOrigin();
		ChunkRenderDispatcher.CompiledChunk chunk = renderChunk.compiled.get();
		SUCapableChunk capable = ((SUCompiledChunkAttachments) chunk).getSUCapable();
		if (capable == null)
			((SUCompiledChunkAttachments) chunk).setSUCapable(capable = ((SUCapableChunk) level.getChunk(origin)));
		
		for (BlockEntity tile : capable.getTiles()) {
			TileRendererHelper.setupStack(stk, tile, origin);
			blockEntityRenderDispatcher.render(
					tile, 0,
					stk, Minecraft.getInstance().renderBuffers().bufferSource()
			);
			stk.popPose();
		}
		
		return instance.getCompiledChunk();
	}
	
	@Redirect(method = "renderChunkLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$CompiledChunk;isEmpty(Lnet/minecraft/client/renderer/RenderType;)Z"))
	public boolean preDrawLayer(ChunkRenderDispatcher.CompiledChunk instance, RenderType pRenderType) {
		ShaderInstance shaderinstance = RenderSystem.getShader();
		Uniform uniform = shaderinstance.CHUNK_OFFSET;
		
		BlockPos origin = renderChunk.getOrigin();
		ChunkRenderDispatcher.CompiledChunk chunk = renderChunk.compiled.get();
		SUCapableChunk capable = ((SUCompiledChunkAttachments) chunk).getSUCapable();
		if (capable == null)
			((SUCompiledChunkAttachments) chunk).setSUCapable(capable = ((SUCapableChunk) level.getChunk(origin)));
		
		if (uniform != null) {
			uniform.set((float) ((double) origin.getX() - pCamX), (float) ((double) origin.getY() - pCamY), (float) ((double) origin.getZ() - pCamZ));
			uniform.upload();
		}
		
		SURenderManager.drawChunk(((LevelChunk) capable), level, renderChunk, pRenderType);
		return instance.isEmpty(pRenderType);
	}
}
