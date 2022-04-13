package tfc.smallerunits.client.render;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;
import tfc.smallerunits.UnitSpace;
import tfc.smallerunits.client.render.storage.BufferStorage;
import tfc.smallerunits.client.render.util.TranslatingVertexBuilder;
import tfc.smallerunits.client.tracking.SUCapableChunk;
import tfc.smallerunits.data.capability.ISUCapability;
import tfc.smallerunits.utils.storage.DefaultedMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class SUVBOEmitter {
	private static final ArrayList<BufferStorage> vbosFree = new ArrayList<>();
	private static final Object lock = new Object();
	private static final DefaultedMap<RenderType, BufferBuilder> buffers = new DefaultedMap<RenderType, BufferBuilder>().setDefaultVal(() -> new BufferBuilder(16));
	private static final ChunkBufferBuilderPack bufferBuilderPack = new ChunkBufferBuilderPack();
	
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
//				Math1D.chunkMod(pos.getY(), 16),
				pos.getY() & 15,
				pos.getZ() - chunk.getPos().getMinBlockZ()
		);
		float scl = 1f / space.unitsPerBlock;
		stack.scale(scl, scl, scl);
		DefaultedMap<RenderType, BufferBuilder> buffers = new DefaultedMap<>();
		buffers.setDefaultVal((type) -> {
//			BufferBuilder builder = SUVBOEmitter.buffers.get(type);
			BufferBuilder builder = bufferBuilderPack.builder(type);
			builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
			return builder;
		});
		MultiBufferSource bufferSource = new MultiBufferSource() {
			@Override
			public VertexConsumer getBuffer(RenderType pRenderType) {
				return buffers.get(pRenderType);
			}
		};
		int upb = space.unitsPerBlock;
		for (RenderType chunkBufferLayer : RenderType.chunkBufferLayers()) {
			ForgeHooksClient.setRenderType(chunkBufferLayer);
			for (int x = 0; x < upb; x++) {
				for (int y = 0; y < upb; y++) {
					for (int z = 0; z < upb; z++) {
						int indx = (((x * upb) + y) * upb) + z;
						BlockState block = states[indx];
						if (ItemBlockRenderTypes.canRenderInLayer(block.getFluidState(), chunkBufferLayer)) {
							BlockPos rPos = new BlockPos(x, y, z);
							TranslatingVertexBuilder builder = new TranslatingVertexBuilder(1f / unit.unitsPerBlock, buffers.get(chunkBufferLayer));
							builder.offset = new Vec3(
									space.pos.getX() * 16,
									space.pos.getY() * 16,
									space.pos.getZ() * 16
							);
							dispatcher.renderLiquid(
									space.getOffsetPos(rPos),
									space.getMyLevel(),
									builder,
									block.getFluidState()
							);
						}
						if (block.getRenderShape() != RenderShape.MODEL) continue;
						if (block.isAir()) continue;
						if (ItemBlockRenderTypes.canRenderInLayer(block, chunkBufferLayer)) {
							stack.pushPose();
							stack.translate(x, y, z);
//							IModelData data = EmptyModelData.INSTANCE;
//							if (value.tileEntity != null) data = value.tileEntity.getModelData();
							BlockPos rPos = new BlockPos(x, y, z);
							dispatcher.renderBatched(
									block, space.getOffsetPos(rPos),
									space.getMyLevel(), stack,
									buffers.get(chunkBufferLayer),
									true, new Random(space.getOffsetPos(rPos).asLong()),
									EmptyModelData.INSTANCE // TODO
							);
							stack.popPose();
						}
					}
				}
			}
		}
		ForgeHooksClient.setRenderType(null);
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
	
	public void markFree(BlockPos pos) {
		vbosFree.add(getBuffers(pos));
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
