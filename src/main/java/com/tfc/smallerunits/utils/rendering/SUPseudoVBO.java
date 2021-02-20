package com.tfc.smallerunits.utils.rendering;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.tfc.smallerunits.utils.world.FakeServerWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.settings.AmbientOcclusionStatus;
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
				IVertexBuilder builder1 = buffer1.getBuffer(RenderTypeHelper.getType(builder2.type));
				for (int i = 0; i < builder2.vertices.size(); i += 4) {
					CustomBuffer.Vertex vert = builder2.vertices.get(i);
					CustomBuffer.Vertex vert1 = builder2.vertices.get(i + 1);
					CustomBuffer.Vertex vert2 = builder2.vertices.get(i + 2);
					CustomBuffer.Vertex vert3 = builder2.vertices.get(i + 3);
					drawFace(
							vert, vert1, vert2, vert3,
							matrixStack, overworldLight, world, builder1, combinedOverlay
					);
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
		
		drawVertex(vertex1, new Vector3d(normal.getX(), normal.getY(), normal.getZ()), matrixStack, overworldLight, world, builder1, combinedOverlay);
		drawVertex(vertex2, new Vector3d(normal.getX(), normal.getY(), normal.getZ()), matrixStack, overworldLight, world, builder1, combinedOverlay);
		drawVertex(vertex3, new Vector3d(normal.getX(), normal.getY(), normal.getZ()), matrixStack, overworldLight, world, builder1, combinedOverlay);
		drawVertex(vertex4, new Vector3d(normal.getX(), normal.getY(), normal.getZ()), matrixStack, overworldLight, world, builder1, combinedOverlay);
	}
	
	//TODO: optimize, fix smooth lighting
	public void drawVertex(CustomBuffer.Vertex vert, Vector3d normal, MatrixStack matrixStack, int overworldLight, FakeServerWorld world, IVertexBuilder builder1, int combinedOverlay) {
		Vector3f vector3f = translate(matrixStack, (float) vert.x, (float) vert.y, (float) vert.z);
		BlockPos posLight = vert.pos;
		/*if (Minecraft.getInstance().gameSettings.ambientOcclusionStatus.equals(AmbientOcclusionStatus.MIN)) {
			posLight = new BlockPos(
					(vert.pos.getX() <= vert.x*world.owner.unitsPerBlock || vert.pos.getX() >= world.owner.unitsPerBlock-1) ? Math.ceil((vert.x * world.owner.unitsPerBlock) - 0.5) : Math.floor((vert.x * world.owner.unitsPerBlock) - 0.5),
					(vert.pos.getY() <= vert.y*world.owner.unitsPerBlock ? Math.floor((vert.y * world.owner.unitsPerBlock) - 0.5) : Math.ceil((vert.y * world.owner.unitsPerBlock) - 0.5)) + 64,
					vert.pos.getZ() <= vert.z*world.owner.unitsPerBlock ? Math.ceil((vert.z * world.owner.unitsPerBlock) - 0.5) : Math.floor((vert.z * world.owner.unitsPerBlock) - 0.5)
			);
		} else*/
		if (Minecraft.getInstance().gameSettings.ambientOcclusionStatus.equals(AmbientOcclusionStatus.MAX)) {
			Vector3f normal1;
			normal1 = new Vector3f(vert.nx, vert.ny, vert.nz);
			normal1.normalize();
			Vector3d offset = new Vector3d(
					vert.x * world.owner.unitsPerBlock - posLight.getX(),
					vert.y * world.owner.unitsPerBlock - (posLight.getY() - 64),
					vert.z * world.owner.unitsPerBlock - posLight.getZ()
			);
			int offX = offset.getX() == 0 ? -0 : offset.getX() == 1 ? 1 : 0;
			int offY = offset.getY() == 0 ? -0 : offset.getY() == 1 ? 1 : 0;
			int offZ = offset.getZ() == 0 ? -0 : offset.getZ() == 1 ? 1 : 0;
			posLight = new BlockPos(
					posLight.getX() + offX,
					posLight.getY() + offY,
					posLight.getZ() + offZ
			);
		} else {
			Vector3f normal1;
			normal1 = new Vector3f(vert.nx, vert.ny, vert.nz);
			normal1.normalize();
			posLight = posLight.add(-Math.round(normal1.getX()), -Math.round(normal1.getY()), -Math.round(normal1.getZ()));
		}
		
		int overworldSky = LightTexture.getLightSky(overworldLight);
		int overworldBlock = LightTexture.getLightBlock(overworldLight);
		int blockLight = world.getLightFor(LightType.BLOCK, posLight);
		int skyLight = world.getLightFor(LightType.SKY, posLight);
		
		if (normal.y != 0) {
			normal = new Vector3d(0, -normal.y, 0);
		}
		
		if (vert.nx != 0 || vert.ny != 1 || vert.nz != 0)
			normal = new Vector3d(vert.nx, vert.ny, vert.nz);
		
		normal = normal.normalize();
		
		double amt = normal.dotProduct(new Vector3d(1, 0, 0.5).normalize());
		if (Double.isNaN(amt))
			amt = 1;
		amt = Math.abs(amt);
		amt /= 2.25;
		amt = 1 - amt;
		if (normal.y > 0)
			amt = amt / 2.15;
		
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
