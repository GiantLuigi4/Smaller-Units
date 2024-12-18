package tfc.smallerunits.mixin.data;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import tfc.smallerunits.client.access.tracking.SUCapableChunk;
import tfc.smallerunits.client.render.SUChunkRender;
import tfc.smallerunits.utils.asm.ModCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin implements SUCapableChunk {
	@Unique
	private ArrayList<BlockPos> dirtyBlocks = new ArrayList<>();
	@Unique
	private ArrayList<BlockPos> forRemoval = new ArrayList<>();
	@Unique
	private final ArrayList<BlockEntity> renderableBlockEntities = new ArrayList<>();
	
	@Override
	public BlockPos[] SU$dirty() {
		return dirtyBlocks.toArray(new BlockPos[0]);
	}
	
	@Override
	public BlockPos[] SU$toRemove() {
		return forRemoval.toArray(new BlockPos[0]);
	}
	
	@Override
	public BlockPos[] SU$forRemoval() {
		return dirtyBlocks.toArray(new BlockPos[0]);
	}
	
	@Override
	public void SU$markDirty(BlockPos pos) {
		if (!dirtyBlocks.contains(pos))
			dirtyBlocks.add(pos);
	}
	
	@Override
	public ArrayList<BlockEntity> getTiles() {
		return renderableBlockEntities;
	}
	
	@Override
	public void addTile(BlockEntity be) {
		synchronized (renderableBlockEntities) {
			for (BlockEntity renderableBlockEntity : renderableBlockEntities) {
				if (renderableBlockEntity.getBlockPos().equals(be.getBlockPos())) {
					renderableBlockEntities.remove(renderableBlockEntity);
					ModCompat.onRemoveBE(renderableBlockEntity);
					break;
				}
			}
			if (Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(be) != null) {
				// I believe this is how this should work
				renderableBlockEntities.add(be);
				ModCompat.onAddBE(be);
			}
		}
	}
	
	@Override
	public void SU$reset(ArrayList<BlockPos> notDone, ArrayList<BlockPos> notFree) {
		dirtyBlocks = notDone;
		forRemoval = notFree;
	}
	
	@Override
	public void SU$markGone(BlockPos pos) {
		forRemoval.add(pos);
	}

	Map<Integer, SUChunkRender> renderers = new HashMap<>();

	@Override
	public SUChunkRender SU$getRenderer(int yCoord) {
		SUChunkRender chrdr = renderers.get(yCoord);
		if (chrdr == null)
			renderers.put(yCoord, chrdr = new SUChunkRender((LevelChunk) (Object) this));
		return chrdr;
	}
}
