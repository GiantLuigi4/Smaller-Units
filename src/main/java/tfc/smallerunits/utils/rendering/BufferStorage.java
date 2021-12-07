package tfc.smallerunits.utils.rendering;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.VertexBuffer;

import java.util.Comparator;
import java.util.Optional;

public class BufferStorage implements Comparable<BufferStorage>, Comparator<BufferStorage> {
	public RenderType renderType;
	public Optional<VertexBuffer> terrainBuffer;
	public Optional<VertexBuffer> fluidBuffer;
	public boolean isPresent = false;
	
	public BufferStorage() {
		//I've had a weird experience where java decided to mess up init order and initialized fields *after* I assigned values to them so
		terrainBuffer = Optional.empty();
		fluidBuffer = Optional.empty();
	}
	
	@Override
	public int compareTo(BufferStorage o) {
		// TODO: move to RenderTypeHelper
		RenderType otherType = o.renderType;
		if (otherType == renderType) return 0;
		if (RenderTypeHelper.getType(otherType) == RenderTypeHelper.getType(renderType)) return 0;
		if (RenderTypeHelper.getType(renderType) == RenderTypeHelper.getType(RenderType.getTranslucent())) return 1;
		if (RenderTypeHelper.getType(renderType) == RenderTypeHelper.getType(RenderType.getCutoutMipped())) return 1;
		if (RenderTypeHelper.getType(renderType) == RenderTypeHelper.getType(RenderType.getCutout())) return 1;
		if (RenderTypeHelper.getType(otherType) == RenderTypeHelper.getType(RenderType.getTranslucent())) return -1;
		if (RenderTypeHelper.getType(otherType) == RenderTypeHelper.getType(RenderType.getCutoutMipped())) return -1;
		if (RenderTypeHelper.getType(otherType) == RenderTypeHelper.getType(RenderType.getCutout())) return -1;
		return 0;
	}
	
	@Override
	public int compare(BufferStorage o1, BufferStorage o2) {
		return o1.compareTo(o2);
	}
}
