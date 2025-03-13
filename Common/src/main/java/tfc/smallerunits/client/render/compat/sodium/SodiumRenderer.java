package tfc.smallerunits.client.render.compat.sodium;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import me.jellysquid.mods.sodium.client.render.chunk.lists.ChunkRenderList;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;
import me.jellysquid.mods.sodium.client.util.iterator.ByteIterator;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.UnitSpace;
import tfc.smallerunits.client.abstraction.IFrustum;
import tfc.smallerunits.client.abstraction.SodiumFrustum;
import tfc.smallerunits.client.access.tracking.SUCapableChunk;
import tfc.smallerunits.client.access.tracking.SUCompiledChunkAttachments;
import tfc.smallerunits.client.render.SURenderManager;
import tfc.smallerunits.client.render.TileRendererHelper;
import tfc.smallerunits.data.capability.ISUCapability;
import tfc.smallerunits.data.capability.SUCapabilityManager;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.utils.BreakData;
import tfc.smallerunits.utils.IHateTheDistCleaner;
import tfc.smallerunits.utils.asm.ModCompatClient;
import tfc.smallerunits.utils.config.compat.ClientCompatConfig;

import java.util.Iterator;
import java.util.SortedSet;

public class SodiumRenderer {
	public static void render(RenderType type, PoseStack poseStack, double camX, double camY, double camZ, CallbackInfo ci, SodiumFrustum frustum, Minecraft client, ClientLevel level, RenderSectionManager renderSectionManager) {
		if (ClientCompatConfig.RenderCompatOptions.sodiumRenderMode == SodiumRenderMode.VANILLA) {
			renderVanilla(type, frustum, level, poseStack, camX, camY, camZ);
		} else {
			throw new RuntimeException("Sodium renderer not implemented yet");
		}

		ModCompatClient.postRenderLayer(type, poseStack, camX, camY, camZ, level);
	}

	public static void renderVanilla(RenderType type, IFrustum su$Frustum, ClientLevel level, PoseStack poseStack, double camX, double camY, double camZ) {
		type.setupRenderState();

		ShaderInstance instance = RenderSystem.getShader();
		// I don't want to know
		instance.setSampler("Sampler0", RenderSystem.getShaderTexture(0));
		instance.setSampler("Sampler2", RenderSystem.getShaderTexture(2));
		if (instance.MODEL_VIEW_MATRIX != null) instance.MODEL_VIEW_MATRIX.set(poseStack.last().pose());
//		if (instance.MODEL_VIEW_MATRIX != null) instance.MODEL_VIEW_MATRIX.set(new Matrix4f().identity());
//		if (instance.PROJECTION_MATRIX != null) instance.PROJECTION_MATRIX.set(new Matrix4f().identity());
		if (instance.PROJECTION_MATRIX != null) instance.PROJECTION_MATRIX.set(RenderSystem.getProjectionMatrix());
		instance.apply();
		if (instance.MODEL_VIEW_MATRIX != null) instance.MODEL_VIEW_MATRIX.upload();
		if (instance.PROJECTION_MATRIX != null) instance.PROJECTION_MATRIX.upload();

		int min = level.getMinBuildHeight();
		int max = level.getMaxBuildHeight();

		for (SUCompiledChunkAttachments chunk : ((SodiumGridAttachments) level).renderChunksWithUnits().values()) {
			SUCapableChunk capableChunk = chunk.getSUCapable();

//			if (capable == null)
//				((SUCompiledChunkAttachments) instance).setSUCapable(origin.getY(), capable = ((SUCapableChunk) level.getChunk(origin)));
//			System.out.println(chunk);

			LevelChunk chunk1 = ((LevelChunk) capableChunk);
			if (!su$Frustum.test(
					new AABB(
							chunk1.getPos().getMinBlockX() - 1,
							min - 1,
							chunk1.getPos().getMinBlockZ() - 1,
							chunk1.getPos().getMaxBlockX() + 1,
							max + 1,
							chunk1.getPos().getMaxBlockZ() + 1
					)
			)) continue;

			int sectY = chunk1.getMinSection();
			for (LevelChunkSection section : chunk1.getSections()) {
				if (section.hasOnlyAir()) {
					sectY++;
					continue;
				}

				BlockPos pos = new BlockPos(
						chunk1.getPos().getMinBlockX(),
						SectionPos.sectionToBlockCoord(sectY),
						chunk1.getPos().getMinBlockZ()
				);

				instance.CHUNK_OFFSET.set(
						(float) (pos.getX() - camX),
						(float) (pos.getY() - camY),
						(float) (pos.getZ() - camZ)
				);

				SURenderManager.drawChunk(
						((SodiumSUAttached) chunk).SU$getChunkRender(sectY),
						chunk, chunk1,
						level, pos, type,
						su$Frustum,
						camX, camY, camZ,
						instance.CHUNK_OFFSET
				);

				sectY++;
			}
		}

		instance.CHUNK_OFFSET.set(0f, 0, 0);

		instance.setSampler("Sampler0", null);
		instance.setSampler("Sampler2", null);
		instance.clear();
		type.clearRenderState();
	}

