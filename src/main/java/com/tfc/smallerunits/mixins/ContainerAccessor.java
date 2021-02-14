package com.tfc.smallerunits.mixins;

import net.minecraft.inventory.container.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({Container.class, PlayerContainer.class, ChestContainer.class, FurnaceContainer.class, AbstractFurnaceContainer.class, EnchantmentContainer.class, HopperContainer.class, LoomContainer.class, WorkbenchContainer.class, SmithingTableContainer.class, RepairContainer.class, DispenserContainer.class})
public interface ContainerAccessor {
	@Invoker
	void setCanCloseNaturally(boolean canCloseNaturally);
	
	@Accessor
	boolean canCloseNaturally();
}
