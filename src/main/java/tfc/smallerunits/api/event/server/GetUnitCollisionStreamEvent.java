package tfc.smallerunits.api.event.server;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraftforge.eventbus.api.Event;
import tfc.smallerunits.api.SUEvent;
import tfc.smallerunits.block.UnitTileEntity;

import java.util.List;

public class GetUnitCollisionStreamEvent extends Event implements SUEvent {
	public final UnitTileEntity tile;
	public final Entity entity;
	public final List<VoxelShape> shapes;
	
	public GetUnitCollisionStreamEvent(UnitTileEntity tile, Entity entity, List<VoxelShape> shapes) {
		this.tile = tile;
		this.entity = entity;
		this.shapes = shapes;
	}
}
