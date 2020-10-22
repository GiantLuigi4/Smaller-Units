package tfc.smallerunits;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import tfc.smallerunits.registry.Deferred;
import tfc.smallerunits.utils.FakeWorld;
import tfc.smallerunits.utils.SmallUnit;

import javax.annotation.Nullable;

public class SmallerUnitsTileEntity extends TileEntity {
	public FakeWorld containedWorld = null;
	
	public SmallerUnitsTileEntity() {
		super(Deferred.TILE_ENTITY.get());
		containedWorld = new FakeWorld(4, this);
	}
	
	boolean isEnchanted = false;
	boolean useManual = false;
	
	@Override
	public void read(CompoundNBT compound) {
//		if (!this.serializeNBT().equals(compound)) {
//			System.out.println(compound);
//			System.out.println(this.serializeNBT());
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
		containedWorld = new FakeWorld(compound.getInt("upb"), this);
		containedWorld.fromString(compound.getString("world"));
		for (SmallUnit unit : containedWorld.unitHashMap.values()) {
//				if ("x1y1z1".equals("x"+unit.x+"y"+unit.y+"z"+unit.z))
//				System.out.println(("x"+unit.x+"y"+unit.y+"z"+unit.z));
//				if (compound.contains("x"+unit.x+"y"+unit.y+"z"+unit.z)) {
			try {
//					System.out.println(unit.te);
				containedWorld.setTileEntity(new BlockPos(unit.x, unit.y, unit.z), unit.readTileEntity(compound.getCompound("tile_entities").getCompound("x" + unit.x + "y" + unit.y + "z" + unit.z)));
			} catch (Exception err) {
			}
//				}
		}
//		}
//		}
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound) {
		super.write(compound);
		if (containedWorld != null) {
			compound.putString("world", containedWorld.toString());
			compound.putInt("upb", containedWorld.unitsPerBlock);
			CompoundNBT tileEntities = new CompoundNBT();
			for (SmallUnit unit : containedWorld.unitHashMap.values()) {
				try {
					if (containedWorld.getTileEntity(new BlockPos(unit.x, unit.y, unit.z)) != null) {
						tileEntities.put("" + ("x" + unit.x + "y" + unit.y + "z" + unit.z), containedWorld.getTileEntity(new BlockPos(unit.x, unit.y, unit.z)).serializeNBT());
					}
				} catch (Exception err) {
				
				}
			}
			compound.put("tile_entities", tileEntities);
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
		containedWorld = new FakeWorld(pkt.getNbtCompound().getInt("upb"), this);
		deserializeNBT(pkt.getNbtCompound());
	}
	
	@Override
	public void handleUpdateTag(CompoundNBT tag) {
		this.read(tag);
	}
	
	@Nullable
	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
//		System.out.println(this.serializeNBT().getCompound("tile_entities"));
		CompoundNBT nbt = this.serializeNBT();
		return new SUpdateTileEntityPacket(this.pos, 1, nbt);
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
