package tfc.smallerunits.mixin;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.UnitSpace;
import tfc.smallerunits.client.abstraction.VanillaFrustum;
import tfc.smallerunits.client.access.tracking.SUCapableChunk;
import tfc.smallerunits.client.access.tracking.SUCompiledChunkAttachments;
import tfc.smallerunits.client.render.SURenderManager;
import tfc.smallerunits.client.render.TileRendererHelper;
import tfc.smallerunits.data.capability.ISUCapability;
import tfc.smallerunits.data.capability.SUCapabilityManager;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.utils.BreakData;
import tfc.smallerunits.utils.IHateTheDistCleaner;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixinBlocks {
	@Shadow
	public ClientLevel level;
	@Unique
	PoseStack stk;
	@Shadow
	@Final
	private BlockEntityRenderDispatcher blockEntityRenderDispatcher;
	
	@Shadow
	private @Nullable Frustum capturedFrustum;
	@Shadow
	private Frustum cullingFrustum;
	@Shadow
	@Final
	private RenderBuffers renderBuffers;
	@Shadow
	@Final
	private Minecraft minecraft;
	@Unique
	double pCamX, pCamY, pCamZ;
	
	@Inject(at = @At("HEAD"), method = "renderChunkLayer")
	public void preStartDraw(RenderType j, PoseStack d0, double d1, double d2, double i, Matrix4f k, CallbackInfo ci) {
		pCamX = d1;
		pCamY = d2;
		pCamZ = i;
	}
	
	@Unique
	VanillaFrustum SU$Frustum = new VanillaFrustum();
	@Unique
	float pct;
	
	@Inject(at = @At("HEAD"), method = "renderLevel")
	public void preDrawLevel(PoseStack pPoseStack, float pPartialTick, long pFinishNanoTime, boolean pRenderBlockOutline, Camera pCamera, GameRenderer pGameRenderer, LightTexture pLightTexture, Matrix4f pProjectionMatrix, CallbackInfo ci) {
		pct = pPartialTick;
	}
	
	@Inject(at = @At("HEAD"), method = "checkPoseStack")
	public void preCheckMatrices(PoseStack pPoseStack, CallbackInfo ci) {
		stk = pPoseStack;
	}
	
	@Redirect(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$RenderChunk;getCompiledChunk()Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$CompiledChunk;"))
	public ChunkRenderDispatcher.CompiledChunk preGetCompiledChunk(ChunkRenderDispatcher.RenderChunk instance) {
		BlockPos origin = instance.getOrigin();
		ChunkRenderDispatcher.CompiledChunk chunk = instance.compiled.get();
		SUCapableChunk capable = ((SUCompiledChunkAttachments) chunk).getSUCapable();
		
		if (capable == null)
			((SUCompiledChunkAttachments) chunk).setSUCapable(capable = ((SUCapableChunk) level.getChunk(origin)));
		
		ISUCapability capability = SUCapabilityManager.getCapability((LevelChunk) capable);
		if (capability == null) return instance.getCompiledChunk();
		
		UnitSpace[] spaces = capability.getUnits();
		// no reason to do SU related rendering in chunks where SU has not been used
		if (spaces.length == 0) return instance.getCompiledChunk();
		
		Frustum frustum = capturedFrustum != null ? capturedFrustum : cullingFrustum;
		SU$Frustum.set(frustum);
		
		stk.pushPose();
		Vec3 cam = Minecraft.getInstance().getEntityRenderDispatcher().camera.getPosition();
		stk.translate(origin.getX() - cam.x, origin.getY() - cam.y, origin.getZ() - cam.z);
		
		/* draw indicators */
		RenderType.solid().setupRenderState();
		ShaderInstance shader = GameRenderer.getPositionColorShader();
		shader.apply();
		RenderSystem.setShader(() -> shader);
		BufferUploader.reset();
		RenderSystem.setupShaderLights(shader);
		if (shader.PROJECTION_MATRIX != null) {
			shader.PROJECTION_MATRIX.set(RenderSystem.getProjectionMatrix());
			shader.PROJECTION_MATRIX.upload();
		}
		TileRendererHelper.markNewFrame();
		
		boolean hammerHeld = IHateTheDistCleaner.isHammerHeld();
		for (UnitSpace unit : spaces) {
			if (unit != null) {
				TileRendererHelper.drawUnit(
						SU$Frustum,
						unit.pos, unit.unitsPerBlock, unit.isNatural,
						hammerHeld, unit.isEmpty(), null, stk,
//						LightTexture.pack(level.getBrightness(LightLayer.BLOCK, unit.pos), level.getBrightness(LightLayer.SKY, unit.pos)),
						LightTexture.pack(0, 0),
						origin.getX(), origin.getY(), origin.getZ()
				);
			}
		}
		
		shader.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
		shader.COLOR_MODULATOR.upload();
		
		VertexBuffer.unbind();
		shader.clear();
		RenderType.solid().clearRenderState();
		
		/* breaking overlays */
		for (UnitSpace unit : capability.getUnits()) {
			if (unit != null) {
				ITickerLevel world = (ITickerLevel) unit.getMyLevel();
				for (BreakData integer : world.getBreakData().values()) {
					BlockPos minPos = unit.getOffsetPos(new BlockPos(0, 0, 0));
					BlockPos maxPos = unit.getOffsetPos(new BlockPos(unit.unitsPerBlock, unit.unitsPerBlock, unit.unitsPerBlock));
					BlockPos posInQuestion = integer.pos;
					if (
							maxPos.getX() > posInQuestion.getX() && posInQuestion.getX() >= minPos.getX() &&
									maxPos.getY() > posInQuestion.getY() && posInQuestion.getY() >= minPos.getY() &&
									maxPos.getZ() > posInQuestion.getZ() && posInQuestion.getZ() >= minPos.getZ()
					)
						TileRendererHelper.drawBreakingOutline(integer.prog, renderBuffers, stk, unit.getMyLevel(), integer.pos, ((Level) world).getBlockState(integer.pos), minecraft);
				}
			}
		}
		
		synchronized (capable.getTiles()) {
			BlockEntity[] bes = new BlockEntity[0];
			// TODO: debug????
			try {
				bes = capable.getTiles().toArray(bes);
			} catch (Throwable ignored) {
			}
			stk.pushPose();
			stk.translate(-origin.getX(), -origin.getY(), -origin.getZ());
			for (BlockEntity tile : bes)
				TileRendererHelper.renderBE(tile, origin, SU$Frustum, stk, blockEntityRenderDispatcher, pct);
			stk.popPose();
		}
		stk.popPose();
		
		return instance.getCompiledChunk();
	}
	
	@Redirect(method = "renderChunkLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$CompiledChunk;isEmpty(Lnet/minecraft/client/renderer/RenderType;)Z"))
	public boolean preDrawLayer(ChunkRenderDispatcher.CompiledChunk instance, RenderType pRenderType) {
		ShaderInstance shaderinstance = RenderSystem.getShader();
		Uniform uniform = shaderinstance.CHUNK_OFFSET;
		
		BlockPos origin = IHateTheDistCleaner.currentRenderChunk.get().getOrigin();
		ChunkRenderDispatcher.CompiledChunk chunk = IHateTheDistCleaner.currentRenderChunk.get().compiled.get();
		SUCapableChunk capable = ((SUCompiledChunkAttachments) chunk).getSUCapable();
		
		if (capable == null)
			((SUCompiledChunkAttachments) chunk).setSUCapable(capable = ((SUCapableChunk) level.getChunk(origin)));
		
		if (uniform != null) {
			uniform.set((float) ((double) origin.getX() - pCamX), (float) ((double) origin.getY() - pCamY), (float) ((double) origin.getZ() - pCamZ));
		}
		
		SU$Frustum.set(capturedFrustum != null ? capturedFrustum : cullingFrustum);
		SURenderManager.drawChunk(((LevelChunk) capable), level, IHateTheDistCleaner.currentRenderChunk.get().getOrigin(), pRenderType, SU$Frustum, pCamX, pCamY, pCamZ, uniform);
		return instance.isEmpty(pRenderType);
	}
}
