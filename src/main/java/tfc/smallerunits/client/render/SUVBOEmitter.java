package tfc.smallerunits.client.render;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.client.model.data.EmptyModelData;
import tfc.smallerunits.UnitSpace;
import tfc.smallerunits.client.render.storage.BufferStorage;
import tfc.smallerunits.client.tracking.SUCapableChunk;
import tfc.smallerunits.data.capability.ISUCapability;
import tfc.smallerunits.utils.math.Math1D;
import tfc.smallerunits.utils.storage.DefaultedMap;

import java.util.ArrayList;
import java.util.HashMap;

public class SUVBOEmitter {
	private static final ArrayList<BufferStorage> vbosFree = new ArrayList<>();
	private static final Object lock = new Object();
	private static final DefaultedMap<RenderType, BufferBuilder> buffers = new DefaultedMap<RenderType, BufferBuilder>().setDefaultVal(() -> new BufferBuilder(16));
	
	private final HashMap<BlockPos, BufferStorage> used = new HashMap<>();
	private final HashMap<BlockPos, BufferStorage> free = new HashMap<>();
	
	public BufferStorage genBuffers(LevelChunk chunk, SUCapableChunk suCapableChunk, ISUCapability capability, BlockPos pos) {
		UnitSpace space = capability.getUnit(pos);
		BufferStorage storage = getAndMark(pos);
		storage.deactivate();
		
		UnitSpace unit = capability.getUnit(pos);
		if (unit == null) {
			free.put(pos, getBuffers(pos));
			return null;
		}
		BlockState[] states = unit.getBlocks();
		BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
		PoseStack stack = new PoseStack();
		stack.translate(
				pos.getX() - chunk.getPos().getMinBlockX(),
//				pos.getY() < 0 ? ((16 - pos.getY() % 16) - 16) : (pos.getY() % 16),
				Math1D.chunkMod(pos.getY(), 16),
				pos.getZ() - chunk.getPos().getMinBlockZ()
		);
		float scl = 1f / space.unitsPerBlock;
		stack.scale(scl, scl, scl);
		DefaultedMap<RenderType, BufferBuilder> buffers = new DefaultedMap<>();
		buffers.setDefaultVal((type) -> {
			BufferBuilder builder = SUVBOEmitter.buffers.get(type);
			builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
			return builder;
		});
		for (int x = 0; x < 16; x++) {
			for (int y = 0; y < 16; y++) {
				for (int z = 0; z < 16; z++) {
					int indx = (((x * 16) + y) * 16) + z;
					BlockState block = states[indx];
					if (block.isAir()) continue;
					stack.pushPose();
					stack.translate(x, y, z);
					for (RenderType chunkBufferLayer : RenderType.chunkBufferLayers()) {
						if (ItemBlockRenderTypes.canRenderInLayer(block, chunkBufferLayer)) {
							dispatcher.getModelRenderer().renderModel(
									stack.last(),
									buffers.get(chunkBufferLayer),
									block,
									dispatcher.getBlockModel(block),
									1, 1, 1, LightTexture.FULL_SKY,
									OverlayTexture.NO_OVERLAY,
									EmptyModelData.INSTANCE
							);
						}
					}
					stack.popPose();
				}
			}
		}
		buffers.forEach(storage::upload);
		
		return storage;
	}
	
	@Deprecated(forRemoval = true)
	private BufferStorage getBuffers(BlockPos pos) {
		if (used.containsKey(pos)) return used.remove(pos);
		else if (free.containsKey(pos)) return free.remove(pos);
		else if (!vbosFree.isEmpty()) return vbosFree.remove(0);
		else return new BufferStorage();
	}
	
	public BufferStorage getAndMark(BlockPos pos) {
		BufferStorage strg = getBuffers(pos);
		used.put(pos, strg);
		return strg;
	}
	
	public void free() {
		synchronized (lock) {
			vbosFree.addAll(used.values());
			vbosFree.addAll(free.values());
			used.clear();
			free.clear();
		}
	}
}
