package com.tfc.smallerunits;


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.datafixers.util.Pair;
import com.tfc.smallerunits.block.UnitTileEntity;
import com.tfc.smallerunits.utils.DefaultedMap;
import com.tfc.smallerunits.utils.SmallUnit;
import com.tfc.smallerunits.utils.rendering.BufferCache;
import com.tfc.smallerunits.utils.rendering.SUPseudoVBO;
import com.tfc.smallerunits.utils.rendering.SUVBO;
import com.tfc.smallerunits.utils.world.client.FakeClientWorld;
import com.tfc.smallerunits.utils.world.server.FakeServerWorld;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.*;
import net.minecraft.world.LightType;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public class SmallerUnitsTESR extends TileEntityRenderer<UnitTileEntity> {
	public static SmallerUnitsTESR INSTANCE;

//	private static final Object2ObjectLinkedOpenHashMap<RenderType, BufferBuilder> bufferBuilderHashMap = new Object2ObjectLinkedOpenHashMap<>();
	
	public static final Object2ObjectLinkedOpenHashMap<BlockPos, Pair<AtomicReference<CompoundNBT>, SUPseudoVBO>> bufferCache = new Object2ObjectLinkedOpenHashMap<>();
	public static final Object2ObjectLinkedOpenHashMap<BlockPos, SUVBO> vertexBufferCacheUsed = new Object2ObjectLinkedOpenHashMap<>();
	public static final Object2ObjectLinkedOpenHashMap<BlockPos, SUVBO> vertexBufferCacheFree = new Object2ObjectLinkedOpenHashMap<>();
	private static final Quaternion quat90X = new Quaternion(90, 0, 0, true);
	private static final Quaternion quat180X = new Quaternion(180, 0, 0, true);
	private static final Quaternion quat90Y = new Quaternion(0, 90, 0, true);
	
	public static final Logger LOGGER = LogManager.getLogger();
	
	//	private static final IRenderTypeBuffer buffers = new RenderTypeBuffers().getBufferSource();
	private static final DefaultedMap<RenderType, BufferBuilder> buffers = new DefaultedMap<RenderType, BufferBuilder>().setDefaultVal(() -> new BufferBuilder(16));
	
	public static void renderCube(float r, float g, float b, float x, float y, float z, IVertexBuilder builder, int combinedOverlay, int combinedLight, MatrixStack matrixStack, boolean useNormals) {
		Minecraft.getInstance().getProfiler().startSection("renderSquare1");
		renderSquare(r, g, b, x, y, z + 0.25f, builder, combinedOverlay, combinedLight, matrixStack, useNormals);
		Minecraft.getInstance().getProfiler().endStartSection("renderSquare2");
		matrixStack.rotate(quat90Y);
		renderSquare(r, g, b, x - 0.25f, y, z + 0.25f, builder, combinedOverlay, combinedLight, matrixStack, useNormals);
		Minecraft.getInstance().getProfiler().endStartSection("renderSquare3");
		matrixStack.rotate(quat90Y);
		renderSquare(r, g, b, -0.25f, 0, 0, builder, combinedOverlay, combinedLight, matrixStack, useNormals);
		Minecraft.getInstance().getProfiler().endStartSection("renderSquare4");
		matrixStack.rotate(quat90Y);
		renderSquare(r, g, b, 0, 0, 0, builder, combinedOverlay, combinedLight, matrixStack, useNormals);
		Minecraft.getInstance().getProfiler().endStartSection("renderSquare5");
		matrixStack.rotate(quat90X);
		renderSquare(r, g, b, 0, -0.25f, 0, builder, combinedOverlay, combinedLight, matrixStack, useNormals);
		Minecraft.getInstance().getProfiler().endStartSection("renderSquare6");
		matrixStack.rotate(quat180X);
		renderSquare(r, g, b, 0, 0, 0.25f, builder, combinedOverlay, combinedLight, matrixStack, useNormals);
		Minecraft.getInstance().getProfiler().endSection();
	}
	
	public static void renderSquare(float r, float g, float b, float x, float y, float z, IVertexBuilder builder, int combinedOverlay, int combinedLight, MatrixStack matrixStack, boolean useNormals) {
		Vector3f corner1 = translate(matrixStack, x, y, z);
		Vector3f corner2 = translate(matrixStack, x + 0.25f, y, z);
		Vector3f corner3 = translate(matrixStack, x + 0.25f, y + 0.25f, z);
		Vector3f corner4 = translate(matrixStack, x, y + 0.25f, z);
		
		Vector3f normal;
		if (useNormals) {
			//https://www.khronos.org/opengl/wiki/Calculating_a_Surface_Normal
			Vector3f normalU = new Vector3f(x, y, z);
			Vector3f normalV = normalU.copy();
			normalU.sub(new Vector3f(x + 0.25f, y, z));
			normalV.sub(new Vector3f(x + 0.25f, y + 0.25f, z));
			
			normal = new Vector3f(
					(normalU.getY() * normalV.getZ()) - (normalU.getZ() * normalV.getY()),
					(normalU.getZ() * normalV.getX()) - (normalU.getX() * normalV.getZ()),
					(normalU.getX() * normalV.getY()) - (normalU.getY() * normalV.getX())
			);
			
			Matrix3f matrix3f = matrixStack.getLast().getNormal();
			normal.transform(matrix3f);
			
			normal.normalize();
		} else {
			normal = new Vector3f(0, 1, 0);
		}
		
		builder.addVertex(
				corner1.getX(), corner1.getY(), corner1.getZ(),
				r, g, b, 1,
				0, 0,
				combinedOverlay, combinedLight,
				normal.getX(), normal.getY(), normal.getZ()
		);
		
		builder.addVertex(
				corner2.getX(), corner2.getY(), corner2.getZ(),
				r, g, b, 1,
				0, 0,
				combinedOverlay, combinedLight,
				normal.getX(), normal.getY(), normal.getZ()
		);
		
		builder.addVertex(
				corner3.getX(), corner3.getY(), corner3.getZ(),
				r, g, b, 1,
				0, 0,
				combinedOverlay, combinedLight,
				normal.getX(), normal.getY(), normal.getZ()
		);
		
		builder.addVertex(
				corner4.getX(), corner4.getY(), corner4.getZ(),
				r, g, b, 1,
				0, 0,
				combinedOverlay, combinedLight,
				normal.getX(), normal.getY(), normal.getZ()
		);
	}
	
	public static Vector3f translate(MatrixStack stack, float x, float y, float z) {
		Vector4f vector4f = new Vector4f(x, y, z, 1.0F);
		vector4f.transform(stack.getLast().getMatrix());
		return new Vector3f(vector4f.getX(), vector4f.getY(), vector4f.getZ());
	}
	
	public SmallerUnitsTESR(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
		INSTANCE = this;
	}
	
	public static void render(UnitTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn, BufferCache bufferIn, int combinedLightIn, int combinedOverlayIn) {
		if (tileEntityIn.getFakeWorld() == null) return;
		if (tileEntityIn.getFakeWorld() instanceof FakeServerWorld) {
			CompoundNBT tag = tileEntityIn.serializeNBT();
			tileEntityIn.worldServer = null;
			tileEntityIn.handleUpdateTag(tileEntityIn.getBlockState(), tag);
		}
		
		Minecraft.getInstance().getProfiler().startSection("renderSUTE");
//		CompoundNBT nbt = tileEntityIn.serializeNBT();
//
//		if (tileEntityIn.getWorld() == null) return;
//
		MatrixStack oldStack = matrixStackIn;
		matrixStackIn = new MatrixStack();
		matrixStackIn.stack.add(oldStack.getLast());
//
		tileEntityIn.getProfiler().startTick();
//
////		tileEntityIn.worldServer.animateTick();
//
		
		tileEntityIn.worldClient.get().lightManager.tick(SmallerUnitsConfig.CLIENT.lightingUpdatesPerFrame.get(), true, true);
		
		// TODO: make it render without vbos if the player is not in the same world
		if (tileEntityIn.getWorld() == null || tileEntityIn.getWorld().equals(Minecraft.getInstance().world)) {
			boolean isRefreshing = tileEntityIn.needsRefresh(false);
			if (!vertexBufferCacheUsed.containsKey(tileEntityIn.getPos()) || isRefreshing) {
				if (vertexBufferCacheFree.containsKey(tileEntityIn.getPos())) {
					vertexBufferCacheUsed.put(tileEntityIn.getPos(), vertexBufferCacheFree.get(tileEntityIn.getPos()));
				} else {
					MatrixStack stack = new MatrixStack();
//					MatrixStack stack = oldStack;
					Minecraft.getInstance().getProfiler().startSection("doSURender");
					BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
					FakeClientWorld fakeWorld = ((FakeClientWorld) tileEntityIn.getFakeWorld());
					stack.push();
					stack.scale(1f / tileEntityIn.unitsPerBlock, 1f / tileEntityIn.unitsPerBlock, 1f / tileEntityIn.unitsPerBlock);
					boolean renderedAnything = false;
//					CustomBuffer redirection = new CustomBuffer();
					for (SmallUnit value : fakeWorld.blockMap.values()) {
						stack.push();
						renderedAnything = true;
						RenderType type = RenderType.getSolid();
						for (RenderType blockRenderType : RenderType.getBlockRenderTypes())
							if (RenderTypeLookup.canRenderInLayer(value.state, blockRenderType)) type = blockRenderType;
						stack.translate(value.pos.getX(), value.pos.getY() - 64, value.pos.getZ());
//						IVertexBuilder buffer = redirection.getBuffer(type);
						BufferBuilder buffer = buffers.get(type);
						if (!buffer.isDrawing()) buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
						dispatcher.renderModel(value.state, value.pos, fakeWorld, stack, buffer, true, new Random(value.pos.toLong()));
						stack.pop();
					}

//					for (CustomBuffer.CustomVertexBuilder builder : redirection.builders) {
//						if (builder.vertices.size() == 0) continue;
//						FlywheelVertexBuilder buffer = new FlywheelVertexBuilder(builder.vertices.size() * FlywheelVertexFormats.BLOCK.getStride() * 2);
////						int number = 0;
//						for (CustomBuffer.Vertex vertex : builder.vertices) {
////							number += 1;
////							System.out.println(number);
//							buffer.pos(vertex.x, vertex.y, vertex.z);
//							buffer.color(vertex.r, vertex.g, vertex.b, vertex.a);
//							buffer.tex(vertex.u, vertex.v);
//							buffer.lightmap((int)(vertex.lu * 15), (int)(vertex.lv * 15));
//							buffer.normal(vertex.nx, vertex.ny, vertex.nz);
//						}
//						IndexedModel mdl = IndexedModel.fromSequentialQuads(FlywheelVertexFormats.BLOCK, buffer.unwrap(), buffer.vertices());
//						RenderType.getSolid().setupRenderState();
//						mdl.setupState();
//						RenderSystem.pushMatrix();
//						RenderSystem.loadIdentity();
//						RenderSystem.multMatrix(oldStack.getLast().getMatrix());
//						mdl.drawCall();
//						mdl.clearState();
//						RenderSystem.popMatrix();
//						RenderType.getSolid().clearRenderState();
//						buffer.close();
//						mdl.delete();
//					}
					
					stack.pop();
					
					if (renderedAnything && SmallerUnitsConfig.CLIENT.useVBOS.get()) {
						SUVBO suvbo;
						if (!vertexBufferCacheUsed.containsKey(tileEntityIn.getPos())) {
							if (isRefreshing && vertexBufferCacheUsed.containsKey(tileEntityIn.getPos()))
								suvbo = vertexBufferCacheUsed.remove(tileEntityIn.getPos());
							else suvbo = new SUVBO();
						} else suvbo = vertexBufferCacheUsed.remove(tileEntityIn.getPos());
						if (suvbo == null) suvbo = new SUVBO();
						suvbo.markAllUnused();
						buffers.forEach(suvbo::uploadTerrain);
						vertexBufferCacheUsed.put(tileEntityIn.getPos(), suvbo);
					} else {
//						buffers.forEach((type, buffer) -> {
//							buffer.sortVertexData(
//									(float) Minecraft.getInstance().getRenderManager().info.getProjectedView().getX(),
//									(float) Minecraft.getInstance().getRenderManager().info.getProjectedView().getY(),
//									(float) Minecraft.getInstance().getRenderManager().info.getProjectedView().getZ()
//							);
//							RenderSystem.pushMatrix();
//							RenderSystem.loadIdentity();
//							RenderSystem.multMatrix(oldStack.getLast().getMatrix());
//							type.finish(buffer,
//									0, 0, 0
//							);
//							RenderSystem.popMatrix();
//						});
					}
				}
				if (SmallerUnitsConfig.CLIENT.useVBOS.get()) {
					matrixStackIn = oldStack;
					SUVBO vbo = vertexBufferCacheUsed.get(tileEntityIn.getPos());
					if (vbo != null) vbo.render(matrixStackIn);
				}
			} else {
				matrixStackIn = oldStack;
				SUVBO vbo = vertexBufferCacheUsed.get(tileEntityIn.getPos());
				if (vbo != null) vbo.render(matrixStackIn);
			}
		}
		matrixStackIn = oldStack;
		
		Minecraft.getInstance().getProfiler().endStartSection("renderTileEntities");
		matrixStackIn.push();
		matrixStackIn.scale(1f / tileEntityIn.unitsPerBlock, 1f / tileEntityIn.unitsPerBlock, 1f / tileEntityIn.unitsPerBlock);
		for (SmallUnit value : tileEntityIn.getBlockMap().values()) {
			if (value.tileEntity != null) {
				tileEntityIn.getFakeWorld().isRemote = true;
				matrixStackIn.push();
				matrixStackIn.translate(value.pos.getX(), value.pos.getY() - 64, value.pos.getZ());
				TileEntity tileEntity = value.tileEntity;
				TileEntityRenderer<TileEntity> renderer = TileEntityRendererDispatcher.instance.getRenderer(tileEntity);
				int matrixSize = matrixStackIn.stack.size();
				boolean isExceptionPresent = false;
				String exceptionAt = "";
				if (renderer != null) {
					try {
						renderer.render(tileEntity, partialTicks, matrixStackIn, bufferIn.getWrapped(),
								LightTexture.packLight(
										Math.max(LightTexture.getLightBlock(combinedLightIn), tileEntityIn.getFakeWorld().getLightFor(LightType.BLOCK, value.pos)),
										Math.max(LightTexture.getLightSky(combinedLightIn), tileEntityIn.getFakeWorld().getLightFor(LightType.SKY, value.pos))
								), combinedOverlayIn);
					} catch (Throwable ignored) {
						isExceptionPresent = true;
						for (StackTraceElement element : ignored.getStackTrace()) {
							if (element.getClassName().equals(renderer.getClass().getName())) {
								exceptionAt = element.getClassName() + "." + element.getMethodName() + "(" + element.getFileName() + ":" + element.getLineNumber() + ")";
							}
						}
					}
				}
				if (matrixSize != matrixStackIn.stack.size()) {
					LOGGER.log(Level.WARN, ("What's going on? Tile Entity renderer for " + tileEntity.getType().getRegistryName() + " missed " + (matrixStackIn.stack.size() - matrixSize) + " pops." + (isExceptionPresent ? (" An exception was thrown:\n" + exceptionAt) : " No exceptions were found.")));
				}
				while (matrixStackIn.stack.size() != matrixSize) {
					matrixStackIn.pop();
				}
				matrixStackIn.pop();
			}
		}
		matrixStackIn.pop();
		
		Minecraft.getInstance().getProfiler().endStartSection("renderEmpty");
		boolean isEmpty = tileEntityIn.getBlockMap().isEmpty();
		if (!isEmpty) {
			isEmpty = true;
			for (SmallUnit value : tileEntityIn.getBlockMap().values()) {
				if (!value.state.isAir()) {
					isEmpty = false;
					break;
				}
			}
			if (isEmpty) {
				tileEntityIn.getBlockMap().clear();
			}
		}
		if (isEmpty) {
			matrixStackIn.push();
			matrixStackIn.scale(4, 4, 4);
			RenderSystem.disableTexture();
			matrixStackIn.scale(1f / tileEntityIn.unitsPerBlock, 1f / tileEntityIn.unitsPerBlock, 1f / tileEntityIn.unitsPerBlock);
			renderHalf(matrixStackIn, bufferIn, combinedOverlayIn, combinedLightIn, tileEntityIn);
			matrixStackIn.push();
			matrixStackIn.translate(tileEntityIn.unitsPerBlock / 4f, 0, tileEntityIn.unitsPerBlock / 4f);
			matrixStackIn.rotate(new Quaternion(0, 180, 0, true));
			renderHalf(matrixStackIn, bufferIn, combinedOverlayIn, combinedLightIn, tileEntityIn);
			matrixStackIn.pop();
			RenderSystem.enableTexture();
			matrixStackIn.pop();
		}

//		matrixStackIn.push();
//		matrixStackIn.scale(1f / tileEntityIn.unitsPerBlock, 1f / tileEntityIn.unitsPerBlock, 1f / tileEntityIn.unitsPerBlock);
		
		Minecraft.getInstance().getProfiler().endStartSection("renderEntities");
		for (Entity entity : tileEntityIn.getEntitiesById().values()) {
			MatrixStack finalMatrixStackIn = new MatrixStack();
			finalMatrixStackIn.stack.add(oldStack.getLast());
			
			finalMatrixStackIn.push();
			finalMatrixStackIn.scale(1f / tileEntityIn.unitsPerBlock, 1f / tileEntityIn.unitsPerBlock, 1f / tileEntityIn.unitsPerBlock);
			finalMatrixStackIn.translate(
					(entity.getPositionVec().getX()),
					(entity.getPositionVec().getY() - 64),
					(entity.getPositionVec().getZ())
			);
			EntityRenderer<Entity> renderer = (EntityRenderer<Entity>) Minecraft.getInstance().getRenderManager().getRenderer(entity);
			int matrixSize = finalMatrixStackIn.stack.size();
			boolean isExceptionPresent = false;
			String exceptionAt = "";
			try {
				if (Minecraft.getInstance().getRenderManager().isDebugBoundingBox()) {
					IVertexBuilder builder = bufferIn.getBuffer(RenderType.getLines());
					AxisAlignedBB bb = entity.getBoundingBox();
					finalMatrixStackIn.push();
					finalMatrixStackIn.translate(-entity.getPosX(), -entity.getPosY(), -entity.getPosZ());
					WorldRenderer.drawBoundingBox(finalMatrixStackIn, builder, bb, 1, 1, 1, 1.0F);
					if (entity instanceof LivingEntity) {
						bb = new AxisAlignedBB(
								entity.getEyePosition(partialTicks).x - (entity.getSize(entity.getPose()).width / 2),
								entity.getEyePosition(partialTicks).y,
								entity.getEyePosition(partialTicks).z - (entity.getSize(entity.getPose()).width / 2),
								entity.getEyePosition(partialTicks).x + (entity.getSize(entity.getPose()).width / 2),
								entity.getEyePosition(partialTicks).y + 0.1f,
								entity.getEyePosition(partialTicks).z + (entity.getSize(entity.getPose()).width / 2)
						);
						WorldRenderer.drawBoundingBox(finalMatrixStackIn, builder, bb, 1, 0, 0, 1.0F);
					}
					Vector3d vector3d = entity.getLook(partialTicks);
					finalMatrixStackIn.pop();
					Matrix4f matrix4f = finalMatrixStackIn.getLast().getMatrix();
					builder.pos(matrix4f, 0.0F, entity.getEyeHeight(), 0.0F).color(0, 0, 255, 255).endVertex();
					builder.pos(matrix4f, (float) (vector3d.x * 2.0D), (float) ((double) entity.getEyeHeight() + vector3d.y * 2.0D), (float) (vector3d.z * 2.0D)).color(0, 0, 255, 255).endVertex();
				}
				Vector3d offset = renderer.getRenderOffset(entity, partialTicks);
				finalMatrixStackIn.translate(offset.getX(), offset.getY(), offset.getZ());
				renderer.render(entity, entity.getYaw(partialTicks), partialTicks, finalMatrixStackIn, bufferIn.getWrapped(), combinedLightIn);
			} catch (Throwable err) {
				isExceptionPresent = true;
				for (StackTraceElement element : err.getStackTrace()) {
					if (element.getClassName().equals(renderer.getClass().getName())) {
						exceptionAt = element.getClassName() + "." + element.getMethodName() + "(" + element.getFileName() + ":" + element.getLineNumber() + ")";
					}
				}
			}
			if (matrixSize != finalMatrixStackIn.stack.size()) {
				LOGGER.log(Level.WARN, ("What's going on? Entity renderer for " + entity.getType().getRegistryName() + " missed " + (finalMatrixStackIn.stack.size() - matrixSize) + " pops." + (isExceptionPresent ? (" An exception was thrown:\n" + exceptionAt) : " No exceptions were found.")));
			}
			while (finalMatrixStackIn.stack.size() != matrixSize) {
				finalMatrixStackIn.pop();
			}
			finalMatrixStackIn.pop();
		}
		Minecraft.getInstance().getProfiler().endSection();
		
		tileEntityIn.getProfiler().endTick();
//		oldStack.pop();
		
		if (Minecraft.getInstance().getRenderManager().isDebugBoundingBox()) {
			IVertexBuilder lines = bufferIn.getBuffer(RenderType.getLines());
			WorldRenderer.drawBoundingBox(
					matrixStackIn, lines,
					tileEntityIn.getRenderBoundingBox().offset(-tileEntityIn.getPos().getX(), -tileEntityIn.getPos().getY(), -tileEntityIn.getPos().getZ()), 0.5f, 0, 0.5f, 1
			);
		}
//		if (!Minecraft.getInstance().debugRenderer.toggleChunkBorders()) {
//			for (int x = 0; x <= tileEntityIn.unitsPerBlock; x++) {
//				for (int y = 0; y <= tileEntityIn.unitsPerBlock; y++) {
//					WorldRenderer.drawBoundingBox(
//							matrixStackIn, lines,
//							x / (float) tileEntityIn.unitsPerBlock, y / (float) tileEntityIn.unitsPerBlock, 0,
//							0, 1f / tileEntityIn.unitsPerBlock, 0,
//							1, 1, 0, 1
//					);
//					WorldRenderer.drawBoundingBox(
//							matrixStackIn, lines,
//							0, y / (float) tileEntityIn.unitsPerBlock, x / (float) tileEntityIn.unitsPerBlock,
//							0, 1f / tileEntityIn.unitsPerBlock, 0,
//							1, 1, 0, 1
//					);
//				}
//			}
//			WorldRenderer.drawBoundingBox(
//					matrixStackIn,lines,
//					0,0,0,
//					16/(float)tileEntityIn.unitsPerBlock,16/(float)tileEntityIn.unitsPerBlock,16/(float)tileEntityIn.unitsPerBlock,
//					0,0,1,1
//			);
//		}
//		Minecraft.getInstance().debugRenderer.toggleChunkBorders();

//		if (tileEntityIn.worldClient != null) {
//			for (Particle particle : tileEntityIn.worldClient.particles) {
//				particle.getRenderType().beginRender(bufferIn.buffer.getBuffer(particle.),Minecraft.getInstance().textureManager);
//				particle.renderParticle(bufferIn.getBuffer(),new ActiveRenderInfo(),partialTicks);
//				particle.getRenderType().finishRender(Tessellator.getInstance());
//				particle.tick();
//			}
//		}
		
		Minecraft.getInstance().getProfiler().endSection();
	}
	
	public static void renderHalf(MatrixStack matrixStackIn, BufferCache bufferIn, int combinedOverlayIn, int combinedLightIn, UnitTileEntity tileEntityIn) {
		if (tileEntityIn.isNatural) return;
		Minecraft.getInstance().getProfiler().startSection("renderCorner1");
		renderCorner(matrixStackIn, bufferIn, combinedOverlayIn, combinedLightIn);
		Minecraft.getInstance().getProfiler().endStartSection("renderCorner2");
		matrixStackIn.push();
		matrixStackIn.translate(0, tileEntityIn.unitsPerBlock / 4f, 0);
		matrixStackIn.rotate(new Quaternion(90, 0, 0, true));
		renderCorner(matrixStackIn, bufferIn, combinedOverlayIn, combinedLightIn);
		Minecraft.getInstance().getProfiler().endStartSection("renderCorner3");
		matrixStackIn.pop();
		matrixStackIn.push();
		matrixStackIn.translate(0, 0, tileEntityIn.unitsPerBlock / 4f);
		matrixStackIn.rotate(new Quaternion(0, 90, 0, true));
		renderCorner(matrixStackIn, bufferIn, combinedOverlayIn, combinedLightIn);
		Minecraft.getInstance().getProfiler().endStartSection("renderCorner4");
		matrixStackIn.pop();
		matrixStackIn.push();
		matrixStackIn.translate(0, tileEntityIn.unitsPerBlock / 4f, tileEntityIn.unitsPerBlock / 4f);
		matrixStackIn.rotate(new Quaternion(180, 0, 0, true));
		renderCorner(matrixStackIn, bufferIn, combinedOverlayIn, combinedLightIn);
		matrixStackIn.pop();
		Minecraft.getInstance().getProfiler().endSection();
	}
	
	public static void renderCorner(MatrixStack matrixStackIn, BufferCache bufferIn, int combinedOverlayIn, int combinedLightIn) {
		Minecraft.getInstance().getProfiler().startSection("renderCube1");
		matrixStackIn.push();
		matrixStackIn.scale(0.001f, 1, 1);
		renderCube(1, 1, 0, 0, 0, 0, bufferIn.getBuffer(RenderType.getEntitySolid(new ResourceLocation("textures/block/white_concrete.png"))), combinedOverlayIn, combinedLightIn, matrixStackIn, true);
		Minecraft.getInstance().getProfiler().endStartSection("renderCube2");
		matrixStackIn.pop();
		matrixStackIn.push();
		matrixStackIn.scale(1, 0.001f, 1);
		renderCube(1, 1, 0, 0, 0, 0, bufferIn.getBuffer(RenderType.getEntitySolid(new ResourceLocation("textures/block/white_concrete.png"))), combinedOverlayIn, combinedLightIn, matrixStackIn, true);
		Minecraft.getInstance().getProfiler().endStartSection("renderCube3");
		matrixStackIn.pop();
		matrixStackIn.push();
		matrixStackIn.scale(1, 1, 0.001f);
		renderCube(1, 1, 0, 0, 0, 0, bufferIn.getBuffer(RenderType.getEntitySolid(new ResourceLocation("textures/block/white_concrete.png"))), combinedOverlayIn, combinedLightIn, matrixStackIn, true);
		Minecraft.getInstance().getProfiler().endStartSection("renderCube4");
		matrixStackIn.pop();
		Minecraft.getInstance().getProfiler().endSection();
	}
	
	@Override
	public void render(UnitTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
		if (SmallerUnitsConfig.CLIENT.useExperimentalRendererPt2.get()) return;
		if (!bufferIn.getClass().equals(IRenderTypeBuffer.Impl.class))
			bufferIn = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
		render(tileEntityIn, partialTicks, matrixStackIn, new BufferCache(bufferIn, matrixStackIn), combinedLightIn, combinedOverlayIn);
//		render(tileEntityIn, partialTicks, matrixStackIn, BufferCacheHelper.cache, combinedLightIn, combinedOverlayIn);
	}
}
