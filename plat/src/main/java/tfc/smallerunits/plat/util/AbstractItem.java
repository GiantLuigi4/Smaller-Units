package tfc.smallerunits.plat.util;

import net.minecraft.world.item.Item;

public class AbstractItem extends Item {
	public AbstractItem(Properties properties) {
		super(properties);
	}
	
	public void initializeClient(ClientInitContext ctx) {
	}
}
