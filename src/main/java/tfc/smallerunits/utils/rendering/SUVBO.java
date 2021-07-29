package tfc.smallerunits.utils.rendering;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Optional;

public class SUVBO {
	public ArrayList<BufferStorage> buffers = new ArrayList<>();
	
	public void render(MatrixStack matrixStack) {
		buffers.sort(BufferStorage::compareTo);
		Minecraft.getInstance().getProfiler().startSection("renderVBOs");
		boolean isFirst = true;
		for (BufferStorage storage : buffers) {
			if (!storage.isPresent) continue;
			if (isFirst) {
				Minecraft.getInstance().getProfiler().startSection("renderVBO_" + RenderTypeHelper.getTypeName(storage.renderType));
				isFirst = false;
			} else {
				Minecraft.getInstance().getProfiler().endStartSection("renderVBO_" + RenderTypeHelper.getTypeName(storage.renderType));
			}
			VertexBuffer buffer = storage.terrainBuffer.get();
			RenderType type = RenderTypeHelper.getType(storage.renderType);
			matrixStack.push();
			buffer.bindBuffer();
			type.setupRenderState();
			DefaultVertexFormats.BLOCK.setupBufferState(0L);
			RenderSystem.shadeModel(GL11.GL_SMOOTH);
			buffer.draw(matrixStack.getLast().getMatrix(), GL11.GL_QUADS);
			VertexBuffer.unbindBuffer();
			RenderSystem.clearCurrentColor();
			type.clearRenderState();
			matrixStack.pop();
		}
		Minecraft.getInstance().getProfiler().endSection();
		Minecraft.getInstance().getProfiler().endSection();
	}
	
	public void uploadTerrain(RenderType renderType, BufferBuilder bufferBuilder) {
		if (bufferBuilder.isDrawing())
			bufferBuilder.finishDrawing();
		else return;
		if (bufferBuilder != null) {
			for (BufferStorage buffer : buffers) {
				if (buffer.renderType.equals(renderType)) {
					(buffer.terrainBuffer.get()).upload(bufferBuilder);
					buffer.isPresent = true;
					return;
				}
			}
		} else {
			for (BufferStorage buffer : buffers) {
				if (buffer.renderType.equals(renderType)) {
					buffer.isPresent = false;
					return;
				}
			}
		}
		BufferStorage storage = new BufferStorage();
		storage.renderType = renderType;
//		BufferBuilder.State state = bufferBuilder.getVertexState();
		VertexBuffer buffer = new VertexBuffer(renderType.getVertexFormat());
		if (bufferBuilder != null) buffer.upload(bufferBuilder);
//		bufferBuilder.setVertexState(state);
//		MemoryUtil.memFree(bufferBuilder.getNextBuffer().getSecond());
		storage.terrainBuffer = Optional.of(buffer);
		storage.isPresent = bufferBuilder != null;
		buffers.add(storage);
	}
	
	public void markAllUnused() {
		for (BufferStorage buffer : buffers) {
			buffer.isPresent = false;
		}
	}
}
