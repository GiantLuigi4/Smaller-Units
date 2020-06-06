package tfc.smallerunits;

import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Properties extends Item.Properties {
	@OnlyIn(Dist.CLIENT)
	public Properties() {
		try {
			addIster();
		} catch (Exception ignored) {}
	}
	
	@OnlyIn(Dist.CLIENT)
	public void addIster() {
		this.setISTER(()->SmallerUnitsITSER::new);
	}
}
