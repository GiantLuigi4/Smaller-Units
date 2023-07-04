package tfc.smallerunits.client.render;

import com.mojang.blaze3d.vertex.*;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.vertices.BlockSensitiveBufferBuilder;
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
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.fml.ModList;
import tfc.smallerunits.UnitSpace;
import tfc.smallerunits.client.access.tracking.SUCapableChunk;
import tfc.smallerunits.client.render.storage.BufferStorage;
import tfc.smallerunits.client.render.util.RenderWorld;
import tfc.smallerunits.client.render.util.TranslatingVertexBuilder;
import tfc.smallerunits.data.capability.ISUCapability;
import tfc.smallerunits.utils.PositionalInfo;
import tfc.smallerunits.utils.math.Math1D;
import tfc.smallerunits.utils.storage.DefaultedMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

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
		
		for (int i = 0; i < RenderType.chunkBufferLayers().size(); i++) {
			RenderType chunkBufferLayer = RenderType.chunkBufferLayers().get(i);
			handleLayer(chunkBufferLayer, buffers, space.getRenderWorld(), stack, upb, space, dispatcher, states);
		}
		Minecraft.getInstance().getProfiler().popPush("finish");
		
		Minecraft.getInstance().getProfiler().popPush("upload");
		buffers.forEach(storage::upload);
		Minecraft.getInstance().getProfiler().pop();
		
		info.reset(player);
		
		return storage;
	}
	
	protected static boolean irisPresent = ModList.get().isLoaded("oculus");
	
	protected static void beginBlock(VertexConsumer consumer, BlockPos pos, BlockState block, Object2IntMap<BlockState> map, boolean isFluid) {
		if (irisPresent) {
			((BlockSensitiveBufferBuilder) consumer).beginBlock(
					(short) (map == null ? -1 : map.getOrDefault(isFluid ? block.getFluidState().createLegacyBlock() : block, -1)),
					(short) -1,
					pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15
			);
		}
	}
	
	private void endBlock(VertexConsumer consumer) {
		if (irisPresent) {
			((BlockSensitiveBufferBuilder) consumer).endBlock();
		}
	}
	
	private void handleLayer(RenderType chunkBufferLayer, DefaultedMap<RenderType, BufferBuilder> buffers, RenderWorld wld, PoseStack stack, int upb, UnitSpace space, BlockRenderDispatcher dispatcher, BlockState[] states) {
		Object2IntMap<BlockState> map = null;
		if (irisPresent) map = BlockRenderingSettings.INSTANCE.getBlockStateIds();
		
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
					if (block == null || block.isAir()) continue;
					
					BlockPos offsetPos = space.getOffsetPos(blockPosMut);
					// for some reason
					// this single line of code causes a lot of lag
					// TODO: what???
					FluidState fluid = block.getFluidState();
					if (!fluid.isEmpty()) {
						RenderType rendertype = ItemBlockRenderTypes.getRenderLayer(fluid);
						if (rendertype.equals(chunkBufferLayer)) {
							if (vertexBuilder == null) {
								if (consumer == null) consumer = buffers.get(chunkBufferLayer);
								vertexBuilder = new TranslatingVertexBuilder(1f / upb, consumer);
							}
							vertexBuilder.offset = new Vec3(
									(Math1D.getChunkOffset(offsetPos.getX(), 16)) * 16 - chunkOffset.getX() * space.unitsPerBlock,
									(Math1D.getChunkOffset(offsetPos.getY(), 16)) * 16 - chunkOffset.getY() * space.unitsPerBlock,
									(Math1D.getChunkOffset(offsetPos.getZ(), 16)) * 16 - chunkOffset.getZ() * space.unitsPerBlock
							);
							beginBlock(consumer, offsetPos, block, map, true);
							dispatcher.renderLiquid(
									offsetPos, wld, vertexBuilder,
									block, fluid
							);
							endBlock(consumer);
						}
					}
					
					if (block.getRenderShape() == RenderShape.MODEL) {
						RandomSource randomSource = new XoroshiroRandomSource(offsetPos.asLong());
						ModelData modelData = Objects.requireNonNull(wld.getModelDataManager()).getAt(offsetPos);
						BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(block);
						if (model.getRenderTypes(block, randomSource, modelData).contains(chunkBufferLayer)) {
							if (consumer == null) consumer = buffers.get(chunkBufferLayer);
							stk.pushPose();
							stk.translate(x, y, z);
							
							ModelData data = wld.getModelDataManager().getAt(offsetPos);
							if (data == null) data = ModelData.EMPTY;
							
							beginBlock(consumer, offsetPos, block, map, false);
							dispatcher.getModelRenderer().tesselateBlock(
									wld, dispatcher.getBlockModel(block),
									block, offsetPos, stk,
									consumer, true,
									randomSource,
									0, 0
							);
							endBlock(consumer);
							
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
