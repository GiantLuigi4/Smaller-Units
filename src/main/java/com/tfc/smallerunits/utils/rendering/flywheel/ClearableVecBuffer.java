package com.tfc.smallerunits.utils.rendering.flywheel;

import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ClearableVecBuffer extends VecBuffer {
	public ClearableVecBuffer() {
	}
	
	public ClearableVecBuffer(ByteBuffer internal) {
		super(internal);
	}
	
	public static ClearableVecBuffer allocate(int bytes) {
		ByteBuffer buffer = ByteBuffer.allocate(bytes);
		buffer.order(ByteOrder.nativeOrder());
		return new ClearableVecBuffer(buffer);
	}
	
	public void clear() {
		internal.clear();
	}
}
