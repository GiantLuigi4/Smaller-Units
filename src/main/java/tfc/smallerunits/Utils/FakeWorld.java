package tfc.smallerunits.Utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.IParticleData;
import net.minecraft.profiler.IProfiler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.tags.NetworkTagManager;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.palette.UpgradeData;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.biome.Biomes;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.LogicalSide;
import org.apache.logging.log4j.Level;
import tfc.smallerunits.Registry.Deferred;
//import tfc.smallerunits.Registry.ModEventRegistry;
import tfc.smallerunits.Registry.ModEventRegistry;
import tfc.smallerunits.SmallerUnitBlock;
import tfc.smallerunits.SmallerUnitsTileEntity;
import tfc.smallerunits.Smallerunits;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class FakeWorld extends World implements IWorld {
	public HashMap<BlockPos,SmallUnit> unitHashMap=new HashMap<>();
	public HashMap<BlockPos,SmallUnit> lastUnitsHashMap=new HashMap<>();
	public int upb; //units per block
	public SmallerUnitsTileEntity owner;
	
	@Override
	public void setTileEntity(BlockPos pos, @Nullable TileEntity tileEntityIn) {
		if (unitHashMap.containsKey(pos))
			unitHashMap.get(pos).te=tileEntityIn;
	}
	
	@Override
	public void notifyBlockUpdate(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
		oldState.onReplaced(this,pos,newState,((flags&4)==4));
		for (Direction dir:Direction.values()) {
			this.getBlockState(pos.offset(dir)).onNeighborChange(this,pos.offset(dir),pos);
			this.getBlockState(pos.offset(dir)).neighborChanged(this,pos.offset(dir),oldState.getBlock(),pos,false);
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
	public void registerMapData(MapData mapDataIn) {}
	
	@Override
	public int getNextMapId() {
		return 0;
	}
	
	//TODO
	@Override
	public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {}
	
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
	
	@Override
	public IChunk getChunk(int chunkX, int chunkZ, ChunkStatus requiredStatus) {
		return getChunk(chunkX,chunkZ,requiredStatus,false);
	}
	
	public void tick(ServerWorld realWorld) {
		try {
//			BlockPos randomPos=new BlockPos(new Random().nextInt(16/2)*16,(new Random().nextInt(255/16/2)+2)*16,new Random().nextInt(16/2)*16);
			time++;
			BlockPos randomPos=new BlockPos((1/2)*16,((1/2)+2)*16,(1/2)*16);
			ServerWorld sworld=realWorld.getServer().getWorld(Objects.requireNonNull(DimensionType.byName(new ResourceLocation("smallerunits", "susimulator"))));
			sworld.getChunkProvider().getChunk(0,0,true);
			sworld.getChunkProvider().getChunk(0,-1,true);
			sworld.getChunkProvider().getChunk(-1,-1,true);
			sworld.getChunkProvider().getChunk(-1,0,true);
			for (BlockPos pos:lastUnitsHashMap.keySet()) {
				SmallUnit newUnit=unitHashMap.containsKey(pos)?unitHashMap.get(pos):SmallUnit.fromString("0",upb);
				sworld.setBlockState(randomPos.add(pos),this.getBlockState(pos),64);
				BlockState oldState=lastUnitsHashMap.containsKey(pos)?lastUnitsHashMap.get(pos).s:Blocks.AIR.getDefaultState();
				BlockState newState=this.getBlockState(pos);
				if (!oldState.equals(newState)) {
					boolean isMoving=newState.getBlock() instanceof MovingPistonBlock||oldState.getBlock() instanceof MovingPistonBlock;
					this.onBlockStateChange(randomPos.add(pos),oldState,newState);
					newState.onBlockAdded(this,pos,oldState,isMoving);
					newState.onNeighborChange(this,pos,pos);
					newState.neighborChanged(this,pos,oldState.getBlock(),pos,isMoving);
					for (Direction dir:Direction.values()) {
						this.getBlockState(pos.offset(dir)).neighborChanged(this,pos.offset(dir),newState.getBlock(),pos,isMoving);
						this.getBlockState(pos.offset(dir)).onNeighborChange(this,pos.offset(dir),pos);
						this.getBlockState(pos.offset(dir)).observedNeighborChange(this,pos.offset(dir),oldState.getBlock(),pos);
					}
				}
				newUnit=unitHashMap.containsKey(pos)?unitHashMap.get(pos):SmallUnit.fromString("0",upb);
				if (lastUnitsHashMap.containsKey(pos))
					lastUnitsHashMap.replace(pos,newUnit);
				else
					lastUnitsHashMap.put(pos,newUnit);
			}
			for (BlockPos pos:unitHashMap.keySet()) {
				BlockState oldState=lastUnitsHashMap.containsKey(pos)?lastUnitsHashMap.get(pos).s:Blocks.AIR.getDefaultState();
				BlockState newState=this.getBlockState(pos);
				if (!oldState.equals(newState)) {
					sworld.setBlockState(randomPos.add(pos),this.getBlockState(pos),64);
					this.onBlockStateChange(randomPos.add(pos),oldState,newState);
					newState.onBlockAdded(this,pos,oldState,false);
					newState.onNeighborChange(this,pos,pos);
					newState.neighborChanged(this,pos,oldState.getBlock(),pos,false);
					for (Direction dir:Direction.values()) {
						this.getBlockState(pos.offset(dir)).neighborChanged(this,pos.offset(dir),newState.getBlock(),pos,false);
						this.getBlockState(pos.offset(dir)).onNeighborChange(this,pos.offset(dir),pos);
						this.getBlockState(pos.offset(dir)).observedNeighborChange(this,pos.offset(dir),oldState.getBlock(),pos);
						if (!this.tickList.ticklist.containsKey(pos.offset(dir))) {
							this.tickList.scheduleTick(pos.offset(dir),this.getBlockState(pos.offset(dir)).getBlock(),this.getBlockState(pos.offset(dir)).getBlock().tickRate(this)/10);
						}
					}
				}
				if (lastUnitsHashMap.containsKey(pos))
					lastUnitsHashMap.replace(pos,unitHashMap.get(pos));
				else
					lastUnitsHashMap.put(pos,unitHashMap.get(pos));
			}
			for (BlockPos pos:unitHashMap.keySet()) {
				sworld.setBlockState(randomPos.add(pos),this.getBlockState(pos),64);
			}
			MinecraftForge.EVENT_BUS.post(new TickEvent.WorldTickEvent(LogicalSide.SERVER, TickEvent.Phase.START,sworld));
//			tickList.ticklist.keySet().forEach((pos)->sworld.getPendingBlockTicks().scheduleTick(pos,tickList.blocklist.get(pos),tickList.ticklist.get(pos).intValue()));
//			try{if(true)sworld.tick(()->true);if(true)sworld.getPendingBlockTicks().tick();}catch(Throwable ignored){}
			try {
				for (int x=0;x<upb;x++) {
					for (int y = 0; y < upb; y++) {
						for (int z = 0; z < upb; z++) {
							if (!(sworld.getBlockState(randomPos.add(x,y,z)).getBlock() instanceof SmallerUnitBlock))
								if (tickList.isTickPending(new BlockPos(x,y,z),sworld.getBlockState(randomPos.add(x,y,z)).getBlock())) {
									sworld.getBlockState(randomPos.add(x,y,z)).tick(sworld,randomPos.add(x,y,z),rand);
									tickList.unscheduleTick(new BlockPos(x,y,z));
								}
							TileEntity te=this.getTileEntity(new BlockPos(x,y,z));
							if(te==null)te=sworld.getTileEntity(randomPos.add(new BlockPos(x,y,z)));
							this.setBlockState(new BlockPos(x,y,z),sworld.getBlockState(randomPos.add(new BlockPos(x,y,z))));
							if (te!=null) {
								te.setWorldAndPos(this,new BlockPos(x,y,z));
								if(te instanceof ITickableTileEntity)((ITickableTileEntity)te).tick();
							}
							this.setTileEntity(new BlockPos(x,y,z),te);
							if (te!=null)
							te.setWorldAndPos(sworld,new BlockPos(x,y,z));
							sworld.setBlockState(randomPos.add(x,y,z),this.getBlockState(new BlockPos(x,y,z)),64);
							for (Direction dir:Direction.values())sworld.setBlockState(randomPos.add(x,y,z).offset(dir),this.getBlockState(new BlockPos(x,y,z).offset(dir)),64);
						}
					}
				}
			} catch (Throwable err) {
				StringBuilder stack=new StringBuilder("\n"+err.toString() + "(" + err.getMessage() + ")");
				for (StackTraceElement element:err.getStackTrace()) stack.append(element.toString()).append("\n");
				System.out.println(stack.toString());
			}
			MinecraftForge.EVENT_BUS.post(new TickEvent.WorldTickEvent(LogicalSide.SERVER, TickEvent.Phase.END,sworld));
			for (BlockPos pos:lastUnitsHashMap.keySet()) {
				if (sworld.getBlockState(pos)!=Blocks.AIR.getDefaultState())
				this.setBlockState(pos,sworld.getBlockState(randomPos.add(pos)));
				sworld.setBlockState(randomPos.add(pos),Blocks.AIR.getDefaultState(),64);
				sworld.getEntitiesWithinAABB(ItemEntity.class,new AxisAlignedBB(randomPos.getX()+pos.getX(),randomPos.getY()+pos.getY(),randomPos.getZ()+pos.getZ(),randomPos.getX()+pos.getX()+1,randomPos.getY()+pos.getY()+1,randomPos.getZ()+pos.getZ()+1)).forEach((e)->e.remove());
			}
		} catch (Throwable ignored) {}
	}
	
	@Override
	public List<Entity> getEntitiesWithinAABBExcludingEntity(@Nullable Entity entityIn, AxisAlignedBB bb) {
		return ImmutableList.of();
	}
	
	@Override
	public Biome getBiome(BlockPos p_226691_1_) {
		return Biomes.THE_VOID;
	}
	
	@Override
	public int getLightValue(BlockPos pos) {
		return getLight(pos);
	}
	
	public int getSkyLightValue(BlockPos pos) {
		return (owner.getWorld()!=null)?owner.getWorld().getLightFor(LightType.SKY,owner.getPos()):15;
	}
	
	public int getBlockLightValue(BlockPos pos) {
		float light=15;
		BlockPos pos1=owner.getPos();
		if (owner.getWorld()!=null) {
			light=0;
			for (Direction dir:Direction.values()) {
				light=Math.max(light,owner.getWorld().getLightFor(LightType.BLOCK,pos1.offset(dir)));
			}
		}
		return (int)light;
	}
	
	@Override
	public float getCelestialAngle(float partialTicks) {
		return owner.getWorld().getCelestialAngle(partialTicks);
	}
	
	@Override
	public int getMaxLightLevel() {
		return (owner.getWorld()!=null)?owner.getWorld().getMaxLightLevel():15;
	}
	
	@Override
	public int getLight(BlockPos pos) {
		return getLightFor(LightType.BLOCK,pos)|getLightFor(LightType.SKY,pos);
	}
	
	@Override
	public int getNeighborAwareLightSubtracted(BlockPos pos, int amount) {
		return getLight(pos)-amount;
	}
	
	@Override
	public int getLightFor(LightType lightTypeIn, BlockPos blockPosIn) {
		return (owner.getWorld()!=null)?(lightTypeIn.equals(LightType.SKY)?getSkyLightValue(blockPosIn):getBlockLightValue(blockPosIn)):15;
	}
	
	@Override
	public int getLightSubtracted(BlockPos blockPosIn, int amount) {
		return getLight(blockPosIn)-amount;
	}
	
	@Override
	public <T extends Entity> List<T> getEntitiesWithinAABB(@Nullable EntityType<T> type, AxisAlignedBB boundingBox, Predicate<? super T> predicate) {
		return owner.getWorld().getEntitiesWithinAABB(type,boundingBox.shrink(upb).offset(owner.getPos()),predicate);
	}
	
	@Override
	public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> p_217357_1_, AxisAlignedBB p_217357_2_) {
		return owner.getWorld().getEntitiesWithinAABB(p_217357_1_,p_217357_2_.shrink(upb).offset(owner.getPos()));
	}
	
	@Override
	public void tickBlockEntities() {
		super.tickBlockEntities();
	}
	
	@Override
	public boolean addTileEntity(TileEntity tile) {
		unitHashMap.get(tile.getPos()).te=tile;
		return true;
	}
	
	@Override
	public void addTileEntities(Collection<TileEntity> tileEntityCollection) {
		tileEntityCollection.forEach((tile)->unitHashMap.get(tile.getPos()).te=tile);
	}
	
	@Override
	public void removeTileEntity(BlockPos pos) {
		unitHashMap.get(pos).te=null;
	}
	
	public int time=0;
	
	public static class FakeTickList implements ITickList<Block> {
		public HashMap<BlockPos,Long> ticklist=new HashMap<>();
		public ArrayList<BlockPos> updatorlist=new ArrayList<>();
		public HashMap<BlockPos,Block> blocklist=new HashMap<>();
		FakeWorld world;
		
		public FakeTickList(FakeWorld world) {
			this.world = world;
		}
		
		@Override
		public boolean isTickScheduled(BlockPos pos, Block itemIn) {
			if (blocklist.containsKey(pos)&&blocklist.get(pos).equals(itemIn))
				return ticklist.containsKey(pos);
			return false;
		}
		
		@Override
		public void scheduleTick(BlockPos pos, Block itemIn, int scheduledTime, TickPriority priority) {
			if (!itemIn.getDefaultState().isAir()) {
//				System.out.println(scheduledTime);
//				System.out.println(itemIn);
//				System.out.println(world.time+scheduledTime);
				ticklist.put(pos,world.time+(long)scheduledTime);
				blocklist.put(pos,itemIn);
			}
		}
		
		public void unscheduleTick(BlockPos pos) {
			try {
				while (ticklist.containsKey(pos))
					ticklist.remove(pos);
				while (blocklist.containsKey(pos))
					blocklist.remove(pos);
			} catch (Exception err) {}
		}
		
		public void addBlockUpdate(BlockPos pos, BlockPos source) {
			this.scheduleTick(pos, Blocks.DIRT,0);
			this.updatorlist.add(source);
		}
		
		@Override
		public void scheduleTick(BlockPos pos, Block itemIn, int scheduledTime) {
			this.scheduleTick(pos,itemIn,scheduledTime,TickPriority.NORMAL);
		}
		
		@Override
		public boolean isTickPending(BlockPos pos, Block obj) {
//			System.out.println(ticklist.containsKey(pos)&&ticklist.get(pos).intValue()<=world.time);
			return ticklist.containsKey(pos)&&ticklist.get(pos).intValue()<=world.time;
		}
		
		@Override
		public void addAll(Stream<NextTickListEntry<Block>> p_219497_1_) {
			p_219497_1_.forEach((nextTickListEntry)->{
				ticklist.put(nextTickListEntry.position,nextTickListEntry.scheduledTime);
				blocklist.put(nextTickListEntry.position,nextTickListEntry.getTarget());
			});
		}
	}
	
	FakeTickList tickList= new FakeTickList(this);
	FakeTickList blockUpdateList= new FakeTickList(this);
	
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
		return this;
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
			this.getBlockState(pos.offset(dir)).neighborChanged(this,pos.offset(dir),blockIn,pos,false);
		}
	}
	
	@Override
	public BlockPos getSpawnPoint() {
		return new BlockPos(0,0,0);
	}
	
	@Override
	public void playSound(@Nullable PlayerEntity player, BlockPos pos, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {
		try {
			player.world.playSound(player,pos,soundIn,category,volume,pitch);
		} catch (Throwable err) {}
	}
	
	@Override
	public void addParticle(IParticleData particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
	}
	
	@Override
	public void playEvent(@Nullable PlayerEntity player, int type, BlockPos pos, int data) {
		try {
			player.world.playEvent(player,type,pos,data);
		} catch (Throwable err) {}
	}
	
	@Override
	public WorldBorder getWorldBorder() {
		WorldBorder border=new WorldBorder();
		border.setCenter(upb/2f,upb/2f);
		border.setSize(upb*2);
		return border;
	}
	
	@Nullable
	@Override
	public TileEntity getTileEntity(BlockPos pos) {
		if (unitHashMap.containsKey(pos)) return unitHashMap.get(pos).te;
		else return null;
	}
	
	@Override
	public BlockState getBlockState(BlockPos pos) {
		if (unitHashMap.containsKey(pos)) {
			return unitHashMap.get(pos).s;
		}
		return Blocks.AIR.getDefaultState();
	}
	
	@Override
	public void markAndNotifyBlock(BlockPos pos, @Nullable Chunk chunk, BlockState blockstate, BlockState newState, int flags) {
		super.markAndNotifyBlock(pos, chunk, blockstate, newState, flags);
	}
	
	@Override
	public void notifyNeighborsOfStateChange(BlockPos pos, Block blockIn) {
		for (Direction dir:Direction.values()) {
			this.getBlockState(pos.offset(dir)).onNeighborChange(this,pos.offset(dir),pos);
			this.getBlockState(pos.offset(dir)).neighborChanged(this,pos.offset(dir),blockIn,pos,false);
		}
	}
	
	@Override
	public void notifyNeighborsOfStateExcept(BlockPos pos, Block blockType, Direction skipSide) {
		for (Direction dir:Direction.values()) if (!dir.equals(skipSide)) {
			this.getBlockState(pos.offset(dir)).onNeighborChange(this,pos.offset(dir),pos);
			this.getBlockState(pos.offset(dir)).neighborChanged(this,pos.offset(dir),blockType,pos,false);
		}
	}
	
	@Override
	public IFluidState getFluidState(BlockPos pos) {
		if (unitHashMap.containsKey(pos)) return unitHashMap.get(pos).s.getFluidState();
		else return Blocks.WATER.getDefaultState().getFluidState();
	}
	
	@Override
	public boolean checkNoEntityCollision(@Nullable Entity entityIn, VoxelShape shape) {
		return true;
	}
	
	@Override
	public boolean checkNoEntityCollision(Entity p_226668_1_) {
		return true;
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
				try {
					if (world.getTileEntity(pos)!=null)
						unit.te=world.getTileEntity(pos);
					try {
						this.getBlockState(pos).onReplaced(world,pos,state,false);
					} catch (Throwable ignored) {}
					if (unitHashMap.containsKey(pos))
						unitHashMap.replace(pos,unit);
					else
						unitHashMap.put(pos,unit);
					if (state.equals(Blocks.AIR.getDefaultState()))
						unitHashMap.remove(pos);
					for (Direction dir:Direction.values()) {
						this.getBlockState(pos.offset(dir)).onNeighborChange(world,pos.offset(dir),pos);
//						this.getBlockState(pos.offset(dir)).neighborChanged(world,pos.offset(dir),state.getBlock(),pos,false);
						this.getBlockState(pos.offset(dir)).observedNeighborChange(world,pos.offset(dir),this.getBlockState(pos).getBlock(),pos);
					}
				} catch (Throwable err) {
					StringBuilder stack=new StringBuilder("\n"+err.toString() + "(" + err.getMessage() + ")");
					for (StackTraceElement element:err.getStackTrace()) stack.append(element.toString()).append("\n");
					System.out.println(stack.toString());
				}
				state.onBlockAdded(world,pos,state,isMoving);
				return state;
			}
			
			@Override
			public void addTileEntity(BlockPos pos, TileEntity tileEntityIn) {
				unitHashMap.get(pos).te=tileEntityIn;
			}
			@Override
			public void addEntity(Entity entityIn) {}
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
			public void setHeightmap(Heightmap.Type type, long[] data) {}
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
			public void setLastSaveTime(long saveTime) {}
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
				return world.getPendingBlockTicks();
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
				if (unitHashMap.containsKey(pos))
					return unitHashMap.get(pos).s;
				return Blocks.AIR.getDefaultState();
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
			public void putStructureStart(String structureIn, StructureStart structureStartIn) {}
			@Override
			public LongSet getStructureReferences(String structureIn) {
				return null;
			}
			@Override
			public void addStructureReference(String strucutre, long reference) {}
			@Override
			public Map<String, LongSet> getStructureReferences() {
				return null;
			}
			@Override
			public void setStructureReferences(Map<String, LongSet> p_201606_1_) {}
		};
	}
	
	@Override
	public int getHeight(Heightmap.Type heightmapType, int x, int z) {
		return 0;
	}
	
	@Override
	public int getSkylightSubtracted() {
		return getMaxLightLevel();
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
		} catch (Throwable ignored) {}
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
		getChunk(0,0,null,true).setBlockState(pos,newState,false);
		return true;
	}
	
	@Override
	public boolean removeBlock(BlockPos pos, boolean isMoving) {
		for (Direction dir:Direction.values()) {
			this.getBlockState(pos.offset(dir)).onNeighborChange(this,pos.offset(dir),pos);
			this.getBlockState(pos.offset(dir)).neighborChanged(this,pos.offset(dir),this.getBlockState(pos).getBlock(),pos,false);
		}
		unitHashMap.remove(pos);
		return true;
	}
	
	@Override
	public boolean destroyBlock(BlockPos p_225521_1_, boolean p_225521_2_, @Nullable Entity p_225521_3_) {
		unitHashMap.remove(p_225521_1_);
		for (Direction dir:Direction.values()) {
			this.getBlockState(p_225521_1_.offset(dir)).onNeighborChange(this,p_225521_1_.offset(dir),p_225521_1_);
			this.getBlockState(p_225521_1_.offset(dir)).neighborChanged(this,p_225521_1_.offset(dir),this.getBlockState(p_225521_1_).getBlock(),p_225521_1_,false);
		}
		return true;
	}
	
	@Override
	public boolean hasBlockState(BlockPos p_217375_1_, Predicate<BlockState> p_217375_2_) {
		return p_217375_2_.test(getBlockState(p_217375_1_));
	}
}
