package tfc.smallerunits.utils.rendering;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import tfc.smallerunits.config.SmallerUnitsConfig;

public class BufferCache {
	public IRenderTypeBuffer buffer;
	public MatrixStack stack;
	public Object2ObjectLinkedOpenHashMap<RenderType, IVertexBuilder> builderHashMap = new Object2ObjectLinkedOpenHashMap<>();
	
	public BufferCache(IRenderTypeBuffer buffer, MatrixStack stack) {
		this.buffer = buffer;
		this.stack = stack;
	}
	
	public IVertexBuilder getBuffer(RenderType type) {
		if (!SmallerUnitsConfig.CLIENT.useExperimentalRenderer.get()) {
			return buffer.getBuffer(type);
		} else {
			if (!type.getVertexFormat().equals(DefaultVertexFormats.BLOCK)) return buffer.getBuffer(type);
			if (!builderHashMap.containsKey(type)) builderHashMap.put(type, buffer.getBuffer(type));
			return builderHashMap.get(type);
		}
	}
	
	public IRenderTypeBuffer getWrapped() {
		return buffer;
	}
}
