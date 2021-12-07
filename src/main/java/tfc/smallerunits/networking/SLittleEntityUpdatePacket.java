package tfc.smallerunits.networking;

import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.INetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.NetworkEvent;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.helpers.ClientUtils;
import tfc.smallerunits.utils.data.SUCapabilityManager;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Supplier;

public class SLittleEntityUpdatePacket implements IPacket {
	// TODO: change to array list
	BlockPos updatingUnit;
	ArrayList<Pair<UUID, CompoundNBT>> data;
	
	public SLittleEntityUpdatePacket(BlockPos updatingUnit, ArrayList<Pair<UUID, CompoundNBT>> data) {
		this.updatingUnit = updatingUnit;
		this.data = data;
	}
	
	public SLittleEntityUpdatePacket(PacketBuffer buffer) {
		readPacketData(buffer);
	}
	
	@Override
	public void readPacketData(PacketBuffer buf) {
		updatingUnit = buf.readBlockPos();
		int elements = buf.readInt();
		data = new ArrayList<>();
		for (int index = 0; index < elements; index++) data.add(Pair.of(buf.readUniqueId(), buf.readCompoundTag()));
	}
	
	@Override
	public void writePacketData(PacketBuffer buf) {
		buf.writeBlockPos(updatingUnit);
		buf.writeInt(data.size());
		for (Pair<UUID, CompoundNBT> datum : data) {
			buf.writeUniqueId(datum.getFirst());
			buf.writeCompoundTag(datum.getSecond());
		}
	}
	
	@Override
	public void processPacket(INetHandler handler) {
	}
	
	public void handle(Supplier<NetworkEvent.Context> ctx) {
		if (ctx.get().getDirection().getReceptionSide().isClient()) {
//			BlockState state = Minecraft.getInstance().world.getBlockState(updatingUnit);
//			if (!(state.getBlock() instanceof SmallerUnitBlock)) return;
//			TileEntity te = Minecraft.getInstance().world.getTileEntity(updatingUnit);
//			if (!(te instanceof UnitTileEntity)) return;
//			UnitTileEntity tileEntity = (UnitTileEntity) te;
			UnitTileEntity tileEntity = SUCapabilityManager.getUnitAtBlock(ClientUtils.getWorld(), updatingUnit);
			if (tileEntity == null) return;
			for (Pair<UUID, CompoundNBT> datum : data) {
//				if (!tileEntity.getEntitiesById().containsKey(datum.getFirst())) continue;
//				tileEntity.getEntitiesById().get(datum.getFirst()).deserializeNBT(datum.getSecond());
				for (Entity value : tileEntity.getEntitiesById().values()) {
					if (value.getUniqueID().equals(datum.getFirst())) {
						Vector3d lastPosition = value.getPositionVec();
						value.deserializeNBT(datum.getSecond());
						value.lastTickPosX = lastPosition.x;
						value.lastTickPosY = lastPosition.y;
						value.lastTickPosZ = lastPosition.z;
					}
				}
			}
		}
	}
}
