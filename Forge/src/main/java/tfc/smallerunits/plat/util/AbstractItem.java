package tfc.smallerunits.plat.util;

import net.minecraft.world.item.Item;
import net.minecraftforge.client.IItemRenderProperties;

import java.util.function.Consumer;

public class AbstractItem extends Item {
	public AbstractItem(Properties properties) {
		super(properties);
	}
	
	public void initializeClient(ClientInitContext ctx) {
	}
	
	@Override
	public void initializeClient(Consumer<IItemRenderProperties> consumer) {
		ClientInitContext ctx = new ClientInitContext(consumer);
		initializeClient(ctx);
		ctx.finish();
	}
}
