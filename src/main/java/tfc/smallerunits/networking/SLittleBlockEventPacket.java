package tfc.smallerunits.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.network.INetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import tfc.smallerunits.block.UnitTileEntity;

public class SLittleBlockEventPacket implements IPacket {
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
	public void processPacket(INetHandler handler) {
		TileEntity tileEntity = Minecraft.getInstance().world.getTileEntity(pos);
		if (!(tileEntity instanceof UnitTileEntity)) return;
		UnitTileEntity te = (UnitTileEntity) tileEntity;
		te.worldClient.get().playEvent(type, smallBlockPos, data);
	}
}
