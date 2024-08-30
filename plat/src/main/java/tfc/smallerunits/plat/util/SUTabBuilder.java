package tfc.smallerunits.plat.util;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SUTabBuilder {
	public SUTabBuilder setTitle(Component component) {
		throw new RuntimeException("h");
	}
	
	public SUTabBuilder addItem(Supplier<ItemStack> supplier) {
		throw new RuntimeException("h");
	}
	
	public CreativeModeTab build() {
		throw new RuntimeException("h");
	}
	
	public SUTabBuilder addItems(Consumer<Consumer<ItemStack>> populator) {
		throw new RuntimeException("h");
	}
}
