package com.tfc.smallerunits.utils.rendering;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.BufferBuilder;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class FinalizableBufferBuilder extends BufferBuilder {
	ArrayList<ByteBuffer> buffers = new ArrayList<>();
	
	public FinalizableBufferBuilder(int bufferSizeIn) {
		super(bufferSizeIn);
	}
	
	@Override
	public Pair<DrawState, ByteBuffer> getNextBuffer() {
		Pair<DrawState, ByteBuffer> buffers = super.getNextBuffer();
		this.buffers.add(buffers.getSecond());
		return buffers;
	}
	
	@Override
	protected void finalize() throws Throwable {
		for (ByteBuffer buffer : buffers) {
			MemoryUtil.memFree(buffer);
		}
		super.finalize();
	}
}
