package tfc.smallerunits;

import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class Registry {
	public static final DeferredRegister<Block> BLOCK_REGISTER = DeferredRegister.create(Block.class, "smaller_units");
	
	public static final RegistryObject<Block> UNIT_SPACE = BLOCK_REGISTER.register("unit_space", UnitSpaceBlock::new);
}
