package tfc.smallerunits.client.render;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import tfc.smallerunits.UnitSpace;
import tfc.smallerunits.client.access.tracking.SUCapableChunk;
import tfc.smallerunits.client.render.storage.BufferStorage;
import tfc.smallerunits.client.render.util.RenderWorld;
import tfc.smallerunits.client.render.util.TranslatingVertexBuilder;
import tfc.smallerunits.data.capability.ISUCapability;
import tfc.smallerunits.plat.util.PlatformUtils;
import tfc.smallerunits.utils.PositionalInfo;
import tfc.smallerunits.utils.math.Math1D;
import tfc.smallerunits.utils.storage.DefaultedMap;

import java.util.ArrayList;
import java.util.HashMap;

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
//				pos.getY() < 0 ? ((16 - pos.getY() % 16) - 16) : (pos.getY() % 16),
//				Math1D.chunkMod(pos.getY(), 16),
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
		for (RenderType chunkBufferLayer : RenderType.chunkBufferLayers()) {
			handleLayer(chunkBufferLayer, buffers, space.getRenderWorld(), stack, upb, space, dispatcher, states);
		}
		Minecraft.getInstance().getProfiler().popPush("finish");
		
		Minecraft.getInstance().getProfiler().popPush("upload");
		buffers.forEach(storage::upload);
		Minecraft.getInstance().getProfiler().pop();
		
		info.reset(player);
		
		return storage;
	}
	
	private void handleLayer(RenderType chunkBufferLayer, DefaultedMap<RenderType, BufferBuilder> buffers, RenderWorld wld, PoseStack stack, int upb, UnitSpace space, BlockRenderDispatcher dispatcher, BlockState[] states) {
		Minecraft.getInstance().getProfiler().push("prepare");
		VertexConsumer consumer = null;
		TranslatingVertexBuilder vertexBuilder = null;
		SectionPos chunkPos = SectionPos.of(new BlockPos(space.pos.getX() & 511, space.pos.getY() & 511, space.pos.getZ() & 511));
		int chunkX = chunkPos.minBlockX() * space.unitsPerBlock;
		int chunkY = chunkPos.minBlockY() * space.unitsPerBlock;
		int chunkZ = chunkPos.minBlockZ() * space.unitsPerBlock;
		
		PoseStack stk = new PoseStack();
		stk.last().pose().set(stack.last().pose());
		stk.last().normal().set(stack.last().normal());
		BlockPos.MutableBlockPos blockPosMut = new BlockPos.MutableBlockPos();
		
		float scl = 1f / upb;
		
		Vector3f pTranslation = stack.last().pose().getTranslation(new Vector3f());
		
		RandomSource randomSource = new XoroshiroRandomSource(0);
		Matrix4f pose = stk.last().pose();
		Minecraft.getInstance().getProfiler().popPush("draw");
		Minecraft.getInstance().getProfiler().push("iterate");
		for (int x = 0; x < upb; x++) {
			int xUpb = x * upb;
			
			for (int y = 0; y < upb; y++) {
				int yUpb = (xUpb + y) * upb;
				
				for (int z = 0; z < upb; z++) {
					int indx = yUpb + z;
					
//					Minecraft.getInstance().getProfiler().popPush("get_block");
					BlockState block = states[indx];
//					Minecraft.getInstance().getProfiler().popPush("check_air");
					if (block == null || block.isAir()) {
//						Minecraft.getInstance().getProfiler().popPush("iterate");
						continue;
					}
					
					Minecraft.getInstance().getProfiler().popPush("space_pos");
					BlockPos offsetPos = space.getOffsetPosMut(blockPosMut.set(x, y, z));
					
					// render fluid
					Minecraft.getInstance().getProfiler().popPush("fluid");
					FluidState fluid = block.getFluidState();
					if (!fluid.isEmpty()) {
						RenderType rendertype = ItemBlockRenderTypes.getRenderLayer(fluid);
						if (rendertype.equals(chunkBufferLayer)) {
							if (vertexBuilder == null) {
								if (consumer == null) consumer = buffers.get(chunkBufferLayer);
								vertexBuilder = new TranslatingVertexBuilder(scl, consumer);
							}
							vertexBuilder.offset = new Vec3(
									(Math1D.getChunkOffset(offsetPos.getX(), 16)) * 16 - chunkX,
									(Math1D.getChunkOffset(offsetPos.getY(), 16)) * 16 - chunkY,
									(Math1D.getChunkOffset(offsetPos.getZ(), 16)) * 16 - chunkZ
							);
							dispatcher.renderLiquid(
									offsetPos, wld, vertexBuilder,
									block, fluid
							);
						}
					}
					
					// render block
					Minecraft.getInstance().getProfiler().popPush("block");
					if (block.getRenderShape() == RenderShape.MODEL) {
						Minecraft.getInstance().getProfiler().push("prepare");
						randomSource.setSeed(offsetPos.asLong());
						BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(block);
						Minecraft.getInstance().getProfiler().popPush("get_data");
						Object modelData = wld.getModelData(offsetPos);
						Minecraft.getInstance().getProfiler().popPush("check_render");
						if (PlatformUtils.canRenderIn(model, block, randomSource, modelData, chunkBufferLayer)) {
							if (consumer == null) consumer = buffers.get(chunkBufferLayer);
							
							Minecraft.getInstance().getProfiler().popPush("translate");
							pose.setTranslation(
									pTranslation.x + x * scl,
									pTranslation.y + y * scl,
									pTranslation.z + z * scl
							);
							Minecraft.getInstance().getProfiler().popPush("render");
							PlatformUtils.tesselate(dispatcher,
									wld, model,
									block, offsetPos, stk,
									consumer, true,
									randomSource,
									0, 0,
									modelData, chunkBufferLayer
							);
							Minecraft.getInstance().getProfiler().popPush("reset");
							pose.setTranslation(pTranslation);
						}
						Minecraft.getInstance().getProfiler().pop();
					}
					Minecraft.getInstance().getProfiler().popPush("iterate");
				}
			}
		}
		Minecraft.getInstance().getProfiler().pop();
		Minecraft.getInstance().getProfiler().pop();
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
