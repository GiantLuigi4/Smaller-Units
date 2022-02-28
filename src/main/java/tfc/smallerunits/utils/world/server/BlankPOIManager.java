package tfc.smallerunits.utils.world.server;

import com.mojang.datafixers.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.village.PointOfInterest;
import net.minecraft.village.PointOfInterestData;
import net.minecraft.village.PointOfInterestManager;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.chunk.ChunkSection;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Random;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class BlankPOIManager extends PointOfInterestManager {
	private final PointOfInterestData doNothingData = new PointOfInterestData(() -> {
	});
	
	public BlankPOIManager(File p_i231554_1_, DataFixer p_i231554_2_, boolean p_i231554_3_) {
		super(p_i231554_1_, p_i231554_2_, p_i231554_3_);
	}
	
	@Override
	public void add(BlockPos p_219135_1_, PointOfInterestType p_219135_2_) {
	}
	
	@Override
	public void remove(BlockPos p_219140_1_) {
	}
	
	@Override
	public long getCountInRange(Predicate<PointOfInterestType> p_219145_1_, BlockPos p_219145_2_, int p_219145_3_, Status p_219145_4_) {
		return 0;
	}
	
	@Override
	public boolean hasTypeAtPosition(PointOfInterestType p_234135_1_, BlockPos p_234135_2_) {
		return false;
	}
	
	@Override
	public Stream<PointOfInterest> getInSquare(Predicate<PointOfInterestType> p_226353_1_, BlockPos p_226353_2_, int p_226353_3_, Status p_226353_4_) {
		return Stream.empty();
	}
	
	@Override
	public Stream<PointOfInterest> func_219146_b(Predicate<PointOfInterestType> p_219146_1_, BlockPos p_219146_2_, int p_219146_3_, Status p_219146_4_) {
		return Stream.empty();
	}
	
	@Override
	public Stream<PointOfInterest> getInChunk(Predicate<PointOfInterestType> p_219137_1_, ChunkPos p_219137_2_, Status p_219137_3_) {
		return Stream.empty();
	}
	
	@Override
	public Stream<BlockPos> findAll(Predicate<PointOfInterestType> p_225399_1_, Predicate<BlockPos> p_225399_2_, BlockPos p_225399_3_, int p_225399_4_, Status p_225399_5_) {
		return Stream.empty();
	}
	
	@Override
	public Stream<BlockPos> func_242324_b(Predicate<PointOfInterestType> p_242324_1_, Predicate<BlockPos> p_242324_2_, BlockPos p_242324_3_, int p_242324_4_, Status p_242324_5_) {
		return Stream.empty();
	}
	
	@Override
	public Optional<BlockPos> find(Predicate<PointOfInterestType> p_219127_1_, Predicate<BlockPos> p_219127_2_, BlockPos p_219127_3_, int p_219127_4_, Status p_219127_5_) {
		return Optional.empty();
	}
	
	@Override
	public Optional<BlockPos> func_234148_d_(Predicate<PointOfInterestType> p_234148_1_, BlockPos p_234148_2_, int p_234148_3_, Status p_234148_4_) {
		return Optional.empty();
	}
	
	@Override
	public Optional<BlockPos> take(Predicate<PointOfInterestType> p_219157_1_, Predicate<BlockPos> p_219157_2_, BlockPos p_219157_3_, int p_219157_4_) {
		return Optional.empty();
	}
	
	@Override
	public Optional<BlockPos> getRandom(Predicate<PointOfInterestType> p_219163_1_, Predicate<BlockPos> p_219163_2_, Status p_219163_3_, BlockPos p_219163_4_, int p_219163_5_, Random p_219163_6_) {
		return Optional.empty();
	}
	
	@Override
	public boolean release(BlockPos p_219142_1_) {
		return false;
	}
	
	@Override
	public boolean exists(BlockPos p_219138_1_, Predicate<PointOfInterestType> p_219138_2_) {
		return false;
	}
	
	@Override
	public Optional<PointOfInterestType> getType(BlockPos p_219148_1_) {
		return Optional.empty();
	}
	
	@Override
	public int sectionsToVillage(SectionPos p_219150_1_) {
		return 0;
	}
	
	@Override
	public void tick(BooleanSupplier p_219115_1_) {
	}
	
	@Override
	protected void markDirty(long p_219116_1_) {
	}
	
	@Override
	protected void onSectionLoad(long p_219111_1_) {
	}
	
	@Override
	public void checkConsistencyWithBlocks(ChunkPos p_219139_1_, ChunkSection p_219139_2_) {
	}
	
	@Override
	public void ensureLoadedAndValid(IWorldReader p_226347_1_, BlockPos p_226347_2_, int p_226347_3_) {
	}
	
	@Nullable
	@Override
	protected Optional<PointOfInterestData> func_219106_c(long p_219106_1_) {
		return Optional.empty();
	}
	
	@Override
	protected Optional<PointOfInterestData> func_219113_d(long p_219113_1_) {
		return Optional.empty();
	}
	
	@Override
	protected boolean func_219114_b(SectionPos p_219114_1_) {
		return false;
	}
	
	@Override
	protected PointOfInterestData func_235995_e_(long p_235995_1_) {
		return doNothingData;
	}
	
	@Override
	public void saveIfDirty(ChunkPos p_219112_1_) {
	}
	
	@Override
	public void close() throws IOException {
		super.close();
	}
}
