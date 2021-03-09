package com.tfc.smallerunits.utils.world.common;

import com.mojang.datafixers.DataFixer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.function.Supplier;

public class FakeDimensionSavedData extends DimensionSavedDataManager {
	public CompoundNBT savedNBT = new CompoundNBT();
	HashMap<String, WorldSavedData> dataHashMap = new HashMap<>();
	private Supplier<CompoundNBT> nbt;
	
	public FakeDimensionSavedData(File dataFolder, DataFixer dataFixerIn, Supplier<CompoundNBT> nbt) {
		super(dataFolder, dataFixerIn);
		this.nbt = nbt;
	}
	
	@Override
	public <T extends WorldSavedData> T getOrCreate(Supplier<T> defaultSupplier, String name) {
		if (!dataHashMap.containsKey(name)) {
			dataHashMap.put(name, defaultSupplier.get());
			if (nbt.get().contains(name)) {
				dataHashMap.get(name).deserializeNBT(nbt.get().getCompound(name));
			}
		}
		return (T) dataHashMap.get(name);
	}
	
	@Nullable
	@Override
	public <T extends WorldSavedData> T get(Supplier<T> defaultSupplier, String name) {
		if (!dataHashMap.containsKey(name)) {
			dataHashMap.put(name, defaultSupplier.get());
			if (nbt.get().contains(name)) {
				dataHashMap.get(name).deserializeNBT(nbt.get().getCompound(name));
			}
		}
		return (T) dataHashMap.get(name);
	}
	
	@Override
	public void set(WorldSavedData data) {
		if (dataHashMap.containsKey(data.getName())) {
			dataHashMap.replace(data.getName(), data);
		} else {
			dataHashMap.put(data.getName(), data);
		}
	}
	
	@Override
	public CompoundNBT load(String name, int worldVersion) throws IOException {
		return nbt.get().getCompound(name);
	}
	
	@Override
	public void save() {
		for (WorldSavedData value : dataHashMap.values()) {
			savedNBT.put(value.getName(), value.serializeNBT());
		}
	}
}
