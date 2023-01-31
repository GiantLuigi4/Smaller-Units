package tfc.smallerunits;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.data.tracking.ICanUseUnits;
import tfc.smallerunits.simulation.level.ITickerLevel;

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
		if (pLevel instanceof ITickerLevel tickerLevel) {
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
	
	@Override
	public void attack(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
		if (pPlayer instanceof ICanUseUnits unitUser)
			unitUser.removeUnit();
		super.attack(pState, pLevel, pPos, pPlayer);
	}
	
	@Override
	public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
		if (player instanceof ICanUseUnits unitUser)
			unitUser.removeUnit();
		return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
	}
	
	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
		if (player instanceof ICanUseUnits unitUser) {
			if (level instanceof ITickerLevel tickerLevel) {
				if (unitUser.actualResult() instanceof BlockHitResult bhr) {
					// TODO: move player out temporarily
					BlockPos pPos = bhr.getBlockPos().relative(bhr.getDirection().getOpposite());
					return tickerLevel.getParent().getBlockState(pPos).getCloneItemStack(
							bhr, tickerLevel.getParent(), pPos,
							player
					);
				}
			}
		}
		return super.getCloneItemStack(state, target, level, pos, player);
	}
}
