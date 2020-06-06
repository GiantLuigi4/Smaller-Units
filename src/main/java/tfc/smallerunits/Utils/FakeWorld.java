package tfc.smallerunits.Utils;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.IParticleData;
import net.minecraft.profiler.IProfiler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.tags.NetworkTagManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.palette.UpgradeData;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.*;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.WorldInfo;
import tfc.smallerunits.SmallerUnitsTileEntity;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class FakeWorld extends World implements IWorld {
	public HashMap<BlockPos,SmallUnit> unitHashMap=new HashMap<>();
	public int upb; //units per block
	public SmallerUnitsTileEntity owner;
	
	@Override
	public void notifyBlockUpdate(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
		oldState.onReplaced(this,pos,newState,((flags&4)==4));
		for (Direction dir:Direction.values()) {
			this.getBlockState(pos.offset(dir)).onNeighborChange(this,pos.offset(dir),pos);
		}
	}
	
	@Override
	public void playSound(@Nullable PlayerEntity player, double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {
	}
	
	@Override
	public void playMovingSound(@Nullable PlayerEntity playerIn, Entity entityIn, SoundEvent eventIn, SoundCategory categoryIn, float volume, float pitch) {
	
	}
	
	@Nullable
	@Override
	public Entity getEntityByID(int id) {
		return null;
	}
	
	@Nullable
	@Override
	public MapData getMapData(String mapName) {
		return null;
	}
	
	@Override
	public void registerMapData(MapData mapDataIn) {
	
	}
	
	@Override
	public int getNextMapId() {
		return 0;
	}
	
	@Override
	public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
	
	}
	
	@Override
	public Scoreboard getScoreboard() {
		return null;
	}
	
	@Override
	public RecipeManager getRecipeManager() {
		return null;
	}
	
	@Override
	public NetworkTagManager getTags() {
		return null;
	}
	
	@Override
	public String toString() {
		StringBuilder str= new StringBuilder();
		for (SmallUnit unit:unitHashMap.values()) {
			str.append(unit.toString()).append(";");
		}
		return str.toString();
	}
	
	public void fromString(String s) {
		for (String s1:s.split(";")) {
			SmallUnit unit=SmallUnit.fromString(s1,upb);
			unitHashMap.put(new BlockPos(unit.x,unit.y,unit.z),unit);
		}
	}
	
	public void tick(ServerWorld realWorld) {
//		System.out.println("h");
		ArrayList<BlockPos> tickedBlocks=new ArrayList<>();
		for (BlockPos pos:tickList.ticklist.keySet()) {
			Long time=tickList.ticklist.get(pos);
			if (time<new Date().getTime()) {
				try {
					this.getBlockState(pos).tick(new FakeServerWorld(realWorld),pos,new Random());
					tickedBlocks.add(pos);
				} catch (Exception err) {}
			}
		}
		int e=0;
		for (BlockPos pos:blockUpdateList.ticklist.keySet()) {
			try {
				this.getBlockState(pos).onNeighborChange(this,pos,blockUpdateList.updatorlist.get(e));
				this.getBlockState(pos).observedNeighborChange(this,pos,this.getBlockState(blockUpdateList.updatorlist.get(e)).getBlock(),blockUpdateList.updatorlist.get(e));
			} catch (Exception err) {}
			e++;
		}
		this.blockUpdateList.updatorlist.clear();
		this.blockUpdateList.ticklist.clear();
		this.blockUpdateList.blocklist.clear();
		tickedBlocks.forEach((blockPos)->this.tickList.unscheduleTick(blockPos));
		tickBlockEntities();
	}
	
	@Override
	public void tickBlockEntities() {
		super.tickBlockEntities();
	}
	
	public static class FakeTickList implements ITickList<Block> {
		public HashMap<BlockPos,Long> ticklist=new HashMap<>();
		public ArrayList<BlockPos> updatorlist=new ArrayList<>();
		public HashMap<BlockPos,Block> blocklist=new HashMap<>();
		@Override
		public boolean isTickScheduled(BlockPos pos, Block itemIn) {
			if (blocklist.get(pos).equals(itemIn)) {
				return ticklist.containsKey(pos);
			} else {
				return false;
			}
		}
		
		@Override
		public void scheduleTick(BlockPos pos, Block itemIn, int scheduledTime, TickPriority priority) {
			ticklist.put(pos,(long)scheduledTime+new Date().getTime());
			blocklist.put(pos,itemIn);
//			System.out.println(scheduledTime+new Date().getTime());
		}
		
		public void unscheduleTick(BlockPos pos) {
			try {
				ticklist.remove(pos);
				blocklist.remove(pos);
			} catch (Exception err) {}
		}
		
		public void addBlockUpdate(BlockPos pos, BlockPos source) {
			this.scheduleTick(pos, Blocks.DIRT,0);
			this.updatorlist.add(source);
		}
		
		@Override
		public boolean isTickPending(BlockPos pos, Block obj) {
			return ticklist.containsKey(pos);
		}
		
		@Override
		public void addAll(Stream<NextTickListEntry<Block>> p_219497_1_) {
			p_219497_1_.forEach((nextTickListEntry)->{
				ticklist.put(nextTickListEntry.position,nextTickListEntry.scheduledTime);
				blocklist.put(nextTickListEntry.position,nextTickListEntry.getTarget());
			});
		}
	}
	
	FakeTickList tickList= new FakeTickList();
	FakeTickList blockUpdateList= new FakeTickList();
	
	public FakeWorld(int upb, SmallerUnitsTileEntity owner) {
		super(new WorldInfo() {
		}, DimensionType.OVERWORLD, (world, dimension) -> null, new IProfiler() {
			@Override
			public void startTick() {
			
			}
			
			@Override
			public void endTick() {
			
			}
			
			@Override
			public void startSection(String name) {
			
			}
			
			@Override
			public void startSection(Supplier<String> nameSupplier) {
			
			}
			
			@Override
			public void endSection() {
			
			}
			
			@Override
			public void endStartSection(String name) {
			
			}
			
			@Override
			public void endStartSection(Supplier<String> nameSupplier) {
			
			}
			
			@Override
			public void func_230035_c_(String p_230035_1_) {
			
			}
			
			@Override
			public void func_230036_c_(Supplier<String> p_230036_1_) {
			
			}
		},false);
		this.upb = upb;
		this.owner=owner;
	}
	
	@Override
	public long getSeed() {
		return 0;
	}
	
	@Override
	public ITickList<Block> getPendingBlockTicks() {
		return tickList;
	}
	
	@Override
	public ITickList<Fluid> getPendingFluidTicks() {
		return null;
	}
	
	@Override
	public World getWorld() {
		return null;
	}
	
	@Override
	public WorldInfo getWorldInfo() {
		return new WorldInfo() {};
	}
	
	@Override
	public DifficultyInstance getDifficultyForLocation(BlockPos pos) {
		return new DifficultyInstance(Difficulty.EASY,0,0,this.getMoonPhase());
	}
	
	@Override
	public AbstractChunkProvider getChunkProvider() {
		return null;
	}
	
	@Override
	public Random getRandom() {
		return new Random();
	}
	
	@Override
	public void notifyNeighbors(BlockPos pos, Block blockIn) {
		for (Direction dir:Direction.values()) {
			this.getBlockState(pos.offset(dir)).onNeighborChange(this,pos.offset(dir),pos);
		}
	}
	
	@Override
	public BlockPos getSpawnPoint() {
		return null;
	}
	
	@Override
	public void playSound(@Nullable PlayerEntity player, BlockPos pos, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {
		player.world.playSound(player,pos,soundIn,category,volume,pitch);
	}
	
	@Override
	public void addParticle(IParticleData particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
	}
	
	@Override
	public void playEvent(@Nullable PlayerEntity player, int type, BlockPos pos, int data) {
		player.world.playEvent(player,type,pos,data);
	}
	
	@Override
	public WorldBorder getWorldBorder() {
		return null;
	}
	
	@Nullable
	@Override
	public TileEntity getTileEntity(BlockPos pos) {
		return unitHashMap.get(pos).te;
	}
	
	@Override
	public BlockState getBlockState(BlockPos pos) {
		return unitHashMap.get(pos).s;
	}
	
	@Override
	public void markAndNotifyBlock(BlockPos pos, @Nullable Chunk chunk, BlockState blockstate, BlockState newState, int flags) {
		super.markAndNotifyBlock(pos, chunk, blockstate, newState, flags);
	}
	
	@Override
	public void notifyNeighborsOfStateChange(BlockPos pos, Block blockIn) {
		for (Direction dir:Direction.values()) {
			this.getBlockState(pos.offset(dir)).onNeighborChange(this,pos.offset(dir),pos);
		}
	}
	
	@Override
	public void notifyNeighborsOfStateExcept(BlockPos pos, Block blockType, Direction skipSide) {
		for (Direction dir:Direction.values()) {
			if (!dir.equals(skipSide)) {
				this.getBlockState(pos.offset(dir)).onNeighborChange(this,pos.offset(dir),pos);
			}
		}
	}
	
	@Override
	public IFluidState getFluidState(BlockPos pos) {
		return unitHashMap.get(pos).s.getFluidState();
	}
	
	@Override
	public List<Entity> getEntitiesInAABBexcluding(@Nullable Entity entityIn, AxisAlignedBB boundingBox, @Nullable Predicate<? super Entity> predicate) {
		return null;
	}
	
	@Override
	public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> clazz, AxisAlignedBB aabb, @Nullable Predicate<? super T> filter) {
		return null;
	}
	
	@Override
	public List<? extends PlayerEntity> getPlayers() {
		return null;
	}
	
	@Nullable
	@Override
	public IChunk getChunk(int x, int z, ChunkStatus requiredStatus, boolean nonnull) {
		World world=this;
		return new IChunk() {
			@Nullable
			@Override
			public BlockState setBlockState(BlockPos pos, BlockState state, boolean isMoving) {
				SmallUnit unit=new SmallUnit(pos.getX(),pos.getY(),pos.getZ(),upb,state);
				if (unitHashMap.containsKey(pos)) {
					unitHashMap.replace(pos,unit);
				} else {
					unitHashMap.put(pos,unit);
				}
				for (Direction dir:Direction.values()) {
					this.getBlockState(pos.offset(dir)).onNeighborChange(world,pos.offset(dir),pos);
				}
				return state;
			}
			
			@Override
			public void addTileEntity(BlockPos pos, TileEntity tileEntityIn) {
				unitHashMap.get(pos).te=tileEntityIn;
			}
			
			@Override
			public void addEntity(Entity entityIn) {
			}
			
			@Override
			public Set<BlockPos> getTileEntitiesPos() {
				ArrayList<BlockPos> poses=new ArrayList<>();
				for (SmallUnit unit:unitHashMap.values()) {
					if (unit.te!=null) {
						poses.add(new BlockPos(unit.x,unit.y,unit.z));
					}
				}
				return ImmutableSet.copyOf(poses);
			}
			
			@Override
			public ChunkSection[] getSections() {
				return new ChunkSection[0];
			}
			
			@Override
			public Collection<Map.Entry<Heightmap.Type, Heightmap>> getHeightmaps() {
				return null;
			}
			
			@Override
			public void setHeightmap(Heightmap.Type type, long[] data) {
			}
			
			@Override
			public Heightmap getHeightmap(Heightmap.Type typeIn) {
				return null;
			}
			
			@Override
			public int getTopBlockY(Heightmap.Type heightmapType, int x, int z) {
				return 0;
			}
			
			@Override
			public ChunkPos getPos() {
				return new ChunkPos(0,0);
			}
			
			@Override
			public void setLastSaveTime(long saveTime) {
			}
			
			@Override
			public Map<String, StructureStart> getStructureStarts() {
				return null;
			}
			
			@Override
			public void setStructureStarts(Map<String, StructureStart> structureStartsIn) {
			}
			
			@Nullable
			@Override
			public BiomeContainer getBiomes() {
				return null;
			}
			
			@Override
			public void setModified(boolean modified) {
			}
			
			@Override
			public boolean isModified() {
				return false;
			}
			
			@Override
			public ChunkStatus getStatus() {
				return null;
			}
			
			@Override
			public void removeTileEntity(BlockPos pos) {
				unitHashMap.get(pos).te=null;
			}
			
			@Override
			public ShortList[] getPackedPositions() {
				return new ShortList[0];
			}
			
			@Nullable
			@Override
			public CompoundNBT getDeferredTileEntity(BlockPos pos) {
				return unitHashMap.get(pos).te.serializeNBT();
			}
			
			@Nullable
			@Override
			public CompoundNBT getTileEntityNBT(BlockPos pos) {
				return unitHashMap.get(pos).te.serializeNBT();
			}
			
			@Override
			public Stream<BlockPos> getLightSources() {
				return null;
			}
			
			@Override
			public ITickList<Block> getBlocksToBeTicked() {
				return tickList;
			}
			
			@Override
			public ITickList<Fluid> getFluidsToBeTicked() {
				return null;
			}
			
			@Override
			public UpgradeData getUpgradeData() {
				return null;
			}
			
			@Override
			public void setInhabitedTime(long newInhabitedTime) {
			}
			
			@Override
			public long getInhabitedTime() {
				return 0;
			}
			
			@Override
			public boolean hasLight() {
				return false;
			}
			
			@Override
			public void setLight(boolean lightCorrectIn) {
			}
			
			@Nullable
			@Override
			public TileEntity getTileEntity(BlockPos pos) {
				return unitHashMap.get(pos).te;
			}
			
			@Override
			public BlockState getBlockState(BlockPos pos) {
				return unitHashMap.get(pos).s;
			}
			
			@Override
			public IFluidState getFluidState(BlockPos pos) {
				return unitHashMap.get(pos).s.getFluidState();
			}
			
			@Nullable
			@Override
			public StructureStart getStructureStart(String stucture) {
				return null;
			}
			
			@Override
			public void putStructureStart(String structureIn, StructureStart structureStartIn) {
			}
			
			@Override
			public LongSet getStructureReferences(String structureIn) {
				return null;
			}
			
			@Override
			public void addStructureReference(String strucutre, long reference) {
			}
			
			@Override
			public Map<String, LongSet> getStructureReferences() {
				return null;
			}
			
			@Override
			public void setStructureReferences(Map<String, LongSet> p_201606_1_) {
			}
		};
	}
	
	@Override
	public int getHeight(Heightmap.Type heightmapType, int x, int z) {
		return 0;
	}
	
	@Override
	public int getSkylightSubtracted() {
		return 0;
	}
	
	@Override
	public BiomeManager getBiomeManager() {
		return null;
	}
	
	@Override
	public Biome getNoiseBiomeRaw(int x, int y, int z) {
		return null;
	}
	
	@Override
	public boolean isRemote() {
		try {
			return Minecraft.getInstance().world.isRemote;
		} catch (Exception err) {}
		return false;
	}
	
	@Override
	public int getSeaLevel() {
		return 0;
	}
	
	@Override
	public Dimension getDimension() {
		return null;
	}
	
	@Override
	public WorldLightManager getLightManager() {
		return null;
	}
	
	@Override
	public boolean setBlockState(BlockPos pos, BlockState newState, int flags) {
		try {
			this.getBlockState(pos).onReplaced(this,pos,newState,false);
		} catch (Exception err) {}
		try {
			newState.onBlockAdded(this,pos,getBlockState(pos),false);
		} catch (Exception err) {}
		getChunk(0,0,null,true).setBlockState(pos,newState,false);
		try {
			tickList.unscheduleTick(pos);
		} catch (Exception err) {}
		tickList.scheduleTick(pos,newState.getBlock(),(newState.getBlock().tickRate(this)*2000));
		return true;
	}
	
	@Override
	public boolean removeBlock(BlockPos pos, boolean isMoving) {
		unitHashMap.remove(pos);
		for (Direction dir:Direction.values()) {
			this.getBlockState(pos.offset(dir)).onNeighborChange(this,pos.offset(dir),pos);
		}
		return true;
	}
	
	@Override
	public boolean destroyBlock(BlockPos p_225521_1_, boolean p_225521_2_, @Nullable Entity p_225521_3_) {
		unitHashMap.remove(p_225521_1_);
		for (Direction dir:Direction.values()) {
			this.getBlockState(p_225521_1_.offset(dir)).onNeighborChange(this,p_225521_1_.offset(dir),p_225521_1_);
		}
		return true;
	}
	
	@Override
	public boolean hasBlockState(BlockPos p_217375_1_, Predicate<BlockState> p_217375_2_) {
		return p_217375_2_.test(getBlockState(p_217375_1_));
	}
}
