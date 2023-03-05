package tfc.smallerunits.client.render;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import tfc.smallerunits.UnitSpace;
import tfc.smallerunits.client.access.tracking.SUCapableChunk;
import tfc.smallerunits.client.render.storage.BufferStorage;
import tfc.smallerunits.client.render.util.RenderWorld;
import tfc.smallerunits.client.render.util.TranslatingVertexBuilder;
import tfc.smallerunits.data.capability.ISUCapability;
import tfc.smallerunits.utils.PositionalInfo;
import tfc.smallerunits.utils.math.Math1D;
import tfc.smallerunits.utils.platform.PlatformUtils;
import tfc.smallerunits.utils.platform.PlatformUtilsClient;
import tfc.smallerunits.utils.storage.DefaultedMap;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class SUVBOEmitter {
	private static final ArrayList<BufferStorage> vbosFree = new ArrayList<>();
	private static final Object lock = new Object();
	
	private final HashMap<BlockPos, BufferStorage> used = new HashMap<>();
	private final HashMap<BlockPos, BufferStorage> free = new HashMap<>();

//	private static final ReusableThread[] threads = new ReusableThread[16];
	
	static {
//		for (int i = 0; i < threads.length; i++) {
//			threads[i] = new ReusableThread(() -> {
//			});
//		}
	}
	
	public BufferStorage genBuffers(LevelChunk chunk, SUCapableChunk suCapableChunk, ISUCapability capability, BlockPos pos) {
		UnitSpace space = capability.getUnit(pos);
		
		BufferStorage storage = getAndMark(pos);
		storage.deactivate();
		
		UnitSpace unit = capability.getUnit(pos);
		if (unit == null) {
			free.put(pos, getBuffers(pos));
			return null;
		}
		
		Player player = Minecraft.getInstance().player;
		PositionalInfo info = new PositionalInfo(player, false);
		info.scalePlayerReach(player, space.unitsPerBlock);
		info.adjust(player, space);
		
		Minecraft.getInstance().getProfiler().push("get_blocks");
		BlockState[] states = unit.getBlocks();
		Minecraft.getInstance().getProfiler().pop();
		BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
		PoseStack stack = new PoseStack();
		stack.translate(
				pos.getX() - chunk.getPos().getMinBlockX(),
				pos.getY() & 15,
				pos.getZ() - chunk.getPos().getMinBlockZ()
		);
		float scl = 1f / space.unitsPerBlock;
		stack.scale(scl, scl, scl);
		DefaultedMap<RenderType, BufferBuilder> buffers = new DefaultedMap<>();
		buffers.setDefaultVal((type) -> {
			BufferBuilder builder = storage.getBuilder(type);
			builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
			return builder;
		});
		int upb = space.unitsPerBlock;
		Minecraft.getInstance().getProfiler().push("draw_loop");
		
		for (int i = 0; i < RenderType.chunkBufferLayers().size(); i++) {
			RenderType chunkBufferLayer = RenderType.chunkBufferLayers().get(i);
			// TODO:
//			ForgeHooksClient.setRenderType(chunkBufferLayer);
			handleLayer(chunkBufferLayer, buffers, space.getRenderWorld(), stack, upb, space, dispatcher, states);
		}
		Minecraft.getInstance().getProfiler().popPush("finish");
		
		// TODO:
//		ForgeHooksClient.setRenderType(null);
		Minecraft.getInstance().getProfiler().popPush("upload");
		buffers.forEach(storage::upload);
		Minecraft.getInstance().getProfiler().pop();
		
		info.reset(player);
		
		return storage;
	}
	
	private void handleLayer(RenderType chunkBufferLayer, DefaultedMap<RenderType, BufferBuilder> buffers, RenderWorld wld, PoseStack stack, int upb, UnitSpace space, BlockRenderDispatcher dispatcher, BlockState[] states) {
		VertexConsumer consumer = null;
		TranslatingVertexBuilder vertexBuilder = null;
		SectionPos chunkPos = SectionPos.of(new BlockPos(space.pos.getX() & 511, space.pos.getY() & 511, space.pos.getZ() & 511));
		BlockPos chunkOffset = new BlockPos(chunkPos.minBlockX(), chunkPos.minBlockY(), chunkPos.minBlockZ());
		PoseStack stk = new PoseStack();
		stk.last().pose().load(stack.last().pose());
		stk.last().normal().load(stack.last().normal());
		BlockPos.MutableBlockPos blockPosMut = new BlockPos.MutableBlockPos();
		for (int x = 0; x < upb; x++) {
			for (int y = 0; y < upb; y++) {
				for (int z = 0; z < upb; z++) {
					blockPosMut.set(x, y, z);
					int indx = (((x * upb) + y) * upb) + z;
					BlockState block = states[indx];
					if (block.isAir()) continue;
					
					FluidState fluid = block.getFluidState();
					if (!fluid.isEmpty()) {
						if (PlatformUtilsClient.checkRenderLayer(fluid, chunkBufferLayer)) {
							if (vertexBuilder == null) {
								if (consumer == null) consumer = buffers.get(chunkBufferLayer);
								vertexBuilder = new TranslatingVertexBuilder(1f / upb, consumer);
							}
							BlockPos offsetPos = space.getOffsetPos(blockPosMut);
							vertexBuilder.offset = new Vec3(
									((int) Math1D.getChunkOffset(offsetPos.getX(), 16)) * 16 - chunkOffset.getX() * space.unitsPerBlock,
									((int) Math1D.getChunkOffset(offsetPos.getY(), 16)) * 16 - chunkOffset.getY() * space.unitsPerBlock,
									((int) Math1D.getChunkOffset(offsetPos.getZ(), 16)) * 16 - chunkOffset.getZ() * space.unitsPerBlock
							);
							dispatcher.renderLiquid(
									offsetPos, wld, vertexBuilder,
									block, fluid
							);
						}
					}
					if (block.getRenderShape() != RenderShape.INVISIBLE) {
						if (PlatformUtilsClient.checkRenderLayer(block, chunkBufferLayer)) {
							if (consumer == null) consumer = buffers.get(chunkBufferLayer);
							stk.pushPose();
							stk.translate(x, y, z);
							
							BlockPos offsetPos = space.getOffsetPos(blockPosMut);
//							dispatcher.getModelRenderer().tesselateWithAO(
//									wld, dispatcher.getBlockModel(block),
//									block, offsetPos, stk,
//									consumer, true,
//									new Random(offsetPos.asLong()),
//									0, 0
//							);
							
							dispatcher.renderBatched(
									block, offsetPos, wld, stk, consumer,
									true, new Random(offsetPos.asLong())
							);
							
							stk.popPose();
						}
					}
				}
			}
		}
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
