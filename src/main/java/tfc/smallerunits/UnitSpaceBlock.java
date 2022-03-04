package tfc.smallerunits;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

public class UnitSpaceBlock extends Block {
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
}
