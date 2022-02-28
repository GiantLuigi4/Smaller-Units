package tfc.smallerunits.networking.screens;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.networking.util.Packet;
import tfc.smallerunits.utils.data.SUCapabilityManager;

import java.util.function.Supplier;

public class CUpdateLittleSignPacket extends Packet {
	private static final Logger LOGGER = LogManager.getLogger();
	private BlockPos realPos;
	private BlockPos pos;
	private String[] lines;
	
	public CUpdateLittleSignPacket(PacketBuffer buffer) {
		super(buffer);
	}
	
	public CUpdateLittleSignPacket(BlockPos realPos, BlockPos pos, String line0, String line1, String line3, String line4) {
		this.realPos = realPos;
		this.pos = pos;
		this.lines = new String[]{line0, line1, line3, line4};
	}
	
	public void readPacketData(PacketBuffer buffer) {
		this.realPos = buffer.readBlockPos();
		this.pos = buffer.readBlockPos();
		this.lines = new String[4];
		
		for (int i = 0; i < 4; ++i)
			this.lines[i] = buffer.readString(384);
	}
	
	public void writePacketData(PacketBuffer buffer) {
		buffer.writeBlockPos(this.realPos);
		buffer.writeBlockPos(this.pos);
		
		for (int i = 0; i < 4; ++i) buffer.writeString(this.lines[i]);
	}
	
	@Override
	public void handle(Supplier<NetworkEvent.Context> ctx) {
		if (checkServer(ctx.get())) {
			PlayerEntity player = ctx.get().getSender();
			UnitTileEntity tileEntity = SUCapabilityManager.getUnitAtBlock(player.getEntityWorld(), realPos);
			if (tileEntity == null) return;
			
			SignTileEntity sign;
			{
				TileEntity te = tileEntity.getTileEntity(pos);
				if (!(te instanceof SignTileEntity)) return;
				sign = (SignTileEntity) te;
			}
			
			if (!sign.getIsEditable())
				LOGGER.warn("Player {} just tried to change non-editable sign", player.getName().getString());
			else for (int i = 0; i < lines.length; i++)
				sign.setText(i, new StringTextComponent(lines[i]));
			
			sign.markDirty();
			BlockState state = sign.getBlockState();
			sign.getWorld().notifyBlockUpdate(pos, state, state, 3);
		}
	}
}
