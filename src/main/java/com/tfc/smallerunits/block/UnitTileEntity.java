package com.tfc.smallerunits.block;

import com.tfc.smallerunits.SmallerUnitsTESR;
import com.tfc.smallerunits.registry.Deferred;
import com.tfc.smallerunits.utils.SmallUnit;
import com.tfc.smallerunits.utils.UnitPallet;
import com.tfc.smallerunits.utils.world.FakeDimensionSavedData;
import com.tfc.smallerunits.utils.world.FakeServerWorld;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.TickPriority;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;
import sun.misc.Unsafe;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

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
	
	public final FakeServerWorld world;
	public int unitsPerBlock = 4;
	
	public UnitTileEntity() {
		super(Deferred.UNIT_TE.get());
		
		try {
			world = (FakeServerWorld) theUnsafe.allocateInstance(FakeServerWorld.class);
			world.init(this);
		} catch (Throwable err) {
			throw new RuntimeException(err);
		}
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(getPos().getX(), getPos().getY(), getPos().getZ(), getPos().getX() + 1, getPos().getY() + 1, getPos().getZ() + 1);
	}
	
	public IBlockReader loadingWorld;
	
	
	@Override
	public void tick() {
		if (world.isRemote) {
			world.profiler.get().startTick();
			for (SmallUnit value : this.world.blockMap.values()) {
				if (value.tileEntity instanceof ITickableTileEntity) {
					try {
						((ITickableTileEntity) value.tileEntity).tick();
					} catch (Throwable ignored) {
					}
				}
			}
			for (Entity value : world.entitiesByUuid.values()) {
				try {
					value.tick();
				} catch (Throwable ignored) {
				}
			}
			world.profiler.get().endTick();
		} else {
			for (Entity value : world.entitiesByUuid.values()) {
				if (value.getDataManager().isDirty()) {
					this.markDirty();
					this.getWorld().notifyBlockUpdate(this.getPos(), this.getBlockState(), this.getBlockState(), 3);
				}
			}
		}
//		if (world.isRemote) {
//		World worldIn = getWorld();
//		BlockState state = this.getBlockState();
//			TileEntity tileEntity = worldIn.getTileEntity(pos);
//			if (!(tileEntity instanceof UnitTileEntity)) return;
//			UnitTileEntity tileEntity1 = (UnitTileEntity) tileEntity;
//			if (tileEntity1.world != null) {
//				ArrayList<SmallUnit> toRemove = new ArrayList<>();
//				ArrayList<SmallUnit> toMove = new ArrayList<>();
//				for (SmallUnit value : tileEntity1.world.blockMap.values()) {
//					BlockPos blockPos = value.pos;
//					if (value.pos == null) {
//						toRemove.add(value);
//						continue;
//					}
//					int y = value.pos.getY() - 64;
//					if (
//							blockPos.getX() < 0 ||
//									blockPos.getX() > tileEntity1.unitsPerBlock - 1 ||
//									blockPos.getZ() < 0 ||
//									blockPos.getZ() > tileEntity1.unitsPerBlock - 1 ||
//									y < 0 ||
//									y > tileEntity1.unitsPerBlock - 1
//					) {
//						toMove.add(value);
//					}
//				}
//				for (SmallUnit smallUnit : toRemove) {
//					tileEntity1.world.blockMap.remove(smallUnit.pos);
//				}
//				for (SmallUnit value : toMove) {
//					BlockPos blockPos = value.pos;
//					ExternalUnitInteractionContext context = new ExternalUnitInteractionContext(((UnitTileEntity) tileEntity).world, value.pos);
//					if (context.teInRealWorld instanceof UnitTileEntity) {
//						if (((UnitTileEntity) context.teInRealWorld).world.blockMap.isEmpty()) {
//							((UnitTileEntity) context.teInRealWorld).unitsPerBlock = tileEntity1.unitsPerBlock;
//						}
//					}
//					if (context.stateInRealWorld.getBlock().equals(Blocks.AIR)) {
//						UnitTileEntity tileEntity2 = new UnitTileEntity();
//						worldIn.setBlockState(context.posInRealWorld, Deferred.UNIT.get().getDefaultState());
//						worldIn.setTileEntity(context.posInRealWorld,tileEntity2);
//						tileEntity2.isNatural = true;
//						continue;
//					}
//					TileEntity te = context.teInRealWorld;
//					if (te instanceof UnitTileEntity) {
//						value.pos = context.posInFakeWorld;
//						((UnitTileEntity) te).world.setBlockState(value.pos,value.state,3,0);
//						((UnitTileEntity) te).world.setTileEntity(value.pos,value.tileEntity);
//						tileEntity1.world.blockMap.remove(blockPos);
//
//						tileEntity.markDirty();
//						te.markDirty();
//						worldIn.notifyBlockUpdate(tileEntity.getPos(), state, state, 3);
//						worldIn.notifyBlockUpdate(te.getPos(), state, state, 3);
//					}
//				}
//				long start = new Date().getTime();
//				tileEntity1.world.tick(() -> Math.abs(new Date().getTime() - start) <= 10);
//
//				if (tileEntity1.isNatural && tileEntity1.world.blockMap.isEmpty()) {
//					worldIn.setBlockState(pos,Blocks.AIR.getDefaultState());
//				}
//			}
//		}
	}
	
	@Override
	public void read(BlockState state, CompoundNBT nbt) {
		super.read(state, nbt);
		if (FMLEnvironment.dist.isClient()) {
			for (Direction dir : Direction.values()) {
				if (SmallerUnitsTESR.bufferCache.containsKey(this.getPos().offset(dir))) {
					SmallerUnitsTESR.bufferCache.get(this.getPos().offset(dir)).getSecond().dispose();
				}
				SmallerUnitsTESR.bufferCache.remove(this.getPos().offset(dir));
			}
			if (SmallerUnitsTESR.bufferCache.containsKey(this.getPos())) {
				SmallerUnitsTESR.bufferCache.get(this.getPos()).getSecond().dispose();
			}
			SmallerUnitsTESR.bufferCache.remove(this.getPos());
		}
		this.unitsPerBlock = Math.min(Math.max(nbt.getInt("upb"), 1), 256);
		UnitPallet pallet = new UnitPallet(nbt.getCompound("containedUnits"), world);
		this.world.blockMap = pallet.posUnitMap;
		for (SmallUnit value : world.blockMap.values()) {
			if (value.tileEntity == null) continue;
			if (value.tileEntity instanceof ITickableTileEntity) {
				world.tickableTileEntities.add(value.tileEntity);
			}
			world.tileEntityPoses.add(value.pos);
			world.loadedTileEntityList.add(value.tileEntity);
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
						world.getPendingBlockTicks().scheduleTick(pos, pallet.posUnitMap.get(pos).state.getBlock(), (int) time, TickPriority.getPriority(priority));
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
					world.getPendingFluidTicks().scheduleTick(pos, pallet.posUnitMap.get(pos).state.getFluidState().getFluid(), (int) time, TickPriority.getPriority(priority));
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
		world.entitiesByUuid.clear();
		world.entitiesById.clear();
		for (String key : entities.keySet()) {
			CompoundNBT entityNBT = entities.getCompound(key);
			EntityType<?> type = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(entityNBT.getString("id")));
			if (type == null) continue;
			Entity entity = type.create(world);
			if (entity == null) continue;
			entity.deserializeNBT(entityNBT);
			Entity entityIn = entity;
			if (entityIn instanceof LivingEntity || entityIn instanceof ItemEntity || entityIn instanceof ExperienceOrbEntity || entityIn instanceof PotionEntity) {
				world.addEntity(entityIn);
			} else {
				world.entitiesByUuid.put(entity.getUniqueID(), entity);
				world.entitiesById.put(entity.getEntityId(), entity);
			}
		}
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound) {
		UnitPallet unitPallet = new UnitPallet(world.blockMap.values());
		compound.put("containedUnits", unitPallet.nbt);
		compound.putInt("upb", Math.max(1, unitsPerBlock));
		CompoundNBT ticks = new CompoundNBT();
		{
			ListNBT pendingBlockTicks = new ListNBT();
			for (NextTickListEntry<Block> blockNextTickListEntry : this.world.getPendingBlockTicks().getPending(new ChunkPos(0), false, false)) {
				CompoundNBT tick = new CompoundNBT();
				tick.putLong("time", blockNextTickListEntry.field_235017_b_ - world.getGameTime());
				tick.putInt("x", blockNextTickListEntry.position.getX());
				tick.putInt("y", blockNextTickListEntry.position.getY());
				tick.putInt("z", blockNextTickListEntry.position.getZ());
				tick.putInt("priority", blockNextTickListEntry.priority.ordinal());
				pendingBlockTicks.add(tick);
			}
			ticks.put("blockTicks", pendingBlockTicks);
		}
		{
			ListNBT pendingFluidTicks = new ListNBT();
			for (NextTickListEntry<Fluid> blockNextTickListEntry : this.world.getPendingFluidTicks().getPending(new ChunkPos(0), false, false)) {
				CompoundNBT tick = new CompoundNBT();
				tick.putLong("time", blockNextTickListEntry.field_235017_b_ - world.getGameTime());
				tick.putInt("x", blockNextTickListEntry.position.getX());
				tick.putInt("y", blockNextTickListEntry.position.getY());
				tick.putInt("z", blockNextTickListEntry.position.getZ());
				tick.putInt("priority", blockNextTickListEntry.priority.ordinal());
				pendingFluidTicks.add(tick);
			}
			ticks.put("fluidTicks", pendingFluidTicks);
		}
		compound.put("ticks", ticks);
		world.getSavedData().save();
		compound.put("savedData", ((FakeDimensionSavedData) world.getSavedData()).savedNBT);
		compound.putBoolean("isNatural", isNatural);
		CompoundNBT entities = new CompoundNBT();
		world.entitiesByUuid.forEach((uuid, entity) -> {
			if (!world.entitiesToRemove.contains(entity)) {
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
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		deserializeNBT(pkt.getNbtCompound());
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
}