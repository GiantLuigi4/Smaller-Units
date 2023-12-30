package tfc.smallerunits.mixin.compat.optimization.alternate_current;

import alternate.current.wire.LevelHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.simulation.level.ITickerLevel;

@Mixin(value = LevelHelper.class, remap = false)
public class ACLevelAccessMixin {
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkAccess;setUnsaved(Z)V"), method = "setWireState")
	private static void preUpdate(ServerLevel level, BlockPos pos, BlockState state, boolean updateNeighborShapes, CallbackInfoReturnable<Boolean> cir) {
		ITickerLevel.update(level, pos);
	}
	
	@Inject(at = @At("HEAD"), method = "setWireState", cancellable = true)
	private static void setWireState(ServerLevel level, BlockPos pos, BlockState state, boolean updateNeighborShapes, CallbackInfoReturnable<Boolean> cir) {
		if (!(level instanceof ITickerLevel)) return;
		
		if (!state.is(Blocks.REDSTONE_WIRE)) cir.setReturnValue(false);
		
		int y = pos.getY();
		int x = pos.getX();
		int z = pos.getZ();
		int index = level.getSectionIndex(y);
		
		ChunkAccess chunk = level.getChunk(x >> 4, z >> 4, ChunkStatus.FULL, false);
		if (chunk == null) {
			cir.setReturnValue(false);
			return;
		}
		
		LevelChunkSection section = chunk.getSection(index);
		//noinspection ConstantConditions
		if (section == null) {
			cir.setReturnValue(false); // the code definitely can get here
			return;
		}
		
		BlockState prevState = section.setBlockState(x & 15, y & 15, z & 15, state);
		
		level.getChunkSource().blockChanged(pos);
		chunk.setUnsaved(true);
		ITickerLevel.update(level, pos);
		
		if (updateNeighborShapes) {
			prevState.updateIndirectNeighbourShapes(level, pos, Block.UPDATE_CLIENTS);
			state.updateNeighbourShapes(level, pos, Block.UPDATE_CLIENTS);
			state.updateIndirectNeighbourShapes(level, pos, Block.UPDATE_CLIENTS);
		}
		
		cir.setReturnValue(true);
	}
}
