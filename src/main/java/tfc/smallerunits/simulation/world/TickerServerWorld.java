package tfc.smallerunits.simulation.world;

import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.TickPriority;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import tfc.smallerunits.UnitSpace;
import tfc.smallerunits.client.tracking.SUCapableChunk;
import tfc.smallerunits.data.capability.ISUCapability;
import tfc.smallerunits.data.capability.SUCapabilityManager;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.networking.SUNetworkRegistry;
import tfc.smallerunits.networking.sync.UpdateStatesS2C;
import tfc.smallerunits.simulation.chunk.BasicVerticalChunk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BooleanSupplier;

public class TickerServerWorld extends ServerLevel {
	private static final NoStorageSource src = NoStorageSource.make();
	private static final LevelStorageSource.LevelStorageAccess noAccess;
	
	static {
		try {
			noAccess = src.createAccess("no");
		} catch (IOException e) {
			RuntimeException ex = new RuntimeException(e.getMessage(), e);
			ex.setStackTrace(e.getStackTrace());
			throw ex;
		}
	}
	
	public final Level parent;
	//	public final UnitSpace parentU;
	Region region;
	int upb;
	
	public TickerServerWorld(MinecraftServer p_8571_, ServerLevelData p_8574_, ResourceKey<Level> p_8575_, DimensionType p_8576_, ChunkProgressListener p_8577_, ChunkGenerator p_8578_, boolean p_8579_, long p_8580_, List<CustomSpawner> p_8581_, boolean p_8582_, Level parent, int upb, Region region) throws IOException {
		super(
				p_8571_,
				Util.backgroundExecutor(),
				noAccess,
				p_8574_,
				p_8575_,
				p_8576_,
				p_8577_,
				p_8578_,
				p_8579_,
				p_8580_,
				p_8581_,
				p_8582_
		);
//		this.parentU = parentU;
		this.parent = parent;
		this.upb = upb;
		this.chunkSource = new TickerChunkCache(
				this, noAccess,
				null, getStructureManager(),
				Util.backgroundExecutor(),
				p_8578_,
				0, 0,
				true,
				p_8577_, (pPos, pStatus) -> {
		}, () -> null,
				upb
		);
		this.region = region;
		this.blockTicks = new SUTickList<>(null, null);
		this.fluidTicks = new SUTickList<>(null, null);
	}
	
	@Override
	public float getShade(Direction pDirection, boolean pShade) {
		return parent.getShade(pDirection, pShade);
	}
	
	@Override
	public boolean isOutsideBuildHeight(int pY) {
		return false;
	}
	
	@Override
	public LevelChunk getChunkAt(BlockPos pPos) {
		int pX = SectionPos.blockToSectionCoord(pPos.getX());
//		int pY = SectionPos.blockToSectionCoord(pPos.getY());
		int pY = 0;
		int pZ = SectionPos.blockToSectionCoord(pPos.getZ());
		ChunkAccess chunkaccess = ((TickerChunkCache) this.getChunkSource()).getChunk(pX, pY, pZ, ChunkStatus.FULL, true);
		return (LevelChunk) chunkaccess;
	}
	
	@Override
	public BlockState getBlockState(BlockPos pPos) {
		return getChunkAt(pPos).getBlockState(new BlockPos(pPos.getX(), pPos.getY(), pPos.getZ()));
	}
	
	@Override
	public LevelChunk getChunk(int pChunkX, int pChunkZ) {
		return super.getChunk(pChunkX, pChunkZ);
	}
	
	@Nullable
	@Override
	public ChunkAccess getChunk(int pX, int pZ, ChunkStatus pRequiredStatus, boolean pNonnull) {
		return super.getChunk(pX, pZ, pRequiredStatus, pNonnull);
	}
	
	@Override
	public void setBlockEntity(BlockEntity pBlockEntity) {
		BlockPos rp = region.pos.toBlockPos();
		BlockPos pos = pBlockEntity.getBlockPos();
		int xo = (pos.getX() / upb);
		int yo = (pos.getY() / upb);
		int zo = (pos.getZ() / upb);
		BlockPos parentPos = rp.offset(xo, yo, zo);
		ChunkAccess ac = parent.getChunkAt(parentPos);
		ac.setBlockState(parentPos, tfc.smallerunits.Registry.UNIT_SPACE.get().defaultBlockState(), false);
//		((SUCapableChunk) ac).getTiles().add(pBlockEntity);
		super.setBlockEntity(pBlockEntity);
	}
	
