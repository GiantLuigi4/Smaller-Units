package com.tfc.smallerunits.utils.rendering;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.util.math.vector.Vector3d;

public class TranslatingVertexBuilder implements IVertexBuilder {
	public Vector3d offset = new Vector3d(0, 0, 0);
	float scl;
	IVertexBuilder parent;
	
	public TranslatingVertexBuilder(float scl, IVertexBuilder parent) {
		this.scl = scl;
		this.parent = parent;
	}
	
	@Override
	public IVertexBuilder pos(double x, double y, double z) {
		parent = parent.pos((x + offset.x) * scl, (y + offset.y) * scl, (z + offset.z) * scl);
		return this;
	}
	
	@Override
	public IVertexBuilder color(int red, int green, int blue, int alpha) {
		parent = parent.color(red, green, blue, alpha);
		return this;
	}
	
	@Override
	public IVertexBuilder tex(float u, float v) {
		parent = parent.tex(u, v);
		return this;
	}
	
	@Override
	public IVertexBuilder overlay(int u, int v) {
		parent = parent.overlay(u, v);
		return this;
	}
	
	@Override
	public IVertexBuilder lightmap(int u, int v) {
		parent = parent.lightmap(u, v);
		return this;
	}
	
	@Override
	public IVertexBuilder normal(float x, float y, float z) {
		parent = parent.normal(x, y, z);
		return this;
	}
	
	@Override
	public void endVertex() {
		parent.endVertex();
	}
}
