package tfc.smallerunits.api.event.common;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraftforge.eventbus.api.Event;
import tfc.smallerunits.api.SUEvent;
import tfc.smallerunits.block.UnitTileEntity;

public class GetUnitCollisionEvent extends Event implements SUEvent {
	public final UnitTileEntity tile;
	public final Entity entity;
	private VoxelShape shape;
	
	public GetUnitCollisionEvent(VoxelShape shape, UnitTileEntity tile, Entity entity) {
		this.shape = shape;
		this.tile = tile;
		this.entity = entity;
	}
	
	public VoxelShape getShape() {
		return shape;
	}
	
	public void setShape(VoxelShape shape) {
		if (shape == null)
			throw new IllegalArgumentException("A listener for GetUnitCollisionEvent tried to return null. This is not allowed, please report it to the devs of the mod with said event listener.");
		this.shape = shape;
	}
}
