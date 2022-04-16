package tfc.smallerunits.client.render.util;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import tfc.smallerunits.utils.threading.ReusableThread;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class ThreadedVertexBuilder extends BufferBuilder {
	BufferBuilder parent;
	ThreadLocal<ArrayList<Vertex>> vertices = ThreadLocal.withInitial(ArrayList::new);
	ThreadLocal<Vertex> vertex = ThreadLocal.withInitial(Vertex::new);
	
	public ThreadedVertexBuilder(int pCapacity, BufferBuilder parent) {
		super(pCapacity);
		this.parent = parent;
	}
	
	@Override
	public void begin(VertexFormat.Mode pMode, VertexFormat pFormat) {
		parent.begin(pMode, pFormat);
	}
	
	@Override
	public void setQuadSortOrigin(float pSortX, float pSortY, float pSortZ) {
		parent.setQuadSortOrigin(pSortX, pSortY, pSortZ);
	}
	
	@Override
	public SortState getSortState() {
		return parent.getSortState();
	}
	
	@Override
	public void restoreSortState(SortState pSortState) {
		parent.restoreSortState(pSortState);
	}
	
	@Override
	public void end() {
		parent.end();
	}
	
	@Override
	public void putByte(int pIndex, byte pByteValue) {
		parent.putByte(pIndex, pByteValue);
	}
	
	@Override
	public void putShort(int pIndex, short pShortValue) {
		parent.putShort(pIndex, pShortValue);
	}
	
	@Override
	public void putFloat(int pIndex, float pFloatValue) {
		parent.putFloat(pIndex, pFloatValue);
	}
	
	@Override
	public void nextElement() {
		parent.nextElement();
	}
	
	@Override
	public void vertex(float pX, float pY, float pZ, float pRed, float pGreen, float pBlue, float pAlpha, float pTexU, float pTexV, int pOverlayUV, int pLightmapUV, float pNormalX, float pNormalY, float pNormalZ) {
		vertices.get().add(
				new Vertex(pX, pY, pZ, pRed, pGreen, pBlue, pAlpha, pTexU, pTexV, pOverlayUV, pLightmapUV, pNormalX, pNormalY, pNormalZ)
		);
	}
	
	@Override
	public Pair<DrawState, ByteBuffer> popNextBuffer() {
		return parent.popNextBuffer();
	}
	
	@Override
	public void clear() {
		parent.clear();
	}
	
	@Override
	public void discard() {
		parent.discard();
	}
	
	@Override
	public VertexFormatElement currentElement() {
		return parent.currentElement();
	}
	
	@Override
	public boolean building() {
		return parent.building();
	}
	
	@Override
	public void putBulkData(ByteBuffer buffer) {
		parent.putBulkData(buffer);
	}
	
	@Override
	public VertexFormat getVertexFormat() {
		return parent.getVertexFormat();
	}
	
	@Override
	public VertexConsumer vertex(double pX, double pY, double pZ) {
		Vertex v = vertex.get();
		v.x = pX;
		v.y = pY;
		v.z = pZ;
		return this;
	}
	
	@Override
	public VertexConsumer color(int pRed, int pGreen, int pBlue, int pAlpha) {
		Vertex v = vertex.get();
		v.r = pRed;
		v.g = pGreen;
		v.b = pBlue;
		v.a = pAlpha;
		return this;
	}
	
	@Override
	public VertexConsumer uv(float pU, float pV) {
		Vertex v = vertex.get();
		v.tu = pU;
		v.tv = pV;
		return this;
	}
	
	@Override
	public VertexConsumer overlayCoords(int pU, int pV) {
		Vertex v = vertex.get();
		v.ou = pU;
		v.ov = pV;
		return this;
	}
	
	@Override
	public VertexConsumer uv2(int pU, int pV) {
		Vertex v = vertex.get();
		v.lu = pU;
		v.lv = pV;
		return this;
	}
	
	@Override
	public VertexConsumer normal(float pX, float pY, float pZ) {
		Vertex v = vertex.get();
		v.nx = pX;
		v.ny = pY;
		v.nz = pZ;
		return this;
	}
	
	@Override
	public void endVertex() {
		vertices.get().add(vertex.get());
		vertex.set(new Vertex());
	}
	
	@Override
	public void defaultColor(int pDefaultR, int pDefaultG, int pDefaultB, int pDefaultA) {
	}
	
	@Override
	public void unsetDefaultColor() {
	}
	
	public void finish(ReusableThread[] threads) {
		for (ReusableThread thread : threads) {
			while (thread.isInUse()) {
			}
			thread.setAction(() -> {
				for (Vertex vertex1 : vertices.get()) {
					parent.vertex(
							(float) vertex1.x, (float) vertex1.y, (float) vertex1.z,
							vertex1.r / 255f,
							vertex1.b / 255f,
							vertex1.g / 255f,
							vertex1.a / 255f,
							vertex1.tu, vertex1.tv,
							OverlayTexture.pack(vertex1.ou, vertex1.ov),
							OverlayTexture.pack(vertex1.lu, vertex1.lv),
							vertex1.nx, vertex1.ny, vertex1.nz
					);
				}
			});
			thread.start();
		}
	}
	
	public class Vertex {
		double x, y, z;
		int r, g, b, a;
		float tu, tv;
		int ou, ov;
		int lu, lv;
		float nx, ny, nz;
		
		public Vertex(double x, double y, double z, int r, int g, int b, int a, float tu, float tv, int ou, int ov, int lu, int lv, float nx, float ny, float nz) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.r = r;
			this.g = g;
			this.b = b;
			this.a = a;
			this.tu = tu;
			this.tv = tv;
			this.ou = ou;
			this.ov = ov;
			this.lu = lu;
			this.lv = lv;
			this.nx = nx;
			this.ny = ny;
			this.nz = nz;
		}
		
		public Vertex(float pX, float pY, float pZ, float pRed, float pGreen, float pBlue, float pAlpha, float pTexU, float pTexV, int pOverlayUV, int pLightmapUV, float pNormalX, float pNormalY, float pNormalZ) {
			this(
					pX, pY, pZ,
					(int) (pRed * 255), (int) (pGreen * 255),
					(int) (pBlue * 255), (int) (pAlpha * 255),
					pTexU, pTexV, (pOverlayUV & 0xFFFF) >> 4,
					pOverlayUV >> 20 & '\uffff' /* decompiler output be like */,
					LightTexture.block(pLightmapUV),
					LightTexture.sky(pLightmapUV),
					pNormalX, pNormalY, pNormalZ
			);
		}
		
		public Vertex() {
		}
	}
}
