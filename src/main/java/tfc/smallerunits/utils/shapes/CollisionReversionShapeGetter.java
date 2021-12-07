package tfc.smallerunits.utils.shapes;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import tfc.collisionreversion.api.ILegacyContext;
import tfc.collisionreversion.api.lookup.CollisionLookup;
import tfc.collisionreversion.api.lookup.SelectionLookup;
import tfc.collisionreversion.api.lookup.VisualShapeLookup;
import tfc.collisionreversion.utils.CommonUtils;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.config.SmallerUnitsConfig;
import tfc.smallerunits.utils.SmallUnit;
import tfc.smallerunits.utils.UnitRaytraceHelper;
import tfc.smallerunits.utils.async.AsyncDispatcher;
import tfc.smallerunits.utils.data.SUCapabilityManager;

import java.util.List;

public class CollisionReversionShapeGetter {
	private static final ThreadLocal<AsyncDispatcher> dispatcher = ThreadLocal.withInitial(() -> new AsyncDispatcher("Collision Filler Group").resize(4));
	
	public static void register() {
		SelectionLookup.registerBoxFiller(CollisionReversionShapeGetter::fillSelection);
		VisualShapeLookup.registerBoxFiller(CollisionReversionShapeGetter::fillVisual);
		CollisionLookup.registerBoxFiller(CollisionReversionShapeGetter::fillCollision);
	}
	
	public static void fillSelection(ILegacyContext context) {
		fill(context, false);
	}
	
	public static void fillVisual(ILegacyContext context) {
		fill(context, true);
	}
	
	public static void fill(ILegacyContext context, boolean isVisual) {
		List<AxisAlignedBB> boxes = context.getBoxes();
		AxisAlignedBB box = context.boundingBox();
		
		World world = context.getWorld();
		BlockPos pos = context.getPos();
		UnitTileEntity tileEntity;
		{
			tileEntity = SUCapabilityManager.getUnitAtBlock(world, pos);
			if (tileEntity == null) return;
		}
		World fakeWorld = tileEntity.getFakeWorld();
		int upb = tileEntity.unitsPerBlock;
		for (SmallUnit value : tileEntity.getBlockMap().values()) {
			// ... why does this null check exist? lol
			if (value.state == null || value.state.isAir()) continue;
			
			AxisAlignedBB blockBB =
					new AxisAlignedBB(
							0, 0, 0,
							1 / (double) upb, 1 / (double) upb, 1 / (double) upb
					);
			blockBB = blockBB
					.offset(context.getPos())
					.offset(
							value.pos.getX() / (double) upb,
							(value.pos.getY() - 64) / (double) upb,
							value.pos.getZ() / (double) upb
					);
			if (!context.raytrace(blockBB)) continue;
			
			VoxelShape shape;
			
			if (isVisual) shape = value.state.getRaytraceShape(fakeWorld, value.pos, ISelectionContext.dummy());
			else shape = value.state.getShape(fakeWorld, value.pos, ISelectionContext.dummy());
			
			for (AxisAlignedBB axisAlignedBB : UnitRaytraceHelper.shrink(shape, upb)) {
				axisAlignedBB = axisAlignedBB.offset(pos).offset(value.pos.getX() / (double) upb, (value.pos.getY() - 64) / (double) upb, value.pos.getZ() / (double) upb);
				if (axisAlignedBB.intersects(box)) {
					boxes.add(axisAlignedBB);
				}
			}
		}
	}
	
	public static void fillCollision(ILegacyContext context) {
		final BlockPos pos = context.getPos();
		final UnitTileEntity tileEntity = SUCapabilityManager.getUnitAtBlock(context.getWorld(), pos);
		if (tileEntity == null) return;
		final int upb = tileEntity.unitsPerBlock;
		
		final Vector3d motionVec = context.getEntity().getMotion();
		final AxisAlignedBB motionBB1 = context.getEntity().getBoundingBox().expand(
				motionVec.getX(),
				motionVec.getY(),
				motionVec.getZ()
		);
		final AxisAlignedBB motionBB = new AxisAlignedBB(
				(Math.floor(motionBB1.minX * upb) - 1) / upb,
				(Math.floor(motionBB1.minY * upb) - 1) / upb, // TODO: expand by stepAssist
				(Math.floor(motionBB1.minZ * upb) - 1) / upb,
				(Math.ceil(motionBB1.maxX * upb) + 1) / upb,
				(Math.ceil(motionBB1.maxY * upb) + 1) / upb, // TODO: expand by stepAssist
				(Math.ceil(motionBB1.maxZ * upb) + 1) / upb
		);
		
		final List<AxisAlignedBB> boxesOut = context.getBoxes();
		if (SmallerUnitsConfig.COMMON.slightlyAsyncCollision.get() && tileEntity.getBlockMap().size() > SmallerUnitsConfig.COMMON.asyncThreshold.get()) {
			try {
				List<AxisAlignedBB>[] outputs = new List[tileEntity.unitsPerBlock];
				
				if (dispatcher.get().size() != SmallerUnitsConfig.COMMON.maxThreads.get())
					dispatcher.get().resize(SmallerUnitsConfig.COMMON.maxThreads.get());
				
				dispatcher.get().setIterations(tileEntity.unitsPerBlock).updateFunction(
						(id, trueIndex, dispatcher) -> {
							List<AxisAlignedBB> boxes = CommonUtils.makeList();
							int x = trueIndex;
							// TODO: vertical box merger
							for (SmallUnit value : tileEntity.getBlockMap().values()) {
								if (value.pos.getX() == x) {
									if (!value.state.isAir()) {
										VoxelShape shape = value.state.getCollisionShape(
												tileEntity.getFakeWorld(),
												value.pos
										);
										for (AxisAlignedBB axisAlignedBB : UnitRaytraceHelper.shrink(shape, tileEntity.unitsPerBlock)) {
											axisAlignedBB = axisAlignedBB.offset(pos.getX(), pos.getY(), pos.getZ()).offset(value.pos.getX() / (double) upb, (value.pos.getY() - 64) / (double) upb, value.pos.getZ() / (double) upb);
											if (context.checkBoundingBox(axisAlignedBB))
												if (motionBB.intersects(axisAlignedBB))
													boxesOut.add(axisAlignedBB);
										}
									}
								}
							}
//							try {
//								if (id != 0) dispatcher.await(id - 1);
//								else dispatcher.await(dispatcher.maxThreads() - 1);
////								boxesOut.addAll(boxes);
//							} catch (InterruptedException ex) {
//							}
							outputs[trueIndex] = boxes;
						}
				).dispatch().await();
				for (List<AxisAlignedBB> output : outputs) {
					boxesOut.addAll(output);
				}
			} catch (InterruptedException ex) {
			}
		} else {
			for (SmallUnit value : tileEntity.getBlockMap().values()) {
				if (!value.state.isAir()) {
					VoxelShape shape = value.state.getCollisionShape(
							tileEntity.getFakeWorld(),
							value.pos
					);
					for (AxisAlignedBB axisAlignedBB : UnitRaytraceHelper.shrink(shape, tileEntity.unitsPerBlock)) {
						axisAlignedBB = axisAlignedBB.offset(pos.getX(), pos.getY(), pos.getZ()).offset(value.pos.getX() / (double) upb, (value.pos.getY() - 64) / (double) upb, value.pos.getZ() / (double) upb);
						if (context.checkBoundingBox(axisAlignedBB))
							if (motionBB.intersects(axisAlignedBB))
								boxesOut.add(axisAlignedBB);
					}
				}
			}
		}
	}
}
