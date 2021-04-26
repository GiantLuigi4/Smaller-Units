package com.tfc.smallerunits;


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.blaze3d.vertex.MatrixApplyingVertexBuilder;
import com.mojang.datafixers.util.Pair;
import com.tfc.smallerunits.block.SmallerUnitBlock;
import com.tfc.smallerunits.block.UnitTileEntity;
import com.tfc.smallerunits.utils.NBTStripper;
import com.tfc.smallerunits.utils.SmallUnit;
import com.tfc.smallerunits.utils.UnitRaytraceContext;
import com.tfc.smallerunits.utils.UnitRaytraceHelper;
import com.tfc.smallerunits.utils.rendering.BufferCache;
import com.tfc.smallerunits.utils.rendering.CustomBuffer;
import com.tfc.smallerunits.utils.rendering.SUPseudoVBO;
import com.tfc.smallerunits.utils.world.client.FakeClientWorld;
import com.tfc.smallerunits.utils.world.server.FakeServerWorld;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.settings.AmbientOcclusionStatus;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.*;
import net.minecraft.world.LightType;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.Random;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicReference;

public class SmallerUnitsTESR extends TileEntityRenderer<UnitTileEntity> {
	public static SmallerUnitsTESR INSTANCE;
	
	public static final Object2ObjectLinkedOpenHashMap<BlockPos, Pair<AtomicReference<CompoundNBT>, SUPseudoVBO>> bufferCache = new Object2ObjectLinkedOpenHashMap<>();
	private static final Quaternion quat90X = new Quaternion(90, 0, 0, true);
	private static final Quaternion quat180X = new Quaternion(180, 0, 0, true);
	private static final Quaternion quat90Y = new Quaternion(0, 90, 0, true);
	
	public static final Logger LOGGER = LogManager.getLogger();
	
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
		CompoundNBT nbt = tileEntityIn.serializeNBT();
		
		if (tileEntityIn.getWorld() == null) return;
		
		MatrixStack oldStack = matrixStackIn;
		matrixStackIn = new MatrixStack();
		matrixStackIn.stack.add(oldStack.getLast());
		
		tileEntityIn.getProfiler().startTick();

//		tileEntityIn.worldServer.animateTick();
		
		Minecraft.getInstance().getProfiler().startSection("stripNBT");
		nbt.remove("x");
		nbt.remove("y");
		nbt.remove("z");
		nbt.remove("id");
		nbt.remove("ForgeData");
		nbt.remove("ForgeCaps");
		nbt.remove("ticks");
		nbt.remove("entities");
		nbt = NBTStripper.stripOfTEData(nbt);
		
