package tfc.smallerunits.utils.platform.hooks;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.TrappedChestBlock;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import tfc.smallerunits.utils.selection.MutableAABB;

public interface ICullableBE {
	AABB getCullingAABB();
	
	static AABB getCullingBB(BlockEntity pBlockEntity) {
		if (pBlockEntity instanceof ICullableBE cullableBE)
			return cullableBE.getCullingAABB();
		
		if (pBlockEntity instanceof LidBlockEntity lidBlockEntity) {
			BlockState state = pBlockEntity.getBlockState();
			
			if (
				// check that it's a vanilla chest
					state.getBlock().getClass() == ChestBlock.class ||
							state.getBlock().getClass() == EnderChestBlock.class ||
							state.getBlock().getClass() == TrappedChestBlock.class
			) {
				// if so, return a very precise AABB
				Direction dir = state.getValue(ChestBlock.FACING);
				
				// vanilla bounding box for standalone chest
				AABB bb = new MutableAABB(
						1.0 / 16f, 0.0 / 16f, 1.0 / 16f, 15.0 / 16f, 14.0 / 16f, 15.0 / 16f
				).move(pBlockEntity.getBlockPos());
				
				// account for open lids
				if (lidBlockEntity.getOpenNess(1) != 0) {
					bb.expandTowards(
							-dir.getStepX() * (6 / 16f),
							(11 / 16f),
							-dir.getStepZ() * (6 / 16f)
					);
				}
				
				ChestType type = state.getValue(ChestBlock.TYPE);
				// fill in 1 px gap caused by using the standalone bounding box
				if (type == ChestType.RIGHT) {
					Direction side = dir.getCounterClockWise();
					bb.expandTowards(
							side.getStepX() * (1 / 16f),
							0,
							side.getStepZ() * (1 / 16f)
					);
				} else if (type == ChestType.LEFT) {
					Direction side = dir.getClockWise();
					bb.expandTowards(
							side.getStepX() * (1 / 16f),
							0,
							side.getStepZ() * (1 / 16f)
					);
				}
				
				return bb;
			}
		}
		
		if (pBlockEntity instanceof BeaconBlockEntity beacon) {
			if (!beacon.getBeamSections().isEmpty()) {
				return new AABB(
						pBlockEntity.getBlockPos().getX(),
						pBlockEntity.getBlockPos().getY(),
						pBlockEntity.getBlockPos().getZ(),
						pBlockEntity.getBlockPos().getX() + 1,
						pBlockEntity.getBlockPos().getY() + pBlockEntity.getLevel().getMaxBuildHeight() * 3,
						pBlockEntity.getBlockPos().getZ() + 1
				);
			}
			return new AABB(beacon.getBlockPos());
		} else if (pBlockEntity instanceof TheEndGatewayBlockEntity gateway) {
			if (gateway.isSpawning()) {
				return new AABB(
						pBlockEntity.getBlockPos().getX(),
						pBlockEntity.getBlockPos().getY(),
						pBlockEntity.getBlockPos().getZ(),
						pBlockEntity.getBlockPos().getX() + 1,
						pBlockEntity.getBlockPos().getY() + pBlockEntity.getLevel().getMaxBuildHeight() * 3,
						pBlockEntity.getBlockPos().getZ() + 1
				);
			}
			return new AABB(gateway.getBlockPos());
		} else if (pBlockEntity.getClass() == StructureBlockEntity.class) {
			// structure block should always render
			return new AABB(
					Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE,
					Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE
			);
		} else if (pBlockEntity instanceof PistonMovingBlockEntity) {
			return new AABB(pBlockEntity.getBlockPos());
		}
		
		VoxelShape shape = pBlockEntity.getBlockState().getShape(pBlockEntity.getLevel(), pBlockEntity.getBlockPos());
		if (shape == Shapes.empty() || shape.isEmpty()) return new AABB(pBlockEntity.getBlockPos()).inflate(2);
		AABB box = shape.bounds();
		
		// if the block doesn't have custom collision, it probably uses a default render box
		if (
				box.minX == 0 && box.minY == 0 && box.minZ == 0 &&
						box.maxX == 1 && box.maxY == 1 && box.maxZ == 1
		) return box.move(pBlockEntity.getBlockPos());
		// elsewise, it should be assumed that it might go a bit outside of its shape's bounds
		return box.move(pBlockEntity.getBlockPos()).inflate(1.05);
	}
}
