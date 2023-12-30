package tfc.smallerunits.client.render.util;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.Vec3;

public class TranslatingVertexBuilder implements VertexConsumer {
	public Vec3 offset = new Vec3(0, 0, 0);
	float scl;
	VertexConsumer parent;
	
	public TranslatingVertexBuilder(float scl, VertexConsumer parent) {
		this.scl = scl;
		this.parent = parent;
	}
	
	@Override
	public VertexConsumer vertex(double x, double y, double z) {
		parent = parent.vertex((x + offset.x) * scl, (y + offset.y) * scl, (z + offset.z) * scl);
		return this;
	}
	
	@Override
	public VertexConsumer color(int red, int green, int blue, int alpha) {
		parent = parent.color(red, green, blue, alpha);
		return this;
	}
	
	@Override
	public VertexConsumer uv(float u, float v) {
		parent = parent.uv(u, v);
		return this;
	}
	
	@Override
	public VertexConsumer overlayCoords(int u, int v) {
		parent = parent.overlayCoords(u, v);
		return this;
	}
	
	@Override
	public VertexConsumer uv2(int u, int v) {
		parent = parent.uv2(u, v);
		return this;
	}
	
	@Override
	public VertexConsumer normal(float x, float y, float z) {
		parent = parent.normal(x, y, z);
		return this;
	}
	
	@Override
	public void endVertex() {
		parent.endVertex();
	}
	
	@Override
	public void defaultColor(int pDefaultR, int pDefaultG, int pDefaultB, int pDefaultA) {
		parent.defaultColor(pDefaultR, pDefaultG, pDefaultB, pDefaultA);
	}
	
	@Override
	public void unsetDefaultColor() {
		parent.unsetDefaultColor();
	}
}
