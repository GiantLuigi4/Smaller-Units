package tfc.smallerunits.simulation.level.server;

import com.mojang.datafixers.DataFixer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Random;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class BlankPOIManager extends PoiManager {
	public BlankPOIManager(Path p_196651_, DataFixer p_196652_, boolean p_196653_, LevelHeightAccessor p_196654_) {
		super(p_196651_, p_196652_, p_196653_, p_196654_);
	}
	
	@Override
	public void add(BlockPos pPos, PoiType pPoiType) {
	}
	
	@Override
	public void remove(BlockPos pPos) {
	}
	
	@Override
	public long getCountInRange(Predicate<PoiType> p_27122_, BlockPos pPos, int pDistance, Occupancy pStatus) {
		return 0;
	}
	
	@Override
	public boolean existsAtPosition(PoiType pType, BlockPos pPos) {
		return false;
	}
	
	@Override
	public Stream<PoiRecord> getInSquare(Predicate<PoiType> pTypePredicate, BlockPos pPos, int pDistance, Occupancy pStatus) {
		return Stream.<PoiRecord>builder().build();
	}
	
	@Override
	public Stream<PoiRecord> getInRange(Predicate<PoiType> p_27182_, BlockPos p_27183_, int p_27184_, Occupancy p_27185_) {
		return Stream.<PoiRecord>builder().build();
	}
	
	@Override
	public Stream<PoiRecord> getInChunk(Predicate<PoiType> p_27118_, ChunkPos pPosChunk, Occupancy pStatus) {
		return Stream.<PoiRecord>builder().build();
	}
	
	@Override
	public Stream<BlockPos> findAll(Predicate<PoiType> pTypePredicate, Predicate<BlockPos> pPosPredicate, BlockPos pPos, int pDistance, Occupancy pStatus) {
		return Stream.<BlockPos>builder().build();
	}
	
	@Override
	public Stream<BlockPos> findAllClosestFirst(Predicate<PoiType> p_27172_, Predicate<BlockPos> p_27173_, BlockPos p_27174_, int p_27175_, Occupancy p_27176_) {
		return Stream.<BlockPos>builder().build();
	}
	
	@Override
	public Optional<BlockPos> find(Predicate<PoiType> pTypePredicate, Predicate<BlockPos> pPosPredicate, BlockPos pPos, int pDistance, Occupancy pStatus) {
		return Optional.empty();
	}
	
	@Override
	public Optional<BlockPos> findClosest(Predicate<PoiType> p_27193_, BlockPos p_27194_, int p_27195_, Occupancy p_27196_) {
		return Optional.empty();
	}
	
	@Override
	public Optional<BlockPos> findClosest(Predicate<PoiType> p_148659_, Predicate<BlockPos> p_148660_, BlockPos p_148661_, int p_148662_, Occupancy p_148663_) {
		return Optional.empty();
	}
	
	@Override
	public Optional<BlockPos> take(Predicate<PoiType> pTypePredicate, Predicate<BlockPos> pPosPredicate, BlockPos pPos, int pDistance) {
		return Optional.empty();
	}
	
	@Override
	public Optional<BlockPos> getRandom(Predicate<PoiType> pTypePredicate, Predicate<BlockPos> pPosPredicate, Occupancy pStatus, BlockPos pPos, int pDistance, Random pRand) {
		return Optional.empty();
	}
	
	@Override
	public boolean release(BlockPos pPos) {
		return false;
	}
	
	@Override
	public boolean exists(BlockPos pPos, Predicate<PoiType> p_27093_) {
		return false;
	}
	
	@Override
	public Optional<PoiType> getType(BlockPos pPos) {
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
}