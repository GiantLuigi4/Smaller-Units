package tfc.smallerunits.simulation.level.server;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class BlankPOIManager extends PoiManager {

	public BlankPOIManager(Path p_217869_, DataFixer p_217870_, boolean p_217871_, RegistryAccess p_217872_, LevelHeightAccessor p_217873_) {
		super(p_217869_, p_217870_, p_217871_, p_217872_, p_217873_);
	}

	@Override
	public void remove(BlockPos pPos) {
	}
	
	@Override
	public Optional<Holder<PoiType>> getType(BlockPos pPos) {
		return Optional.empty();
	}
	
	@Override
	public int getFreeTickets(BlockPos p_148654_) {
		return 0;
	}
	
	@Override
	public int sectionsToVillage(SectionPos pSectionPos) {
		return 0; // TODO: ?
	}
	
	@Override
	public void tick(BooleanSupplier pAheadOfTime) {
	}
	
	@Override
	protected void setDirty(long pSectionPos) {
	}
	
	@Override
	protected void onSectionLoad(long pSectionKey) {
	}
	
	@Override
	public void checkConsistencyWithBlocks(ChunkPos pPos, LevelChunkSection pSection) {
	}
	
	@Override
	public void ensureLoadedAndValid(LevelReader pLevelReader, BlockPos pPos, int pCoordinateOffset) {
	}
	
	@Override
	public void add(BlockPos $$0, Holder<PoiType> $$1) {
	}
	
	@Override
	public Optional<BlockPos> take(Predicate<Holder<PoiType>> $$0, BiPredicate<Holder<PoiType>, BlockPos> $$1, BlockPos $$2, int $$3) {
		return Optional.empty();
	}
	
	@Override
	public long getCountInRange(Predicate<Holder<PoiType>> $$0, BlockPos $$1, int $$2, Occupancy $$3) {
		return 0;
	}
	
	@Override
	public boolean existsAtPosition(ResourceKey<PoiType> $$0, BlockPos $$1) {
		return false;
	}
	
	@Override
	public Stream<PoiRecord> getInSquare(Predicate<Holder<PoiType>> $$0, BlockPos $$1, int $$2, Occupancy $$3) {
		return Stream.of();
	}
	
	@Override
	public Stream<PoiRecord> getInRange(Predicate<Holder<PoiType>> $$0, BlockPos $$1, int $$2, Occupancy $$3) {
		return Stream.of();
	}
	
	@Override
	public Stream<PoiRecord> getInChunk(Predicate<Holder<PoiType>> $$0, ChunkPos $$1, Occupancy $$2) {
		return Stream.of();
	}
	
	@Override
	public Stream<BlockPos> findAll(Predicate<Holder<PoiType>> $$0, Predicate<BlockPos> $$1, BlockPos $$2, int $$3, Occupancy $$4) {
		return Stream.of();
	}
	
	@Override
	public Stream<Pair<Holder<PoiType>, BlockPos>> findAllWithType(Predicate<Holder<PoiType>> $$0, Predicate<BlockPos> $$1, BlockPos $$2, int $$3, Occupancy $$4) {
		return Stream.of();
	}
	
	@Override
	public Stream<Pair<Holder<PoiType>, BlockPos>> findAllClosestFirstWithType(Predicate<Holder<PoiType>> $$0, Predicate<BlockPos> $$1, BlockPos $$2, int $$3, Occupancy $$4) {
		return Stream.of();
	}
	
	@Override
	public Optional<BlockPos> find(Predicate<Holder<PoiType>> $$0, Predicate<BlockPos> $$1, BlockPos $$2, int $$3, Occupancy $$4) {
		return Optional.empty();
	}
	
	@Override
	public Optional<BlockPos> findClosest(Predicate<Holder<PoiType>> $$0, BlockPos $$1, int $$2, Occupancy $$3) {
		return Optional.empty();
	}
	
	@Override
	public Optional<Pair<Holder<PoiType>, BlockPos>> findClosestWithType(Predicate<Holder<PoiType>> $$0, BlockPos $$1, int $$2, Occupancy $$3) {
		return Optional.empty();
	}
	
	@Override
	public Optional<BlockPos> findClosest(Predicate<Holder<PoiType>> $$0, Predicate<BlockPos> $$1, BlockPos $$2, int $$3, Occupancy $$4) {
		return Optional.empty();
	}
	
	@Override
	public Optional<BlockPos> getRandom(Predicate<Holder<PoiType>> $$0, Predicate<BlockPos> $$1, Occupancy $$2, BlockPos $$3, int $$4, RandomSource $$5) {
		return Optional.empty();
	}
	
	@Override
	public boolean release(BlockPos $$0) {
		return false;
	}
	
	@Override
	public boolean exists(BlockPos $$0, Predicate<Holder<PoiType>> $$1) {
		return false;
	}
}