	public static void renderSection(BlockPos origin, RenderSection instance, PoseStack stk, RenderBuffers bufferBuilders, Long2ObjectMap<SortedSet<BlockDestructionProgress>> blockBreakingProgressions, float tickDelta, CallbackInfo ci, SodiumFrustum frustum, Minecraft client, ClientLevel level) {
		SUCapableChunk capable = ((SUCompiledChunkAttachments) instance).getSUCapable();

		if (capable == null)
			((SUCompiledChunkAttachments) instance).setSUCapable(origin.getY(), capable = ((SUCapableChunk) level.getChunk(origin)));

		ISUCapability capability = SUCapabilityManager.getCapability((LevelChunk) capable);

		UnitSpace[] spaces = capability.getUnits();
		// no reason to do SU related rendering in chunks where SU has not been used
		if (spaces.length == 0) return;

		stk.pushPose();
		stk.translate(origin.getX(), origin.getY(), origin.getZ());

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
			int y = unit.pos.getY();

			if (y < origin.getY() + 16 &&
					y >= origin.getY()) {
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
		}

		if (shader.COLOR_MODULATOR != null) {
			shader.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
			shader.COLOR_MODULATOR.upload();
		}

		VertexBuffer.unbind();
		shader.clear();
		RenderType.solid().clearRenderState();

		/* breaking overlays */
		for (UnitSpace unit : capability.getUnits()) {
			if (unit != null) {
				ITickerLevel world = (ITickerLevel) unit.getMyLevel();
				if (world != null) {
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
		}
		stk.popPose();

		synchronized (capable.getTiles()) {
			BlockEntity[] bes = new BlockEntity[0];
			// TODO: debug????
			try {
				bes = capable.getTiles().toArray(bes);
			} catch (Throwable ignored) {
			}
			for (BlockEntity tile : bes)
				ModCompatClient.drawBE(
						tile, origin, frustum,
						stk, tickDelta
				);
		}
	}

	public static void renderTEs(PoseStack matrices, RenderBuffers bufferBuilders, Long2ObjectMap<SortedSet<BlockDestructionProgress>> blockBreakingProgressions, Camera camera, float tickDelta, CallbackInfo ci, SodiumFrustum frustum, Minecraft client, ClientLevel level, RenderSectionManager renderSectionManager) {
		BlockPos.MutableBlockPos origin = new BlockPos.MutableBlockPos();
		Iterator<ChunkRenderList> lists = renderSectionManager.getRenderLists().iterator(false);
		PoseStack stk = matrices;

		stk.pushPose();
		stk.translate(-camera.getPosition().x, -camera.getPosition().y, -camera.getPosition().z);

		while (lists.hasNext()) {
			ChunkRenderList chunkrenderlist = lists.next();
			RenderRegion renderregion = chunkrenderlist.getRegion();
			ByteIterator byteiterator = chunkrenderlist.sectionsWithGeometryIterator(false);
			if (byteiterator != null) {
				while (byteiterator.hasNext()) {
					int i = byteiterator.nextByteAsInt();
					RenderSection rendersection = renderregion.getSection(i);

					origin.set(rendersection.getChunkX() << 4, rendersection.getChunkY() << 4, rendersection.getChunkZ() << 4);

					renderSection(
							origin, rendersection, stk,
							bufferBuilders, blockBreakingProgressions,
							tickDelta, ci, frustum,
							client, level
					);
				}
			}
		}
		stk.popPose();
	}
}
