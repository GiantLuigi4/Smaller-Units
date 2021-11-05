package tfc.smallerunits.utils.rendering;

import com.jozufozu.flywheel.backend.model.IndexedModel;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import tfc.smallerunits.helpers.GameRendererHelper;
import tfc.smallerunits.renderer.FlywheelProgram;
import tfc.smallerunits.renderer.SmallerUnitsProgram;
import tfc.smallerunits.utils.rendering.flywheel.FlywheelVertexFormats;
import tfc.smallerunits.utils.rendering.flywheel.SUModel;

import java.util.ArrayList;

public class SUFLWVBO extends SURenderable {
	public ArrayList<ModelStorage> buffers = new ArrayList<>();
	
	public void render(MatrixStack matrixStack) {
		buffers.sort(ModelStorage::compareTo);
		Minecraft.getInstance().getProfiler().startSection("renderVBOs");
		SmallerUnitsProgram shader = FlywheelProgram.UNIT.getProgram(new ResourceLocation("smallerunits:unit_shader"));
		boolean isFirst = true;
		
		shader.bind();
		Matrix4f matrix4f = GameRendererHelper.matrix.copy();
		matrix4f.mul(matrixStack.getLast().getMatrix());
		shader.uploadViewProjection(matrix4f);
		shader.uploadCameraPos(
				Minecraft.getInstance().getRenderManager().info.getProjectedView().x,
				Minecraft.getInstance().getRenderManager().info.getProjectedView().y,
				Minecraft.getInstance().getRenderManager().info.getProjectedView().z
		);
		
		for (ModelStorage storage : buffers) {
			if (!storage.isPresent) continue;
			if (isFirst) {
				Minecraft.getInstance().getProfiler().startSection("renderVBO_" + RenderTypeHelper.getTypeName(storage.renderType));
				isFirst = false;
			} else {
				Minecraft.getInstance().getProfiler().endStartSection("renderVBO_" + RenderTypeHelper.getTypeName(storage.renderType));
			}
			storage.renderType.setupRenderState();
			storage.model.setupState();
			storage.model.drawCall();
			storage.model.clearState();
			storage.renderType.clearRenderState();
		}
		
		shader.unbind();
		
		Minecraft.getInstance().getProfiler().endSection();
		Minecraft.getInstance().getProfiler().endSection();
	}
	
	public void uploadTerrain(RenderType renderType, BufferBuilder bufferBuilder) {
		if (bufferBuilder.isDrawing())
			bufferBuilder.finishDrawing();
		else return;
		if (bufferBuilder != null) {
			for (ModelStorage buffer : buffers) {
				if (buffer.renderType.equals(renderType)) {
					(buffer.model).delete();
					IndexedModel mdl = new IndexedModel(new SUModel(FlywheelVertexFormats.BLOCK, bufferBuilder));
					buffer.model = mdl;
					buffer.isPresent = true;
					return;
				}
			}
		} else {
			for (ModelStorage buffer : buffers) {
				if (buffer.renderType.equals(renderType)) {
					buffer.isPresent = false;
					return;
				}
			}
		}
		ModelStorage storage = new ModelStorage();
		storage.renderType = renderType;
//		BufferBuilder.State state = bufferBuilder.getVertexState();
//		VertexBuffer buffer = new VertexBuffer(renderType.getVertexFormat());
//		if (bufferBuilder != null) buffer.upload(bufferBuilder);
//		bufferBuilder.setVertexState(state);
//		MemoryUtil.memFree(bufferBuilder.getNextBuffer().getSecond());
		IndexedModel mdl = new IndexedModel(new SUModel(FlywheelVertexFormats.BLOCK, bufferBuilder));
		storage.model = mdl;
		storage.isPresent = bufferBuilder != null;
		buffers.add(storage);
	}
	
	public void markAllUnused() {
		for (ModelStorage buffer : buffers) {
			buffer.isPresent = false;
		}
	}
	
	@Override
	public void delete() {
		for (ModelStorage buffer : buffers) buffer.model.delete();
		buffers.clear();
	}
	
	@Override
	public boolean isValid() {
		for (ModelStorage buffer : buffers) {
			if (!buffer.model.valid()) return false;
		}
		return true;
	}
}
