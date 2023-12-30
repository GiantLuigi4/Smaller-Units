package tfc.smallerunits.plat.util;

import net.minecraft.world.item.Item;

public class AbstractItem extends Item {
	public AbstractItem(Properties properties) {
		super(properties);
		if (PlatformUtils.isClient()) {
			initializeClient(new ClientInitContext(this));
		}
	}
	
	public void initializeClient(ClientInitContext ctx) {
	}
}
