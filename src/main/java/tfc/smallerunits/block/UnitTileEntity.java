package tfc.smallerunits.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShapes;
import sun.misc.Unsafe;
import tfc.smallerunits.registry.Deferred;
import tfc.smallerunits.utils.FakeServerWorld;
import tfc.smallerunits.utils.UnitPallet;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

public class UnitTileEntity extends TileEntity {
	public final FakeServerWorld world;
	
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
	
	public int unitsPerBlock = 4;
	
	public UnitTileEntity() {
		super(Deferred.UNIT_TE.get());
		
		try {
			world = (FakeServerWorld) theUnsafe.allocateInstance(FakeServerWorld.class);
			world.init();
			world.owner = this;
		} catch (Throwable err) {
			throw new RuntimeException(err);
		}
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(getPos().getX(), getPos().getY(), getPos().getZ(), getPos().getX() + 1, getPos().getY() + 1, getPos().getZ() + 1);
	}
	
	@Override
	public void read(BlockState state, CompoundNBT nbt) {
		super.read(state, nbt);
		this.unitsPerBlock = Math.max(nbt.getInt("upb"),1);
		UnitPallet pallet = new UnitPallet(nbt.getCompound("containedUnits"));
		this.world.blockMap = pallet.posUnitMap;
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound) {
		UnitPallet unitPallet = new UnitPallet(world.blockMap.values());
		compound.put("containedUnits", unitPallet.nbt);
		compound.putInt("upb", Math.max(1,unitsPerBlock));
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
		return new SUpdateTileEntityPacket(this.getPos(), 0, serializeNBT());
	}
	
	@Override
	public CompoundNBT getTileData() {
		return serializeNBT();
	}
	
	@Override
	public CompoundNBT getUpdateTag() {
		return serializeNBT();
	}
}