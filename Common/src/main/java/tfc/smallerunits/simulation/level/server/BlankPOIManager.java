package tfc.smallerunits.simulation.level.server;

import com.mojang.datafixers.DataFixer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.BooleanSupplier;

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
}
