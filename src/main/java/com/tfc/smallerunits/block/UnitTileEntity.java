package com.tfc.smallerunits.block;

import com.tfc.smallerunits.registry.Deferred;
import com.tfc.smallerunits.utils.SmallUnit;
import com.tfc.smallerunits.utils.UnitPallet;
import com.tfc.smallerunits.utils.world.client.FakeClientWorld;
import com.tfc.smallerunits.utils.world.common.FakeDimensionSavedData;
import com.tfc.smallerunits.utils.world.server.FakeServerWorld;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.profiler.IProfiler;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;
import sun.misc.Unsafe;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

public class UnitTileEntity extends TileEntity implements ITickableTileEntity {
	private static final Unsafe theUnsafe;
	public boolean isNatural = false;
	public CompoundNBT dataNBT = new CompoundNBT();
	
	static {
		try {
			Field f = Unsafe.class.getDeclaredField("theUnsafe");
			f.setAccessible(true);
			theUnsafe = (Unsafe) f.get(null);
		} catch (Throwable err) {
			throw new RuntimeException(err);
		}
	}
	
	public FakeServerWorld worldServer = null;
	public AtomicReference<FakeClientWorld> worldClient = null;
	public int unitsPerBlock = 4;
	
	public UnitTileEntity() {
		super(Deferred.UNIT_TE.get());
	}
	
	boolean needsRefresh = false;
	
	public Map<Integer, Entity> getEntitiesById() {
		if (worldServer == null && worldClient == null) return new HashMap<>();
		else if (worldServer == null) return worldClient.get().entitiesById;
		else return worldServer.entitiesById;
	}
	
	public World getFakeWorld() {
		return worldClient != null ? worldClient.get() : worldServer;
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		AxisAlignedBB bb = null;
		for (Entity value : getEntitiesById().values()) {
			AxisAlignedBB renderBB = value.getRenderBoundingBox();
			renderBB = renderBB.offset(0, -64, 0);
			renderBB = new AxisAlignedBB(
					renderBB.minX / unitsPerBlock,
					renderBB.minY / unitsPerBlock,
					renderBB.minZ / unitsPerBlock,
					renderBB.maxX / unitsPerBlock,
					renderBB.maxY / unitsPerBlock,
					renderBB.maxZ / unitsPerBlock
			);
			renderBB = renderBB.offset(getPos());
			if (bb == null) {
				bb = renderBB;
			} else {
				bb = new AxisAlignedBB(
						Math.min(renderBB.minX, bb.minX),
						Math.min(renderBB.minY, bb.minY),
						Math.min(renderBB.minZ, bb.minZ),
						Math.max(renderBB.maxX, bb.maxX),
						Math.max(renderBB.maxY, bb.maxY),
						Math.max(renderBB.maxZ, bb.maxZ)
				);
			}
		}
		for (SmallUnit value : getBlockMap().values()) {
			VoxelShape shape = value.state.getRenderShape(getFakeWorld(), value.pos);
			if (shape.isEmpty()) shape = value.state.getShape(getFakeWorld(), value.pos);
			AxisAlignedBB renderBB = new AxisAlignedBB(0, 0, 0, 0, 0, 0);
			if (!shape.isEmpty())
				renderBB = shape.getBoundingBox();
			renderBB = renderBB.offset(value.pos);
			if (value.tileEntity != null) {
				AxisAlignedBB bb1 = value.tileEntity.getRenderBoundingBox();
				renderBB = new AxisAlignedBB(
						Math.min(renderBB.minX, bb1.minX),
						Math.min(renderBB.minY, bb1.minY),
						Math.min(renderBB.minZ, bb1.minZ),
						Math.max(renderBB.maxX, bb1.maxX),
						Math.max(renderBB.maxY, bb1.maxY),
						Math.max(renderBB.maxZ, bb1.maxZ)
				);
			}
			if (!value.state.getFluidState().isEmpty()) {
				AxisAlignedBB bb1 = new AxisAlignedBB(0, 0, 0, 1, value.state.getFluidState().getHeight(), 1);
				bb1 = bb1.offset(value.pos);
				renderBB = new AxisAlignedBB(
						Math.min(renderBB.minX, bb1.minX),
						Math.min(renderBB.minY, bb1.minY),
						Math.min(renderBB.minZ, bb1.minZ),
						Math.max(renderBB.maxX, bb1.maxX),
						Math.max(renderBB.maxY, bb1.maxY),
						Math.max(renderBB.maxZ, bb1.maxZ)
				);
			}
			renderBB = renderBB.offset(0, -64, 0);
			renderBB = new AxisAlignedBB(
					renderBB.minX / unitsPerBlock,
					renderBB.minY / unitsPerBlock,
					renderBB.minZ / unitsPerBlock,
					renderBB.maxX / unitsPerBlock,
					renderBB.maxY / unitsPerBlock,
					renderBB.maxZ / unitsPerBlock
			);
			renderBB = renderBB.offset(getPos());
			if (bb == null) {
				bb = renderBB;
			} else {
				bb = new AxisAlignedBB(
						Math.min(renderBB.minX, bb.minX),
						Math.min(renderBB.minY, bb.minY),
						Math.min(renderBB.minZ, bb.minZ),
						Math.max(renderBB.maxX, bb.maxX),
						Math.max(renderBB.maxY, bb.maxY),
						Math.max(renderBB.maxZ, bb.maxZ)
				);
			}
		}
		if (bb == null) {
			bb = new AxisAlignedBB(getPos().getX(), getPos().getY(), getPos().getZ(), getPos().getX() + 1, getPos().getY() + 1, getPos().getZ() + 1);
		}
		return bb;
	}
	
