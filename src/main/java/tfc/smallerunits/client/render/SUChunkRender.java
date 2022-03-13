package tfc.smallerunits.client.render;

import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;
import tfc.smallerunits.client.render.storage.BufferStorage;

import java.util.ArrayList;

public class SUChunkRender {
	private final LevelChunk chunk;
	private final ArrayList<Pair<BlockPos, BufferStorage>> buffers = new ArrayList<>();
	private boolean isDirty = false;
	
	public SUChunkRender(LevelChunk chunk) {
		this.chunk = chunk;
	}
	
	public void draw(ChunkRenderDispatcher.RenderChunk renderChunk, RenderType type) {
		int yRL = renderChunk.getOrigin().getY();
		int yRM = renderChunk.getOrigin().getY() + 16;
		for (Pair<BlockPos, BufferStorage> buffer : buffers) {
			if (buffer.getFirst().getY() > yRM || buffer.getFirst().getY() < yRL) continue;
			BufferStorage strg = buffer.getSecond();
			if (strg.hasActive(type)) {
				VertexBuffer buffer1 = buffer.getSecond().getBuffer(type);
				if (type == RenderType.translucent()) {
					type.clearRenderState();
					RenderType.translucentNoCrumbling().setupRenderState();
					buffer1.drawChunkLayer();
					RenderType.translucentNoCrumbling().clearRenderState();
					type.setupRenderState();
				} else {
					buffer1.drawChunkLayer();
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
		isDirty = true;
	}
}
