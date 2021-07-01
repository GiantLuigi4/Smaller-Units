package com.tfc.smallerunits.utils.rendering.flywheel;

import com.jozufozu.flywheel.backend.gl.buffer.GlBuffer;
import com.jozufozu.flywheel.backend.gl.buffer.MappedBufferUsage;
import com.jozufozu.flywheel.backend.gl.buffer.MappedFullBuffer;

public class SUMappedBuffer extends MappedFullBuffer {
	GlBuffer glBuffer;
	
	public SUMappedBuffer(GlBuffer buffer, MappedBufferUsage usage) {
		super(buffer, usage);
		this.glBuffer = buffer;
	}
	
	@Override
	protected void checkAndMap() {
		glBuffer.bind();
		super.checkAndMap();
	}
}
