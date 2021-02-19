package com.tfc.smallerunits;


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.datafixers.util.Pair;
import com.tfc.smallerunits.block.SmallerUnitBlock;
import com.tfc.smallerunits.block.UnitTileEntity;
import com.tfc.smallerunits.utils.SmallUnit;
import com.tfc.smallerunits.utils.rendering.CustomBuffer;
import com.tfc.smallerunits.utils.rendering.SUPseudoVBO;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.settings.AmbientOcclusionStatus;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.world.LightType;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.Random;

public class SmallerUnitsTESR extends TileEntityRenderer<UnitTileEntity> {
	public SmallerUnitsTESR(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}
	
	public static final Object2ObjectLinkedOpenHashMap<BlockPos, VertexBuffer> bufferCache1 = new Object2ObjectLinkedOpenHashMap<>();
	public static final Object2ObjectLinkedOpenHashMap<BlockPos, Pair<CompoundNBT, SUPseudoVBO>> bufferCache = new Object2ObjectLinkedOpenHashMap<>();
	private static final Quaternion quat90X = new Quaternion(90, 0, 0, true);
	private static final Quaternion quat180X = new Quaternion(180, 0, 0, true);
	private static final Quaternion quat90Y = new Quaternion(0, 90, 0, true);
	private int lastGameTime = 0;
	
