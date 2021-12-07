package tfc.smallerunits.utils.data;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.utils.accessor.IRenderUnitsInBlocks;

import javax.annotation.Nullable;

public class CapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundNBT> {
	SUCapabilityImpl backend = new SUCapabilityImpl();
	LazyOptional<SUCapability> optionalStorage = LazyOptional.of(() -> backend);
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction direction) {
		if (cap == SUCapabilityManager.SUCapability) return optionalStorage.cast();
		return LazyOptional.empty();
	}
	
	public void attach(AttachCapabilitiesEvent<Chunk> event) {
		event.addCapability(new ResourceLocation("smallerunits:sucap"), this);
		event.addListener(() -> {
			for (UnitTileEntity value : optionalStorage.resolve().get().getMap().values()) {
				((IRenderUnitsInBlocks) Minecraft.getInstance().worldRenderer).SmallerUnits_addUnitInBlock(value);
			}
			optionalStorage.invalidate();
		});
	}
	
	@Override
	public CompoundNBT serializeNBT() {
		return (CompoundNBT) optionalStorage.resolve().get().serialize();
	}
	
	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		optionalStorage.resolve().get().deserialze(nbt);
	}
}
