package tfc.smallerunits.mixins;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(World.class)
public interface WorldAccessor {
	@Accessor
	@Mutable
	void setTileEntitiesToBeRemoved(Set<TileEntity> tileEntitiesToBeRemoved);
}
