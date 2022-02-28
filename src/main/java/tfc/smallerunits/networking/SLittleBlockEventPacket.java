package tfc.smallerunits.networking;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.helpers.ClientUtils;
import tfc.smallerunits.networking.util.Packet;
import tfc.smallerunits.utils.data.SUCapabilityManager;

import java.util.function.Supplier;

public class SLittleBlockEventPacket extends Packet {
	// TODO: change small block pos to array list
	BlockPos pos, smallBlockPos;
	int type, data;
	
	public SLittleBlockEventPacket(BlockPos pos, BlockPos smallBlockPos, int type, int data) {
		this.pos = pos;
		this.type = type;
		this.data = data;
		this.smallBlockPos = smallBlockPos;
	}
	
	public SLittleBlockEventPacket(PacketBuffer buffer) {
		readPacketData(buffer);
	}
	
	@Override
	public void readPacketData(PacketBuffer buf) {
		type = buf.readInt();
		data = buf.readInt();
		pos = buf.readBlockPos();
		smallBlockPos = buf.readBlockPos();
	}
	
	@Override
	public void writePacketData(PacketBuffer buf) {
		buf.writeInt(type);
		buf.writeInt(data);
		buf.writeBlockPos(pos);
		buf.writeBlockPos(smallBlockPos);
	}
	
	@Override
	public void handle(Supplier<NetworkEvent.Context> ctx) {
//		TileEntity tileEntity = Minecraft.getInstance().world.getTileEntity(pos);
//		if (!(tileEntity instanceof UnitTileEntity)) return;
//		UnitTileEntity te = (UnitTileEntity) tileEntity;
		if (checkClient(ctx.get())) {
			UnitTileEntity te = SUCapabilityManager.getUnitAtBlock(ClientUtils.getWorld(), pos);
			if (te == null) return;
			te.getFakeWorld().playEvent(type, smallBlockPos, data);
		}
	}
}
