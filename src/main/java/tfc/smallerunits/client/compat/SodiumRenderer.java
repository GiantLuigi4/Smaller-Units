package tfc.smallerunits.client.compat;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.math.Matrix4f;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import me.jellysquid.mods.sodium.client.gl.device.RenderDevice;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;
import net.coderbot.iris.Iris;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL40;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.UnitSpace;
import tfc.smallerunits.client.abstraction.SodiumFrustum;
import tfc.smallerunits.client.access.tracking.SUCapableChunk;
import tfc.smallerunits.client.access.tracking.SUCompiledChunkAttachments;
import tfc.smallerunits.client.access.tracking.sodium.RenderSectionManagerAccessor;
import tfc.smallerunits.client.render.SURenderManager;
import tfc.smallerunits.client.render.TileRendererHelper;
import tfc.smallerunits.data.capability.ISUCapability;
import tfc.smallerunits.data.capability.SUCapabilityManager;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.utils.BreakData;
import tfc.smallerunits.utils.IHateTheDistCleaner;
import tfc.smallerunits.utils.platform.PlatformUtils;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;

public class SodiumRenderer {
	public static void doRender(boolean isShaderPresent, RenderType shaderType, RenderType renderLayer, PoseStack matrixStack, double x, double y, double z, CallbackInfo ci, SodiumFrustum frustum, Minecraft client, ClientLevel world, RenderSectionManager renderSectionManager) {
		RenderDevice.exitManagedCode();
		
		// I hate all of these
		// but I couldn't find any other solution
		if (!isShaderPresent) {
			if (renderLayer == RenderType.translucent()) shaderType = RenderType.translucentMovingBlock();
			else if (renderLayer == RenderType.tripwire()) shaderType = RenderType.translucentMovingBlock();
			else if (renderLayer == RenderType.cutout()) shaderType = RenderType.translucentMovingBlock();
			else if (renderLayer == RenderType.cutoutMipped()) shaderType = RenderType.translucentMovingBlock();
		}
		shaderType.setupRenderState();
		
		ShaderInstance instance = RenderSystem.getShader();
		
		if (instance == null) {
			shaderType.clearRenderState();
			return;
		}
		
		if (
				(renderLayer != RenderType.solid() && isShaderPresent) ||
						(renderLayer != shaderType && !isShaderPresent)
		) {
			// I don't know why the heck this is needed
			GlStateManager._glUseProgram(instance.getId());
			instance.apply();
			
			// this is slow
			int[] id = new int[1];
			GL11.glGetIntegerv(GL40.GL_CURRENT_PROGRAM, id);
			if (id[0] == 0) {
				instance.clear();
				shaderType.clearRenderState();
			}
		} else {
			instance.apply();
		}
		
		if (instance.MODEL_VIEW_MATRIX != null) {
			instance.MODEL_VIEW_MATRIX.set(matrixStack.last().pose());
			instance.MODEL_VIEW_MATRIX.upload();
		}
		if (instance.PROJECTION_MATRIX != null) {
			instance.PROJECTION_MATRIX.set(RenderSystem.getProjectionMatrix());
			instance.PROJECTION_MATRIX.upload();
		}
		if (instance.COLOR_MODULATOR != null) {
			instance.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
			instance.COLOR_MODULATOR.upload();
		}
		
		boolean isMatrix = false;
		Uniform uniform;
		if (instance.CHUNK_OFFSET == null) {
			isMatrix = true;
			uniform = instance.MODEL_VIEW_MATRIX;
		} else {
			uniform = instance.CHUNK_OFFSET;
		}
		
		BlockPos.MutableBlockPos origin = new BlockPos.MutableBlockPos();
		for (Map.Entry<RenderRegion, List<RenderSection>> renderRegionListEntry : ((RenderSectionManagerAccessor) renderSectionManager).getChunkRenderList().sorted(false)) {
			for (RenderSection renderSection : renderRegionListEntry.getValue()) {
				SUCompiledChunkAttachments attachments = ((SUCompiledChunkAttachments) renderSection);
				SUCapableChunk chunk = attachments.getSUCapable();
				if (chunk == null) {
					LevelChunk chunk1 = world.getChunkAt(renderSection.getChunkPos().center());
					if (chunk1 instanceof SUCapableChunk chk)
						attachments.setSUCapable(chunk = chk);
				}
				
				origin.set(
						-(renderSection.getChunkX() << 4),
						-(renderSection.getChunkY() << 4),
						-(renderSection.getChunkZ() << 4)
				);
				if (isMatrix) {
					matrixStack.pushPose();
					matrixStack.translate(
							(float) (-(origin.getX()) - x),
							(float) (-(origin.getY()) - y),
							(float) (-(origin.getZ()) - z)
					);
					uniform.set(matrixStack.last().pose());
					matrixStack.popPose();
				} else {
					uniform.set(
							(float) (-(origin.getX()) - x),
							(float) (-(origin.getY()) - y),
							(float) (-(origin.getZ()) - z)
					);
				}
				
				SURenderManager.drawChunk(
						(LevelChunk) chunk, world, origin, renderLayer,
						frustum, x, y, z,
						uniform
				);
			}
		}
		
		if (instance.MODEL_VIEW_MATRIX != null) {
			Matrix4f mat = new Matrix4f();
			mat.setIdentity();
			instance.MODEL_VIEW_MATRIX.set(mat);
			instance.MODEL_VIEW_MATRIX.upload();
		}
		if (!isMatrix) {
			uniform.set(0f, 0, 0);
			uniform.upload();
		}
		
		shaderType.clearRenderState();
		
		RenderDevice.enterManagedCode();
	}
	
