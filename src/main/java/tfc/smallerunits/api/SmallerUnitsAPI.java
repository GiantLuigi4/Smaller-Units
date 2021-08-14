package tfc.smallerunits.api;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.api.IEventBus;
import tfc.smallerunits.api.event.client.RenderUnitLastEvent;
import tfc.smallerunits.api.event.common.GetUnitCollisionEvent;
import tfc.smallerunits.api.event.server.GetUnitCollisionStreamEvent;
import tfc.smallerunits.api.placement.UnitPos;
import tfc.smallerunits.block.UnitTileEntity;

import java.util.List;

public class SmallerUnitsAPI {
	public static final IEventBus EVENT_BUS = BusBuilder.builder().markerType(SUEvent.class).build();
	
	public static UnitPos createPos(int x, int y, int z, UnitTileEntity tileEntity) {
		return new UnitPos(x, y, z, tileEntity.getPos(), tileEntity.unitsPerBlock);
	}
	
	public static UnitPos createPos(BlockPos pos, UnitTileEntity tileEntity) {
		return new UnitPos(pos.getX(), pos.getY(), pos.getZ(), tileEntity.getPos(), tileEntity.unitsPerBlock);
	}
	
	public static void postRenderUnitLast(IRenderTypeBuffer buffers, UnitTileEntity tileEntity) {
		EVENT_BUS.post(new RenderUnitLastEvent(buffers, tileEntity));
	}
	
	public static VoxelShape postCollisionEvent(VoxelShape shape, UnitTileEntity tileEntity, Entity entity) {
		GetUnitCollisionEvent event = new GetUnitCollisionEvent(shape, tileEntity, entity);
		EVENT_BUS.post(event);
		return event.getShape();
	}
	
	public static List<VoxelShape> postCollisionEvent(List<VoxelShape> shapes, Entity entity, UnitTileEntity tileEntity) {
		GetUnitCollisionStreamEvent event = new GetUnitCollisionStreamEvent(tileEntity, entity, shapes);
		EVENT_BUS.post(event);
		return event.shapes;
	}
}
