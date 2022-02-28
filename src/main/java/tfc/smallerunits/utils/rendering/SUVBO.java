package tfc.smallerunits.utils.rendering;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Optional;

public class SUVBO extends SURenderable {
	public ArrayList<BufferStorage> buffers = new ArrayList<>();
	int countPresent = 0;
	
	public void render(MatrixStack matrixStack) {
		if (buffers.isEmpty()) return;
		if (countPresent == 0) return;
		
		Minecraft.getInstance().getProfiler().startSection("vbos");
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
			
			Minecraft.getInstance().getProfiler().startSection("setType");
			VertexBuffer buffer = storage.terrainBuffer.get();
			RenderType type = RenderTypeHelper.getType(storage.renderType);
			matrixStack.push();
			buffer.bindBuffer();
			type.setupRenderState();
			DefaultVertexFormats.BLOCK.setupBufferState(0L);
//			RenderSystem.shadeModel(GL11.GL_SMOOTH);
			Minecraft.getInstance().getProfiler().endStartSection("drawCall");
			buffer.draw(matrixStack.getLast().getMatrix(), GL11.GL_QUADS);
			Minecraft.getInstance().getProfiler().endStartSection("clearType");
//			RenderSystem.clearCurrentColor();
			type.clearRenderState();
			matrixStack.pop();
			Minecraft.getInstance().getProfiler().endSection();
		}
		VertexBuffer.unbindBuffer();
		
		Minecraft.getInstance().getProfiler().endSection();
		Minecraft.getInstance().getProfiler().endSection();
		Minecraft.getInstance().getProfiler().endSection();
	}
	
	public void uploadTerrain(RenderType renderType, BufferBuilder bufferBuilder) {
		if (bufferBuilder != null) {
			if (bufferBuilder.isDrawing())
				bufferBuilder.finishDrawing();
			else return;
		}
		
		Minecraft.getInstance().getProfiler().startSection("vbos");
		if (bufferBuilder != null) {
			for (BufferStorage buffer : buffers) {
				if (buffer.renderType.equals(renderType)) {
					Minecraft.getInstance().getProfiler().startSection("genAndUpload");
					
					(buffer.terrainBuffer.get()).upload(bufferBuilder);
					if (!buffer.isPresent) countPresent++;
					buffer.isPresent = true;
					
					Minecraft.getInstance().getProfiler().endSection();
					Minecraft.getInstance().getProfiler().endSection();
					return;
				}
			}
		} else {
			for (BufferStorage buffer : buffers) {
				if (buffer.renderType.equals(renderType)) {
					buffer.isPresent = false;
					countPresent--;
					
					Minecraft.getInstance().getProfiler().endSection();
					return;
				}
			}
		}
		
		Minecraft.getInstance().getProfiler().startSection("uploadVBO");
		BufferStorage storage = new BufferStorage();
		storage.renderType = renderType;
//		BufferBuilder.State state = bufferBuilder.getVertexState();
		VertexBuffer buffer = new VertexBuffer(renderType.getVertexFormat());
		if (bufferBuilder != null) buffer.upload(bufferBuilder);
//		bufferBuilder.setVertexState(state);
//		MemoryUtil.memFree(bufferBuilder.getNextBuffer().getSecond());
		storage.terrainBuffer = Optional.of(buffer);
		if (bufferBuilder != null) countPresent++;
		storage.isPresent = bufferBuilder != null;
		buffers.add(storage);
		Minecraft.getInstance().getProfiler().endSection();
		Minecraft.getInstance().getProfiler().endSection();
	}
	
	public void markAllUnused() {
		for (BufferStorage buffer : buffers) {
			buffer.isPresent = false;
		}
	}
	
	@Override
	public void delete() {
		for (BufferStorage buffer : buffers) buffer.terrainBuffer.get().close();
		buffers.clear();
	}
	
	@Override
	public boolean isValid() {
		return true;
	}
}
