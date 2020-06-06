package tfc.smallerunits;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import tfc.smallerunits.Registry.Deferred;
import tfc.smallerunits.Utils.FakeWorld;

import javax.annotation.Nullable;

public class SmallerUnitsTileEntity extends TileEntity {
	public FakeWorld containedWorld=null;
	
	public SmallerUnitsTileEntity() {
		super(Deferred.TILE_ENTITY.get());
		containedWorld=new FakeWorld(4,this);
	}
	
	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
//		try {
//			if (Minecraft.getInstance().world.isRemote) {
//				String s=compound.toString().substring(compound.toString().indexOf("upb:")+4);
//				int num=Integer.parseInt(s.substring(0,s.indexOf(',')));
////				System.out.println(compound.toString());
////				System.out.println(compound.getInt("upb"));
//				containedWorld=new FakeWorld(num);
//				containedWorld.fromString(compound.getString("world"));
//			}
//		} catch (Throwable err) {
			containedWorld=new FakeWorld(compound.getInt("upb"),this);
			containedWorld.fromString(compound.getString("world"));
//		}
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound) {
		super.write(compound);
		if (containedWorld!=null) {
			compound.putString("world",containedWorld.toString());
			compound.putInt("upb",containedWorld.upb);
		}
		return compound;
	}
	
	@Override
	public TileEntity getTileEntity() {
		return this;
	}
	
	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		read(nbt);
	}
	
	@Override
	public CompoundNBT serializeNBT() {
		return write(new CompoundNBT());
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		containedWorld=new FakeWorld(pkt.getNbtCompound().getInt("upb"),this);
		deserializeNBT(pkt.getNbtCompound());
	}
	
	@Override
	public void handleUpdateTag(CompoundNBT tag) {
		this.read(tag);
	}
	
	@Nullable
	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		return new SUpdateTileEntityPacket(this.pos, 1, this.serializeNBT());
	}
	
	@Override
	public CompoundNBT getUpdateTag() {
		return super.getUpdateTag();
	}
	
	@Override
	public BlockState getBlockState() {
		return super.getBlockState();
	}
}