	public IProfiler getProfiler() {
		return worldClient != null ? worldClient.get().profiler.get() : worldServer.profiler.get();
	}
	
	public Map<Long, SmallUnit> getBlockMap() {
		if (worldServer == null && worldClient == null) return new Long2ObjectLinkedOpenHashMap<>();
		else if (worldServer == null) return worldClient.get().blockMap;
		else return worldServer.blockMap;
	}
	
	private void setBlockMap(Long2ObjectLinkedOpenHashMap<SmallUnit> posUnitMap) {
		if (worldServer != null) worldServer.blockMap = posUnitMap;
		else worldClient.get().blockMap = posUnitMap;
	}
	
	public IBlockReader loadingWorld;
	
	public void setRaytraceResult(BlockRayTraceResult result) {
		if (worldServer != null) worldServer.result = result;
		else if (worldClient != null) worldClient.get().result = result;
	}
	
	public BlockRayTraceResult getResult() {
		if (worldServer != null) return worldServer.result;
		else if (worldClient != null) return worldClient.get().result;
		else return null;
	}
	
	@Override
	public void tick() {
		if (getFakeWorld() == null) return;
		
		if (getFakeWorld().isRemote) {
			getProfiler().startTick();
			
			for (SmallUnit value : this.getBlockMap().values()) {
				if (value.tileEntity instanceof ITickableTileEntity) {
					try {
						((ITickableTileEntity) value.tileEntity).tick();
					} catch (Throwable ignored) {
					}
				}
			}
			
			for (Entity value : getEntitiesById().values()) {
				try {
					value.tick();
				} catch (Throwable ignored) {
				}
			}
			
			getProfiler().endTick();
			if (worldServer != null) worldServer.tick(() -> false);
			else worldClient.get().tick(() -> false);
			return;
		}
		
		if (!getWorld().getPendingBlockTicks().isTickScheduled(pos, getBlockState().getBlock())) {
			getWorld().getPendingBlockTicks().scheduleTick(pos, getBlockState().getBlock(), 1);
		}
		
		long start = new Date().getTime();
		BooleanSupplier supplier = () -> Math.abs(new Date().getTime() - start) <= 10;
		if (worldServer != null) worldServer.tick(supplier);
		else worldClient.get().tick(supplier);
		
		getProfiler().startTick();
		for (SmallUnit value : this.getBlockMap().values()) {
			if (value != null && value.tileEntity != null) {
				try {
					if (value.tileEntity instanceof ITickableTileEntity) {
						((ITickableTileEntity) value.tileEntity).tick();
					}
				} catch (Throwable err) {
					StringBuilder stacktrace = new StringBuilder(err.toString() + "\n");
					for (StackTraceElement element : err.getStackTrace()) {
						stacktrace.append(element.toString()).append("\n");
					}
					System.out.println(stacktrace);
					err = err.getCause();
					if (err != null) {
						stacktrace = new StringBuilder(err.toString() + "\n");
						for (StackTraceElement element : err.getStackTrace()) {
							stacktrace.append(element.toString()).append("\n");
						}
						System.out.println(stacktrace);
					}
				}
			}
		}
		
		for (Entity value : getEntitiesById().values()) {
			if (!value.removed) {
				try {
					value.tick();
				} catch (Throwable ignored) {
				
				}
			}

//			if (value.getDataManager().isDirty()) {
//				value.getDataManager().setClean();
			//TODO: change this to send packets to update only the specific entity being updated
			this.markDirty();
			this.getWorld().notifyBlockUpdate(this.getPos(), this.getBlockState(), this.getBlockState(), 3);
//			}
		}
		getProfiler().endTick();
	}
	
