package tfc.smallerunits.simulation.world;

import com.mojang.datafixers.DataFixer;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class UnitChunkMap extends ChunkMap {
	public UnitChunkMap(ServerLevel p_143040_, LevelStorageSource.LevelStorageAccess p_143041_, DataFixer p_143042_, StructureManager p_143043_, Executor p_143044_, BlockableEventLoop<Runnable> p_143045_, LightChunkGetter p_143046_, ChunkGenerator p_143047_, ChunkProgressListener p_143048_, ChunkStatusUpdateListener p_143049_, Supplier<DimensionDataStorage> p_143050_, int p_143051_, boolean p_143052_) {
		super(p_143040_, p_143041_, p_143042_, p_143043_, p_143044_, p_143045_, p_143046_, p_143047_, p_143048_, p_143049_, p_143050_, p_143051_, p_143052_);
		this.distanceManager = new ButcheredDistMap(p_143044_, p_143045_);
	}
	
	public class ButcheredDistMap extends ChunkMap.DistanceManager {
		public ButcheredDistMap(Executor p_140459_, Executor p_140460_) {
			super(p_140459_, p_140460_);
		}
		
		@Override
		public boolean inBlockTickingRange(long p_183917_) {
			// TODO: check if it's in the parent's ticking range
			return true;
		}
	}
}
