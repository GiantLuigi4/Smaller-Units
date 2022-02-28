package tfc.smallerunits.utils.world.common;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.palette.UpgradeData;
import net.minecraft.world.ITickList;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraftforge.registries.ForgeRegistries;
import tfc.smallerunits.api.SmallerUnitsAPI;
import tfc.smallerunits.api.placement.UnitPos;
import tfc.smallerunits.utils.SmallUnit;
import tfc.smallerunits.utils.world.server.FakeServerWorld;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

public class FakeIChunk implements IChunk {
	public FakeServerWorld owner;
	private final HashMap<Heightmap.Type, Heightmap> heightmapHashMap = new HashMap<>();
	
	public FakeIChunk(FakeServerWorld owner) {
		this.owner = owner;
	}
	
	@Nullable
	@Override
	public BlockState setBlockState(BlockPos pos, BlockState state, boolean isMoving) {
		if (!(pos instanceof UnitPos)) pos = SmallerUnitsAPI.createPos(pos, owner.owner);
		BlockState oldState;
		if (owner.blockMap.containsKey(pos.toLong())) oldState = owner.blockMap.get(pos.toLong()).state;
		else oldState = Blocks.AIR.getDefaultState();
		if (owner.blockMap.containsKey(pos.toLong())) owner.blockMap.get(pos.toLong()).state = state;
		else owner.blockMap.put(pos.toLong(), new SmallUnit(SmallerUnitsAPI.createPos(pos, owner.owner), state));
		oldState.onReplaced(owner, pos, state, isMoving);
		owner.blockMap.get(pos.toLong()).state.onBlockAdded(owner, pos, oldState, isMoving);
		owner.toUpdate.add(pos);
		
		return oldState;
	}
	
	@Override
	public void addTileEntity(BlockPos pos, TileEntity tileEntityIn) {
		SmallUnit unit = owner.blockMap.get(pos.toLong());
		owner.tileEntityPoses.add(pos);
		unit.tileEntity = tileEntityIn;
	}
	
	@Override
	public void addEntity(Entity entityIn) {
	}
	
	@Nullable
	@Override
	public IWorld getWorldForge() {
		return owner;
	}
	
	@Override
	public void addTileEntity(CompoundNBT nbt) {
		TileEntityType<?> type = ForgeRegistries.TILE_ENTITIES.getValue(new ResourceLocation(nbt.getString("id")));
		if (type == null) return;
		TileEntity te = type.create();
		if (te == null) return;
		te.read(owner.getBlockState(new BlockPos(nbt.getInt("x"), nbt.getInt("y"), nbt.getInt("z"))), nbt);
		owner.addTileEntity(te);
	}
	
	@Override
	public Set<BlockPos> getTileEntitiesPos() {
		return ImmutableSet.copyOf(owner.tileEntityPoses);
	}
	
	@Override
	public ChunkSection[] getSections() {
		ArrayList<ChunkSection> sections = new ArrayList<>();
		for (int i = 64; i < 64 + 16; i++) {
			sections.add(
					new ChunkSection(
							i,
							(short) (owner.owner.unitsPerBlock * owner.owner.unitsPerBlock),
							(short) (owner.owner.unitsPerBlock * owner.owner.unitsPerBlock),
							(short) (owner.owner.unitsPerBlock * owner.owner.unitsPerBlock)
					)
			);
		}
		return sections.toArray(new ChunkSection[0]);
	}
	
	@Override
	public Collection<Map.Entry<Heightmap.Type, Heightmap>> getHeightmaps() {
		return null;
	}
	
	@Override
	public void setHeightmap(Heightmap.Type type, long[] data) {
		if (heightmapHashMap.containsKey(type)) {
			Heightmap map = heightmapHashMap.get(type);
			map.setDataArray(data);
		} else {
			Heightmap map = new Heightmap(this, type);
			map.setDataArray(data);
			heightmapHashMap.put(type, map);
		}
	}
	
	@Override
	public Heightmap getHeightmap(Heightmap.Type typeIn) {
		if (heightmapHashMap.containsKey(typeIn)) {
			return heightmapHashMap.get(typeIn);
		} else {
			//TODO
			return null;
		}
	}
	
