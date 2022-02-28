package tfc.smallerunits;


import com.jozufozu.flywheel.backend.Backend;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.datafixers.util.Pair;
import com.tfc.better_fps_graph.API.Profiler;
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.*;
import net.minecraft.world.LightType;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import tfc.smallerunits.api.SmallerUnitsAPI;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.config.SmallerUnitsConfig;
import tfc.smallerunits.helpers.ClientUtils;
import tfc.smallerunits.renderer.FlywheelProgram;
import tfc.smallerunits.utils.*;
import tfc.smallerunits.utils.compat.vr.SUVRPlayer;
import tfc.smallerunits.utils.rendering.*;
import tfc.smallerunits.utils.world.client.FakeClientWorld;
import tfc.smallerunits.utils.world.client.SmallBlockReader;
import tfc.smallerunits.utils.world.server.FakeServerWorld;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class SmallerUnitsTESR extends TileEntityRenderer<UnitTileEntity> {
	public static SmallerUnitsTESR INSTANCE;

//	private static final Object2ObjectLinkedOpenHashMap<RenderType, BufferBuilder> bufferBuilderHashMap = new Object2ObjectLinkedOpenHashMap<>();
	
	public static final Object2ObjectLinkedOpenHashMap<BlockPos, Pair<AtomicReference<CompoundNBT>, SUPseudoVBO>> bufferCache = new Object2ObjectLinkedOpenHashMap<>();
	
	public static final Object2ObjectLinkedOpenHashMap<BlockPos, SURenderable> vertexBufferCacheUsed = new Object2ObjectLinkedOpenHashMap<>();
	public static final Object2ObjectLinkedOpenHashMap<BlockPos, SURenderable> vertexBufferCacheFree = new Object2ObjectLinkedOpenHashMap<>();
	private static final DefaultedMap<RenderType, BufferBuilder> buffersFlw = new DefaultedMap<RenderType, BufferBuilder>().setDefaultVal(() -> new BufferBuilder(16));
	
	private static final Quaternion quat90X = new Quaternion(90, 0, 0, true);
	private static final Quaternion quat180X = new Quaternion(180, 0, 0, true);
	private static final Quaternion quat90Y = new Quaternion(0, 90, 0, true);
	
	public static final Logger LOGGER = LogManager.getLogger();
	
	//	private static final IRenderTypeBuffer buffers = new RenderTypeBuffers().getBufferSource();
	private static final DefaultedMap<RenderType, BufferBuilder> buffers = new DefaultedMap<RenderType, BufferBuilder>().setDefaultVal(() -> new BufferBuilder(16));
	
	public static void closeRenderables(Function<SURenderable, Boolean> validator) {
		ArrayList<BlockPos> toRemove = new ArrayList<>();
		for (BlockPos pos : vertexBufferCacheFree.keySet()) {
			SURenderable renderable = vertexBufferCacheFree.get(pos);
			// TODO: figure out what the heck is causing the renderables to wind up being null, they should never be null
			if (renderable == null) toRemove.add(pos);
			else if (!renderable.isValid() || !validator.apply(renderable)) {
				renderable.delete();
				toRemove.add(pos);
			}
		}
		for (BlockPos pos : toRemove) {
			vertexBufferCacheFree.remove(pos);
		}
		toRemove.clear();
		for (BlockPos pos : vertexBufferCacheUsed.keySet()) {
			SURenderable renderable = vertexBufferCacheUsed.get(pos);
			if (renderable == null) toRemove.add(pos);
			else {
				if (!renderable.isValid() || !validator.apply(renderable)) {
					renderable.delete();
					toRemove.add(pos);
				}
			}
		}
		for (BlockPos pos : toRemove) {
			vertexBufferCacheUsed.remove(pos);
		}
	}
	
	public static void renderCube(float r, float g, float b, float x, float y, float z, IVertexBuilder builder, int combinedOverlay, int combinedLight, MatrixStack matrixStack, boolean useNormals) {
		Minecraft.getInstance().getProfiler().startSection("renderEmptySquares");
		renderSquare(r, g, b, x, y, z + 0.25f, builder, combinedOverlay, combinedLight, matrixStack, useNormals);
		matrixStack.rotate(new Quaternion(180, 0, -90, true));
		renderSquare(r, g, b, x, y, z + 0.25f, builder, combinedOverlay, combinedLight, matrixStack, useNormals);
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
		if (ModList.get().isLoaded("better_fps_graph")) Profiler.addSection("su:get_and_check_world");
		if (tileEntityIn.getFakeWorld() == null) return;
		if (tileEntityIn.getFakeWorld() instanceof FakeServerWorld) {
			CompoundNBT tag = tileEntityIn.serializeNBT();
			tileEntityIn.worldServer = null;
			tileEntityIn.handleUpdateTag(tileEntityIn.getBlockState(), tag);
		}
		
		if (vertexBufferCacheFree.containsKey(tileEntityIn.getPos())) {
			vertexBufferCacheUsed.put(tileEntityIn.getPos(), vertexBufferCacheFree.remove(tileEntityIn.getPos()));
		}

//		CompoundNBT nbt = tileEntityIn.serializeNBT();
//
//		if (tileEntityIn.getWorld() == null) return;
//
		if (ModList.get().isLoaded("better_fps_graph")) Profiler.addSection("su:matrix_copy");
		MatrixStack oldStack = matrixStackIn;
		matrixStackIn = new MatrixStack();
		matrixStackIn.stack.add(oldStack.getLast());
//
		tileEntityIn.getProfiler().startTick();
//
////		tileEntityIn.worldServer.animateTick();
//
		
		Minecraft.getInstance().getProfiler().startSection("tileEntities");
		matrixStackIn.push();
		matrixStackIn.scale(1f / tileEntityIn.unitsPerBlock, 1f / tileEntityIn.unitsPerBlock, 1f / tileEntityIn.unitsPerBlock);
		for (SmallUnit value : tileEntityIn.getBlockMap().values()) {
			if (value.tileEntity != null) {
				Minecraft.getInstance().getProfiler().startSection(() -> value.tileEntity.getType().getRegistryName().toString());
				Minecraft.getInstance().getProfiler().startSection("setup");
				tileEntityIn.getFakeWorld().isRemote = true;
				matrixStackIn.push();
				matrixStackIn.translate(value.pos.getX(), value.pos.getY() - 64, value.pos.getZ());
				TileEntity tileEntity = value.tileEntity;
				TileEntityRenderer<TileEntity> renderer = TileEntityRendererDispatcher.instance.getRenderer(tileEntity);
				int matrixSize = matrixStackIn.stack.size();
				boolean isExceptionPresent = false;
				String exceptionAt = "";
				Minecraft.getInstance().getProfiler().endStartSection("draw");
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
				Minecraft.getInstance().getProfiler().endSection();
				if (matrixSize != matrixStackIn.stack.size()) {
					LOGGER.log(Level.WARN, ("What's going on? Tile Entity renderer for " + tileEntity.getType().getRegistryName() + " missed " + (matrixStackIn.stack.size() - matrixSize) + " pops." + (isExceptionPresent ? (" An exception was thrown:\n" + exceptionAt) : " No exceptions were found.")));
				}
				while (matrixStackIn.stack.size() != matrixSize) {
					matrixStackIn.pop();
				}
				matrixStackIn.pop();
				Minecraft.getInstance().getProfiler().endSection();
			}
		}
		matrixStackIn.pop();
		Minecraft.getInstance().getProfiler().endSection();

//		matrixStackIn.push();
//		matrixStackIn.scale(1f / tileEntityIn.unitsPerBlock, 1f / tileEntityIn.unitsPerBlock, 1f / tileEntityIn.unitsPerBlock);
		
		Minecraft.getInstance().getProfiler().startSection("entities");
		for (Entity entity : tileEntityIn.getEntitiesById().values()) {
			MatrixStack finalMatrixStackIn = new MatrixStack();
			finalMatrixStackIn.stack.add(oldStack.getLast());
			
			finalMatrixStackIn.push();
			finalMatrixStackIn.scale(1f / tileEntityIn.unitsPerBlock, 1f / tileEntityIn.unitsPerBlock, 1f / tileEntityIn.unitsPerBlock);
			finalMatrixStackIn.translate(
					(MathHelper.lerp(partialTicks, entity.lastTickPosX, entity.getPositionVec().getX())),
					(MathHelper.lerp(partialTicks, entity.lastTickPosY, entity.getPositionVec().getY())) - 64,
					(MathHelper.lerp(partialTicks, entity.lastTickPosZ, entity.getPositionVec().getZ()))
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
				int light = LightTexture.packLight(
						Math.max(LightTexture.getLightBlock(combinedLightIn), entity.getEntityWorld().getLightFor(LightType.BLOCK, entity.getPosition())),
						Math.max(LightTexture.getLightSky(combinedLightIn), entity.getEntityWorld().getLightFor(LightType.SKY, entity.getPosition()))
				);
				renderer.render(entity, entity.getYaw(partialTicks), partialTicks, finalMatrixStackIn, bufferIn.getWrapped(), light);
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
		
		boolean isFlywheelSupported = ModList.get().isLoaded("flywheel") && Backend.getInstance().canUseVBOs() && Backend.isFlywheelWorld(tileEntityIn.getWorld());
		
		Minecraft.getInstance().getProfiler().startSection("clearRenderables");
		if (isFlywheelSupported) closeRenderables((renderable) -> renderable instanceof SUFLWVBO);
		else closeRenderables((renderable) -> renderable instanceof SUVBO);
		Minecraft.getInstance().getProfiler().endSection();
		
		Minecraft.getInstance().getProfiler().startSection("testResortNeeded");
		boolean needsResort = false;
		if (ModList.get().isLoaded("better_fps_graph")) Profiler.addSection("su:refresh_check");
		{
			PlayerEntity playerEntity = Minecraft.getInstance().player;
			if (playerEntity != null) {
				Vector3d lastPos = new Vector3d(playerEntity.prevPosX, playerEntity.prevPosY, playerEntity.prevPosZ);
				Vector3d currentPos = playerEntity.getPositionVec();

//				float scale = ResizingUtils.getSize(playerEntity);
				float scale = 1;
//				System.out.println(tileEntityIn.getPos().distanceSq(currentPos, true));
//				if (tileEntityIn.getPos().distanceSq(currentPos, true) < 12) {
				if (tileEntityIn.getPos().distanceSq(currentPos, true) < 6) {
					scale = tileEntityIn.unitsPerBlock;
				}
				lastPos = lastPos.mul(scale, scale, scale);
				currentPos = currentPos.mul(scale, scale, scale);
				lastPos = new Vector3d(Math.round(lastPos.x), Math.round(lastPos.y), Math.round(lastPos.z));
				currentPos = new Vector3d(Math.round(currentPos.x), Math.round(currentPos.y), Math.round(currentPos.z));
				if (!lastPos.equals(currentPos)) {
//					tileEntityIn.needsRefresh(true);
					needsResort = true;
//					if (vertexBufferCacheUsed.containsKey(tileEntityIn.getPos())) {
//						vertexBufferCacheFree.put(tileEntityIn.getPos(), vertexBufferCacheUsed.remove(tileEntityIn.getPos()));
//					}
				}
			}
		}
		Minecraft.getInstance().getProfiler().endSection();
		
		Minecraft.getInstance().getProfiler().startSection("blocks");
		if (ModList.get().isLoaded("better_fps_graph")) Profiler.addSection("su:light");
		tileEntityIn.worldClient.get().lightManager.tick(SmallerUnitsConfig.CLIENT.lightingUpdatesPerFrame.get(), true, true);
		// TODO: make it render without vbos if the player is not in the same world
		if (ModList.get().isLoaded("better_fps_graph")) Profiler.endSection();
		if (!tileEntityIn.getBlockMap().isEmpty()) {
			if (
					tileEntityIn.getWorld() != null &&
							tileEntityIn.getWorld().equals(Minecraft.getInstance().world) &&
							SmallerUnitsConfig.CLIENT.useVBOS.get()
			) {
				boolean isRefreshing = tileEntityIn.needsRefresh(false) || !vertexBufferCacheUsed.containsKey(tileEntityIn.getPos());
				if (isRefreshing || needsResort || !vertexBufferCacheUsed.containsKey(tileEntityIn.getPos())) {
					if (vertexBufferCacheFree.containsKey(tileEntityIn.getPos())) {
						vertexBufferCacheUsed.put(tileEntityIn.getPos(), vertexBufferCacheFree.get(tileEntityIn.getPos()));
					}
					{
						HashMap<RenderType, BufferBuilder> buffersUsed = new HashMap<>();
						boolean renderedAnything = renderWorld(tileEntityIn, isRefreshing, isFlywheelSupported ? buffersFlw : buffers, buffersUsed);

//					if (ModList.get().isLoaded("flywheel") && false) {
//						for (CustomBuffer.CustomVertexBuilder builder : redirection.builders) {
//							if (builder.vertices.size() == 0) continue;
//							FlywheelVertexBuilder buffer = new FlywheelVertexBuilder(builder.vertices.size() * FlywheelVertexFormats.BLOCK.getStride() * 2);
////							int number = 0;
//							for (CustomBuffer.Vertex vertex : builder.vertices) {
////								number += 1;
////								System.out.println(number);
//								buffer.pos(vertex.x, vertex.y, vertex.z);
//								buffer.color(vertex.r, vertex.g, vertex.b, vertex.a);
//								buffer.tex(vertex.u, vertex.v);
//								buffer.lightmap((int) (vertex.lu * 15), (int) (vertex.lv * 15));
//								buffer.normal(vertex.nx, vertex.ny, vertex.nz);
//							}
//							SmallerUnitsProgram shader = FlywheelProgram.UNIT.getProgram(new ResourceLocation("smallerunits:unit_shader"));
//							shader.bind();
//							shader.uploadViewProjection(GameRendererHelper.matrix);
//							shader.uploadCameraPos(
//									Minecraft.getInstance().getRenderManager().info.getProjectedView().x,
//									Minecraft.getInstance().getRenderManager().info.getProjectedView().y,
//									Minecraft.getInstance().getRenderManager().info.getProjectedView().z
//							);
//							IndexedModel mdl = new IndexedModel(new SUModel(FlywheelVertexFormats.BLOCK, buffer.unwrap(), buffer.vertices()));
////							RenderSystem.pushMatrix();
////							RenderSystem.loadIdentity();
////							RenderSystem.multMatrix(oldStack.getLast().getMatrix());
//							RenderType.getSolid().setupRenderState();
//							mdl.setupState();
//							mdl.drawCall();
//							mdl.clearState();
//							RenderType.getSolid().clearRenderState();
////							RenderSystem.popMatrix();
//							shader.unbind();
//							buffer.close();
//							mdl.delete();
//						}
//					} else
						if (isFlywheelSupported) {
							SURenderable suvbo;
							if (!vertexBufferCacheUsed.containsKey(tileEntityIn.getPos())) {
								if (isRefreshing && vertexBufferCacheUsed.containsKey(tileEntityIn.getPos()))
									suvbo = vertexBufferCacheUsed.remove(tileEntityIn.getPos());
								else suvbo = new SUFLWVBO();
							} else suvbo = vertexBufferCacheUsed.remove(tileEntityIn.getPos());
							if (suvbo == null) suvbo = new SUFLWVBO();
							if (isRefreshing) suvbo.markAllUnused();
							SURenderable finalSuvbo = suvbo;
							
							buffersUsed.forEach((type, buffer) -> {
								if (FlywheelProgram.UNIT != null) {
									if (RenderTypeHelper.isTransparent(type)) {
										if (ModList.get().isLoaded("better_fps_graph"))
											Profiler.addSection("su:sort_quads", 0.2, 0.5, 0.7);
										buffer.sortVertexData(
												(float) Minecraft.getInstance().getRenderManager().info.getProjectedView().getX() - tileEntityIn.getPos().getX(),
												(float) Minecraft.getInstance().getRenderManager().info.getProjectedView().getY() - tileEntityIn.getPos().getY(),
												(float) Minecraft.getInstance().getRenderManager().info.getProjectedView().getZ() - tileEntityIn.getPos().getZ()
										);
									}
									if (ModList.get().isLoaded("better_fps_graph"))
										Profiler.addSection("su:upload_vbo", 0.2, 0.2, 0.7);
									finalSuvbo.uploadTerrain(type, buffer);
									if (ModList.get().isLoaded("better_fps_graph")) Profiler.endSection();

//								SmallerUnitsProgram shader = FlywheelProgram.UNIT.getProgram(new ResourceLocation("smallerunits:unit_shader"));
//								shader.bind();
//								Matrix4f matrix4f = GameRendererHelper.matrix.copy();
//								matrix4f.mul(oldStack.getLast().getMatrix());
//								shader.uploadViewProjection(matrix4f);
//								shader.uploadCameraPos(
//										Minecraft.getInstance().getRenderManager().info.getProjectedView().x,
//										Minecraft.getInstance().getRenderManager().info.getProjectedView().y,
//										Minecraft.getInstance().getRenderManager().info.getProjectedView().z
//								);
//								try {
//									IndexedModel mdl = new IndexedModel(new SUModel(FlywheelVertexFormats.BLOCK, buffer));
//									type.setupRenderState();
//									mdl.setupState();
//									mdl.drawCall();
//									mdl.clearState();
//									type.clearRenderState();
//									mdl.delete();
//								} catch (IndexOutOfBoundsException ignored) {
//									// TODO: look for a way to not need to do this
//									buffer.reset();
//									buffer.discard();
//									buffers.remove(type);
//								}
//								shader.unbind();
//
//								if (vertexBufferCacheUsed.containsKey(tileEntityIn.getPos())) {
//									vertexBufferCacheFree.put(tileEntityIn.getPos(), vertexBufferCacheUsed.remove(tileEntityIn.getPos()));
//								}
								}
								
								if (ModList.get().isLoaded("better_fps_graph")) Profiler.endSection();
							});
							vertexBufferCacheUsed.put(tileEntityIn.getPos(), suvbo);
						} else {
							if (renderedAnything && SmallerUnitsConfig.CLIENT.useVBOS.get()) {
								SURenderable suvbo;
								if (!vertexBufferCacheUsed.containsKey(tileEntityIn.getPos())) {
									if (isRefreshing && vertexBufferCacheUsed.containsKey(tileEntityIn.getPos()))
										suvbo = vertexBufferCacheUsed.remove(tileEntityIn.getPos());
									else suvbo = new SUVBO();
								} else suvbo = vertexBufferCacheUsed.remove(tileEntityIn.getPos());
								if (suvbo == null) suvbo = new SUVBO();
								if (isRefreshing) suvbo.markAllUnused();
								SURenderable finalSuvbo = suvbo;
								buffers.forEach((type, buffer) -> {
									if (RenderTypeHelper.isTransparent(type)) {
										if (ModList.get().isLoaded("better_fps_graph"))
											Profiler.addSection("su:sort_quads", 0.2, 0.5, 0.7);
										buffer.sortVertexData(
												(float) Minecraft.getInstance().getRenderManager().info.getProjectedView().getX() - tileEntityIn.getPos().getX(),
												(float) Minecraft.getInstance().getRenderManager().info.getProjectedView().getY() - tileEntityIn.getPos().getY(),
												(float) Minecraft.getInstance().getRenderManager().info.getProjectedView().getZ() - tileEntityIn.getPos().getZ()
										);
									}
									if (ModList.get().isLoaded("better_fps_graph"))
										Profiler.addSection("su:upload_vbo", 0.2, 0.2, 0.7);
									finalSuvbo.uploadTerrain(type, buffer);
									if (ModList.get().isLoaded("better_fps_graph")) Profiler.endSection();
								});
								vertexBufferCacheUsed.put(tileEntityIn.getPos(), suvbo);
							} else {
								buffers.forEach((type, buffer) -> {
									buffer.sortVertexData(
											(float) Minecraft.getInstance().getRenderManager().info.getProjectedView().getX() - tileEntityIn.getPos().getX(),
											(float) Minecraft.getInstance().getRenderManager().info.getProjectedView().getY() - tileEntityIn.getPos().getY(),
											(float) Minecraft.getInstance().getRenderManager().info.getProjectedView().getZ() - tileEntityIn.getPos().getZ()
									);
									RenderSystem.pushMatrix();
									RenderSystem.loadIdentity();
									RenderSystem.multMatrix(oldStack.getLast().getMatrix());
									type.finish(buffer,
											0, 0, 0
									);
									RenderSystem.popMatrix();
								});
							}
						}
					}
					if (SmallerUnitsConfig.CLIENT.useVBOS.get()) {
						if (ModList.get().isLoaded("better_fps_graph"))
							Profiler.addSection("su:draw_vbo", 0.4, 0.2, 0.8);
						matrixStackIn = oldStack;
						SURenderable vbo = vertexBufferCacheUsed.get(tileEntityIn.getPos());
						if (vbo != null) vbo.render(matrixStackIn);
						if (ModList.get().isLoaded("better_fps_graph")) Profiler.endSection();
					}
				} else {
					if (ModList.get().isLoaded("better_fps_graph")) Profiler.addSection("su:draw_vbo", 0.4, 0.2, 0.8);
					matrixStackIn = oldStack;
					SURenderable vbo = vertexBufferCacheUsed.get(tileEntityIn.getPos());
					if (vbo != null) vbo.render(matrixStackIn);
					if (ModList.get().isLoaded("better_fps_graph")) Profiler.endSection();
				}
			} else {
				BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
				FakeClientWorld fakeWorld = ((FakeClientWorld) tileEntityIn.getFakeWorld());
				matrixStackIn.push();
				matrixStackIn.scale(1f / tileEntityIn.unitsPerBlock, 1f / tileEntityIn.unitsPerBlock, 1f / tileEntityIn.unitsPerBlock);
				for (SmallUnit value : fakeWorld.blockMap.values()) {
					matrixStackIn.push();
					matrixStackIn.translate(value.pos.getX(), value.pos.getY() - 64, value.pos.getZ());
					RenderType type = RenderType.getSolid();
					for (RenderType blockRenderType : RenderType.getBlockRenderTypes())
						if (RenderTypeLookup.canRenderInLayer(value.state, blockRenderType))
							type = blockRenderType;
					IVertexBuilder buffer = bufferIn.getBuffer(type);
					dispatcher.renderModel(value.state, value.pos, fakeWorld, matrixStackIn, buffer, true, new Random(value.pos.toLong()));
					matrixStackIn.pop();
				}
				matrixStackIn.pop();
			}
		}
		Minecraft.getInstance().getProfiler().endSection();
		
		Minecraft.getInstance().getProfiler().startSection("getAtlasSize");
		Texture texture = Minecraft.getInstance().getTextureManager().getTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getGlTextureId());
		int width = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
		int height = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
		
		Minecraft.getInstance().getProfiler().endStartSection("destroyProgress");
		for (SortedSet<DestroyBlockProgress> value : Minecraft.getInstance().worldRenderer.damageProgress.values()) {
			for (DestroyBlockProgress destroyBlockProgress : value) {
				if (destroyBlockProgress.getPosition().equals(tileEntityIn.getPos())) {
					int phase = destroyBlockProgress.getPartialBlockDamage() - 1;
					Entity entity = tileEntityIn.getWorld().getEntityByID(destroyBlockProgress.miningPlayerEntId);
					UnitRaytraceContext context = UnitRaytraceHelper.raytraceBlock(
							tileEntityIn,
							entity,
							false,
							tileEntityIn.getPos(),
							Optional.empty(),
							Optional.of(SUVRPlayer.getPlayer$(entity))
					);
					BlockState miningState = tileEntityIn.getFakeWorld().getBlockState(context.posHit);
					Minecraft.getInstance().getProfiler().startSection("setup_" + miningState.toString());
					matrixStackIn.push();
					matrixStackIn.scale(1f / tileEntityIn.unitsPerBlock, 1f / tileEntityIn.unitsPerBlock, 1f / tileEntityIn.unitsPerBlock);
					matrixStackIn.translate(context.posHit.getX(), context.posHit.getY() - 64, context.posHit.getZ());
					Minecraft.getInstance().getProfiler().endStartSection("getModel");
					IBakedModel model = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(miningState);
					Minecraft.getInstance().getProfiler().endStartSection("getAllQuads");
					
					IModelData data = EmptyModelData.INSTANCE;
					TileEntity te = tileEntityIn.getTileEntity(context.posHit);
					if (te != null) data = te.getModelData();
					data = model.getModelData(tileEntityIn.getFakeWorld(), context.posHit, miningState, data);
					
					ArrayList<BakedQuad> allQuads = new ArrayList<>();
					for (RenderType blockRenderType : RenderType.getBlockRenderTypes()) {
						ForgeHooksClient.setRenderLayer(blockRenderType);
						for (Direction direction : Direction.values())
							for (BakedQuad quad : model.getQuads(miningState, direction, new Random(miningState.getPositionRandom(context.posHit)), data))
								if (!allQuads.contains(quad))
									allQuads.add(quad);
						for (BakedQuad quad : model.getQuads(miningState, null, new Random(miningState.getPositionRandom(context.posHit)), data))
							if (!allQuads.contains(quad))
								allQuads.add(quad);
					}
					ForgeHooksClient.setRenderLayer(null);
					
					Minecraft.getInstance().getProfiler().endStartSection("draw");
					{
						IVertexBuilder builder1 = bufferIn.getBuffer(ModelBakery.DESTROY_RENDER_TYPES.get(phase + 1));
						for (BakedQuad quad : allQuads) {
							// quad unpacking
							float x = 0, y = 0, z = 0;
							float u = 0, v = 0;
							float nx = 0, ny = 0, nz = 0;
							for (int i1 = 0; i1 < quad.getVertexData().length; i1++) {
								int vertexDatum = quad.getVertexData()[i1];
								float f = Float.intBitsToFloat(vertexDatum);
								
								short vElement = (short) (i1 % 8);
								
								// wow this looks so much better in J16
								switch (vElement) {
									// get vertex position
									case 0:
										x = f;
										break;
									case 1:
										y = f;
										break;
									case 2:
										z = f;
										break;
									// texture coords
									case 4:
										u = f;
										break;
									case 5:
										v = f;
										break;
									case 7:
										float left = quad.getSprite().getMinU();
										float sizeX = (quad.getSprite().getMaxU() - quad.getSprite().getMinU());
										float right = quad.getSprite().getMinV();
										float sizeY = (quad.getSprite().getMaxV() - quad.getSprite().getMinV());
										
										// normalize the vertex's texture coords
										u = (u - left) / sizeX;
										v = (v - right) / sizeY;
										
										Vector4f vector4f = new Vector4f((float) x, (float) y, (float) z, 1);
										vector4f.transform(matrixStackIn.getLast().getMatrix());
										builder1.addVertex(
												vector4f.getX(),
												vector4f.getY(),
												vector4f.getZ(),
												1,
												1,
												1,
												1,
												u, v,
												combinedOverlayIn, combinedLightIn,
												nx, ny, nz
										);
										break;
								}
							}
						}
					}
					Minecraft.getInstance().getProfiler().endSection();
					matrixStackIn.pop();
				}
			}
		}
		Minecraft.getInstance().getProfiler().endSection();
		
		matrixStackIn = oldStack;
		
		Minecraft.getInstance().getProfiler().startSection("checkEmpty");
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
		Minecraft.getInstance().getProfiler().endStartSection("renderEmpty");
		boolean doRender = isEmpty || ClientUtils.isHammerHeld();
		if (doRender) {
			matrixStackIn.push();
			matrixStackIn.scale(4, 4, 4);
			RenderSystem.disableTexture();
			matrixStackIn.scale(1f / tileEntityIn.unitsPerBlock, 1f / tileEntityIn.unitsPerBlock, 1f / tileEntityIn.unitsPerBlock);
			renderHalf(matrixStackIn, bufferIn, combinedOverlayIn, combinedLightIn, tileEntityIn, !isEmpty, tileEntityIn.isNatural);
			matrixStackIn.push();
			matrixStackIn.translate(tileEntityIn.unitsPerBlock / 4f, 0, tileEntityIn.unitsPerBlock / 4f);
			matrixStackIn.rotate(new Quaternion(0, 180, 0, true));
			renderHalf(matrixStackIn, bufferIn, combinedOverlayIn, combinedLightIn, tileEntityIn, !isEmpty, tileEntityIn.isNatural);
			matrixStackIn.pop();
			RenderSystem.enableTexture();
			matrixStackIn.pop();
		}
		
		Minecraft.getInstance().getProfiler().endStartSection("RULE");
		SmallerUnitsAPI.postRenderUnitLast(bufferIn.buffer, tileEntityIn);
		Minecraft.getInstance().getProfiler().endSection();
		
		tileEntityIn.getProfiler().endTick();
//		oldStack.pop();
		
		if (Minecraft.getInstance().getRenderManager().isDebugBoundingBox()) {
			Minecraft.getInstance().getProfiler().startSection("drawRenderShape");
			IVertexBuilder lines = bufferIn.getBuffer(RenderType.getLines());
			WorldRenderer.drawBoundingBox(
					matrixStackIn, lines,
					tileEntityIn.getRenderBoundingBox().offset(-tileEntityIn.getPos().getX(), -tileEntityIn.getPos().getY(), -tileEntityIn.getPos().getZ()), 0.5f, 0, 0.5f, 1
			);
			Minecraft.getInstance().getProfiler().endSection();
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
		
		if (ModList.get().isLoaded("better_fps_graph")) Profiler.endSection();
	}
	
	private static boolean renderWorld(UnitTileEntity tileEntityIn, boolean isRefreshing, DefaultedMap<RenderType, BufferBuilder> buffers, HashMap<RenderType, BufferBuilder> buffersUsed) {
		MatrixStack stack = new MatrixStack();
//					MatrixStack stack = oldStack;
		BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
		FakeClientWorld fakeWorld = ((FakeClientWorld) tileEntityIn.getFakeWorld());
		SmallBlockReader blockReader = new SmallBlockReader(fakeWorld);
		stack.push();
		stack.scale(1f / tileEntityIn.unitsPerBlock, 1f / tileEntityIn.unitsPerBlock, 1f / tileEntityIn.unitsPerBlock);
		boolean renderedAnything = false;
		CustomBuffer redirection = new CustomBuffer();
		for (RenderType type : RenderType.getBlockRenderTypes()) {
			if (!RenderTypeHelper.isTransparent(type)) if (!isRefreshing) continue;
			boolean hasSetType = false;
			for (SmallUnit value : fakeWorld.blockMap.values()) {
				stack.push();
				stack.translate(value.pos.getX(), value.pos.getY() - 64, value.pos.getZ());
				
				if (RenderTypeLookup.canRenderInLayer(value.state, type)) {
					renderedAnything = true;
					
					if (!hasSetType) {
						if (ModList.get().isLoaded("better_fps_graph"))
							Profiler.addSection("su:set_render_layer");
						ForgeHooksClient.setRenderLayer(type);
						if (ModList.get().isLoaded("better_fps_graph")) Profiler.endSection();
						hasSetType = true;
					}
					
					if (ModList.get().isLoaded("better_fps_graph"))
						Profiler.addSection("su:render_block", 0.3, 0.4, 0.8);
					IVertexBuilder buffer;
					if (ModList.get().isLoaded("flywheel") && Backend.getInstance().canUseVBOs() && false) {
						buffer = redirection.getBuffer(type);
					} else {
						BufferBuilder bufferBuilder = buffers.get(type);
						buffer = bufferBuilder;
						if (!bufferBuilder.isDrawing())
							bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
						if (!buffersUsed.containsKey(type)) buffersUsed.put(type, bufferBuilder);
					}
//								IModelData data = ModelDataManager.getModelData(fakeWorld, value.pos);
//								if (data == null) {
//								}
					IModelData data = EmptyModelData.INSTANCE;
					if (value.tileEntity != null) data = value.tileEntity.getModelData();
					dispatcher.renderModel(value.state, value.pos, blockReader, stack, buffer, true, new Random(value.pos.toLong()), data);

//								{
//									IBakedModel model = dispatcher.getModelForState(value.state);
//									boolean flag = Minecraft.getInstance().gameSettings.ambientOcclusionStatus.equals(AmbientOcclusionStatus.MAX) && value.state.getLightValue(fakeWorld, value.pos) == 0 && model.isAmbientOcclusion();
//									Vector3d vector3d = value.state.getOffset(fakeWorld, value.pos);
//									stack.push();
//									stack.translate(vector3d.x, vector3d.y, vector3d.z);
//									Random r = new Random(value.pos.toLong());
//									if (value.state.getRenderType() == BlockRenderType.MODEL) {
//										if (flag) {
//											dispatcher.getBlockModelRenderer().renderModelSmooth(fakeWorld, dispatcher.getModelForState(value.state), value.state, value.pos, stack, buffer, true, r, value.state.getPositionRandom(value.pos), OverlayTexture.NO_OVERLAY, data);
//										} else {
//											dispatcher.getBlockModelRenderer().renderModelFlat(fakeWorld, dispatcher.getModelForState(value.state), value.state, value.pos, stack, buffer, true, r, value.state.getPositionRandom(value.pos), OverlayTexture.NO_OVERLAY, data);
//										}
//									}
//									stack.pop();
//								}
					
					if (ModList.get().isLoaded("better_fps_graph")) Profiler.endSection();
				}
				
				FluidState state = value.state.getFluidState();
				if (!state.isEmpty() && RenderTypeLookup.canRenderInLayer(state, type)) {
					IVertexBuilder buffer;
					if (ModList.get().isLoaded("flywheel") && Backend.getInstance().canUseVBOs() && false) {
						buffer = redirection.getBuffer(type);
					} else {
						BufferBuilder bufferBuilder = buffers.get(type);
						buffer = bufferBuilder;
						if (!bufferBuilder.isDrawing())
							bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
						if (!buffersUsed.containsKey(type)) buffersUsed.put(type, bufferBuilder);
					}
					TranslatingVertexBuilder translator = new TranslatingVertexBuilder(1f / tileEntityIn.unitsPerBlock, buffer);
					buffer = translator;
					translator.offset = new Vector3d(
							((int) MathUtils.getChunkOffset(value.pos.getX(), 16)) * 16,
							((int) MathUtils.getChunkOffset(value.pos.getY() - 64, 16)) * 16,
							((int) MathUtils.getChunkOffset(value.pos.getZ(), 16)) * 16
					);
//								dispatcher.renderModel(value.state, value.pos, fakeWorld, stack, buffer, true, new Random(value.pos.toLong()));
					dispatcher.renderFluid(value.pos, fakeWorld, buffer, state);
				}
				
				stack.pop();
			}
		}
		ForgeHooksClient.setRenderLayer(null);
//					for (SmallUnit value : fakeWorld.blockMap.values()) {
//						{
//							stack.push();
//							stack.translate(value.pos.getX(), value.pos.getY() - 64, value.pos.getZ());
//							renderedAnything = true;
//							RenderType type;
//							for (RenderType blockRenderType : RenderType.getBlockRenderTypes()) {
//								if (RenderTypeLookup.canRenderInLayer(value.state, blockRenderType)) {
//									type = blockRenderType;
//									IVertexBuilder buffer;
//									if (ModList.get().isLoaded("flywheel") && Backend.getInstance().canUseVBOs() && false) {
//										buffer = redirection.getBuffer(type);
//									} else {
//										BufferBuilder bufferBuilder = buffers.get(type);
//										buffer = bufferBuilder;
//										if (!bufferBuilder.isDrawing())
//											bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
//									}
////									IModelData data = ModelDataManager.getModelData(fakeWorld, value.pos);
//									ForgeHooksClient.setRenderLayer(type);
//									dispatcher.renderModel(value.state, value.pos, fakeWorld, stack, buffer, true, new Random(value.pos.toLong()));
//								}
//							}
//							stack.pop();
//						}
//						{
//							FluidState state = value.state.getFluidState();
//							if (state.isEmpty()) continue;
//							RenderType type = RenderType.getSolid();
//							for (RenderType blockRenderType : RenderType.getBlockRenderTypes())
//								if (RenderTypeLookup.canRenderInLayer(state, blockRenderType)) type = blockRenderType;
//							IVertexBuilder buffer;
//							if (ModList.get().isLoaded("flywheel") && Backend.getInstance().canUseVBOs() && false) {
//								buffer = redirection.getBuffer(type);
//							} else {
//								BufferBuilder bufferBuilder = buffers.get(type);
//								buffer = bufferBuilder;
//								if (!bufferBuilder.isDrawing())
//									bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
//							}
//							TranslatingVertexBuilder translator = new TranslatingVertexBuilder(1f / tileEntityIn.unitsPerBlock, buffer);
//							buffer = translator;
//							translator.offset = new Vector3d(
//									((int) MathUtils.getChunkOffset(value.pos.getX(), 16)) * 16,
//									((int) MathUtils.getChunkOffset(value.pos.getY() - 64, 16)) * 16,
//									((int) MathUtils.getChunkOffset(value.pos.getZ(), 16)) * 16
//							);
////						dispatcher.renderModel(value.state, value.pos, fakeWorld, stack, buffer, true, new Random(value.pos.toLong()));
//							dispatcher.renderFluid(value.pos, fakeWorld, buffer, state);
//						}
//					}
		
		stack.pop();
		
		return renderedAnything;
	}
	
	public static void renderHalf(MatrixStack matrixStackIn, BufferCache bufferIn, int combinedOverlayIn, int combinedLightIn, UnitTileEntity tileEntityIn, boolean isForced, boolean isNat) {
		if ((tileEntityIn.isNatural && (!SmallerUnitsConfig.CLIENT.forcedIndicators.get() || !isForced))) return;
//		Minecraft.getInstance().getProfiler().startSection("halves");
//		Minecraft.getInstance().getProfiler().startSection("first corner");
		renderCorner(matrixStackIn, bufferIn, combinedOverlayIn, combinedLightIn, isForced, isNat);
//		Minecraft.getInstance().getProfiler().endStartSection("second corner");
		
		matrixStackIn.push();
		matrixStackIn.translate(0, tileEntityIn.unitsPerBlock / 4f, 0);
		matrixStackIn.rotate(new Quaternion(90, 0, 0, true));
		renderCorner(matrixStackIn, bufferIn, combinedOverlayIn, combinedLightIn, isForced, isNat);
//		Minecraft.getInstance().getProfiler().endStartSection("third corner");
		
		matrixStackIn.pop();
		matrixStackIn.push();
		matrixStackIn.translate(0, 0, tileEntityIn.unitsPerBlock / 4f);
		matrixStackIn.rotate(new Quaternion(0, 90, 0, true));
		renderCorner(matrixStackIn, bufferIn, combinedOverlayIn, combinedLightIn, isForced, isNat);
//		Minecraft.getInstance().getProfiler().endStartSection("fourth corner");
		
		matrixStackIn.pop();
		matrixStackIn.push();
		matrixStackIn.translate(0, tileEntityIn.unitsPerBlock / 4f, tileEntityIn.unitsPerBlock / 4f);
		matrixStackIn.rotate(new Quaternion(180, 0, 0, true));
		renderCorner(matrixStackIn, bufferIn, combinedOverlayIn, combinedLightIn, isForced, isNat);
		
		matrixStackIn.pop();
//		Minecraft.getInstance().getProfiler().endSection();
//		Minecraft.getInstance().getProfiler().endSection();
	}
	
	public static void renderCorner(MatrixStack matrixStackIn, BufferCache bufferIn, int combinedOverlayIn, int combinedLightIn, boolean isForced, boolean isNat20) {
//		Minecraft.getInstance().getProfiler().startSection("corners");
//		Minecraft.getInstance().getProfiler().startSection("first cube");
		matrixStackIn.push();
		matrixStackIn.rotate(new Quaternion(180, 0, -90, true));
		matrixStackIn.scale(1, 1, 0.0001f);
		IVertexBuilder builder = bufferIn.getBuffer(RenderType.getEntitySolid(new ResourceLocation("textures/block/white_concrete.png")));
		int r = isForced ? (isNat20 ? 1 : 0) : 1;
		int g = isForced ? (isNat20 ? 0 : 1) : 1;
		int b = 0;
		renderCube(r, g, b, 0, 0, 0, builder, combinedOverlayIn, combinedLightIn, matrixStackIn, true);
//		Minecraft.getInstance().getProfiler().endStartSection("second cube");
		matrixStackIn.pop();
		
		matrixStackIn.push();
		matrixStackIn.rotate(quat90X);
		matrixStackIn.scale(1, 1, 0.001f);
		renderCube(r, g, b, 0, 0, 0, builder, combinedOverlayIn, combinedLightIn, matrixStackIn, true);
//		Minecraft.getInstance().getProfiler().endStartSection("third cube");
		matrixStackIn.pop();
		
		matrixStackIn.push();
		matrixStackIn.rotate(new Quaternion(0, -90, 0, true));
		matrixStackIn.scale(1, 1, 0.001f);
		renderCube(r, g, b, 0, 0, 0, builder, combinedOverlayIn, combinedLightIn, matrixStackIn, true);
		matrixStackIn.pop();
//		Minecraft.getInstance().getProfiler().endSection();
//		Minecraft.getInstance().getProfiler().endSection();
	}
	
	@Override
	public void render(UnitTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
		if (SmallerUnitsConfig.CLIENT.useExperimentalRendererPt2.get()) return;
		if (!bufferIn.getClass().equals(IRenderTypeBuffer.Impl.class))
			bufferIn = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
		Minecraft.getInstance().getProfiler().startSection("renderSU");
		render(tileEntityIn, partialTicks, matrixStackIn, new BufferCache(bufferIn, matrixStackIn), combinedLightIn, combinedOverlayIn);
		Minecraft.getInstance().getProfiler().endSection();
//		render(tileEntityIn, partialTicks, matrixStackIn, BufferCacheHelper.cache, combinedLightIn, combinedOverlayIn);
	}
}
