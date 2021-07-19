package com.tfc.smallerunits.api.event.client;

import com.tfc.smallerunits.api.SUEvent;
import com.tfc.smallerunits.block.UnitTileEntity;
import com.tfc.smallerunits.utils.world.client.FakeClientWorld;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.Event;

public class RenderUnitLastEvent extends Event implements SUEvent {
	public final IRenderTypeBuffer buffers;
	public final BlockPos unitPos;
	public final World realWorld;
	public final FakeClientWorld unitWorld;
	public final UnitTileEntity tile;
	
	public RenderUnitLastEvent(IRenderTypeBuffer buffers, UnitTileEntity tile) {
		this.buffers = buffers;
		this.unitPos = tile.getPos();
		this.realWorld = tile.getWorld();
		this.unitWorld = tile.worldClient.get();
		this.tile = tile;
	}
}
