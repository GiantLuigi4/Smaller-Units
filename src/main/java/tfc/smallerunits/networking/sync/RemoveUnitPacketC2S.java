package tfc.smallerunits.networking.sync;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import tfc.smallerunits.UnitSpace;
import tfc.smallerunits.data.capability.ISUCapability;
import tfc.smallerunits.data.capability.SUCapabilityManager;
import tfc.smallerunits.networking.Packet;
import tfc.smallerunits.networking.platform.NetCtx;
import tfc.smallerunits.utils.platform.PlatformUtils;

public class RemoveUnitPacketC2S extends Packet {
	BlockPos position;
	
	public RemoveUnitPacketC2S(BlockPos position) {
		this.position = position;
	}
	
	public RemoveUnitPacketC2S(FriendlyByteBuf buf) {
		super(buf);
		this.position = buf.readBlockPos();
	}
	
	@Override
	public void write(FriendlyByteBuf buf) {
		super.write(buf);
		buf.writeBlockPos(position);
	}
	
	@Override
	public void handle(NetCtx ctx) {
		super.handle(ctx);
		if (checkServer(ctx)) {
			double reach = PlatformUtils.getReach(ctx.getSender());
			reach *= 1.1; // help account for lag
			reach += 1; // TODO: do this a bit better, helps account for player scaling
			Vec3 pos = ctx.getSender().getPosition(0);
			if (Math.sqrt(pos.distanceToSqr(position.getX() + 0.5, position.getY() + 0.5, position.getZ() + 0.5)) < reach) {
				ISUCapability cap = SUCapabilityManager.getCapability(ctx.getSender().getLevel().getChunkAt(position));
				UnitSpace space = cap.getUnit(position);
				if (space != null && space.isEmpty())
					ctx.getSender().getLevel().removeBlock(position, false);
				ctx.setPacketHandled(true);
			}
		}
	}
}