	@Override
	public void addFreshBlockEntities(Collection<BlockEntity> beList) {
		super.addFreshBlockEntities(beList);
	}
	
	@Override
	public void tick(BooleanSupplier pHasTimeLeft) {
		super.tick(pHasTimeLeft);
		for (BasicVerticalChunk[] column : ((TickerChunkCache) chunkSource).columns) {
			if (column == null) continue;
			// TODO: check only dirty chunks
			for (BasicVerticalChunk basicVerticalChunk : column) {
				if (basicVerticalChunk == null) continue;
				if (!basicVerticalChunk.updated.isEmpty()) {
					// TODO: mark parent dirty
//					if (basicVerticalChunk.yPos == 2) {
					ArrayList<Pair<BlockPos, BlockState>> updates = new ArrayList<>();
					for (BlockPos pos : basicVerticalChunk.updated)
						updates.add(Pair.of(new BlockPos(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15), basicVerticalChunk.getBlockState(pos)));
					basicVerticalChunk.updated.clear();
					basicVerticalChunk.setUnsaved(false);
					UpdateStatesS2C packet = new UpdateStatesS2C(
							region.pos, updates,
							upb, basicVerticalChunk.getPos(),
							basicVerticalChunk.yPos
					);
					BlockPos rp = region.pos.toBlockPos();
					double x = rp.getX() + basicVerticalChunk.getPos().getMinBlockX() + 8;
					double y = rp.getY() + (basicVerticalChunk.yPos << 4) + 8;
					double z = rp.getZ() + basicVerticalChunk.getPos().getMinBlockZ() + 8;
					ChunkPos cp = new ChunkPos(new BlockPos(x, y, z));
					LevelChunk chunk = parent.getChunkAt(cp.getWorldPosition());
					chunk.setUnsaved(true);
					SUNetworkRegistry.NETWORK_INSTANCE.send(
//								PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(x, y, z, 2560, parent.dimension())),
							PacketDistributor.ALL.noArg(),
							packet
					);
//					}
				}
			}
		}
	}
	
	private void tickSUBlock(BlockPos pos) {
		getBlockState(pos).tick(this, pos, this.random);
	}
	
	public CompoundTag getTicksIn(BlockPos myPosInTheLevel, BlockPos offset) {
		CompoundTag tag = new CompoundTag();
		AABB box = new AABB(myPosInTheLevel, offset);
		{
			CompoundTag blockTicks = new CompoundTag();
			ArrayList<ScheduledTick<Block>> ticks = ((SUTickList) this.blockTicks).getTicksInArea(box);
			Registry<Block> blockRegistry = parent.registryAccess().registryOrThrow(Registry.BLOCK_REGISTRY);
			for (ScheduledTick<Block> tick : ticks) {
				CompoundTag tag1 = new CompoundTag();
				tag1.putLong("ttime", tick.triggerTick() - getGameTime());
				tag1.putString("ttype", blockRegistry.getKey(tick.type()).toString());
				tag1.putByte("tpriority", (byte) tick.priority().ordinal());
				tag1.putLong("tsub", (byte) tick.subTickOrder());
				blockTicks.put(tick.pos().toShortString().replace(" ", ""), tag1);
			}
			tag.put("blocks", blockTicks);
		}
		{
			CompoundTag blockTicks = new CompoundTag();
			ArrayList<ScheduledTick<Fluid>> ticks = ((SUTickList) this.fluidTicks).getTicksInArea(box);
			Registry<Fluid> fluidRegistry = parent.registryAccess().registryOrThrow(Registry.FLUID_REGISTRY);
			for (ScheduledTick<Fluid> tick : ticks) {
				CompoundTag tag1 = new CompoundTag();
				tag1.putLong("ttime", tick.triggerTick() - getGameTime());
				tag1.putString("ttype", fluidRegistry.getKey(tick.type()).toString());
				tag1.putByte("tpriority", (byte) tick.priority().ordinal());
				tag1.putByte("tsub", (byte) tick.subTickOrder());
				blockTicks.put(tick.pos().toShortString().replace(" ", ""), tag1);
			}
			tag.put("fluids", blockTicks);
		}
//		((SUTickList) blockTicks).clearBox(box);
//		((SUTickList) fluidTicks).clearBox(box);
		return tag;
	}
	
