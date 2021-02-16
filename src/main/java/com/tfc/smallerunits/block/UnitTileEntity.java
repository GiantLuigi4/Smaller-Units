package com.tfc.smallerunits.block;

import com.tfc.smallerunits.registry.Deferred;
import com.tfc.smallerunits.utils.FakeServerWorld;
import com.tfc.smallerunits.utils.UnitPallet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.TickPriority;
import net.minecraftforge.common.util.Constants;
import sun.misc.Unsafe;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

public class UnitTileEntity extends TileEntity {
	private static final Unsafe theUnsafe;
	
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
	public void read(BlockState state, CompoundNBT nbt) {
		super.read(state, nbt);
		this.unitsPerBlock = Math.max(nbt.getInt("upb"), 1);
		UnitPallet pallet = new UnitPallet(nbt.getCompound("containedUnits"), world);
		this.world.blockMap = pallet.posUnitMap;
		CompoundNBT ticks = nbt.getCompound("ticks");
		{
			ListNBT blockTickList = ticks.getList("blockTicks", Constants.NBT.TAG_COMPOUND);
			for (INBT inbt : blockTickList) {
				CompoundNBT tick = (CompoundNBT) inbt;
				BlockPos pos = new BlockPos(tick.getInt("x"), tick.getInt("y"), tick.getInt("z"));
				long time = tick.getInt("time");
				int priority = tick.getInt("priority");
				world.getPendingBlockTicks().scheduleTick(pos, pallet.posUnitMap.get(pos).state.getBlock(), (int) time, TickPriority.getPriority(priority));
			}
			ListNBT fluidTickList = ticks.getList("fluidTicks", Constants.NBT.TAG_COMPOUND);
			for (INBT inbt : fluidTickList) {
				CompoundNBT tick = (CompoundNBT) inbt;
				BlockPos pos = new BlockPos(tick.getInt("x"), tick.getInt("y"), tick.getInt("z"));
				long time = tick.getInt("time");
				int priority = tick.getInt("priority");
				world.getPendingFluidTicks().scheduleTick(pos, pallet.posUnitMap.get(pos).state.getFluidState().getFluid(), (int) time, TickPriority.getPriority(priority));
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
		return nbtShare;
	}
}