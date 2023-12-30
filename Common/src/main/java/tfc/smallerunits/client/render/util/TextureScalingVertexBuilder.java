package tfc.smallerunits.client.render.util;

import com.mojang.blaze3d.vertex.VertexConsumer;

public class TextureScalingVertexBuilder implements VertexConsumer {
	VertexConsumer src;
	float scale;
	
	public TextureScalingVertexBuilder(VertexConsumer src, float scale) {
		this.src = src;
		this.scale = scale;
	}
	
	@Override
	public VertexConsumer vertex(double pX, double pY, double pZ) {
		src.vertex(pX, pY, pZ);
		return this;
	}
	
	@Override
	public VertexConsumer color(int pRed, int pGreen, int pBlue, int pAlpha) {
		src.color(pRed, pGreen, pBlue, pAlpha);
		return this;
	}
	
	@Override
	public VertexConsumer uv(float pU, float pV) {
		src.uv(pU * scale, pV * scale);
		return this;
	}
	
	@Override
	public VertexConsumer overlayCoords(int pU, int pV) {
		src.overlayCoords(pU, pV);
		return this;
	}
	
	@Override
	public VertexConsumer uv2(int pU, int pV) {
		src.uv2(pU, pV);
		return this;
	}
	
	@Override
	public VertexConsumer normal(float pX, float pY, float pZ) {
		src.normal(pX, pY, pZ);
		return this;
	}
	
	@Override
	public void endVertex() {
		src.endVertex();
	}
	
	@Override
	public void defaultColor(int pDefaultR, int pDefaultG, int pDefaultB, int pDefaultA) {
		src.defaultColor(pDefaultR, pDefaultG, pDefaultB, pDefaultA);
	}
	
	@Override
	public void unsetDefaultColor() {
		src.unsetDefaultColor();
	}
}
