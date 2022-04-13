package tfc.smallerunits.mixin.data;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
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
	private final ArrayList<BlockEntity> renderableBlockEntities = new ArrayList<>();
	@Unique
	private final SUChunkRender compChunk = new SUChunkRender((LevelChunk) (Object) this);
	
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
		dirtyBlocks.add(pos);
	}
	
	@Override
	public ArrayList<BlockEntity> getTiles() {
		return renderableBlockEntities;
	}
	
	@Override
	public void addTile(BlockEntity be) {
		for (BlockEntity renderableBlockEntity : renderableBlockEntities) {
			if (renderableBlockEntity.getBlockPos().equals(be.getBlockPos())) {
				renderableBlockEntities.remove(renderableBlockEntity);
				break;
			}
		}
		if (Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(be) != null) {
			// I believe this is how this should work
			renderableBlockEntities.add(be);
		}
	}
	
	@Override
	public void SU$reset() {
		dirtyBlocks.clear();
		forRemoval.clear();
	}
	
	@Override
	public void SU$markGone(BlockPos pos) {
		forRemoval.add(pos);
	}
	
	@Override
	public SUChunkRender SU$getChunkRender() {
		return compChunk;
	}
}