		Minecraft.getInstance().getProfiler().endStartSection("breakProgress");
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
					CustomBuffer.CustomVertexBuilder customVertexBuilder = new CustomBuffer.CustomVertexBuilder(ModelBakery.DESTROY_RENDER_TYPES.get(phase + 1));
					IVertexBuilder ivertexbuilder1 = new MatrixApplyingVertexBuilder(customVertexBuilder, matrixStackIn.getLast().getMatrix(), matrixStackIn.getLast().getNormal());
					Minecraft.getInstance().getBlockRendererDispatcher().renderBlockDamage(miningState, context.posHit, tileEntityIn.getFakeWorld(), matrixStackIn, ivertexbuilder1);
					IVertexBuilder builder1 = bufferIn.getBuffer(customVertexBuilder.type);
					int index = 0;
					for (CustomBuffer.Vertex vert : customVertexBuilder.vertices) {
						int amt = 1;
						int u = (index == 0 || index == 3) ? 0 : 1;
						int v = (index == 0 || index == 1) ? 1 : 0;
						builder1.addVertex(
								(float) vert.x,
								(float) vert.y,
								(float) vert.z,
								(float) (vert.r * amt) / 255f,
								(float) (vert.g * amt) / 255f,
								(float) (vert.b * amt) / 255f,
								vert.a / 255f,
								v, u,
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
		
		Minecraft.getInstance().getProfiler().endStartSection("checkNeighbors");
		for (Direction dir : Direction.values()) {
			BlockState state = tileEntityIn.getWorld().getBlockState(tileEntityIn.getPos().offset(dir));
			boolean isAir = state.isAir(tileEntityIn.getWorld(), tileEntityIn.getPos().offset(dir));
			boolean isUnit = state.getBlock() instanceof SmallerUnitBlock;
			int scale = 0;
			if (isUnit) {
				TileEntity otherTE = tileEntityIn.getWorld().getTileEntity(tileEntityIn.getPos().offset(dir));
				if (otherTE instanceof UnitTileEntity) {
					scale = ((UnitTileEntity) otherTE).unitsPerBlock;
				}
			}
			nbt.putString(dir.toString(),
					isAir ? "air" : isUnit ? ("unit" + scale) : "obstructed"
			);
		}
		
		tileEntityIn.worldClient.lightManager.tick(SmallerUnitsConfig.CLIENT.lightingUpdatesPerFrame.get(), true, true);
		
		Minecraft.getInstance().getProfiler().endStartSection("renderBlocks");
		Minecraft.getInstance().getProfiler().startSection("cacheLookup");
		if (
				!bufferCache.containsKey(tileEntityIn.getPos())
						|| !bufferCache.get(tileEntityIn.getPos()).getFirst().get().equals(nbt) &&
						!tileEntityIn.getBlockMap().isEmpty()
		) {
			Minecraft.getInstance().getProfiler().endStartSection("createPseudoVBO");
			Minecraft.getInstance().getProfiler().startSection("setup");
			
			CustomBuffer customBuffer = new CustomBuffer();
			
			{
				MatrixStack src = matrixStackIn;
				matrixStackIn = new MatrixStack();
				matrixStackIn.push();
				matrixStackIn.getLast().getNormal().set(new Matrix3f(new Quaternion(1, 0, 0, 0)));
				matrixStackIn.scale(1f / tileEntityIn.unitsPerBlock, 1f / tileEntityIn.unitsPerBlock, 1f / tileEntityIn.unitsPerBlock);
				
				matrixStackIn.translate(0, -64, 0);
				
				Minecraft.getInstance().getProfiler().endStartSection("doRender");
				for (SmallUnit value : tileEntityIn.getBlockMap().values()) {
					customBuffer.pos = value.pos;
					
					Minecraft.getInstance().getProfiler().startSection("renderBlocks");
					Minecraft.getInstance().getProfiler().startSection("render:" + value.state.getBlock().toString());
					
					Minecraft.getInstance().getProfiler().startSection("getRenderType");
					RenderType type = RenderType.getSolid();
					
					for (RenderType blockRenderType : RenderType.getBlockRenderTypes())
						if (RenderTypeLookup.canRenderInLayer(value.state, blockRenderType)) type = blockRenderType;
					Minecraft.getInstance().getProfiler().endStartSection("translateStack");
					
					matrixStackIn.push();
					matrixStackIn.translate(value.pos.getX(), value.pos.getY(), value.pos.getZ());
					
					Minecraft.getInstance().getProfiler().endStartSection("checkCanRender");
					if (value.state.getRenderType().equals(BlockRenderType.MODEL)) {
						IVertexBuilder builder;
						
						Minecraft.getInstance().getProfiler().endStartSection("getModelData");
						
						builder = customBuffer.getBuffer(type);
						IBakedModel model = Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(value.state);
						IModelData data = EmptyModelData.INSTANCE;
						data = model.getModelData(tileEntityIn.getFakeWorld(), value.pos, value.state, data);
						
						Minecraft.getInstance().getProfiler().endStartSection("renderBlock");
						if (Minecraft.getInstance().gameSettings.ambientOcclusionStatus.equals(AmbientOcclusionStatus.MAX)) {
							Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelRenderer().renderModelSmooth(
									tileEntityIn.getFakeWorld(), Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(value.state),
									value.state, value.pos, matrixStackIn, builder, true, new Random(value.pos.toLong()),
									value.pos.toLong(), combinedOverlayIn, data
							);
						} else {
							Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelRenderer().renderModelFlat(
									tileEntityIn.getFakeWorld(), Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(value.state),
									value.state, value.pos, matrixStackIn, builder, true, new Random(value.pos.toLong()),
									value.pos.toLong(), combinedOverlayIn, data
							);
						}
						Minecraft.getInstance().getProfiler().endSection();
					}
					Minecraft.getInstance().getProfiler().endSection();
					Minecraft.getInstance().getProfiler().endSection();
					
					//TODO: profile fluids
					if (!value.state.getFluidState().isEmpty()) {
						RenderType type1 = RenderTypeLookup.getRenderType(value.state.getFluidState());
						CustomBuffer.CustomVertexBuilder builder1;
						
						builder1 = (CustomBuffer.CustomVertexBuilder) customBuffer.getBuffer(type1);
						
						try {
							matrixStackIn.push();
							builder1.matrix = matrixStackIn;
							Minecraft.getInstance().getBlockRendererDispatcher().fluidRenderer.render(
									tileEntityIn.getFakeWorld(), value.pos,
									builder1, value.state.getFluidState()
							);
							matrixStackIn.pop();
							builder1.matrix = null;
						} catch (Throwable ignored) {
							StringBuilder builder2 = new StringBuilder(ignored.toString()).append("\n");
							for (StackTraceElement element : ignored.getStackTrace()) {
								builder2.append(element.toString()).append("\n");
							}
							System.out.println(builder2.toString());
						}
					}
					
					matrixStackIn.pop();
				}
				
				matrixStackIn.pop();
				matrixStackIn = src;
				
				if (bufferCache.containsKey(tileEntityIn.getPos())) {
					SUPseudoVBO vbo = bufferCache.get(tileEntityIn.getPos()).getSecond();
					vbo.isDirty = true;
					bufferCache.get(tileEntityIn.getPos()).getFirst().set(nbt);
					vbo.buffer = customBuffer;
					vbo.render(
							bufferIn, matrixStackIn, combinedLightIn, combinedOverlayIn, (FakeClientWorld) tileEntityIn.getFakeWorld()
					);
				} else {
					SUPseudoVBO vbo = new SUPseudoVBO(customBuffer);
					vbo.isDirty = true;
					vbo.render(
							bufferIn, matrixStackIn, combinedLightIn, combinedOverlayIn, (FakeClientWorld) tileEntityIn.getFakeWorld()
					);

//					bufferCache.put(nbt, vbo);
//					System.out.println(bufferCache.size());
					bufferCache.put(tileEntityIn.getPos(), Pair.of(new AtomicReference<>(nbt), vbo));
				}
			}
			
			Minecraft.getInstance().getProfiler().endSection();
//			tileEntityIn.worldServer.isRendering = false;
		} else {
			Minecraft.getInstance().getProfiler().endStartSection("renderPseudoVBO");
			bufferCache.get(tileEntityIn.getPos()).getSecond().render(bufferIn, matrixStackIn, combinedLightIn, combinedOverlayIn, (FakeClientWorld) tileEntityIn.getFakeWorld());
		}
		Minecraft.getInstance().getProfiler().endSection();
		
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
		if (tileEntityIn.getBlockMap().isEmpty()) {
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