	public static void render(RenderType renderLayer, PoseStack matrixStack, double x, double y, double z, CallbackInfo ci, SodiumFrustum frustum, Minecraft client, ClientLevel world, RenderSectionManager renderSectionManager) {
		RenderType shaderType = renderLayer;
		
		boolean isShaderPresent = false;
		if (PlatformUtils.isLoaded("iris")) {
			if (Iris.getCurrentPack().isPresent()) {
				// for some reason, translucent bricks rendering with iris if I render it where it's meant to be rendered
				// so instead, I render it where tripwires render, because for some reason, that doesn't brick rendering
				if (renderLayer == RenderType.translucent())
					return;
				isShaderPresent = true;
			}
		}
		
		if (isShaderPresent && renderLayer == RenderType.tripwire()) {
			doRender(
					true, RenderType.translucent(), RenderType.translucent(),
					matrixStack, x, y, z, ci,
					frustum, client, world,
					renderSectionManager
			);
		}
		doRender(
				isShaderPresent, shaderType, renderLayer,
				matrixStack, x, y, z, ci,
				frustum, client, world,
				renderSectionManager
		);
	}
	
	public static void renderSection(BlockPos origin, RenderSection instance, PoseStack stk, RenderBuffers bufferBuilders, Long2ObjectMap<SortedSet<BlockDestructionProgress>> blockBreakingProgressions, Camera camera, float tickDelta, CallbackInfo ci, SodiumFrustum frustum, Minecraft client, ClientLevel level, RenderSectionManager renderSectionManager) {
		SUCapableChunk capable = ((SUCompiledChunkAttachments) instance).getSUCapable();
		
		if (capable == null)
			((SUCompiledChunkAttachments) instance).setSUCapable(capable = ((SUCapableChunk) level.getChunk(origin)));
		
		ISUCapability capability = SUCapabilityManager.getCapability((LevelChunk) capable);
		
		UnitSpace[] spaces = capability.getUnits();
		// no reason to do SU related rendering in chunks where SU has not been used
		if (spaces.length == 0) return;
		
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
						frustum,
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
		VertexBuffer.unbindVertexArray();
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
						TileRendererHelper.drawBreakingOutline(integer.prog, bufferBuilders, stk, unit.getMyLevel(), integer.pos, ((Level) world).getBlockState(integer.pos), client);
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
				TileRendererHelper.renderBE(tile, origin, frustum, stk, Minecraft.getInstance().getBlockEntityRenderDispatcher(), tickDelta);
			stk.popPose();
		}
		stk.popPose();
	}
	
	public static void renderTEs(PoseStack matrices, RenderBuffers bufferBuilders, Long2ObjectMap<SortedSet<BlockDestructionProgress>> blockBreakingProgressions, Camera camera, float tickDelta, CallbackInfo ci, SodiumFrustum frustum, Minecraft client, ClientLevel world, RenderSectionManager renderSectionManager) {
		BlockPos.MutableBlockPos origin = new BlockPos.MutableBlockPos();
		for (Map.Entry<RenderRegion, List<RenderSection>> renderRegionListEntry : ((RenderSectionManagerAccessor) renderSectionManager).getChunkRenderList().sorted(false)) {
			for (RenderSection renderSection : renderRegionListEntry.getValue()) {
				origin.set(
						-(renderSection.getChunkX() << 4),
						-(renderSection.getChunkY() << 4),
						-(renderSection.getChunkZ() << 4)
				);
				renderSection(
						origin, renderSection,
						matrices, bufferBuilders, blockBreakingProgressions,
						camera, tickDelta, ci,
						frustum, client, world,
						renderSectionManager
				);
			}
		}
	}
}
