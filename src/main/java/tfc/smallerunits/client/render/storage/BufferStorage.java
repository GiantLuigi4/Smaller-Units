package tfc.smallerunits.client.render.storage;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.client.renderer.RenderType;

import java.util.ArrayList;
import java.util.HashMap;

public class BufferStorage {
	HashMap<RenderType, VertexBuffer> buffersActive = new HashMap<>();
	ArrayList<VertexBuffer> buffersInactive = new ArrayList<>();
	
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
		if (buffer.building()) buffer.end();
		VertexBuffer buffer1 = getBuffer(type);
		buffer1.upload(buffer);
		buffer.clear();
	}
	
	public boolean hasActive(RenderType type) {
		return buffersActive.containsKey(type);
	}
}
