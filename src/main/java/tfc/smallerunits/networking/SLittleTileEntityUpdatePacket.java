package tfc.smallerunits.networking;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.INetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.helpers.ClientUtils;
import tfc.smallerunits.utils.data.SUCapabilityManager;

import java.util.function.Supplier;

public class SLittleTileEntityUpdatePacket implements IPacket {
	// TODO: convert to ArrayList
	private BlockPos updatingPos;
	private BlockPos blockPos;
	/**
	 * Used only for vanilla tile entities
	 */
	private int tileEntityType;
	private CompoundNBT nbt;
	
	public SLittleTileEntityUpdatePacket(BlockPos updatingPos, BlockPos blockPos, int tileEntityType, CompoundNBT nbt) {
		this.updatingPos = updatingPos;
		this.blockPos = blockPos;
		this.tileEntityType = tileEntityType;
		this.nbt = nbt;
	}
	
	public SLittleTileEntityUpdatePacket(PacketBuffer buffer) {
		readPacketData(buffer);
	}
	
	@Override
	public void readPacketData(PacketBuffer buf) {
		updatingPos = buf.readBlockPos();
		blockPos = buf.readBlockPos();
		tileEntityType = buf.readInt();
		nbt = buf.readCompoundTag();
	}
	
	@Override
	public void writePacketData(PacketBuffer buf) {
		buf.writeBlockPos(updatingPos);
		buf.writeBlockPos(blockPos);
		buf.writeInt(tileEntityType);
		buf.writeCompoundTag(nbt);
	}
	
	@Override
	public void processPacket(INetHandler handler) {
	}
	
	public void handle(Supplier<NetworkEvent.Context> ctx) {
		if (ctx.get().getDirection().getReceptionSide().isClient()) {
//			BlockState state = Minecraft.getInstance().world.getBlockState(updatingPos);
//			if (!(state.getBlock() instanceof SmallerUnitBlock)) return;
//			TileEntity te = Minecraft.getInstance().world.getTileEntity(updatingPos);
//			if (!(te instanceof UnitTileEntity)) return;
//			UnitTileEntity tileEntity = (UnitTileEntity) te;
			UnitTileEntity tileEntity = SUCapabilityManager.getUnitAtBlock(ClientUtils.getWorld(), blockPos);
			if (tileEntity == null) return;
			
			TileEntity te1 = tileEntity.getFakeWorld().getTileEntity(blockPos);
			if (te1 == null) return;
			te1.handleUpdateTag(tileEntity.getFakeWorld().getBlockState(blockPos), nbt);
			te1.onDataPacket(ctx.get().getNetworkManager(), new SUpdateTileEntityPacket(blockPos, tileEntityType, nbt));
			te1.setWorldAndPos(tileEntity.getFakeWorld(), blockPos);
		}
	}
}
