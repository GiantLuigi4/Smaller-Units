package tfc.smallerunits.utils.world.common;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.palette.UpgradeData;
import net.minecraft.world.ITickList;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraftforge.registries.ForgeRegistries;
import tfc.smallerunits.api.SmallerUnitsAPI;
import tfc.smallerunits.api.placement.UnitPos;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.utils.SmallUnit;
import tfc.smallerunits.utils.world.client.FakeClientWorld;
import tfc.smallerunits.utils.world.server.FakeServerWorld;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class FakeChunk extends Chunk {
	private World world;
	
	public FakeChunk(World worldIn, ChunkPos chunkPosIn, BiomeContainer biomeContainerIn, World world) {
		super(worldIn, chunkPosIn, biomeContainerIn);
		this.world = world;
	}
	
	public FakeChunk(World worldIn, ChunkPos chunkPosIn, BiomeContainer biomeContainerIn, UpgradeData upgradeDataIn, ITickList<Block> tickBlocksIn, ITickList<Fluid> tickFluidsIn, long inhabitedTimeIn, @Nullable ChunkSection[] sectionsIn, @Nullable Consumer<Chunk> postLoadConsumerIn, World world) {
		super(worldIn, chunkPosIn, biomeContainerIn, upgradeDataIn, tickBlocksIn, tickFluidsIn, inhabitedTimeIn, sectionsIn, postLoadConsumerIn);
		this.world = world;
	}
	
	public FakeChunk(World worldIn, ChunkPrimer primer, World world) {
		super(worldIn, primer);
		this.world = world;
	}
	
	@Override
	public void getEntitiesWithinAABBForEntity(@Nullable Entity entityIn, AxisAlignedBB aabb, List<Entity> listToFill, @Nullable Predicate<? super Entity> filter) {
		Collection<Entity> entities = getEntities();
		for (Entity entity : entities) {
			if (entity.getBoundingBox().intersects(aabb) && (filter == null || filter.test(entity))) {
				listToFill.add(entity);
			}
		}
	}
	
	@Override
	public <T extends Entity> void getEntitiesWithinAABBForList(@Nullable EntityType<?> entitytypeIn, AxisAlignedBB aabb, List<? super T> list, Predicate<? super T> filter) {
		Collection<Entity> entities = getEntities();
		for (Entity entity : entities) {
			if (entity.getBoundingBox().intersects(aabb) && (filter.test((T) entity))) {
				list.add((T) entity);
			}
		}
	}
	
	@Override
	public <T extends Entity> void getEntitiesOfTypeWithinAABB(Class<? extends T> entityClass, AxisAlignedBB aabb, List<T> listToFill, @Nullable Predicate<? super T> filter) {
		Collection<Entity> entities = getEntities();
		for (Entity entity : entities) {
			if (entity == null) continue;
			if (entity.getBoundingBox().intersects(aabb) && ((filter == null || filter.test((T) entity))) && entityClass.isInstance(entity)) {
				listToFill.add((T) entity);
			}
		}
	}
	
	public Collection<Entity> getEntities() {
		if (world instanceof FakeServerWorld) return ((FakeServerWorld) world).entitiesById.values();
		else return ((FakeClientWorld) world).entitiesById.values();
	}
	
	@Override
	public Set<BlockPos> getTileEntitiesPos() {
		ObjectArraySet<BlockPos> tileEntityPoses = new ObjectArraySet<>();
		for (TileEntity tileEntity : world.loadedTileEntityList) tileEntityPoses.add(tileEntity.getPos());
		return tileEntityPoses;
	}
	
	@Nullable
	@Override
	public TileEntity getTileEntity(BlockPos pos) {
		return world.getTileEntity(pos);
	}
	
	@Override
	public BlockState getBlockState(BlockPos pos) {
		return world.getBlockState(pos);
	}
	
	@Override
	public FluidState getFluidState(BlockPos pos) {
		return world.getFluidState(pos);
	}
	
	@Override
	public FluidState getFluidState(int bx, int by, int bz) {
		return world.getFluidState(new BlockPos(bx, by, bz));
	}
	
	@Override
	public void addTileEntity(TileEntity tileEntityIn) {
		world.addTileEntity(tileEntityIn);
	}
	
	@Override
	public void addTileEntity(BlockPos pos, TileEntity tileEntityIn) {
		world.setTileEntity(pos, tileEntityIn);
	}
	
	@Override
	public void addTileEntity(CompoundNBT nbt) {
		TileEntityType<?> type = ForgeRegistries.TILE_ENTITIES.getValue(new ResourceLocation(nbt.getString("id")));
		if (type == null) return;
		TileEntity te = type.create();
		if (te == null) return;
		te.read(world.getBlockState(new BlockPos(nbt.getInt("x"), nbt.getInt("y"), nbt.getInt("z"))), nbt);
		world.addTileEntity(te);
	}
	
	@Nullable
	@Override
	public BlockState setBlockState(BlockPos pos, BlockState state, boolean isMoving) {
		if (!(pos instanceof UnitPos)) pos = SmallerUnitsAPI.createPos(pos, getOwner());
		Map<Long, SmallUnit> blockMap;
		if (world instanceof FakeServerWorld) blockMap = ((FakeServerWorld) world).blockMap;
		else blockMap = ((FakeClientWorld) world).blockMap;
		BlockState oldState;
		if (blockMap.containsKey(pos.toLong())) oldState = blockMap.get(pos.toLong()).state;
		else oldState = Blocks.AIR.getDefaultState();
		oldState.onReplaced(world, pos, state, isMoving);
		if (blockMap.containsKey(pos.toLong())) blockMap.get(pos.toLong()).state = state;
		else blockMap.put(pos.toLong(), new SmallUnit(SmallerUnitsAPI.createPos(pos, getOwner()), state));
		state.onBlockAdded(world, pos, oldState, isMoving);
		return state;
	}
	
	private UnitTileEntity getOwner() {
		if (world instanceof FakeServerWorld) return ((FakeServerWorld) world).owner;
		else return ((FakeClientWorld) world).owner;
	}
	
	@Nullable
	@Override
	public WorldLightManager getWorldLightManager() {
		return world.getLightManager();
	}
	
	@Nullable
	@Override
	public TileEntity getTileEntity(BlockPos pos, CreateEntityType creationMode) {
		return world.getTileEntity(pos);
	}
	
	@Nullable
	@Override
	public CompoundNBT getTileEntityNBT(BlockPos pos) {
		return getTileEntity(pos).serializeNBT();
	}
}
