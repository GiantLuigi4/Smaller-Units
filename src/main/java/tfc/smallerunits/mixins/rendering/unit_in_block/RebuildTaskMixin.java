package tfc.smallerunits.mixins.rendering.unit_in_block;

import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.chunk.ChunkRenderCache;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.LazyOptional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.utils.accessor.INeedPosition;
import tfc.smallerunits.utils.data.SUCapability;
import tfc.smallerunits.utils.data.SUCapabilityManager;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@Mixin(ChunkRenderDispatcher.ChunkRender.RebuildTask.class)
public abstract class RebuildTaskMixin implements INeedPosition {
	@Shadow
	@Nullable
	protected ChunkRenderCache chunkRenderCache;
	@Unique
	protected ChunkRenderCache chunkRenderCacheCached;
	@Unique
	BlockPos pos;
	
	@Inject(at = @At("HEAD"), method = "compile")
	public void preCompile(float xIn, float yIn, float zIn, ChunkRenderDispatcher.CompiledChunk compiledChunkIn, RegionRenderCacheBuilder builderIn, CallbackInfoReturnable<Set<TileEntity>> cir) {
		this.chunkRenderCacheCached = chunkRenderCache;
	}
	
	@Inject(at = @At("TAIL"), method = "compile", cancellable = true)
	public void postCompile(float xIn, float yIn, float zIn, ChunkRenderDispatcher.CompiledChunk compiledChunkIn, RegionRenderCacheBuilder builderIn, CallbackInfoReturnable<Set<TileEntity>> cir) {
		if (chunkRenderCacheCached == null) return;
		ChunkRenderCacheAccessor accessor = (ChunkRenderCacheAccessor) chunkRenderCacheCached;
		
		if (pos == null) return;
		BlockPos blockpos = pos.toImmutable();
		BlockPos blockpos1 = blockpos.add(15, 15, 15);
		
		Set<TileEntity> tiles = cir.getReturnValue();
		
		if (tiles == null) {
			tiles = new HashSet<>();
			cir.setReturnValue(tiles);
		}
		
		if (accessor.getWorld() == null) return;
		Chunk chunk = accessor.getWorld().getChunkAt(blockpos);
		if (chunk == null) return;
		LazyOptional<SUCapability> capability = chunk.getCapability(SUCapabilityManager.SUCapability);
		if (capability.isPresent()) {
			if (capability.isPresent()) {
				if (capability.resolve().isPresent()) {
					SUCapability cap = capability.resolve().get();
					if (cap == null) return;
					HashMap<BlockPos, UnitTileEntity> map = cap.getMap();
					if (map == null) return;
					for (BlockPos blockpos2 : BlockPos.getAllInBoxMutable(blockpos, blockpos1)) {
						if (map.containsKey(blockpos2)) {
							tiles.add(map.get(blockpos2));
						}
					}
				}
			}
		}
	}
	
	@Override
	public void SmallerUnits_setPos(BlockPos pos) {
		this.pos = pos;
	}
}
