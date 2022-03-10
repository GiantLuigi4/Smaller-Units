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
	
	public void draw(ChunkRenderDispatcher.RenderChunk renderChunk) {
		int yRL = renderChunk.getOrigin().getY();
		int yRM = renderChunk.getOrigin().getY() + 16;
		for (Pair<BlockPos, BufferStorage> buffer : buffers) {
			if (buffer.getFirst().getY() > yRM || buffer.getFirst().getY() < yRL) continue;
			VertexBuffer buffer1 = buffer.getSecond().getBuffer(RenderType.solid());
			buffer1.drawChunkLayer();
		}
	}
	
	public void addBuffers(BlockPos pos, BufferStorage genBuffers) {
		buffers.add(Pair.of(pos, genBuffers));
		isDirty = true;
	}
}