	public void loadTicks(CompoundTag tag) {
		Registry<Block> blockRegistry = parent.registryAccess().registryOrThrow(Registry.BLOCK_REGISTRY);
		Registry<Fluid> fluidRegistry = parent.registryAccess().registryOrThrow(Registry.FLUID_REGISTRY);
		CompoundTag blocks = tag.getCompound("blocks");
		for (String allKey : blocks.getAllKeys()) {
			CompoundTag tick = blocks.getCompound(allKey);
			long time = tick.getLong("ttime" + getGameTime());
			ResourceLocation regName = new ResourceLocation(tick.getString("ttype"));
			Block type = blockRegistry.get(regName);
			int priority = tick.getByte("tpriority");
			long sub = tick.getLong("tsub");
			String[] pos = allKey.split(",");
			int x = Integer.parseInt(pos[0]);
			int y = Integer.parseInt(pos[1]);
			int z = Integer.parseInt(pos[2]);
			blockTicks.schedule(new ScheduledTick<>(
					type, new BlockPos(x, y, z),
					time, TickPriority.values()[priority], sub
			));
		}
		CompoundTag fluids = tag.getCompound("blocks");
		for (String allKey : fluids.getAllKeys()) {
			CompoundTag tick = fluids.getCompound(allKey);
			long time = tick.getLong("ttime" + getGameTime());
			ResourceLocation regName = new ResourceLocation(tick.getString("ttype"));
			Fluid type = fluidRegistry.get(regName);
			int priority = tick.getByte("tpriority");
			long sub = tick.getLong("tsub");
			String[] pos = allKey.split(",");
			int x = Integer.parseInt(pos[0]);
			int y = Integer.parseInt(pos[1]);
			int z = Integer.parseInt(pos[2]);
			fluidTicks.schedule(new ScheduledTick<>(
					type, new BlockPos(x, y, z),
					time, TickPriority.values()[priority], sub
			));
		}
	}
	
	@Override
	public long getGameTime() {
		return parent.getGameTime();
	}
	
	@Override
	public RegistryAccess registryAccess() {
		if (parent == null) return super.registryAccess();
		return parent.registryAccess();
	}
	
	public void clear(BlockPos myPosInTheLevel, BlockPos offset) {
		for (int x = myPosInTheLevel.getX(); x < offset.getX(); x++) {
			for (int y = myPosInTheLevel.getY(); y < offset.getY(); y++) {
				for (int z = myPosInTheLevel.getZ(); z < offset.getZ(); z++) {
					BlockPos pz = new BlockPos(x, y, z);
					BasicVerticalChunk vc = (BasicVerticalChunk) getChunkAt(pz);
					vc.setBlockFast(new BlockPos(x, pz.getY(), z), null);
				}
			}
		}
	}
	
	public void setFromSync(ChunkPos cp, int cy, int x, int y, int z, BlockState state) {
		BlockPos rp = region.pos.toBlockPos();
		int xo = ((cp.x * 16) / upb) + (x / upb);
		int yo = ((cy * 16) / upb) + (y / upb);
		int zo = ((cp.z * 16) / upb) + (z / upb);
		BlockPos parentPos = rp.offset(xo, yo, zo);
		ChunkAccess ac = parent.getChunkAt(parentPos);
		ac.setBlockState(parentPos, tfc.smallerunits.Registry.UNIT_SPACE.get().defaultBlockState(), false);
		
		ISUCapability cap = SUCapabilityManager.getCapability((LevelChunk) ac);
		UnitSpace space = cap.getUnit(parentPos);
		if (space == null) {
			space = cap.getOrMakeUnit(parentPos);
			space.setUpb(upb);
		}
		BasicVerticalChunk vc = (BasicVerticalChunk) getChunkAt(cp.getWorldPosition());
		vc = vc.getSubChunk(cy);
		vc.setBlockFast(new BlockPos(x, y, z), state);
		
		((SUCapableChunk) ac).SU$markDirty(parentPos);
	}
}
