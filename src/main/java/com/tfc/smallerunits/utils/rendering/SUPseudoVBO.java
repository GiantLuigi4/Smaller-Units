package com.tfc.smallerunits.utils.rendering;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.tfc.smallerunits.utils.FakeServerWorld;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.world.LightType;

public class SUPseudoVBO {
	private final CustomBuffer buffer;
	
	public SUPseudoVBO(CustomBuffer buffer) {
		this.buffer = buffer;
	}
	
	public void render(IRenderTypeBuffer buffer1, MatrixStack matrixStack, int overworldLight, int combinedOverlay, FakeServerWorld world) {
		try {
			for (CustomBuffer.CustomVertexBuilder builder2 : buffer.builders) {
				IVertexBuilder builder1 = buffer1.getBuffer(builder2.type);
				for (int i = 0; i < builder2.vertices.size(); i += 4) {
					CustomBuffer.Vertex vert = builder2.vertices.get(i);
					CustomBuffer.Vertex vert1 = builder2.vertices.get(i + 1);
					CustomBuffer.Vertex vert2 = builder2.vertices.get(i + 2);
					CustomBuffer.Vertex vert3 = builder2.vertices.get(i + 3);
					drawFace(
							vert, vert1, vert2, vert3,
							matrixStack, overworldLight, world, builder1, combinedOverlay
					);
//					Vector3f vector3f = translate(matrixStack, (float) vert.x, (float) vert.y, (float) vert.z);
//					Vector3f normal;
//					normal = new Vector3f(vert.nx, vert.ny, vert.nz);
//					Matrix3f matrix3f = matrixStack.getLast().getNormal();
//					BlockPos posLight = vert.pos;
//					normal.normalize();
//					posLight = posLight.add(Math.round(normal.getX()),Math.round(normal.getY()),Math.round(normal.getZ()));
//					normal.transform(matrix3f);
//					normal.normalize();
//
//					int overworldSky = LightTexture.getLightSky(overworldLight);
//					int overworldBlock = LightTexture.getLightBlock(overworldLight);
//					int blockLight = world.getLightFor(LightType.BLOCK, posLight);
//					int skyLight = world.getLightFor(LightType.SKY, posLight);
//
//					builder1.addVertex(
//							vector3f.getX(),
//							vector3f.getY(),
//							vector3f.getZ(),
//							vert.r / 255f,
//							vert.g / 255f,
//							vert.b / 255f,
//							vert.a / 255f,
//							vert.u, vert.v,
//							combinedOverlay, LightTexture.packLight(
//									Math.max(overworldBlock, blockLight),
//									Math.max(overworldSky, skyLight)
//							),
//							normal.getX(), normal.getY(), normal.getZ()
//					);
				}
			}
		} catch (Throwable ignored) {
		}
	}
	
	public void drawFace(
			CustomBuffer.Vertex vertex1,
			CustomBuffer.Vertex vertex2,
			CustomBuffer.Vertex vertex3, CustomBuffer.Vertex vertex4,
			MatrixStack matrixStack, int overworldLight, FakeServerWorld world, IVertexBuilder builder1, int combinedOverlay
	) {
		Vector3f normal;
		
		Vector3f normalU = new Vector3f((float) vertex1.x, (float) vertex1.y, (float) vertex1.z);
		Vector3f normalV = normalU.copy();
		normalU.sub(new Vector3f((float) vertex2.x, (float) vertex2.y, (float) vertex2.z));
		normalV.sub(new Vector3f((float) vertex3.y, (float) vertex3.y, (float) vertex3.z));
		
		normal = new Vector3f(
				(normalU.getY() * normalV.getZ()) - (normalU.getZ() * normalV.getY()),
				(normalU.getZ() * normalV.getX()) - (normalU.getX() * normalV.getZ()),
				(normalU.getX() * normalV.getY()) - (normalU.getY() * normalV.getX())
		);

//		Matrix3f matrix3f = matrixStack.getLast().getNormal();
//		normal.transform(matrix3f);
		
		normal.normalize();
		normal.setX(-Math.abs(normal.getX()));
		normal.setY(-Math.abs(normal.getY()));
		normal.setZ(-Math.abs(normal.getZ()));
		
		drawVertex(vertex1, new Vector3d(normal.getX(), normal.getY(), normal.getZ()), matrixStack, overworldLight, world, builder1, combinedOverlay);
		drawVertex(vertex2, new Vector3d(normal.getX(), normal.getY(), normal.getZ()), matrixStack, overworldLight, world, builder1, combinedOverlay);
		drawVertex(vertex3, new Vector3d(normal.getX(), normal.getY(), normal.getZ()), matrixStack, overworldLight, world, builder1, combinedOverlay);
		drawVertex(vertex4, new Vector3d(normal.getX(), normal.getY(), normal.getZ()), matrixStack, overworldLight, world, builder1, combinedOverlay);
	}
	
	public void drawVertex(CustomBuffer.Vertex vert, Vector3d normal, MatrixStack matrixStack, int overworldLight, FakeServerWorld world, IVertexBuilder builder1, int combinedOverlay) {
		Vector3f vector3f = translate(matrixStack, (float) vert.x, (float) vert.y, (float) vert.z);
		BlockPos posLight = new BlockPos(
				Math.round((vert.x * world.owner.unitsPerBlock) - 0.5),
				Math.round((vert.y * world.owner.unitsPerBlock) - 0.5) + 64,
				Math.round((vert.z * world.owner.unitsPerBlock) - 0.5)
		);

//		{
//			Vector3f normal1;
// 			normal1 = new Vector3f(vert.nx, vert.ny, vert.nz);
//			normal1.normalize();
//			posLight = posLight.add(Math.round(normal1.getX()), Math.round(normal1.getY()), Math.round(normal1.getZ()));
////			posLight = posLight.add(
////					Math.round((((vert.x) * world.owner.unitsPerBlock)) - posLight.getX()),
////					Math.round((((vert.y) * world.owner.unitsPerBlock)) - posLight.getY()),
////					Math.round((((vert.z) * world.owner.unitsPerBlock)) - posLight.getZ())
////			);
//		}
		
		int overworldSky = LightTexture.getLightSky(overworldLight);
		int overworldBlock = LightTexture.getLightBlock(overworldLight);
		int blockLight = world.getLightFor(LightType.BLOCK, posLight);
		int skyLight = world.getLightFor(LightType.SKY, posLight);
		
		double amt = normal.dotProduct(new Vector3d(1, 0, 0.5).normalize());
		if (Double.isNaN(amt))
			amt = 1;
		amt = amt % 1;
		amt = ((amt / 3f) + 0.96f) % 1;
		amt = Math.abs(amt);
		
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
	}
	
	public Vector3f translate(MatrixStack stack, float x, float y, float z) {
		Vector4f vector4f = new Vector4f(x, y, z, 1.0F);
		vector4f.transform(stack.getLast().getMatrix());
		return new Vector3f(vector4f.getX(), vector4f.getY(), vector4f.getZ());
	}
}
