package tfc.smallerunits.mixin.data;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import tfc.smallerunits.client.render.SUChunkRender;
import tfc.smallerunits.client.tracking.SUCapableChunk;

import java.util.ArrayList;

@Mixin(LevelChunk.class)
public class LevelChunkMixin implements SUCapableChunk {
	@Unique
	private final ArrayList<BlockPos> dirtyBlocks = new ArrayList<>();
	@Unique
	private final ArrayList<BlockPos> forRemoval = new ArrayList<>();
	@Unique
	private final SUChunkRender compChunk = new SUChunkRender((LevelChunk) (Object) this);
	
	@Override
	public BlockPos[] dirty() {
		return dirtyBlocks.toArray(new BlockPos[0]);
	}
	
	@Override
	public BlockPos[] toRemove() {
		return forRemoval.toArray(new BlockPos[0]);
	}
	
	@Override
	public BlockPos[] forRemoval() {
		return dirtyBlocks.toArray(new BlockPos[0]);
	}
	
	@Override
	public void markDirty(BlockPos pos) {
		dirtyBlocks.add(pos);
	}
	
	@Override
	public void reset() {
		dirtyBlocks.clear();
		forRemoval.clear();
	}
	
	@Override
	public void markGone(BlockPos pos) {
		forRemoval.add(pos);
	}
	
	@Override
	public SUChunkRender getChunkRender() {
		return compChunk;
	}
}
