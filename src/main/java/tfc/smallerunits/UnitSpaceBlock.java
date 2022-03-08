package tfc.smallerunits;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import org.jetbrains.annotations.Nullable;
import tfc.smallerunits.data.capability.ISUCapability;
import tfc.smallerunits.data.capability.SUCapabilityManager;

public class UnitSpaceBlock extends Block implements EntityBlock {
	public UnitSpaceBlock() {
		super(
				Properties.of(Material.STONE, MaterialColor.COLOR_BLACK)
						.isSuffocating((a, b, c) -> false)
						.isViewBlocking((a, b, c) -> false)
						.explosionResistance(0)
		);
	}
	
	@Override
	public float getSpeedFactor() {
		return super.getSpeedFactor();
	}
	
	@Override
	public void destroy(LevelAccessor pLevel, BlockPos pPos, BlockState pState) {
//		super.destroy(pLevel, pPos, pState);
		ChunkAccess chunk = pLevel.getChunk(pPos);
		if (chunk instanceof LevelChunk) {
			ISUCapability capability = SUCapabilityManager.getCapability((LevelChunk) chunk);
			capability.removeUnit(pPos);
		}
	}
	
	@Override
	public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
//		super.onPlace(pState, pLevel, pPos, pOldState, pIsMoving);
		ChunkAccess chunk = pLevel.getChunk(pPos);
		if (chunk instanceof LevelChunk) {
			ISUCapability capability = SUCapabilityManager.getCapability((LevelChunk) chunk);
			capability.makeUnit(pPos);
		}
	}
	
	// I might wind up needing a tile entity for sake of setting up world capabilities
	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		return null;
	}
}
