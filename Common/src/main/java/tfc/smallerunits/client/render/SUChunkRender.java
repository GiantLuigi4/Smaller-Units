package tfc.smallerunits.client.render;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;
import tfc.smallerunits.client.abstraction.IFrustum;
import tfc.smallerunits.client.render.storage.BufferStorage;
import tfc.smallerunits.utils.selection.MutableAABB;

import java.util.ArrayList;

public class SUChunkRender {
	private final LevelChunk chunk;
	private final ArrayList<Pair<BlockPos, BufferStorage>> buffers = new ArrayList<>();
	boolean empty = true;
	
	public boolean hasBuffers() {
		return empty;
	}
	
	public SUChunkRender(LevelChunk chunk) {
		this.chunk = chunk;
	}
	
	public void draw(BlockPos positionRendering, RenderType type, IFrustum frustum, AbstractUniform uniform) {
		if (!hasBuffers()) {
			int yRL = positionRendering.getY();
			int yRM = positionRendering.getY() + 15;
			
			((Uniform) uniform).upload();
			
			MutableAABB frustumBB = new MutableAABB(0, 0, 0, 0, 0, 0);
			for (Pair<BlockPos, BufferStorage> buffer : buffers) {
				if (buffer.getFirst().getY() > yRM || buffer.getFirst().getY() < yRL) continue;
				BufferStorage strg = buffer.getSecond();
				if (strg.hasActive(type)) {
					if (frustum.test(frustumBB.set(
							buffer.getFirst().getX(),
							buffer.getFirst().getY(),
							buffer.getFirst().getZ(),
							buffer.getFirst().getX() + 1,
							buffer.getFirst().getY() + 1,
							buffer.getFirst().getZ() + 1
					))) {
						VertexBuffer buffer1 = buffer.getSecond().getBuffer(type);
//						buffer1.bind();
						buffer1.drawChunkLayer();
					}
				}
			}
			
			VertexBuffer.unbind();
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
		
		empty = false;
	}
	
	public void freeBuffers(BlockPos pos, SUVBOEmitter emitter) {
		for (Pair<BlockPos, BufferStorage> buffer : buffers) {
			if (buffer.getFirst().equals(pos)) {
				buffers.remove(buffer);
				emitter.getAndMark(pos);
				
				empty = buffers.isEmpty();
				break;
			}
		}
	}
}
