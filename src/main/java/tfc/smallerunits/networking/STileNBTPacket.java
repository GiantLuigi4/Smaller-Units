package tfc.smallerunits.networking;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.helpers.ClientUtils;
import tfc.smallerunits.networking.util.Packet;
import tfc.smallerunits.utils.data.SUCapabilityManager;

import java.util.HashMap;
import java.util.function.Supplier;

public class STileNBTPacket extends Packet {
	public HashMap<BlockPos, CompoundNBT> tileMap;
	
	public STileNBTPacket(HashMap<BlockPos, CompoundNBT> map) {
		this.tileMap = map;
	}
	
	public STileNBTPacket(PacketBuffer buffer) {
		readPacketData(buffer);
	}
	
	@Override
	public void readPacketData(PacketBuffer buf) {
		HashMap<BlockPos, CompoundNBT> nbtHashMap = new HashMap<>();
		int len = buf.readInt();
		for (int i = 0; i < len; i++) nbtHashMap.put(buf.readBlockPos(), buf.readCompoundTag());
		this.tileMap = nbtHashMap;
	}
	
	@Override
	public void writePacketData(PacketBuffer buf) {
		buf.writeInt(tileMap.size());
		for (BlockPos blockPos : tileMap.keySet()) {
			buf.writeBlockPos(blockPos);
			buf.writeCompoundTag(tileMap.get(blockPos));
		}
	}
	
	@Override
	public void handle(Supplier<NetworkEvent.Context> ctx) {
		if (checkClient(ctx.get())) {
			for (BlockPos pos : tileMap.keySet()) {
				World world = ClientUtils.getWorld();
				UnitTileEntity tileEntity = SUCapabilityManager.getUnitAtBlock(world, pos);
				if (tileEntity == null) {
					tileEntity = new UnitTileEntity();
					SUCapabilityManager.setTile(world, pos, tileEntity);
				}
				tileEntity.setWorldAndPos(world, pos);
				tileEntity.deserializeNBT(tileMap.get(pos));
//				SUCapabilityManager.setTile(world, pos, tileEntity);
			}
		}
	}
}
