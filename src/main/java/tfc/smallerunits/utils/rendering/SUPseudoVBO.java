package tfc.smallerunits.utils.rendering;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.settings.AmbientOcclusionStatus;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.world.LightType;
import org.lwjgl.opengl.GL11;
import tfc.smallerunits.block.SmallerUnitBlock;
import tfc.smallerunits.config.SmallerUnitsConfig;
import tfc.smallerunits.utils.world.client.FakeClientWorld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class SUPseudoVBO {
	public CustomBuffer buffer;
	
	public boolean isDirty = false;
	
	public ArrayList<BufferStorage> vertexBuffers = new ArrayList<>();
//	Object2ObjectLinkedOpenHashMap<Integer, ArrayList<BufferStorage>> vboCache = new Object2ObjectLinkedOpenHashMap<>();
	
	public SUPseudoVBO(CustomBuffer buffer) {
		this.buffer = buffer;
	}
	
	public void render(BufferCache buffer1, MatrixStack matrixStack, int overworldLight, int combinedOverlay, FakeClientWorld world) {
		if (isDirty) {
			try {
				MatrixStack matrixStack1 = new MatrixStack();
				HashMap<RenderType, BufferBuilder> bufferBuilderHashMap = new HashMap<>();
				Minecraft.getInstance().getProfiler().startSection("renderPseudoVBO");
				for (CustomBuffer.CustomVertexBuilder builder2 : buffer.builders) {
					IVertexBuilder builder1;
					Minecraft.getInstance().getProfiler().startSection("getVertexBuilder");
					if (!SmallerUnitsConfig.CLIENT.useVBOS.get()) {
						builder1 = buffer1.getBuffer(RenderTypeHelper.getType(builder2.type));
					} else {
						if (!bufferBuilderHashMap.containsKey(RenderTypeHelper.getType(builder2.type))) {
							BufferBuilder builder = new BufferBuilder(83472);
							builder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
							bufferBuilderHashMap.put(RenderTypeHelper.getType(builder2.type), builder);
						}
						builder1 = bufferBuilderHashMap.get(RenderTypeHelper.getType(builder2.type)).getVertexBuilder();
					}
					
					Minecraft.getInstance().getProfiler().endStartSection("renderQuad");
					for (int i = 0; i < builder2.vertices.size(); i += 4) {
						CustomBuffer.Vertex vert = builder2.vertices.get(i);
						CustomBuffer.Vertex vert1 = builder2.vertices.get(i + 1);
						CustomBuffer.Vertex vert2 = builder2.vertices.get(i + 2);
						CustomBuffer.Vertex vert3 = builder2.vertices.get(i + 3);
						drawFace(
								vert, vert1, vert2, vert3,
								SmallerUnitsConfig.CLIENT.useVBOS.get() ? matrixStack1 : matrixStack, overworldLight, world, builder1, combinedOverlay
						);
					}
					Minecraft.getInstance().getProfiler().endSection();
				}
				Minecraft.getInstance().getProfiler().endStartSection("cacheVBO");
				if (SmallerUnitsConfig.CLIENT.useVBOS.get()) {
					for (BufferStorage vertexBuffer : vertexBuffers) {
						vertexBuffer.isPresent = false;
					}
					bufferBuilderHashMap.forEach((type, bufferBuilder) -> {
						bufferBuilder.finishDrawing();
						VertexBuffer vbo = null;
						BufferStorage storage1 = null;
						for (BufferStorage storage : vertexBuffers) {
							if (storage.renderType.equals(type)) {
								vbo = storage.terrainBuffer.get();
								storage1 = storage;
							}
						}
						if (vbo == null) {
							vbo = new VertexBuffer(DefaultVertexFormats.BLOCK);
							storage1 = new BufferStorage();
						}
						vbo.upload(bufferBuilder);
						storage1.terrainBuffer = Optional.of(vbo);
						storage1.renderType = type;
						storage1.isPresent = true;
						if (!vertexBuffers.contains(storage1)) vertexBuffers.add(storage1);
					});
				}
			} catch (Throwable ignored) {
				ignored.printStackTrace();
			}
			if (SmallerUnitsConfig.CLIENT.useVBOS.get())
				isDirty = false;
			Minecraft.getInstance().getProfiler().endSection();
		}
		if (isDirty) return;
		Minecraft.getInstance().getProfiler().startSection("renderVBOs");
		boolean isFirst = true;
		for (BufferStorage storage : vertexBuffers) {
			if (!storage.isPresent) continue;
			if (isFirst) {
				Minecraft.getInstance().getProfiler().startSection("renderVBO_" + RenderTypeHelper.getTypeName(storage.renderType));
				isFirst = false;
			} else {
				Minecraft.getInstance().getProfiler().endStartSection("renderVBO_" + RenderTypeHelper.getTypeName(storage.renderType));
			}
			VertexBuffer buffer = storage.terrainBuffer.get();
			RenderType type = RenderTypeHelper.getType(storage.renderType);
			matrixStack.push();
			buffer.bindBuffer();
			type.setupRenderState();
			DefaultVertexFormats.BLOCK.setupBufferState(0L);
			RenderSystem.shadeModel(GL11.GL_SMOOTH);
			buffer.draw(matrixStack.getLast().getMatrix(), GL11.GL_QUADS);
			VertexBuffer.unbindBuffer();
			RenderSystem.clearCurrentColor();
			type.clearRenderState();
			matrixStack.pop();
		}
		Minecraft.getInstance().getProfiler().endSection();
		Minecraft.getInstance().getProfiler().endSection();
	}
	
	public void dispose() {
		for (BufferStorage storage : vertexBuffers) {
			if (storage != null && storage.terrainBuffer.isPresent() && RenderSystem.isOnRenderThread()) {
				storage.terrainBuffer.ifPresent(VertexBuffer::close);
			}
		}
	}
	
	public void drawFace(
			CustomBuffer.Vertex vertex1,
			CustomBuffer.Vertex vertex2,
			CustomBuffer.Vertex vertex3, CustomBuffer.Vertex vertex4,
			MatrixStack matrixStack, int overworldLight, FakeClientWorld world, IVertexBuilder builder1, int combinedOverlay
	) {
		Minecraft.getInstance().getProfiler().startSection("calculateNormal");
		Vector3f normal;
		
		Vector3f normalU = new Vector3f((float) vertex1.x, (float) vertex1.y, (float) vertex1.z);
		Vector3f normalV = normalU.copy();
		{
			Vector3f workingVec = new Vector3f((float) vertex2.x, (float) vertex2.y, (float) vertex2.z);
			workingVec.add((float) vertex4.x, (float) vertex4.y, (float) vertex4.z);
			workingVec.mul(0.5f);
			normalU.sub(workingVec);
		}
		{
			Vector3f workingVec = new Vector3f((float) vertex3.x, (float) vertex3.y, (float) vertex3.z);
			workingVec.add((float) vertex4.x, (float) vertex4.y, (float) vertex4.z);
			workingVec.mul(0.5f);
			normalV.sub(workingVec);
		}
		
		normal = new Vector3f(
				(normalU.getY() * normalV.getZ()) - (normalU.getZ() * normalV.getY()),
				(normalU.getZ() * normalV.getX()) - (normalU.getX() * normalV.getZ()),
				(normalU.getX() * normalV.getY()) - (normalU.getY() * normalV.getX())
		);
		
		normal.normalize();
		normal.setX(-Math.abs(normal.getX()));
		normal.setY((normal.getY()));
		normal.setZ(-Math.abs(normal.getZ()));
		
		Minecraft.getInstance().getProfiler().endStartSection("draw");
		drawVertex(vertex1, new Vector3d(normal.getX(), normal.getY(), normal.getZ()), matrixStack, overworldLight, world, builder1, combinedOverlay);
		drawVertex(vertex2, new Vector3d(normal.getX(), normal.getY(), normal.getZ()), matrixStack, overworldLight, world, builder1, combinedOverlay);
		drawVertex(vertex3, new Vector3d(normal.getX(), normal.getY(), normal.getZ()), matrixStack, overworldLight, world, builder1, combinedOverlay);
		drawVertex(vertex4, new Vector3d(normal.getX(), normal.getY(), normal.getZ()), matrixStack, overworldLight, world, builder1, combinedOverlay);
		Minecraft.getInstance().getProfiler().endSection();
	}
	
	//TODO: optimize, fix smooth lighting
	public void drawVertex(CustomBuffer.Vertex vert, Vector3d normal, MatrixStack matrixStack, int overworldLight, FakeClientWorld world, IVertexBuilder builder1, int combinedOverlay) {
		Minecraft.getInstance().getProfiler().startSection("matrixTransform");
		Vector3f vector3f = translate(matrixStack, (float) vert.x, (float) vert.y, (float) vert.z);
		Minecraft.getInstance().getProfiler().endStartSection("setupForLighting");
		BlockPos posLight = vert.pos;
		int offX = 0;
		int offY = 0;
		int offZ = 0;
		Minecraft.getInstance().getProfiler().endStartSection(Minecraft.getInstance().gameSettings.ambientOcclusionStatus.equals(AmbientOcclusionStatus.MAX) ? "smoothLighting" : "lighting");
		if (Minecraft.getInstance().gameSettings.ambientOcclusionStatus.equals(AmbientOcclusionStatus.MAX)) {
//			Minecraft.getInstance().getProfiler().startSection("createHelperNormal");
//			Vector3f normal1;
//			normal1 = new Vector3f(vert.nx, vert.ny, vert.nz);
//			normal1.normalize();
			Minecraft.getInstance().getProfiler().startSection("calculateOffset");
			Vector3d offset = new Vector3d(
					vert.x * world.owner.unitsPerBlock - posLight.getX(),
					vert.y * world.owner.unitsPerBlock - (posLight.getY() - 64),
					vert.z * world.owner.unitsPerBlock - posLight.getZ()
			);
			offX = offset.getX() == 0 ? -1 : offset.getX() == 1 ? 1 : 0;
			offY = offset.getY() == 0 ? -1 : offset.getY() == 1 ? 1 : 0;
			offZ = offset.getZ() == 0 ? -1 : offset.getZ() == 1 ? 1 : 0;
			Minecraft.getInstance().getProfiler().endStartSection("correctOffset");
			if (offX <= 0) {
				BlockState state = world.owner.getWorld().getBlockState(world.owner.getPos().west());
				if ((state.getBlock() instanceof SmallerUnitBlock)) {
					offX = world.getBlockState(posLight.north()).isOpaqueCube(world, posLight.west()) ? 0 : -1;
				} else if (offX == 0) {
					offX = -2;
				} else {
					offX = world.getBlockState(posLight.north()).isOpaqueCube(world, posLight.west()) ? 0 : -1;
				}
			}
			if (offY <= 0) {
				BlockState state = world.owner.getWorld().getBlockState(world.owner.getPos().down());
				if ((state.getBlock() instanceof SmallerUnitBlock)) {
					offY = world.getBlockState(posLight.down()).isOpaqueCube(world, posLight.down()) ? 0 : -1;
				} else if (offY == 0) {
					offY = -2;
				} else {
					offY = world.getBlockState(posLight.down()).isOpaqueCube(world, posLight.down()) ? 0 : -1;
				}
			}
			if (offZ <= 0) {
				BlockState state = world.owner.getWorld().getBlockState(world.owner.getPos().north());
				if ((state.getBlock() instanceof SmallerUnitBlock)) {
					offZ = world.getBlockState(posLight.north()).isOpaqueCube(world, posLight.north()) ? 0 : -1;
				} else if (offZ == 0) {
					offZ = -2;
				} else {
					offZ = world.getBlockState(posLight.north()).isOpaqueCube(world, posLight.north()) ? 0 : -1;
				}
			}
			Minecraft.getInstance().getProfiler().endStartSection("createPos");
			posLight = new BlockPos(
					posLight.getX() + offX,
					posLight.getY() + offY,
					posLight.getZ() + offZ
			);
			Minecraft.getInstance().getProfiler().endSection();
		} else {
			Vector3f normal1;
			normal1 = new Vector3f(vert.nx, vert.ny, vert.nz);
			normal1.normalize();
			posLight = posLight.add(-Math.round(normal1.getX()), -Math.round(normal1.getY()), -Math.round(normal1.getZ()));
		}
		
		Minecraft.getInstance().getProfiler().startSection("unpackOverworldLight");
		int overworldSky = LightTexture.getLightSky(overworldLight);
		int overworldBlock = LightTexture.getLightBlock(overworldLight);
		Minecraft.getInstance().getProfiler().endStartSection("getSmallWorldLight");
		int blockLight = world.getLightFor(LightType.BLOCK, posLight);
		if (offZ == -2 || offX == -2 || offY == -2)
			blockLight = 15;
		blockLight = Math.max(blockLight, world.getLightFor(LightType.BLOCK, vert.pos));
		int skyLight = world.getLightFor(LightType.SKY, posLight);
		Minecraft.getInstance().getProfiler().endSection();
		
		Minecraft.getInstance().getProfiler().endStartSection("correctNormals");
		if (normal.y != 0) normal = new Vector3d(0, -normal.y, 0);
		
		if (vert.nx != 0 || vert.ny != 1 || vert.nz != 0) normal = new Vector3d(vert.nx, vert.ny, vert.nz);
		
		Minecraft.getInstance().getProfiler().endStartSection("normalToDirectionalLight");
		normal = normal.normalize();
		
		double amt = normal.dotProduct(new Vector3d(1, 0, 0.5).normalize());
		
		if (Double.isNaN(amt))
			amt = 1;
		amt = Math.abs(amt);
		amt /= 2;
		amt /= 200000;
		amt /= 250000;
		amt = 1 - amt;
//		if (normal.y > 0)
//			amt = amt / 1;
		
		Minecraft.getInstance().getProfiler().endStartSection("drawVertex");
		builder1.addVertex(
				vector3f.getX(),
				vector3f.getY(),
				vector3f.getZ(),
				(float) (vert.r * amt) / 255f,
				(float) (vert.g * amt) / 255f,
				(float) (vert.b * amt) / 255f,
				vert.a / 255f,
				vert.u, vert.v,
				combinedOverlay, LightTexture.packLight(
						Math.max(overworldBlock, blockLight),
						Math.max(overworldSky, skyLight)
				),
				(float) normal.x, (float) normal.y, (float) normal.z
		);
		Minecraft.getInstance().getProfiler().endSection();
	}
	
	public Vector3f translate(MatrixStack stack, float x, float y, float z) {
		Vector4f vector4f = new Vector4f(x, y, z, 1.0F);
		vector4f.transform(stack.getLast().getMatrix());
		return new Vector3f(vector4f.getX(), vector4f.getY(), vector4f.getZ());
	}
}