	public static void renderCube(float r, float g, float b, float x, float y, float z, IVertexBuilder builder, int combinedOverlay, int combinedLight, MatrixStack matrixStack, boolean useNormals) {
		renderSquare(r, g, b, x, y, z + 0.25f, builder, combinedOverlay, combinedLight, matrixStack, useNormals);
		matrixStack.rotate(quat90Y);
		renderSquare(r, g, b, x - 0.25f, y, z + 0.25f, builder, combinedOverlay, combinedLight, matrixStack, useNormals);
		matrixStack.rotate(quat90Y);
		renderSquare(r, g, b, -0.25f, 0, 0, builder, combinedOverlay, combinedLight, matrixStack, useNormals);
		matrixStack.rotate(quat90Y);
		renderSquare(r, g, b, 0, 0, 0, builder, combinedOverlay, combinedLight, matrixStack, useNormals);
		matrixStack.rotate(quat90X);
		renderSquare(r, g, b, 0, -0.25f, 0, builder, combinedOverlay, combinedLight, matrixStack, useNormals);
		matrixStack.rotate(quat180X);
		renderSquare(r, g, b, 0, 0, 0.25f, builder, combinedOverlay, combinedLight, matrixStack, useNormals);
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

//			normal.mul(0.5f);

//			if (!Config.CLIENT.cacheBuffers.get()) {
			Matrix3f matrix3f = matrixStack.getLast().getNormal();
			normal.transform(matrix3f);
//			}
			
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
	
	@Override
	public void render(UnitTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
		CompoundNBT nbt = tileEntityIn.serializeNBT();
		
		nbt.remove("x");
		nbt.remove("y");
		nbt.remove("z");
		nbt.remove("id");
		nbt.remove("ForgeData");
		nbt.remove("ForgeCaps");
		nbt.remove("ticks");
		
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
		
		tileEntityIn.world.lightManager.tick(100, true, true);

//		if (!bufferCache.containsKey(nbt)) {
		if (
				!bufferCache.containsKey(tileEntityIn.getPos())
						|| !bufferCache.get(tileEntityIn.getPos()).getFirst().equals(nbt)
		) {
			bufferCache.remove(tileEntityIn.getPos());
			
			tileEntityIn.world.isRendering = true;
			
			CustomBuffer customBuffer = new CustomBuffer();
			
			{
				MatrixStack src = matrixStackIn;
				matrixStackIn = new MatrixStack();
				matrixStackIn.push();
				matrixStackIn.getLast().getNormal().set(new Matrix3f(new Quaternion(1, 0, 0, 0)));
				matrixStackIn.scale(1f / tileEntityIn.unitsPerBlock, 1f / tileEntityIn.unitsPerBlock, 1f / tileEntityIn.unitsPerBlock);
				
				matrixStackIn.translate(0, -64, 0);
				
				for (SmallUnit value : tileEntityIn.world.blockMap.values()) {
					customBuffer.pos = value.pos;
					
					RenderType type = RenderType.getSolid();
					
					for (RenderType blockRenderType : RenderType.getBlockRenderTypes())
						if (RenderTypeLookup.canRenderInLayer(value.state, blockRenderType)) type = blockRenderType;
					
					matrixStackIn.push();
					matrixStackIn.translate(value.pos.getX(), value.pos.getY(), value.pos.getZ());
					
					if (value.state.getRenderType().equals(BlockRenderType.MODEL)) {
						IVertexBuilder builder;
						
						builder = customBuffer.getBuffer(type);
						
						
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
					}
					
					if (!value.state.getFluidState().isEmpty()) {
						RenderType type1 = RenderTypeLookup.getRenderType(value.state.getFluidState());
						CustomBuffer.CustomVertexBuilder builder1;
						
						builder1 = (CustomBuffer.CustomVertexBuilder) customBuffer.getBuffer(type1);
						
						try {
							matrixStackIn.push();
							builder1.matrix = matrixStackIn;
							Minecraft.getInstance().getBlockRendererDispatcher().fluidRenderer.render(
									tileEntityIn.world, value.pos,
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
				
				SUPseudoVBO vbo = new SUPseudoVBO(customBuffer);
				vbo.render(
						bufferIn, matrixStackIn, combinedLightIn, combinedOverlayIn, tileEntityIn.world
				);

//				bufferCache.put(nbt, vbo);
//				System.out.println(bufferCache.size());
				bufferCache.put(tileEntityIn.getPos(), Pair.of(nbt, vbo));
			}
			
			tileEntityIn.world.isRendering = false;
		} else {
			bufferCache.get(tileEntityIn.getPos()).getSecond().render(bufferIn, matrixStackIn, combinedLightIn, combinedOverlayIn, tileEntityIn.world);
		}
		
		matrixStackIn.push();
		matrixStackIn.scale(1f / tileEntityIn.unitsPerBlock, 1f / tileEntityIn.unitsPerBlock, 1f / tileEntityIn.unitsPerBlock);
		for (SmallUnit value : tileEntityIn.world.blockMap.values()) {
			if (value.tileEntity != null) {
				matrixStackIn.push();
				matrixStackIn.translate(value.pos.getX(), value.pos.getY() - 64, value.pos.getZ());
				TileEntity tileEntity = value.tileEntity;
				TileEntityRenderer<TileEntity> renderer = TileEntityRendererDispatcher.instance.getRenderer(tileEntity);
				if (renderer != null)
					renderer.render(tileEntity, partialTicks, matrixStackIn, bufferIn, LightTexture.packLight(tileEntityIn.world.getLightFor(LightType.BLOCK, value.pos), tileEntityIn.world.getLightFor(LightType.SKY, value.pos)), combinedOverlayIn);
				matrixStackIn.pop();
			}
		}
		matrixStackIn.pop();
		
		if (tileEntityIn.world.blockMap.isEmpty()) {
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
	}
	
	public void renderHalf(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedOverlayIn, int combinedLightIn, UnitTileEntity tileEntityIn) {
		if (tileEntityIn.isNatural) return;
		renderCorner(matrixStackIn, bufferIn, combinedOverlayIn, combinedLightIn);
		matrixStackIn.push();
		matrixStackIn.translate(0, tileEntityIn.unitsPerBlock / 4f, 0);
		matrixStackIn.rotate(new Quaternion(90, 0, 0, true));
		renderCorner(matrixStackIn, bufferIn, combinedOverlayIn, combinedLightIn);
		matrixStackIn.pop();
		matrixStackIn.push();
		matrixStackIn.translate(0, 0, tileEntityIn.unitsPerBlock / 4f);
		matrixStackIn.rotate(new Quaternion(0, 90, 0, true));
		renderCorner(matrixStackIn, bufferIn, combinedOverlayIn, combinedLightIn);
		matrixStackIn.pop();
		matrixStackIn.push();
		matrixStackIn.translate(0, tileEntityIn.unitsPerBlock / 4f, tileEntityIn.unitsPerBlock / 4f);
		matrixStackIn.rotate(new Quaternion(180, 0, 0, true));
		renderCorner(matrixStackIn, bufferIn, combinedOverlayIn, combinedLightIn);
		matrixStackIn.pop();
	}
	
	public void renderCorner(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedOverlayIn, int combinedLightIn) {
		matrixStackIn.push();
		matrixStackIn.scale(0.001f, 1, 1);
		renderCube(1, 1, 0, 0, 0, 0, bufferIn.getBuffer(RenderType.getEntitySolid(new ResourceLocation("textures/block/white_concrete.png"))), combinedOverlayIn, combinedLightIn, matrixStackIn, true);
		matrixStackIn.pop();
		matrixStackIn.push();
		matrixStackIn.scale(1, 0.001f, 1);
		renderCube(1, 1, 0, 0, 0, 0, bufferIn.getBuffer(RenderType.getEntitySolid(new ResourceLocation("textures/block/white_concrete.png"))), combinedOverlayIn, combinedLightIn, matrixStackIn, true);
		matrixStackIn.pop();
		matrixStackIn.push();
		matrixStackIn.scale(1, 1, 0.001f);
		renderCube(1, 1, 0, 0, 0, 0, bufferIn.getBuffer(RenderType.getEntitySolid(new ResourceLocation("textures/block/white_concrete.png"))), combinedOverlayIn, combinedLightIn, matrixStackIn, true);
		matrixStackIn.pop();
	}
}
