package tfc.smallerunits.client.render;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import tfc.smallerunits.client.render.storage.BufferStorage;

import java.util.ArrayList;

public class SUChunkRender {
	private final LevelChunk chunk;
	private final ArrayList<Pair<BlockPos, BufferStorage>> buffers = new ArrayList<>();
	
	public SUChunkRender(LevelChunk chunk) {
		this.chunk = chunk;
	}
	
	public void draw(BlockPos positionRendering, RenderType type, Frustum frustum, AbstractUniform uniform) {
		int yRL = positionRendering.getY();
		int yRM = positionRendering.getY() + 15;
		if (!buffers.isEmpty()) {
			((Uniform) uniform).upload();
		}
		for (Pair<BlockPos, BufferStorage> buffer : buffers) {
			if (buffer.getFirst().getY() > yRM || buffer.getFirst().getY() < yRL) continue;
			BufferStorage strg = buffer.getSecond();
			if (strg.hasActive(type)) {
				if (frustum.isVisible(new AABB(
						buffer.getFirst().getX(),
						buffer.getFirst().getY(),
						buffer.getFirst().getZ(),
						buffer.getFirst().getX() + 1,
						buffer.getFirst().getY() + 1,
						buffer.getFirst().getZ() + 1
				))) {
					VertexBuffer buffer1 = buffer.getSecond().getBuffer(type);
					buffer1.draw();
				}
			}
		}
	}
	
	public void addBuffers(BlockPos pos, BufferStorage genBuffers) {
		for (Pair<BlockPos, BufferStorage> buffer : buffers) {
			if (buffer.getFirst().equals(pos)) {
				buffers.remove(buffer);
				break;
			}
		}
		if (genBuffers != null) buffers.add(Pair.of(pos, genBuffers));
	}
	
	public void freeBuffers(BlockPos pos, SUVBOEmitter emitter) {
		for (Pair<BlockPos, BufferStorage> buffer : buffers) {
			if (buffer.getFirst().equals(pos)) {
				buffers.remove(buffer);
				emitter.getAndMark(pos);
				break;
			}
		}
	}
}
