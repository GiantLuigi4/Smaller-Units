package tfc.smallerunits.mixin.compat.alternate_current;

import alternate.current.wire.LevelAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.simulation.level.ITickerLevel;

@Mixin(value = LevelAccess.class, remap = false)
public class ACLevelAccessMixin {
	@Shadow
	@Final
	private ServerLevel level;
	
	@Unique
	ChunkPos lastChkPos = new ChunkPos(Integer.MAX_VALUE, Integer.MAX_VALUE);
	@Unique
	ChunkAccess access = null;
	@Unique
	boolean isSU = false;
	
	@Inject(at = @At("TAIL"), method = "<init>")
	public void postInit(ServerLevel level, CallbackInfo ci) {
		if (level instanceof ITickerLevel) {
			isSU = true;
		}
	}
	
	@Inject(at = @At("HEAD"), method = "getBlockState", cancellable = true)
	void getBlockState(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
		if (!isSU) return;
		
		int y = pos.getY();
		int x = pos.getX();
		int z = pos.getZ();
		
		int index = this.level.getSectionIndex(y);
		
		ChunkAccess chunk = this.level.getChunk(x >> 4, z >> 4, ChunkStatus.FULL, false);
		if (chunk == null) {
			cir.setReturnValue(Blocks.VOID_AIR.defaultBlockState());
			return;
		}
		
		LevelChunkSection section = chunk.getSection(index);
		//noinspection ConstantConditions
		if (section == null) {
			cir.setReturnValue(Blocks.AIR.defaultBlockState()); // the code definitely can get here
			return;
		}
		
		cir.setReturnValue(section.getBlockState(x & 15, y & 15, z & 15));
	}
	
	@Inject(at = @At("HEAD"), method = "setWireState", cancellable = true)
	void setBlockState(BlockPos pos, BlockState state, boolean updateNeighborShapes, CallbackInfoReturnable<Boolean> cir) {
		if (!isSU) return;
		
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
		
		if (updateNeighborShapes) {
			prevState.updateIndirectNeighbourShapes(level, pos, Block.UPDATE_CLIENTS);
			state.updateNeighbourShapes(level, pos, Block.UPDATE_CLIENTS);
			state.updateIndirectNeighbourShapes(level, pos, Block.UPDATE_CLIENTS);
		}
		
		cir.setReturnValue(true);
	}
}
