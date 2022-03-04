package tfc.smallerunits.networking.tracking;

import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import tfc.smallerunits.api.placement.UnitPos;
import tfc.smallerunits.helpers.ClientUtils;
import tfc.smallerunits.networking.util.Packet;
import tfc.smallerunits.utils.accessor.SUTracked;
import tfc.smallerunits.utils.tracking.PlayerDataManager;
import tfc.smallerunits.utils.tracking.data.SUDataTracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Supplier;

public class SSyncSUData extends Packet {
	public static final HashMap<BlockPos, ArrayList<DestroyBlockProgress>> suBreakingProgress = new HashMap<>();
	public static final HashMap<UUID, BlockPos> suMiningPoses = new HashMap<>();
	CompoundNBT nbt;
	UUID uniqueID;
	
	public SSyncSUData(PacketBuffer buffer) {
		super(buffer);
	}
	
	public SSyncSUData(CompoundNBT nbt, UUID uniqueID) {
		super();
		this.nbt = nbt;
		this.uniqueID = uniqueID;
	}
	
	@Override
	public void readPacketData(PacketBuffer buf) {
		super.readPacketData(buf);
		this.nbt = buf.readCompoundTag();
		this.uniqueID = buf.readUniqueId();
	}
	
	@Override
	public void writePacketData(PacketBuffer buf) {
		super.writePacketData(buf);
		buf.writeCompoundTag(nbt);
		buf.writeUniqueId(uniqueID);
	}
	
	@Override
	public void handle(Supplier<NetworkEvent.Context> ctx) {
		super.handle(ctx);
		
		if (checkClient(ctx.get())) {
			for (PlayerEntity player : ClientUtils.getWorld().getPlayers()) {
				// single player break progress should not be handled in networking code
				if (player.equals(ClientUtils.getPlayer())) continue;
				if (player.getUniqueID().equals(uniqueID)) {
					SUDataTracker tracker = ((SUTracked) player).SmallerUnits_getTracker();
					tracker.deserialize(nbt);
					int progress = tracker.get(PlayerDataManager.PROGRESS);
					BlockPos p0 = tracker.get(PlayerDataManager.POS0);
					BlockPos p1 = tracker.get(PlayerDataManager.POS1);
					ArrayList<DestroyBlockProgress> set;
					if (suBreakingProgress.containsKey(p0)) {
//						System.out.println(suBreakingProgress);
						set = suBreakingProgress.get(p0);
						int i;
						if (!set.isEmpty()) {
							for (i = 0; i < set.size() + 1; i++) {
								if (i > set.size()) {
									i = -1;
									break;
								}
								if (set.get(i).miningPlayerEntId == player.getEntityId()) break;
							}
							if (i != -1) set.remove(i);
						}
					} else {
						set = new ArrayList<>();
						suBreakingProgress.put(p0, set);
					}
					DestroyBlockProgress prog = new DestroyBlockProgress(
							player.getEntityId(), new UnitPos(p0, p1, 0)
					);
					prog.setPartialBlockDamage(progress);
					int i;
//					if (!set.isEmpty()) {
//						for (i = 0; i < set.size() + 1; i++) {
//							if (i > set.size()) {
//								i = -1;
//								break;
//							}
//							if (set.get(i).miningPlayerEntId == player.getEntityId()) break;
//						}
//						if (i != -1) set.remove(i);
//					}
					if (progress > -1) set.add(prog);
					if (set.isEmpty()) suBreakingProgress.remove(p0);
				}
			}
			
			{
				ArrayList<DestroyBlockProgress>[] progresses = (ArrayList<DestroyBlockProgress>[]) suBreakingProgress.values().toArray(new ArrayList[0]);
				ArrayList<Integer> toRemoveA = new ArrayList<>();
				for (int i1 = 0; i1 < progresses.length; i1++) {
					ArrayList<DestroyBlockProgress> set = progresses[i1];
					ArrayList<Integer> toRemove = new ArrayList<>();
					for (int i = 0; i < set.size(); i++) {
						if (ClientUtils.getWorld().getEntityByID(set.get(i).miningPlayerEntId) == null) {
							toRemove.add(i);
						} else {
							DestroyBlockProgress progress = set.get(i);
							SUDataTracker tracker = ((SUTracked) ClientUtils.getWorld().getEntityByID(progress.miningPlayerEntId)).SmallerUnits_getTracker();
							if (tracker.get(PlayerDataManager.PROGRESS) == -1) {
								toRemove.add(i);
								continue;
							}
							UnitPos up = (UnitPos) progress.getPosition();
							if (!tracker.get(PlayerDataManager.POS0).equals(up)) toRemove.add(i);
							else if (!tracker.get(PlayerDataManager.POS1).equals(up.realPos)) toRemove.add(i);
						}
					}
					int countRemoved = 0;
					for (Integer integer : toRemove) set.remove(integer - (countRemoved++));
					if (set.isEmpty()) toRemoveA.add(i1);
				}
				BlockPos[] javaWhy = suBreakingProgress.keySet().toArray(new BlockPos[0]);
				for (Integer integer : toRemoveA) suBreakingProgress.remove(javaWhy[integer]);
			}
		}
	}
}
