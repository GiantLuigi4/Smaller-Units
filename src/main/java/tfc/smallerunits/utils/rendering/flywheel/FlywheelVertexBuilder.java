package tfc.smallerunits.utils.rendering.flywheel;

import com.jozufozu.flywheel.backend.gl.buffer.*;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import java.nio.ByteBuffer;

public class FlywheelVertexBuilder implements IVertexBuilder {
	//	private final MappedBuffer buffer = new MappedFullBuffer(new GlBuffer(GlBufferType.ARRAY_BUFFER, GlBufferUsage.DYNAMIC_DRAW), MappedBufferUsage.READ_WRITE);
	private final MappedBuffer buffer;
	//	private final ClearableVecBuffer buffer;
	GlBuffer glBuffer;
	int vertices = 0;
	
	public FlywheelVertexBuilder(int allocation) {
		glBuffer = new GlBuffer(GlBufferType.ELEMENT_ARRAY_BUFFER, GlBufferUsage.STATIC_DRAW);
		glBuffer.bind();
		glBuffer.alloc(allocation);
		glBuffer.unbind();
		buffer = new SUMappedBuffer(glBuffer, MappedBufferUsage.READ_WRITE);
//		buffer = ClearableVecBuffer.allocate(160000); // TODO: change this to a MappedBuffer
	}
	
	@Override
	public IVertexBuilder pos(double x, double y, double z) {
		buffer.putVec3((float) x, (float) y, (float) z);
		glBuffer.unbind();
		return this;
	}
	
	@Override
	public IVertexBuilder color(int red, int green, int blue, int alpha) {
		buffer.putVec4((float) red, (float) green, (float) blue, (float) alpha);
		glBuffer.unbind();
		return this;
	}
	
	@Override
	public IVertexBuilder tex(float u, float v) {
		buffer.putVec2(u, v);
		glBuffer.unbind();
		return this;
	}
	
	@Override
	public IVertexBuilder overlay(int u, int v) {
		buffer.putVec2((byte) u, (byte) v);
		glBuffer.unbind();
		return this;
	}
	
	@Override
	public IVertexBuilder lightmap(int u, int v) {
		buffer.putVec2((byte) u, (byte) v);
		glBuffer.unbind();
		return this;
	}
	
	@Override
	public IVertexBuilder normal(float x, float y, float z) {
		buffer.putVec3(x, y, z);
		glBuffer.unbind();
		return this;
	}
	
	@Override
	public void endVertex() {
		vertices++;
	}
	
	public ByteBuffer unwrap() {
		return buffer.unwrap();
	}
	
	public int vertices() {
		return vertices;
	}
	
	public void close() {
		glBuffer.bind();
		buffer.flush();
		glBuffer.delete();
		glBuffer.unbind();
	}
}