	@Override
	public void read(BlockState state, CompoundNBT nbt) {
		super.read(state, nbt);
		
		if (worldClient == null) {
			createServerWorld();
		}

//		if (FMLEnvironment.dist.isClient()) {
//			for (Direction dir : Direction.values()) {
//				if (SmallerUnitsTESR.bufferCache.containsKey(this.getPos().offset(dir))) {
//					SmallerUnitsTESR.bufferCache.get(this.getPos().offset(dir)).getSecond().dispose();
//				}
//
//				SmallerUnitsTESR.bufferCache.remove(this.getPos().offset(dir));
//			}
//
//			if (SmallerUnitsTESR.bufferCache.containsKey(this.getPos())) {
//				SmallerUnitsTESR.bufferCache.get(this.getPos()).getSecond().dispose();
//			}
//
//			SmallerUnitsTESR.bufferCache.remove(this.getPos());
//		}
		
		this.unitsPerBlock = Math.min(Math.max(nbt.getInt("upb"), 1), 256);
		UnitPallet pallet = new UnitPallet(nbt.getCompound("containedUnits"), getFakeWorld());
		this.setBlockMap(pallet.posUnitMap);
		
		needsRefresh(true);
		
		for (SmallUnit value : getBlockMap().values()) {
			if (value.tileEntity == null) continue;
			if (value.tileEntity instanceof ITickableTileEntity) {
				getFakeWorld().tickableTileEntities.add(value.tileEntity);
			}
			
			if (worldServer != null) worldServer.tileEntityPoses.add(value.pos);
			getFakeWorld().loadedTileEntityList.add(value.tileEntity);
		}
		
		CompoundNBT ticks = nbt.getCompound("ticks");
		
		{
			ListNBT blockTickList = ticks.getList("blockTicks", Constants.NBT.TAG_COMPOUND);
			for (INBT inbt : blockTickList) {
				CompoundNBT tick = (CompoundNBT) inbt;
				BlockPos pos = new BlockPos(tick.getInt("x"), tick.getInt("y"), tick.getInt("z"));
				long time = tick.getInt("time");
				int priority = tick.getInt("priority");
				
				try {
					if (pos.getY() > 0 && pos.getX() > 0 && pos.getZ() > 0 && pos.getX() < (unitsPerBlock - 1) && (pos.getY() - 64) < (unitsPerBlock - 1) && pos.getZ() < (unitsPerBlock - 1))
						getFakeWorld().getPendingBlockTicks().scheduleTick(pos, pallet.posUnitMap.get(pos.toLong()).state.getBlock(), (int) time, TickPriority.getPriority(priority));
				} catch (Throwable err) {
					err.printStackTrace();
				}
			}
			
			ListNBT fluidTickList = ticks.getList("fluidTicks", Constants.NBT.TAG_COMPOUND);
			
			for (INBT inbt : fluidTickList) {
				CompoundNBT tick = (CompoundNBT) inbt;
				BlockPos pos = new BlockPos(tick.getInt("x"), tick.getInt("y"), tick.getInt("z"));
				long time = tick.getInt("time");
				int priority = tick.getInt("priority");
				try {
					getFakeWorld().getPendingFluidTicks().scheduleTick(pos, pallet.posUnitMap.get(pos.toLong()).state.getFluidState().getFluid(), (int) time, TickPriority.getPriority(priority));
				} catch (Throwable err) {
					err.printStackTrace();
				}
			}
		}
		if (nbt.contains("savedData"))
			dataNBT = nbt.getCompound("savedData");
		if (nbt.contains("isNatural"))
			isNatural = nbt.getBoolean("isNatural");
		CompoundNBT entities = nbt.getCompound("entities");
		List<Integer> entitiesExisting = new ArrayList<>();
		loop_entities:
		for (String key : entities.keySet()) {
			CompoundNBT entityNBT = entities.getCompound(key);
			EntityType<?> type = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(entityNBT.getString("id")));
			if (type == null) continue;
			Entity entity = type.create(getFakeWorld());
			if (entity == null) continue;
			if (worldServer != null) {
				if (worldServer.containsEntityWithUUID(entity.getUniqueID())) {
					for (Entity value : getEntitiesById().values()) {
						if (value.getUniqueID().toString().equals(key)) {
							int oldID = value.getEntityId();
							entitiesExisting.add(value.getEntityId());
							value.read(entityNBT);
							value.setEntityId(oldID);
							continue loop_entities;
						}
					}
				}
			} else {
				if (worldClient.get().containsEntityWithUUID(entityNBT.getUniqueId("UUID"))) {
					for (Entity value : getEntitiesById().values()) {
						if (value.getUniqueID().toString().equals(key)) {
							if (value.getUniqueID().equals(Minecraft.getInstance().player.getUniqueID())) {
								continue loop_entities;
							}
							int oldID = value.getEntityId();
							entitiesExisting.add(value.getEntityId());
							value.read(entityNBT);
							value.setEntityId(oldID);
							continue loop_entities;
						}
					}
				}
			}
			Entity entityIn = entity;
			getFakeWorld().addEntity(entityIn);
			entity.read(entityNBT);
			entitiesExisting.add(entity.getEntityId());
//			if (!(entityIn instanceof ArmorStandEntity) && (entityIn instanceof LivingEntity || entityIn instanceof ItemEntity || entityIn instanceof ExperienceOrbEntity || entityIn instanceof PotionEntity)) {
//				worldServer.addEntity(entityIn);
//			} else {
//				worldServer.entitiesByUuid.put(entity.getUniqueID(), entity);
//				getEntitiesById().put(entity.getEntityId(), entity);
//			}
		}
		List<Integer> toRemove = new ArrayList<>();
		for (Entity value : getEntitiesById().values()) {
			if (!entitiesExisting.contains(value.getEntityId())) {
				toRemove.add(value.getEntityId());
			}
		}
		for (Integer integer : toRemove) {
			getEntitiesById().remove(integer);
		}
	}
	
	public void createServerWorld() {
		if (worldClient == null && worldServer == null) {
			try {
				worldServer = (FakeServerWorld) theUnsafe.allocateInstance(FakeServerWorld.class);
				worldServer.isFirstTick = true;
				worldServer.owner = this;
				worldServer.init(this);
			} catch (Throwable err) {
				throw new RuntimeException(err);
			}
		}
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound) {
		UnitPallet unitPallet = new UnitPallet(getBlockMap().values());
		compound.put("containedUnits", unitPallet.nbt);
		compound.putInt("upb", Math.max(1, unitsPerBlock));
		CompoundNBT ticks = new CompoundNBT();
		if (this.worldServer != null) {
			ListNBT pendingBlockTicks = new ListNBT();
			for (NextTickListEntry<Block> blockNextTickListEntry : this.worldServer.getPendingBlockTicks().getPending(new ChunkPos(0), false, false)) {
				CompoundNBT tick = new CompoundNBT();
				tick.putLong("time", blockNextTickListEntry.field_235017_b_ - worldServer.getGameTime());
				tick.putInt("x", blockNextTickListEntry.position.getX());
				tick.putInt("y", blockNextTickListEntry.position.getY());
				tick.putInt("z", blockNextTickListEntry.position.getZ());
				tick.putInt("priority", blockNextTickListEntry.priority.ordinal());
				pendingBlockTicks.add(tick);
			}
			ticks.put("blockTicks", pendingBlockTicks);
		}
		if (this.worldServer != null) {
			ListNBT pendingFluidTicks = new ListNBT();
			for (NextTickListEntry<Fluid> blockNextTickListEntry : this.worldServer.getPendingFluidTicks().getPending(new ChunkPos(0), false, false)) {
				CompoundNBT tick = new CompoundNBT();
				tick.putLong("time", blockNextTickListEntry.field_235017_b_ - worldServer.getGameTime());
				tick.putInt("x", blockNextTickListEntry.position.getX());
				tick.putInt("y", blockNextTickListEntry.position.getY());
				tick.putInt("z", blockNextTickListEntry.position.getZ());
				tick.putInt("priority", blockNextTickListEntry.priority.ordinal());
				pendingFluidTicks.add(tick);
			}
			ticks.put("fluidTicks", pendingFluidTicks);
		}
		compound.put("ticks", ticks);
		if (this.worldServer != null) {
			worldServer.getSavedData().save();
			compound.put("savedData", ((FakeDimensionSavedData) worldServer.getSavedData()).savedNBT);
		}
		compound.putBoolean("isNatural", isNatural);
		CompoundNBT entities = new CompoundNBT();
		if (this.worldServer != null) worldServer.entitiesByUuid.forEach((uuid, entity) -> {
			if (!worldServer.entitiesToRemove.contains(entity)) {
				if (this.getWorld().isRemote) {
					try {
						entities.put(uuid.toString(), entity.serializeNBT());
					} catch (Throwable ignored) {
					}
				} else {
					entities.put(uuid.toString(), entity.serializeNBT());
				}
			}
		});
		compound.put("entities", entities);
		return super.write(compound);
	}
	
	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		this.read(Deferred.UNIT.get().getDefaultState(), nbt);
	}
	
	@Override
	public CompoundNBT serializeNBT() {
		return write(new CompoundNBT());
	}
	
	@Override
	public void markDirty() {
		super.markDirty();
		needsRefresh(true);
	}
	
	@Override
	public void handleUpdateTag(BlockState state, CompoundNBT tag) {
		if (worldClient == null) {
			try {
				worldClient = new AtomicReference<>(new FakeClientWorld(
						null,
						new ClientWorld.ClientWorldInfo(getWorld().getDifficulty(), false, true),
						getWorld().dimension,
						getWorld().dimensionType,
						0, () -> new Profiler(() -> 0, () -> 0, false),
						null, true, 0
				));
				worldClient.get().init(this);
			} catch (Throwable err) {
				throw new RuntimeException(err);
			}
		}
		super.handleUpdateTag(state, tag);
	}
	
	@Nullable
	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		return new SUpdateTileEntityPacket(this.getPos(), 0, getUpdateTag());
	}
	
	@Override
	public CompoundNBT getTileData() {
		return serializeNBT();
	}
	
	@Override
	public CompoundNBT getUpdateTag() {
		CompoundNBT nbtShare = serializeNBT();
		nbtShare.remove("ticks");
		nbtShare.remove("savedData");
		return nbtShare;
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		if (worldClient == null) {
			try {
				worldClient = new AtomicReference<>(new FakeClientWorld(
						null,
						new ClientWorld.ClientWorldInfo(getWorld().getDifficulty(), false, true),
						getWorld().dimension,
						getWorld().dimensionType,
						0, () -> new Profiler(() -> 0, () -> 0, false),
						null, true, 0
				));
				worldClient.get().init(this);
			} catch (Throwable err) {
				throw new RuntimeException(err);
			}
		}
		
		deserializeNBT(pkt.getNbtCompound());
	}
	
	public boolean needsRefresh(boolean newValue) {
		boolean oldVal = needsRefresh;
		needsRefresh = newValue;
		return oldVal;
	}
}