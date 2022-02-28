package tfc.smallerunits.networking;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.helpers.ClientUtils;
import tfc.smallerunits.networking.util.Packet;
import tfc.smallerunits.utils.data.SUCapabilityManager;

import java.util.UUID;
import java.util.function.Supplier;

public class SLittleEntityStatusPacket extends Packet {
	// TODO: change to array list
	BlockPos updatingUnit;
	boolean createEntity;
	CompoundNBT nbt;
	
	public SLittleEntityStatusPacket(BlockPos updatingUnit, Entity entity) {
		this.updatingUnit = updatingUnit;
		this.createEntity = !entity.removed;
		nbt = new CompoundNBT();
		nbt.putString("id", entity.getType().getRegistryName().toString());
		nbt.putUniqueId("uuid", entity.getUniqueID());
		if (createEntity) nbt.put("tag", entity.serializeNBT());
	}
	
	public SLittleEntityStatusPacket(PacketBuffer buffer) {
		super(buffer);
	}
	
	@Override
	public void readPacketData(PacketBuffer buf) {
		updatingUnit = buf.readBlockPos();
		createEntity = buf.readBoolean();
		nbt = buf.readCompoundTag();
	}
	
	@Override
	public void writePacketData(PacketBuffer buf) {
		buf.writeBlockPos(updatingUnit);
		buf.writeBoolean(createEntity);
		buf.writeCompoundTag(nbt);
	}
	
	@Override
	public void handle(Supplier<NetworkEvent.Context> ctx) {
		if (checkClient(ctx.get())) {
//			BlockState state = Minecraft.getInstance().world.getBlockState(updatingUnit);
//			if (!(state.getBlock() instanceof SmallerUnitBlock)) return;
//			TileEntity te = Minecraft.getInstance().world.getTileEntity(updatingUnit);
//			if (!(te instanceof UnitTileEntity)) return;
//			UnitTileEntity tileEntity = (UnitTileEntity) te;
			UnitTileEntity tileEntity = SUCapabilityManager.getUnitAtBlock(ClientUtils.getWorld(), updatingUnit);
			if (tileEntity == null) return;
			
			if (createEntity) {
				ResourceLocation id = new ResourceLocation(nbt.getString("id"));
				CompoundNBT data = nbt.getCompound("tag");
				
				Entity e = ForgeRegistries.ENTITIES.getValue(id).create(tileEntity.getFakeWorld());
				e.deserializeNBT(data);
				
				tileEntity.getFakeWorld().addEntity(e);
			} else {
				UUID uuid = nbt.getUniqueId("uuid");
				for (Entity value : tileEntity.getEntitiesById().values()) {
					if (value.getUniqueID().equals(uuid)) {
						value.remove();
					}
				}
			}
		}
	}
	
	public SLittleEntityStatusPacket markRemoval() {
		createEntity = false;
		return this;
	}
}
