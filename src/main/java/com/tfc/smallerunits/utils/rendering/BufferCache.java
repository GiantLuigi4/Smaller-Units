package com.tfc.smallerunits.utils.rendering;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.tfc.smallerunits.SmallerUnitsConfig;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class BufferCache {
	public IRenderTypeBuffer buffer;
	public MatrixStack stack;
	public Object2ObjectLinkedOpenHashMap<RenderType, IVertexBuilder> builderHashMap = new Object2ObjectLinkedOpenHashMap<>();
	
	public BufferCache(IRenderTypeBuffer buffer, MatrixStack stack) {
		this.buffer = buffer;
		this.stack = stack;
	}
	
	public IVertexBuilder getBuffer(RenderType type) {
		if (!SmallerUnitsConfig.CLIENT.useExperimentalRenderer.get() || true) {
			return buffer.getBuffer(type);
		} else {
			if (!type.getVertexFormat().equals(DefaultVertexFormats.BLOCK)) {
				return buffer.getBuffer(type);
			}
//			if (
//					type.equals(RenderType.getCutout()) ||
//							type.equals(RenderType.getCutoutMipped()) ||
//							type.equals(RenderType.getTranslucent()) ||
//							type.equals(RenderType.getTranslucentNoCrumbling()) ||
//							type.equals(RenderType.getTranslucentMovingBlock()) ||
//							type.equals(RenderType.getTripwire())
//			)
//				return buffer.getBuffer(type);
			if (!builderHashMap.containsKey(type)) {
				builderHashMap.put(type, buffer.getBuffer(type));
			}
			return builderHashMap.get(type);
		}
	}
	
	public IRenderTypeBuffer getWrapped() {
		return buffer;
	}
}
