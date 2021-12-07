package tfc.smallerunits.mixins.rendering.unit_in_block;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.utils.accessor.IRenderUnitsInBlocks;

import java.util.ArrayList;
import java.util.Set;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin implements IRenderUnitsInBlocks {
	@Unique
	ArrayList<UnitTileEntity> blocks = new ArrayList<>();
	@Shadow
	@Final
	private Set<TileEntity> setTileEntities;
	
	@Override
	public void SmallerUnits_addUnitInBlock(UnitTileEntity unit) {
		if (!blocks.contains(unit)) {
			blocks.add(unit);
			setTileEntities.add(unit);
		}
	}
	
	@Override
	public void SmallerUnits_removeUnitInBlock(UnitTileEntity unit) {
		blocks.remove(unit);
		setTileEntities.remove(unit);
	}
}
