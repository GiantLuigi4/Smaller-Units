package tfc.smallerunits.networking.screens;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CommandBlockBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.CommandBlockLogic;
import net.minecraft.tileentity.CommandBlockTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.StringUtils;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.networking.util.Packet;
import tfc.smallerunits.utils.data.SUCapabilityManager;

import java.util.function.Supplier;

public class CUpdateLittleCommandBlockPacket extends Packet {
	private BlockPos realPos;
	private BlockPos pos;
	private String command;
	private boolean trackOutput;
	private boolean conditional;
	private boolean auto;
	private CommandBlockTileEntity.Mode mode;
	
	public CUpdateLittleCommandBlockPacket(PacketBuffer buffer) {
		super(buffer);
	}
	
	public CUpdateLittleCommandBlockPacket(BlockPos realPos, BlockPos pos, String cmd, CommandBlockTileEntity.Mode commandBlockMode, boolean trackOutput, boolean conditional, boolean automatic) {
		this.realPos = realPos;
		this.pos = pos;
		this.command = cmd;
		this.trackOutput = trackOutput;
		this.conditional = conditional;
		this.auto = automatic;
		this.mode = commandBlockMode;
	}
	
	@Override
	public void readPacketData(PacketBuffer buffer) {
		this.realPos = buffer.readBlockPos();
		this.pos = buffer.readBlockPos();
		this.command = buffer.readString(32767);
		this.mode = buffer.readEnumValue(CommandBlockTileEntity.Mode.class);
		int settings = buffer.readByte();
		this.trackOutput = (settings & 1) != 0;
		this.conditional = (settings & 2) != 0;
		this.auto = (settings & 4) != 0;
	}
	
	@Override
	public void writePacketData(PacketBuffer buffer) {
		buffer.writeBlockPos(this.realPos);
		buffer.writeBlockPos(this.pos);
		buffer.writeString(this.command);
		buffer.writeEnumValue(this.mode);
		int settings = 0;
		if (this.trackOutput) settings |= 1;
		if (this.conditional) settings |= 2;
		if (this.auto) settings |= 4;
		buffer.writeByte(settings);
	}
	
	@Override
	public void handle(Supplier<NetworkEvent.Context> ctx) {
		PlayerEntity player = ctx.get().getSender();
		
		if (!player.getServer().isCommandBlockEnabled()) {
			player.sendMessage(new TranslationTextComponent("advMode.notEnabled"), Util.DUMMY_UUID);
		} else if (!player.canUseCommandBlock()) {
			player.sendMessage(new TranslationTextComponent("advMode.notAllowed"), Util.DUMMY_UUID);
		} else {
			UnitTileEntity tileEntity = SUCapabilityManager.getUnitAtBlock(player.getEntityWorld(), realPos);
			if (tileEntity == null) return;
			
			CommandBlockTileEntity commandBlock;
			{
				TileEntity tile = tileEntity.getTileEntity(pos);
				if (!(tile instanceof CommandBlockTileEntity)) return;
				commandBlock = (CommandBlockTileEntity) tile;
			}
			
			CommandBlockLogic commandblocklogic = commandBlock.getCommandBlockLogic();
			
			String s = command;
			boolean flag = trackOutput;
			if (commandblocklogic != null) {
				CommandBlockTileEntity.Mode commandblocktileentity$mode = commandBlock.getMode();
				Direction direction = tileEntity.getFakeWorld().getBlockState(pos).get(CommandBlockBlock.FACING);
				switch (mode) {
					case SEQUENCE:
						BlockState blockstate1 = Blocks.CHAIN_COMMAND_BLOCK.getDefaultState();
						tileEntity.getFakeWorld().setBlockState(pos, blockstate1.with(CommandBlockBlock.FACING, direction).with(CommandBlockBlock.CONDITIONAL, conditional), 2);
						break;
					case AUTO:
						BlockState blockstate = Blocks.REPEATING_COMMAND_BLOCK.getDefaultState();
						tileEntity.getFakeWorld().setBlockState(pos, blockstate.with(CommandBlockBlock.FACING, direction).with(CommandBlockBlock.CONDITIONAL, conditional), 2);
						break;
					case REDSTONE:
					default:
						BlockState blockstate2 = Blocks.COMMAND_BLOCK.getDefaultState();
						tileEntity.getFakeWorld().setBlockState(pos, blockstate2.with(CommandBlockBlock.FACING, direction).with(CommandBlockBlock.CONDITIONAL, conditional), 2);
				}
				
				commandBlock.validate();
				tileEntity.getFakeWorld().setTileEntity(pos, commandBlock);
				commandblocklogic.setCommand(s);
				commandblocklogic.setTrackOutput(flag);
				if (!flag) {
					commandblocklogic.setLastOutput(null);
				}
				
				commandBlock.setAuto(auto);
				if (commandblocktileentity$mode != mode) {
					commandBlock.func_226987_h_();
				}
				
				commandblocklogic.updateCommand();
				if (!StringUtils.isNullOrEmpty(s)) {
					player.sendMessage(new TranslationTextComponent("advMode.setCommand.success", new Object[]{s}), Util.DUMMY_UUID);
				}
			}
		}
		ctx.get().setPacketHandled(true);
	}
}
