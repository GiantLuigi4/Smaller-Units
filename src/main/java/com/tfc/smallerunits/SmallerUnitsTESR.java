package com.tfc.smallerunits;


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.tfc.smallerunits.block.UnitTileEntity;
import com.tfc.smallerunits.utils.SmallUnit;
import com.tfc.smallerunits.utils.rendering.CustomBuffer;
import com.tfc.smallerunits.utils.rendering.SUPseudoVBO;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.settings.AmbientOcclusionStatus;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.Random;

public class SmallerUnitsTESR extends TileEntityRenderer<UnitTileEntity> {
	public SmallerUnitsTESR(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}
	
	//	public static final Object2ObjectLinkedOpenHashMap<CompoundNBT, ArrayList<BufferStorage>> bufferCache = new Object2ObjectLinkedOpenHashMap<>();
	public static final Object2ObjectLinkedOpenHashMap<CompoundNBT, SUPseudoVBO> bufferCache = new Object2ObjectLinkedOpenHashMap<>();
	
	@Override
	public void render(UnitTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
		CompoundNBT nbt = tileEntityIn.serializeNBT();
//
//		for (ArrayList<BufferStorage> value : bufferCache.values()) {
//			for (BufferStorage storage : value) {
//				storage.terrainBuffer.ifPresent(VertexBuffer::close);
//				storage.fluidBuffer.ifPresent(VertexBuffer::close);
//			}
//		}
//		bufferCache.clear();
		
		nbt.remove("x");
		nbt.remove("y");
		nbt.remove("z");
		nbt.remove("id");
		nbt.remove("ForgeData");
		nbt.remove("ForgeCaps");
		nbt.remove("ticks");
//
//		if (!bufferCache.containsKey(nbt)) {
//			HashMap<RenderType, Pair<BufferBuilder, IVertexBuilder>> builders = new HashMap<>();
		
		tileEntityIn.world.lightManager.tick(100, true, true);
		
		if (!bufferCache.containsKey(nbt)) {
			CustomBuffer customBuffer = new CustomBuffer();
//			HashMap<RenderType, Pair<BufferBuilder, IVertexBuilder>> buildersFluid = new HashMap<>();
			
			{
				MatrixStack src = matrixStackIn;
				matrixStackIn = new MatrixStack();
				matrixStackIn.push();
				matrixStackIn.scale(1f / tileEntityIn.unitsPerBlock, 1f / tileEntityIn.unitsPerBlock, 1f / tileEntityIn.unitsPerBlock);
				
				matrixStackIn.translate(0, -64, 0);
				
				for (SmallUnit value : tileEntityIn.world.blockMap.values()) {
					customBuffer.pos = value.pos;
					
					RenderType type = RenderTypeLookup.getChunkRenderType(value.state);
					IVertexBuilder builder;

//					if (!builders.containsKey(type)) {
//						BufferBuilder buffer = new BufferBuilder(13853);
//						buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
//						builder = buffer.getVertexBuilder();
//						builders.put(type, Pair.of(buffer, builder));
//					} else {
//						builder = builders.get(type).getSecond();
//					}
					
					builder = customBuffer.getBuffer(type);
					
					matrixStackIn.push();
					matrixStackIn.translate(value.pos.getX(), value.pos.getY(), value.pos.getZ());
					
					if (Minecraft.getInstance().gameSettings.ambientOcclusionStatus.equals(AmbientOcclusionStatus.MAX)) {
						Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelRenderer().renderModelSmooth(
								tileEntityIn.world, Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(value.state),
								value.state, value.pos, matrixStackIn, builder, true, new Random(value.pos.toLong()),
								value.pos.toLong(), combinedOverlayIn, EmptyModelData.INSTANCE
						);
					} else {
						Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelRenderer().renderModelFlat(
								tileEntityIn.world, Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(value.state),
								value.state, value.pos, matrixStackIn, builder, true, new Random(value.pos.toLong()),
								value.pos.toLong(), combinedOverlayIn, EmptyModelData.INSTANCE
						);
					}
					
					if (!value.state.getFluidState().isEmpty()) {
						RenderType type1 = RenderTypeLookup.getRenderType(value.state.getFluidState());
						IVertexBuilder builder1;

//						if (!buildersFluid.containsKey(type1)) {
//							BufferBuilder buffer = new BufferBuilder(13853);
//							buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
//							builder1 = buffer.getVertexBuilder();
//							buildersFluid.put(type1, Pair.of(buffer, builder1));
//						} else {
//							builder1 = buildersFluid.get(type1).getSecond();
//						}
						builder1 = customBuffer.getBuffer(type1);
						
						try {
							Minecraft.getInstance().getBlockRendererDispatcher().fluidRenderer.render(
									tileEntityIn.world, value.pos,
									builder1, value.state.getFluidState()
							);
						} catch (Throwable ignored) {
							ignored.printStackTrace();
						}
					}
					
					TileEntity te = value.tileEntity;
					if (te != null) {
						TileEntityRenderer<TileEntity> renderer = TileEntityRendererDispatcher.instance.getRenderer(te);
						try {
							if (renderer != null)
								renderer.render(te, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
						} catch (Throwable ignored) {
						}
					}
					
					matrixStackIn.pop();
				}
				
				matrixStackIn.pop();
				matrixStackIn = src;
				
				SUPseudoVBO vbo = new SUPseudoVBO(customBuffer);
				vbo.render(
						bufferIn, matrixStackIn, combinedLightIn, combinedOverlayIn, tileEntityIn.world
				);
				
				bufferCache.put(nbt, vbo);

//				matrixStackIn.translate(0, 64, 0);
//				MatrixStack finalMatrixStackIn = matrixStackIn;
//				buildersFluid.forEach((type, bufferBuilderIVertexBuilderPair) -> {
//					bufferBuilderIVertexBuilderPair.getFirst().finishDrawing();
//
//					finalMatrixStackIn.push();
//					VertexBuffer buffer = new VertexBuffer(DefaultVertexFormats.BLOCK);
//					buffer.upload(bufferBuilderIVertexBuilderPair.getFirst());
//					buffer.bindBuffer();
//					type.setupRenderState();
//					DefaultVertexFormats.BLOCK.setupBufferState(0L);
//					buffer.draw(finalMatrixStackIn.getLast().getMatrix(), GL11.GL_QUADS);
//
//					VertexBuffer.unbindBuffer();
//					RenderSystem.clearCurrentColor();
//					type.clearRenderState();
//					buffer.close();
//					finalMatrixStackIn.pop();
//				});
			}
		} else {
			bufferCache.get(nbt).render(bufferIn, matrixStackIn, combinedLightIn, combinedOverlayIn, tileEntityIn.world);
		}

//			ArrayList<BufferStorage> buffers = new ArrayList<>();

//			builders.forEach((type, bufferBuilderPair) -> {
//				VertexBuffer buffer = new VertexBuffer(DefaultVertexFormats.BLOCK);
//				bufferBuilderPair.getFirst().finishDrawing();
//				buffer.upload(bufferBuilderPair.getFirst());
//				BufferStorage storage = new BufferStorage();
//				storage.renderType = type;
//				storage.terrainBuffer = Optional.of(buffer);
//				buffers.add(storage);
//			});

//			buildersFluid.forEach((type, bufferBuilderPair) -> {
//				BufferStorage storage = null;
//				boolean hasStorage = false;
//
//				for (BufferStorage bufferStorage : buffers) {
//					if (bufferStorage.renderType.equals(type)) {
//						storage = bufferStorage;
//						hasStorage = true;
//					}
//				}
//
//				VertexBuffer buffer = new VertexBuffer(DefaultVertexFormats.BLOCK);
//				bufferBuilderPair.getFirst().finishDrawing();
//				buffer.upload(bufferBuilderPair.getFirst());
//				if (!hasStorage) storage = new BufferStorage();
//				storage.renderType = type;
//				storage.fluidBuffer = Optional.of(buffer);
//				if (!hasStorage) buffers.add(storage);
//			});

//			bufferCache.put(nbt, buffers);
	}

//		ArrayList<BufferStorage> buffers = bufferCache.get(nbt);
//
//		for (BufferStorage typeBufferPair : buffers) {
//			RenderType type = typeBufferPair.renderType;
//
//			if (typeBufferPair.terrainBuffer.isPresent()) {
//				VertexBuffer buffer = typeBufferPair.terrainBuffer.get();
//				buffer.bindBuffer();
//				type.setupRenderState();
//				DefaultVertexFormats.BLOCK.setupBufferState(0L);
//				buffer.draw(matrixStackIn.getLast().getMatrix(), 7);
//
//				VertexBuffer.unbindBuffer();
//				RenderSystem.clearCurrentColor();
//				type.clearRenderState();
//			}
//
//			if (typeBufferPair.fluidBuffer.isPresent()) {
//				matrixStackIn.push();
//				matrixStackIn.scale(1f/tileEntityIn.unitsPerBlock,1f/tileEntityIn.unitsPerBlock,1f/tileEntityIn.unitsPerBlock);
//				VertexBuffer buffer = typeBufferPair.fluidBuffer.get();
//				buffer.bindBuffer();
//				type.setupRenderState();
//				DefaultVertexFormats.BLOCK.setupBufferState(0L);
//				buffer.draw(matrixStackIn.getLast().getMatrix(), GL11.GL_QUADS);
//
//				VertexBuffer.unbindBuffer();
//				RenderSystem.clearCurrentColor();
//				type.clearRenderState();
//				matrixStackIn.pop();
//			}
//		}
//	}
}
