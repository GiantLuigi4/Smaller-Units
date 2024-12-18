package tfc.smallerunits.client.render;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;
import tfc.smallerunits.client.abstraction.IFrustum;
import tfc.smallerunits.client.render.storage.BufferStorage;
import tfc.smallerunits.utils.selection.MutableAABB;

import java.util.ArrayList;

public class SUChunkRender {
	private final LevelChunk chunk;
	private final ArrayList<BufferInfo> buffers = new ArrayList<>();
	boolean empty = true;

	public boolean isEmpty() {
		return empty;
	}

	public SUChunkRender(LevelChunk chunk) {
		this.chunk = chunk;
	}

	public void draw(RenderType type, AbstractUniform uniform) {
		if (!isEmpty()) {
			((Uniform) uniform).upload();

			for (BufferInfo buffer : buffers) {
				if (buffer.culled)
					continue;

				BufferStorage strg = buffer.storage;
				if (strg.hasActive(type)) {
                    VertexBuffer buffer1 = buffer.storage.getBuffer(type);
                    buffer1.bind();
                    buffer1.draw();
				}
			}
		}
	}

	public void addBuffers(BlockPos pos, BufferStorage genBuffers) {
		for (BufferInfo buffer : buffers) {
			if (buffer.pos.equals(pos)) {
				buffers.remove(buffer);
				break;
			}
		}
		if (genBuffers != null) buffers.add(new BufferInfo(
				pos, genBuffers
		));

		empty = false;
	}

	public void freeBuffers(BlockPos pos, SUVBOEmitter emitter) {
		for (BufferInfo buffer : buffers) {
			if (buffer.pos.equals(pos)) {
				buffers.remove(buffer);
				emitter.getAndMark(pos);

				empty = buffers.isEmpty();
				break;
			}
		}
	}

	public void performCull(MutableAABB mutableAABB, IFrustum frustum) {
		for (BufferInfo buffer : buffers) {
			buffer.culled = !frustum.test(mutableAABB.set(
                    buffer.pos.getX(),
                    buffer.pos.getY(),
                    buffer.pos.getZ(),
                    buffer.pos.getX() + 1,
                    buffer.pos.getY() + 1,
                    buffer.pos.getZ() + 1
            ));
		}
	}
}
