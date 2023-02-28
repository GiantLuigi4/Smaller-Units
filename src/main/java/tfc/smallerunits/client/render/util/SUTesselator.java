package tfc.smallerunits.client.render.util;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import tfc.smallerunits.utils.asm.AssortedQol;
import tfc.smallerunits.utils.selection.MutableVec3;

public class SUTesselator extends Tesselator {
	float scl;
	MutableVec3 offset = new MutableVec3(0, 0, 0);
	
	public SUTesselator setScale(float scl) {
		this.scl = scl;
		return this;
	}
	
	public SUTesselator setOffset(double x, double y, double z) {
		offset.set(x, y, z);
		return this;
	}
	
	TranslatingBufferBuilder builder;
	
	public SUTesselator(int pCapacity) {
		super(pCapacity);
		builder = new TranslatingBufferBuilder(pCapacity);
	}
	
	public SUTesselator() {
		this(2097152);
	}
	
	@Override
	public BufferBuilder getBuilder() {
		return builder;
	}
	
	@Override
	public void end() {
		BufferUploader.draw(this.builder.end());
	}
	
	public class TranslatingBufferBuilder extends BufferBuilder {
		MutableVec3 vecs = new MutableVec3(0, 0, 0);
		
		public TranslatingBufferBuilder(int pCapacity) {
			super(pCapacity);
		}
		
		@Override
		public VertexConsumer vertex(double pX, double pY, double pZ) {
			AssortedQol.scaleVert(this, pX, pY, pZ, scl, vecs, offset);
			return super.vertex(vecs.x, vecs.y, vecs.z);
		}
		
		@Override
		public void vertex(float pX, float pY, float pZ, float pRed, float pGreen, float pBlue, float pAlpha, float pTexU, float pTexV, int pOverlayUV, int pLightmapUV, float pNormalX, float pNormalY, float pNormalZ) {
			AssortedQol.scaleVert(this, pX, pY, pZ, scl, vecs, offset);
			super.vertex((float) vecs.x, (float) vecs.y, (float) vecs.z, pRed, pGreen, pBlue, pAlpha, pTexU, pTexV, pOverlayUV, pLightmapUV, pNormalX, pNormalY, pNormalZ);
		}
	}
}
