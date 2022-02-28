package tfc.smallerunits.networking.screens;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import tfc.smallerunits.helpers.ClientUtils;
import tfc.smallerunits.networking.util.Packet;

import java.util.function.Supplier;

public class SOpenLittleSignPacket extends Packet {
	private BlockPos realPos;
	private BlockPos pos;
	
	public SOpenLittleSignPacket(PacketBuffer buffer) {
		super(buffer);
	}
	
	public SOpenLittleSignPacket(BlockPos realPos, BlockPos pos) {
		this.realPos = realPos;
		this.pos = pos;
	}
	
	@Override
	public void readPacketData(PacketBuffer buf) {
		realPos = buf.readBlockPos();
		pos = buf.readBlockPos();
	}
	
	@Override
	public void writePacketData(PacketBuffer buf) {
		buf.writeBlockPos(realPos);
		buf.writeBlockPos(pos);
	}
	
	@Override
	public void handle(Supplier<NetworkEvent.Context> ctx) {
		if (checkClient(ctx.get())) {
			// java classloading is pain, so I'mma just offload this whole thing
			ClientUtils.openSign(realPos, pos);
		}
	}
}
