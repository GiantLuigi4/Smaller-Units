package tfc.smallerunits;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import tfc.smallerunits.data.capability.ISUCapability;
import tfc.smallerunits.data.capability.SUCapabilityManager;
import tfc.smallerunits.utils.selection.UnitShape;

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
			chunk.setUnsaved(true);
		}
	}
	
	@Override
	public RenderShape getRenderShape(BlockState pState) {
		return RenderShape.INVISIBLE;
	}
	
	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
//		return Shapes.empty();
		if (pContext instanceof EntityCollisionContext) {
			Entity entity = ((EntityCollisionContext) pContext).getEntity();
			if (entity == null)
				return super.getShape(pState, pLevel, pPos, pContext);
			UnitShape shape = new UnitShape();
			shape.addBox(new AABB(0, 0, 0, 0.25, 0.25, 0.25));
			shape.addBox(new AABB(0, 0.25, 0, 0.25, 0.5, 0.25));
			shape.addBox(new AABB(0, 0.5, 0, 0.25, 0.75, 0.25));
			shape.addBox(new AABB(0, 0.75, 0, 0.25, 1, 0.25));
//			float yLook = entity.xRotO;
//			VoxelShape shape = Shapes.empty();
//			if (yLook > 0) shape = Shapes.or(shape, Shapes.box(0, -0.0001, 0, 1, 0, 1));
//			else shape = Shapes.or(shape, Shapes.box(0, 1, 0, 1, 1.0001, 1));
//			float xLook = entity.yRotO;
//			xLook %= 360;
//			xLook -= 180;
//			if (xLook < 0 && xLook > -180) shape = Shapes.or(shape, Shapes.box(-0.0001, 0, 0, 0, 1, 1));
//			else shape = Shapes.or(shape, Shapes.box(1, 0, 0, 1.0001, 1, 1));
//			if (xLook < 90 && xLook > -90) shape = Shapes.or(shape, Shapes.box(0, 0, -0.0001, 1, 1, 0));
//			else shape = Shapes.or(shape, Shapes.box(0, 0, 1, 1, 1, 1.0001));
			return shape;
		}
		return Shapes.empty();
	}
	
	@Override
	public VoxelShape getOcclusionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
		return Shapes.empty();
	}
	
	@Override
	public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
//		super.onPlace(pState, pLevel, pPos, pOldState, pIsMoving);
		ChunkAccess chunk = pLevel.getChunk(pPos);
		if (chunk instanceof LevelChunk) {
			ISUCapability capability = SUCapabilityManager.getCapability((LevelChunk) chunk);
			capability.makeUnit(pPos);
			chunk.setUnsaved(true);
		}
	}
	
	// I might wind up needing a tile entity for sake of setting up world capabilities
	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		return null;
	}
}
