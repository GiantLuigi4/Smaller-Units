package com.tfc.smallerunits.utils;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.util.concurrent.ThreadTaskExecutor;
import net.minecraft.village.PointOfInterestManager;
import net.minecraft.world.chunk.IChunkLightProvider;
import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ChunkManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.SaveFormat;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class FakeChunkManager extends ChunkManager {
	private static final Unsafe theUnsafe;
	
	static {
		try {
			Field f = Unsafe.class.getDeclaredField("theUnsafe");
			f.setAccessible(true);
			theUnsafe = (Unsafe) f.get(null);
		} catch (Throwable err) {
			throw new RuntimeException(err);
		}
	}
	
	public FakeServerChunkProvider provider;
	private boolean hasInit;
	
	public FakeChunkManager(ServerWorld p_i232602_1_, SaveFormat.LevelSave p_i232602_2_, DataFixer p_i232602_3_, TemplateManager p_i232602_4_, Executor p_i232602_5_, ThreadTaskExecutor<Runnable> p_i232602_6_, IChunkLightProvider p_i232602_7_, ChunkGenerator p_i232602_8_, IChunkStatusListener p_i232602_9_, Supplier<DimensionSavedDataManager> p_i232602_10_, int p_i232602_11_, boolean p_i232602_12_) {
		super(p_i232602_1_, p_i232602_2_, p_i232602_3_, p_i232602_4_, p_i232602_5_, p_i232602_6_, p_i232602_7_, p_i232602_8_, p_i232602_9_, p_i232602_10_, p_i232602_11_, p_i232602_12_);
	}
	
	public void init() {
		if (!hasInit) {
			this.entities = new Int2ObjectOpenHashMap<>();
			try {
				pointOfInterestManager = (PointOfInterestManager) theUnsafe.allocateInstance(PointOfInterestManager.class);
			} catch (Throwable err) {
				throw new RuntimeException(err);
			}
			hasInit = true;
		}
	}
	
	@Override
	protected Iterable<ChunkHolder> getLoadedChunksIterable() {
		return ImmutableSet.of(((FakeTicketManager) provider.ticketManager).getChunkHolder(0));
	}
}