	@Override
	public int getTopBlockY(Heightmap.Type heightmapType, int x, int z) {
		return getHeightmap(heightmapType).getHeight(x, z);
	}
	
	@Override
	public ChunkPos getPos() {
		return new ChunkPos(new BlockPos(0, 0, 0));
	}
	
	@Override
	public void setLastSaveTime(long saveTime) {
	}
	
	@Override
	public Map<Structure<?>, StructureStart<?>> getStructureStarts() {
		return ImmutableMap.of();
	}
	
	@Override
	public void setStructureStarts(Map<Structure<?>, StructureStart<?>> structureStartsIn) {
	}
	
	@Nullable
	@Override
	public BiomeContainer getBiomes() {
		return null;
	}
	
	@Override
	public boolean isModified() {
		return true;
	}
	
	@Override
	public void setModified(boolean modified) {
	}
	
	@Override
	public ChunkStatus getStatus() {
		return ChunkStatus.FULL;
	}
	
	@Override
	public void removeTileEntity(BlockPos pos) {
		owner.tileEntityPoses.remove(pos);
		owner.blockMap.get(pos).tileEntity = null;
	}
	
	@Override
	public ShortList[] getPackedPositions() {
		return new ShortList[0];
	}
	
	@Nullable
	@Override
	public CompoundNBT getDeferredTileEntity(BlockPos pos) {
		return owner.blockMap.get(pos).tileEntity.serializeNBT();
	}
	
	@Nullable
	@Override
	public CompoundNBT getTileEntityNBT(BlockPos pos) {
		return owner.blockMap.get(pos).tileEntity.serializeNBT();
	}
	
	//TODO
	@Override
	public Stream<BlockPos> getLightSources() {
		ArrayList<BlockPos> posArrayList = new ArrayList<>();
		for (SmallUnit value : owner.blockMap.values()) {
			if (value.state.getLightValue() > 1) {
				posArrayList.add(value.pos);
			}
		}
		return Stream.of(posArrayList.toArray(new BlockPos[0]));
	}
	
	@Override
	public ITickList<Block> getBlocksToBeTicked() {
		return owner.getPendingBlockTicks();
	}
	
	@Override
	public ITickList<Fluid> getFluidsToBeTicked() {
		return owner.getPendingFluidTicks();
	}
	
	@Override
	public UpgradeData getUpgradeData() {
		return null;
	}
	
	@Override
	public long getInhabitedTime() {
		return 0;
	}
	
	@Override
	public void setInhabitedTime(long newInhabitedTime) {
	}
	
	@Override
	public boolean hasLight() {
		return true;
	}
	
	@Override
	public int getLightValue(BlockPos pos) {
		return owner.getLightValue(pos);
	}
	
	@Override
	public void setLight(boolean lightCorrectIn) {
	}
	
	@Nullable
	@Override
	public TileEntity getTileEntity(BlockPos pos) {
		return owner.getTileEntity(pos);
	}
	
	@Override
	public BlockState getBlockState(BlockPos pos) {
		return owner.getBlockState(pos);
	}
	
	@Override
	public FluidState getFluidState(BlockPos pos) {
		return owner.getFluidState(pos);
	}
	
	@Nullable
	@Override
	public StructureStart<?> func_230342_a_(Structure<?> p_230342_1_) {
		return null;
	}
	
	@Override
	public void func_230344_a_(Structure<?> p_230344_1_, StructureStart<?> p_230344_2_) {
	}
	
	@Override
	public LongSet func_230346_b_(Structure<?> p_230346_1_) {
		return null;
	}
	
	@Override
	public void func_230343_a_(Structure<?> p_230343_1_, long p_230343_2_) {
	}
	
	@Override
	public Map<Structure<?>, LongSet> getStructureReferences() {
		return null;
	}
	
	@Override
	public void setStructureReferences(Map<Structure<?>, LongSet> structureReferences) {
	}
	
	public void close() {
		owner = null;
		heightmapHashMap.clear();
	}
}