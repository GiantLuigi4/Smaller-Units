package com.tfc.smallerunits.utils.rendering;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.blaze3d.vertex.IVertexConsumer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import org.apache.logging.log4j.Level;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class ByteArrayBuilder implements IVertexConsumer {
	private byte[] bytes;
	private VertexFormat format;
	private byte[] builder = new byte[0];
	private VertexFormatElement element;
	private int elementIndex;
	private int vertexCount = 0;
	
	public ByteArrayBuilder(int initialCapacity) {
		elementIndex = -1;
		bytes = new byte[0];
	}
	
	public void begin(VertexFormat format) {
		elementIndex = -1;
		this.format = format;
		nextVertexFormatIndex();
	}
	
	public byte[] finish() {
		format = null;
		byte[] oldBytes = bytes;
		bytes = new byte[0];
		return oldBytes;
	}
	
	public ByteArrayBuilder grow(int amt) {
		byte[] oldBytes = bytes;
		bytes = new byte[bytes.length + amt];
		if (oldBytes.length >= 0) System.arraycopy(oldBytes, 0, bytes, 0, oldBytes.length);
		return this;
	}
	
	public ByteArrayBuilder growBuilder(int amt) {
		byte[] oldBytes = builder;
		builder = new byte[builder.length + amt];
		if (oldBytes.length >= 0) System.arraycopy(oldBytes, 0, builder, 0, oldBytes.length);
		return this;
	}
	
	@Override
	public IVertexBuilder pos(double x, double y, double z) {
		return IVertexConsumer.super.pos(x, y, z);
//		if (this.getCurrentElement().getType() != VertexFormatElement.Type.FLOAT) {
//			throw new IllegalStateException();
//		} else {
//			this.putFloat(0, (float)x);
//			this.putFloat(4, (float)y);
//			this.putFloat(8, (float)z);
//			this.nextVertexFormatIndex();
//			return this;
//		}
	}
	
	public void nextVertexFormatIndex() {
		List<VertexFormatElement> elements = format.getElements();
		if (++elementIndex >= elements.size()) {
			element = null;
			return;
		}
		VertexFormatElement formatElement = elements.get(elementIndex);
		element = formatElement;
		if (formatElement.getUsage() == VertexFormatElement.Usage.PADDING) this.nextVertexFormatIndex();
	}
	
	public void putByte(int indx, byte b) {
		if (builder.length < indx + 1) growBuilder(1);
		builder[builder.length - 1] = b;
	}
	
	// https://www.baeldung.com/java-convert-float-to-byte-array
	public void putFloat(int indx, float f) {
		if (builder.length < indx + 5) growBuilder(4);
		int intBits = Float.floatToIntBits(f);
		builder[builder.length - 4] = (byte) (intBits >> 24);
		builder[builder.length - 3] = (byte) (intBits >> 16);
		builder[builder.length - 2] = (byte) (intBits >> 8);
		builder[builder.length - 1] = (byte) (intBits);
	}
	
	// https://stackoverflow.com/questions/2188660/convert-short-to-byte-in-java
	public void putShort(int indx, short s) {
		if (builder.length < indx + 2) growBuilder(2);
		builder[builder.length - 2] = (byte) (s & 0xff);
		builder[builder.length - 1] = (byte) ((s >> 8) & 0xff);
	}
	
	@Override
	public IVertexBuilder color(int red, int green, int blue, int alpha) {
		return IVertexConsumer.super.color(red, green, blue, alpha);
//		VertexFormatElement element = this.getCurrentElement();
//		if (element.getUsage() != VertexFormatElement.Usage.COLOR) {
//			return this;
//		} else if (element.getType() != VertexFormatElement.Type.UBYTE) {
//			throw new IllegalStateException();
//		} else {
//			this.putByte(0, (byte)red);
//			this.putByte(1, (byte)green);
//			this.putByte(2, (byte)blue);
//			this.putByte(3, (byte)alpha);
//			this.nextVertexFormatIndex();
//		}
//		return this;
	}
	
	public VertexFormatElement getCurrentElement() {
		if (this.element == null) {
			throw new IllegalStateException("BufferBuilder not started");
		} else {
			return this.element;
		}
	}
	
	@Override
	public IVertexBuilder tex(float u, float v) {
		return IVertexConsumer.super.tex(u, v);
	}
	
	@Override
	public IVertexBuilder overlay(int u, int v) {
		return IVertexConsumer.super.overlay(u, v);
	}
	
	@Override
	public IVertexBuilder lightmap(int u, int v) {
		return IVertexConsumer.super.lightmap(u, v);
	}
	
	@Override
	public IVertexBuilder normal(float x, float y, float z) {
		return IVertexConsumer.super.normal(x, y, z);
	}
	
	@Override
	public void endVertex() {
		elementIndex = -1;
		element = null;
		int indx = bytes.length;
		grow(builder.length);
//		for (int i = 0; i < builder.length; i++) bytes[i + indx] = builder[i];
		System.arraycopy(builder, 0, bytes, 0 + indx, builder.length);
		nextVertexFormatIndex();
		vertexCount++;
	}
	
	public boolean isDrawing() {
		return element != null;
	}
	
	public ByteBuffer getBytes() {
//		int[] ints = new int[bytes.length];
//		for (int i = 0; i < bytes.length; i++) ints[i] = bytes[i];
//		return ints;
		LOGGER.log(Level.INFO, format.getSize() == builder.length);
		LOGGER.log(Level.INFO, Arrays.toString(bytes));
//		ByteBuffer buffer = GLAllocation.createDirectByteBuffer(bytes.length);
//		buffer.put(bytes);
		return ByteBuffer.wrap(bytes);
//		return ByteBuffer.wrap(bytes).compact().order(ByteOrder.BIG_ENDIAN);
//		return buffer;
//		return bytes;
	}
}
