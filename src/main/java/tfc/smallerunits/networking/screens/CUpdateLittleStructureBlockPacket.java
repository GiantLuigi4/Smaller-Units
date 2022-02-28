package tfc.smallerunits.networking.screens;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.properties.StructureMode;
import net.minecraft.tileentity.StructureBlockTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.networking.util.Packet;
import tfc.smallerunits.utils.data.SUCapabilityManager;
import tfc.smallerunits.utils.world.server.FakeServerWorld;

import java.util.function.Supplier;

public class CUpdateLittleStructureBlockPacket extends Packet {
	private BlockPos realPos;
	private BlockPos pos;
	private StructureBlockTileEntity.UpdateCommand field_210392_b;
	private StructureMode mode;
	private String name;
	private BlockPos offset;
	private BlockPos size;
	private Mirror mirror;
	private Rotation rotation;
	private String field_210399_i;
	private boolean ignoreEntities;
	private boolean showAir;
	private boolean showBoundingBox;
	private float integrity;
	private long seed;
	
	public CUpdateLittleStructureBlockPacket(BlockPos realPos, BlockPos pos, StructureBlockTileEntity.UpdateCommand cmd, StructureMode mode, String name, BlockPos p_i49541_5_, BlockPos size, Mirror mirror, Rotation rotation, String p_i49541_9_, boolean p_i49541_10_, boolean p_i49541_11_, boolean p_i49541_12_, float integrity, long seed) {
		this.realPos = realPos;
		this.pos = pos;
		this.field_210392_b = cmd;
		this.mode = mode;
		this.name = name;
		this.offset = p_i49541_5_;
		this.size = size;
		this.mirror = mirror;
		this.rotation = rotation;
		this.field_210399_i = p_i49541_9_;
		this.ignoreEntities = p_i49541_10_;
		this.showAir = p_i49541_11_;
		this.showBoundingBox = p_i49541_12_;
		this.integrity = integrity;
		this.seed = seed;
	}
	
	public CUpdateLittleStructureBlockPacket(PacketBuffer buffer) {
		super(buffer);
	}
	
	@Override
	public void readPacketData(PacketBuffer buffer) {
		this.realPos = buffer.readBlockPos();
		this.pos = buffer.readBlockPos();
		this.field_210392_b = buffer.readEnumValue(StructureBlockTileEntity.UpdateCommand.class);
		this.mode = buffer.readEnumValue(StructureMode.class);
		this.name = buffer.readString(32767);
		this.offset = new BlockPos(MathHelper.clamp(buffer.readByte(), -48, 48), MathHelper.clamp(buffer.readByte(), -48, 48), MathHelper.clamp(buffer.readByte(), -48, 48));
		this.size = new BlockPos(MathHelper.clamp(buffer.readByte(), 0, 48), MathHelper.clamp(buffer.readByte(), 0, 48), MathHelper.clamp(buffer.readByte(), 0, 48));
		this.mirror = buffer.readEnumValue(Mirror.class);
		this.rotation = buffer.readEnumValue(Rotation.class);
		this.field_210399_i = buffer.readString(12);
		this.integrity = MathHelper.clamp(buffer.readFloat(), 0.0F, 1.0F);
		this.seed = buffer.readVarLong();
		int settings = buffer.readByte();
		this.ignoreEntities = (settings & 1) != 0;
		this.showAir = (settings & 2) != 0;
		this.showBoundingBox = (settings & 4) != 0;
	}
	
	@Override
	public void writePacketData(PacketBuffer buffer) {
		buffer.writeBlockPos(this.realPos);
		buffer.writeBlockPos(this.pos);
		buffer.writeEnumValue(this.field_210392_b);
		buffer.writeEnumValue(this.mode);
		buffer.writeString(this.name);
		buffer.writeByte(this.offset.getX());
		buffer.writeByte(this.offset.getY());
		buffer.writeByte(this.offset.getZ());
		buffer.writeByte(this.size.getX());
		buffer.writeByte(this.size.getY());
		buffer.writeByte(this.size.getZ());
		buffer.writeEnumValue(this.mirror);
		buffer.writeEnumValue(this.rotation);
		buffer.writeString(this.field_210399_i);
		buffer.writeFloat(this.integrity);
		buffer.writeVarLong(this.seed);
		int flags = 0;
		if (this.ignoreEntities) flags |= 1;
		if (this.showAir) flags |= 2;
		if (this.showBoundingBox) flags |= 4;
		buffer.writeByte(flags);
	}
	
	@Override
	public void handle(Supplier<NetworkEvent.Context> ctx) {
		if (checkClient(ctx.get())) return;
		PlayerEntity entity = ctx.get().getSender();
		if (entity.canUseCommandBlock()) {
			BlockPos blockpos = pos;
			
			UnitTileEntity tileEntity = SUCapabilityManager.getUnitAtBlock(entity.getEntityWorld(), realPos);
			if (tileEntity == null) return;
			
			BlockState blockstate = tileEntity.getFakeWorld().getBlockState(blockpos);
			TileEntity tileentity = tileEntity.getFakeWorld().getTileEntity(blockpos);
			if (tileentity instanceof StructureBlockTileEntity) {
				StructureBlockTileEntity structureblocktileentity = (StructureBlockTileEntity) tileentity;
				
				structureblocktileentity.setMode(mode);
				structureblocktileentity.setName(name);
				structureblocktileentity.setPosition(offset);
				structureblocktileentity.setSize(size);
				structureblocktileentity.setMirror(mirror);
				structureblocktileentity.setRotation(rotation);
				structureblocktileentity.setMetadata(field_210399_i);
				structureblocktileentity.setIgnoresEntities(ignoreEntities);
				structureblocktileentity.setShowAir(showAir);
				structureblocktileentity.setShowBoundingBox(showBoundingBox);
				structureblocktileentity.setIntegrity(integrity);
				structureblocktileentity.setSeed(seed);
				
				if (structureblocktileentity.hasName()) {
					String s = structureblocktileentity.getName();
					if (field_210392_b == StructureBlockTileEntity.UpdateCommand.SAVE_AREA) {
						if (structureblocktileentity.save())
							entity.sendStatusMessage(new TranslationTextComponent("structure_block.save_success", s), false);
						else
							entity.sendStatusMessage(new TranslationTextComponent("structure_block.save_failure", s), false);
					} else if (field_210392_b == StructureBlockTileEntity.UpdateCommand.LOAD_AREA) {
						if (!structureblocktileentity.isStructureLoadable())
							entity.sendStatusMessage(new TranslationTextComponent("structure_block.load_not_found", s), false);
						else if (structureblocktileentity.func_242687_a((FakeServerWorld) tileEntity.getWorld()))
							entity.sendStatusMessage(new TranslationTextComponent("structure_block.load_success", s), false);
						else
							entity.sendStatusMessage(new TranslationTextComponent("structure_block.load_prepare", s), false);
					} else if (field_210392_b == StructureBlockTileEntity.UpdateCommand.SCAN_AREA) {
						if (structureblocktileentity.detectSize())
							entity.sendStatusMessage(new TranslationTextComponent("structure_block.size_success", s), false);
						else
							entity.sendStatusMessage(new TranslationTextComponent("structure_block.size_failure"), false);
					}
				} else
					entity.sendStatusMessage(new TranslationTextComponent("structure_block.invalid_structure_name", name), false);
				
				structureblocktileentity.markDirty();
				tileEntity.getFakeWorld().notifyBlockUpdate(blockpos, blockstate, blockstate, 3);
			}
		}
		
		ctx.get().setPacketHandled(true);
	}
}
