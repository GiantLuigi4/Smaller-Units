package tfc.smallerunits.client.render.storage;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.RenderType;
import tfc.smallerunits.client.render.util.RenderTypeData;

import java.util.ArrayList;
import java.util.HashMap;

public class BufferStorage {
	private static final ChunkBufferBuilderPack bufferBuilderPack = new ChunkBufferBuilderPack();
	
	HashMap<RenderType, VertexBuffer> buffersActive = new HashMap<>();
	ArrayList<VertexBuffer> buffersInactive = new ArrayList<>();
	HashMap<RenderType, BufferBuilder> sortableBuffers = new HashMap<>();
	
	public void deactivate() {
		buffersInactive.addAll(buffersActive.values());
		buffersActive.clear();
	}
	
	public VertexBuffer getBuffer(RenderType type) {
		if (buffersActive.containsKey(type)) return buffersActive.get(type);
		else if (!buffersInactive.isEmpty()) {
			VertexBuffer buf = buffersInactive.remove(0);
			buffersActive.put(type, buf);
			return buf;
		} else {
			VertexBuffer buf = new VertexBuffer();
			buffersActive.put(type, buf);
			return buf;
		}
	}
	
	public void upload(RenderType type, BufferBuilder buffer) {
		VertexBuffer buffer1 = getBuffer(type);
		buffer1.bind();
		buffer1.upload(buffer.end());
		VertexBuffer.unbind();
		buffer.clear();
		
		if (RenderTypeData.isSortable(type)) {
			sortableBuffers.put(type, buffer);
		}
	}
	
	public boolean hasActive(RenderType type) {
		return buffersActive.containsKey(type);
	}
	
	public BufferBuilder getBuilder(RenderType type) {
		if (RenderTypeData.isSortable(type)) {
			BufferBuilder builder = sortableBuffers.get(type);
			if (builder == null) sortableBuffers.put(type, builder = new BufferBuilder(type.bufferSize()));
			return builder;
		}
		return bufferBuilderPack.builder(type);
	}
}
