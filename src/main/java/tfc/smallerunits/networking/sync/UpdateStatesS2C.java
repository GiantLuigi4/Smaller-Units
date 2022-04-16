package tfc.smallerunits.networking.sync;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraftforge.network.NetworkEvent;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.data.storage.RegionPos;
import tfc.smallerunits.data.storage.UnitPallet;
import tfc.smallerunits.data.tracking.RegionalAttachments;
import tfc.smallerunits.networking.Packet;
import tfc.smallerunits.simulation.world.server.TickerServerWorld;

import java.util.ArrayList;
import java.util.HashMap;

public class UpdateStatesS2C extends Packet {
	UnitPallet pallet;
	RegionPos realPos;
	int upb;
	//	BlockPos rwp;
	ChunkPos cp;
	int cy;
	
	public UpdateStatesS2C(/*BlockPos rwp, */RegionPos pos, ArrayList<Pair<BlockPos, BlockState>> states, int upb, ChunkPos cp, int cy) {
//		this.rwp = rwp;
		pallet = new UnitPallet();
		states.forEach(pallet::put);
		realPos = pos;
		this.upb = upb;
		this.cp = cp;
		this.cy = cy;
	}
	
	public UpdateStatesS2C(FriendlyByteBuf buf) {
		super(buf);
//		rwp = buf.readBlockPos();
		upb = buf.readInt();
		realPos = new RegionPos(buf.readInt(), buf.readInt(), buf.readInt());
		pallet = UnitPallet.fromNBT(buf.readNbt());
		cp = new ChunkPos(buf.readIntLE(), buf.readInt());
		cy = buf.readInt();
	}
	
	@Override
	public void write(FriendlyByteBuf buf) {
		super.write(buf);
//		buf.writeBlockPos(rwp);
		buf.writeInt(upb);
		// I care about vertically
		buf.writeInt(realPos.x);
		buf.writeInt(realPos.y);
		buf.writeInt(realPos.z);
		buf.writeNbt(pallet.toNBT());
		buf.writeIntLE(cp.x); // idk lol
		buf.writeInt(cp.z);
		buf.writeInt(cy);
	}
	
	@Override
	public void handle(NetworkEvent.Context ctx) {
		if (checkClient(ctx)) {
			Region r = ((RegionalAttachments) Minecraft.getInstance().level).SU$getRegion(realPos);
			Level lvl = r.getClientWorld(Minecraft.getInstance().level, upb);
			r.getClientWorld(Minecraft.getInstance().level, upb);
//			ChunkAccess access = Minecraft.getInstance().level.getChunk(rwp);
//			if (access instanceof EmptyLevelChunk) return;
//			if (!(access instanceof LevelChunk chunk)) return;
//			ISUCapability cap = SUCapabilityManager.getCapability(chunk);
//			UnitSpace space = cap.getOrMakeUnit(rwp);
			
			BlockState[] states = new BlockState[16 * 16 * 16];
			pallet.acceptStates(states, false);
			HashMap<ChunkPos, ChunkAccess> accessHashMap = new HashMap<>();
			ArrayList<BlockPos> placesBlocks = new ArrayList<>();
			Minecraft.getInstance().getProfiler().push("sync");
			for (int x = 0; x < 16; x++) {
				for (int y = 0; y < 16; y++) {
					for (int z = 0; z < 16; z++) {
						int indx = ((x * 16) + y) * 16 + z;
						if (states[indx] == null) continue;
//						space.setFast(x, y, z, states[indx]);
						((TickerServerWorld) lvl).setFromSync(cp, cy, x, y, z, states[indx], accessHashMap, placesBlocks);
					}
				}
			}
			Minecraft.getInstance().getProfiler().pop();

//			((SUCapableChunk) chunk).SU$markDirty(realPos);
			
			ctx.setPacketHandled(true);
		}
	}
}
