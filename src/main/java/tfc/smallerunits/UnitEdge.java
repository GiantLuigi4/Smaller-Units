package tfc.smallerunits;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.simulation.level.ITickerWorld;

public class UnitEdge extends Block {
	public UnitEdge() {
		super(Properties.copy(Blocks.BARRIER).destroyTime(0.1f));
	}
	
	@Override
	public RenderShape getRenderShape(BlockState pState) {
		return RenderShape.INVISIBLE;
	}
	
	@Override
	public VoxelShape getOcclusionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
		return Shapes.empty();
	}
	
	@Override
	public VoxelShape getInteractionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
		return getOcclusionShape(pState, pLevel, pPos);
	}
	
	@Override
	protected void spawnDestroyParticles(Level pLevel, Player pPlayer, BlockPos pPos, BlockState pState) {
		if (pLevel instanceof ITickerWorld tickerLevel) {
			Region region = tickerLevel.getRegion();
			int upb = tickerLevel.getUPB();
			BlockPos bp = region.pos.toBlockPos().offset(
					// TODO: double check this
					Math.floor(pPos.getX() / (double) upb),
					Math.floor(pPos.getY() / (double) upb),
					Math.floor(pPos.getZ() / (double) upb)
			);
			
			BlockState state = tickerLevel.getParent().getBlockState(bp);
			pLevel.levelEvent(pPlayer, 2001, pPos, getId(state));
		}
	}
}
