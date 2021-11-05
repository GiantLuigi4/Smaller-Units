package tfc.smallerunits.utils.shapes;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.World;
import tfc.collisionreversion.api.lookup.SelectionLookup;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.utils.SmallUnit;
import tfc.smallerunits.utils.UnitRaytraceHelper;

import java.util.List;

public class CollisionReversionShapeGetter {
	public static void register() {
		SelectionLookup.registerBoxFiller((context) -> {
			List<AxisAlignedBB> boxes = context.getBoxes();
			AxisAlignedBB box = context.boundingBox();
			
			World world = context.getWorld();
			BlockPos pos = context.getPos();
			UnitTileEntity tileEntity;
			{
				TileEntity te = world.getTileEntity(pos);
				if (!(te instanceof UnitTileEntity)) return;
				tileEntity = (UnitTileEntity) te;
			}
			World fakeWorld = tileEntity.getFakeWorld();
			int upb = tileEntity.unitsPerBlock;
			for (SmallUnit value : tileEntity.getBlockMap().values()) {
				if (value.state == null || value.state.isAir()) continue;
				
				AxisAlignedBB blockBB = new AxisAlignedBB(0, 0, 0, 1 / (double) upb, 1 / (double) upb, 1 / (double) upb);
				blockBB = blockBB
						.offset(context.getPos())
						.offset(value.pos.getX() / (double) upb, (value.pos.getY() - 64) / (double) upb, value.pos.getZ() / (double) upb);
				if (!context.raytrace(blockBB)) continue;
				
				for (AxisAlignedBB axisAlignedBB : UnitRaytraceHelper.shrink(value.state.getShape(fakeWorld, value.pos, ISelectionContext.dummy()), upb)) {
					axisAlignedBB = axisAlignedBB.offset(pos).offset(value.pos.getX() / (double) upb, (value.pos.getY() - 64) / (double) upb, value.pos.getZ() / (double) upb);
					if (axisAlignedBB.intersects(box)) {
						boxes.add(axisAlignedBB);
					}
				}
			}
		});
	}
}
