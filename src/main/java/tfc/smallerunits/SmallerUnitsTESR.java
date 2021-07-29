package tfc.smallerunits;


import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.model.IndexedModel;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.datafixers.util.Pair;
import com.tfc.better_fps_graph.API.Profiler;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
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
import tfc.smallerunits.helpers.GameRendererHelper;
import tfc.smallerunits.renderer.FlywheelProgram;
import tfc.smallerunits.renderer.SmallerUnitsProgram;
import tfc.smallerunits.utils.*;
import tfc.smallerunits.utils.rendering.*;
import tfc.smallerunits.utils.rendering.flywheel.FlywheelVertexBuilder;
import tfc.smallerunits.utils.rendering.flywheel.FlywheelVertexFormats;
import tfc.smallerunits.utils.world.client.FakeClientWorld;
import tfc.smallerunits.utils.world.client.SmallBlockReader;
import tfc.smallerunits.utils.world.server.FakeServerWorld;

import java.util.Optional;
import java.util.Random;
import java.util.SortedSet;
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
		if (ModList.get().isLoaded("better_fps_graph")) Profiler.addSection("su:get_and_check_world");
		if (tileEntityIn.getFakeWorld() == null) return;
		if (tileEntityIn.getFakeWorld() instanceof FakeServerWorld) {
			CompoundNBT tag = tileEntityIn.serializeNBT();
			tileEntityIn.worldServer = null;
			tileEntityIn.handleUpdateTag(tileEntityIn.getBlockState(), tag);
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
		if (ModList.get().isLoaded("better_fps_graph")) Profiler.addSection("su:light");
		tileEntityIn.worldClient.get().lightManager.tick(SmallerUnitsConfig.CLIENT.lightingUpdatesPerFrame.get(), true, true);
		// TODO: make it render without vbos if the player is not in the same world
		if (ModList.get().isLoaded("better_fps_graph")) Profiler.endSection();
		if (tileEntityIn.getWorld() != null && tileEntityIn.getWorld().equals(Minecraft.getInstance().world) && SmallerUnitsConfig.CLIENT.useVBOS.get()) {
			boolean isRefreshing = tileEntityIn.needsRefresh(false) || !vertexBufferCacheUsed.containsKey(tileEntityIn.getPos());
			if (!vertexBufferCacheUsed.containsKey(tileEntityIn.getPos()) || isRefreshing || needsResort) {
				if (vertexBufferCacheFree.containsKey(tileEntityIn.getPos())) {
					vertexBufferCacheUsed.put(tileEntityIn.getPos(), vertexBufferCacheFree.get(tileEntityIn.getPos()));
				}
				{
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
					
					if (ModList.get().isLoaded("flywheel") && false) {
						for (CustomBuffer.CustomVertexBuilder builder : redirection.builders) {
							if (builder.vertices.size() == 0) continue;
							FlywheelVertexBuilder buffer = new FlywheelVertexBuilder(builder.vertices.size() * FlywheelVertexFormats.BLOCK.getStride() * 2);
//							int number = 0;
							for (CustomBuffer.Vertex vertex : builder.vertices) {
//								number += 1;
//								System.out.println(number);
								buffer.pos(vertex.x, vertex.y, vertex.z);
								buffer.color(vertex.r, vertex.g, vertex.b, vertex.a);
								buffer.tex(vertex.u, vertex.v);
								buffer.lightmap((int) (vertex.lu * 15), (int) (vertex.lv * 15));
								buffer.normal(vertex.nx, vertex.ny, vertex.nz);
							}
							SmallerUnitsProgram shader = FlywheelProgram.UNIT.getProgram(new ResourceLocation("smallerunits:unit_shader"));
							shader.bind();
							shader.uploadViewProjection(GameRendererHelper.matrix);
							shader.uploadCameraPos(
									Minecraft.getInstance().getRenderManager().info.getProjectedView().x,
									Minecraft.getInstance().getRenderManager().info.getProjectedView().y,
									Minecraft.getInstance().getRenderManager().info.getProjectedView().z
							);
							IndexedModel mdl = IndexedModel.fromSequentialQuads(FlywheelVertexFormats.BLOCK, buffer.unwrap(), buffer.vertices());
//							RenderSystem.pushMatrix();
//							RenderSystem.loadIdentity();
//							RenderSystem.multMatrix(oldStack.getLast().getMatrix());
							RenderType.getSolid().setupRenderState();
							mdl.setupState();
							mdl.drawCall();
							mdl.clearState();
							RenderType.getSolid().clearRenderState();
//							RenderSystem.popMatrix();
							shader.unbind();
							buffer.close();
							mdl.delete();
						}
					} else {
						if (renderedAnything && SmallerUnitsConfig.CLIENT.useVBOS.get()) {
							SUVBO suvbo;
							if (!vertexBufferCacheUsed.containsKey(tileEntityIn.getPos())) {
								if (isRefreshing && vertexBufferCacheUsed.containsKey(tileEntityIn.getPos()))
									suvbo = vertexBufferCacheUsed.remove(tileEntityIn.getPos());
								else suvbo = new SUVBO();
							} else suvbo = vertexBufferCacheUsed.remove(tileEntityIn.getPos());
							if (suvbo == null) suvbo = new SUVBO();
							if (isRefreshing) suvbo.markAllUnused();
							SUVBO finalSuvbo = suvbo;
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
					
					stack.pop();
				}
				if (SmallerUnitsConfig.CLIENT.useVBOS.get()) {
					if (ModList.get().isLoaded("better_fps_graph")) Profiler.addSection("su:draw_vbo", 0.4, 0.2, 0.8);
					matrixStackIn = oldStack;
					SUVBO vbo = vertexBufferCacheUsed.get(tileEntityIn.getPos());
					if (vbo != null) vbo.render(matrixStackIn);
					if (ModList.get().isLoaded("better_fps_graph")) Profiler.endSection();
				}
			} else {
				if (ModList.get().isLoaded("better_fps_graph")) Profiler.addSection("su:draw_vbo", 0.4, 0.2, 0.8);
				matrixStackIn = oldStack;
				SUVBO vbo = vertexBufferCacheUsed.get(tileEntityIn.getPos());
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
		
		for (SortedSet<DestroyBlockProgress> value : Minecraft.getInstance().worldRenderer.damageProgress.values()) {
			for (DestroyBlockProgress destroyBlockProgress : value) {
				if (destroyBlockProgress.getPosition().equals(tileEntityIn.getPos())) {
					int phase = destroyBlockProgress.getPartialBlockDamage() - 1;
					Entity entity = tileEntityIn.getWorld().getEntityByID(destroyBlockProgress.miningPlayerEntId);
					UnitRaytraceContext context = UnitRaytraceHelper.raytraceBlock(tileEntityIn, entity, false, tileEntityIn.getPos(), Optional.empty());
					BlockState miningState = tileEntityIn.getFakeWorld().getBlockState(context.posHit);
					matrixStackIn.push();
					matrixStackIn.scale(1f / tileEntityIn.unitsPerBlock, 1f / tileEntityIn.unitsPerBlock, 1f / tileEntityIn.unitsPerBlock);
					matrixStackIn.translate(context.posHit.getX(), context.posHit.getY() - 64, context.posHit.getZ());
					CustomBuffer.CustomVertexBuilder builder = new CustomBuffer.CustomVertexBuilder(ModelBakery.DESTROY_RENDER_TYPES.get(phase + 1));
					Minecraft.getInstance().getBlockRendererDispatcher().renderModel(
							miningState,
							context.posHit, tileEntityIn.getFakeWorld(),
							new MatrixStack(), builder, true,
							new Random(context.posHit.toLong()),
							EmptyModelData.INSTANCE
					);
					IVertexBuilder builder1 = bufferIn.getBuffer(builder.type);
					int index = 0;
					for (CustomBuffer.Vertex vert : builder.vertices) {
						int amt = 1;
						float u = vert.u * 128;
						float v = vert.v * 128;
						Vector4f vector4f = new Vector4f((float) vert.x, (float) vert.y, (float) vert.z, 1);
						vector4f.transform(matrixStackIn.getLast().getMatrix());
						builder1.addVertex(
								(float) vector4f.getX(),
								(float) vector4f.getY(),
								(float) vector4f.getZ(),
								(float) (vert.r * amt) / 255f,
								(float) (vert.g * amt) / 255f,
								(float) (vert.b * amt) / 255f,
								vert.a / 255f,
								u, v,
								combinedOverlayIn, combinedLightIn,
								vert.nx, vert.ny, vert.nz
						);
						index++;
						if (index == 4) index = 0;
					}
					matrixStackIn.pop();
				}
			}
		}
		
		matrixStackIn = oldStack;
		
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
		
		SmallerUnitsAPI.postRenderUnitLast(bufferIn.buffer, tileEntityIn);
		
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
		
		if (ModList.get().isLoaded("better_fps_graph")) Profiler.endSection();
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
