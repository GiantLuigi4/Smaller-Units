//package tfc.smallerunits.simulation.level.client;
//
//import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
//import it.unimi.dsi.fastutil.objects.ObjectIterator;
//import net.minecraft.client.multiplayer.ClientLevel;
//import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
//import net.minecraft.client.player.LocalPlayer;
//import net.minecraft.core.BlockPos;
//import net.minecraft.world.level.block.state.BlockState;
//import net.minecraft.world.phys.Vec3;
//
//import java.util.Map;
//
//public class BigWorldPredictionHandler extends BlockStatePredictionHandler {
//	private final Object2ObjectOpenHashMap<BlockPos, ServerVerifiedState> serverVerifiedStates = new Object2ObjectOpenHashMap();
//
//	public BigWorldPredictionHandler() {
//	}
//
//	@Override
//	public void retainKnownServerState(BlockPos pos, BlockState state, LocalPlayer player) {
//		int seq = currentSequence();
//		// cleaner than compute... probably performs better as well
//		// lambdas have overhead, and compute ends up removing the entry to then re-add it
//		// in this scenario, that ends up being this logic, but move the put to the end, after the if
//		ServerVerifiedState serverState = this.serverVerifiedStates.get(pos);
//		if (serverState == null)
//			serverVerifiedStates.put(pos, new ServerVerifiedState(seq, player.position()));
//		else serverState.setSequenceIndex(seq);
//	}
//
//	@Override
//	public boolean updateKnownServerState(BlockPos pos, BlockState state) {
//		ServerVerifiedState serverVerifiedState = this.serverVerifiedStates.get(pos);
//		if (serverVerifiedState == null) {
//			return false;
//		} else {
//			serverVerifiedState.setBlockState(state);
//			return true;
//		}
//	}
//
//	@Override
//	public void endPredictionsUpTo(int i, ClientLevel level) {
//		ObjectIterator<Map.Entry<BlockPos, ServerVerifiedState>> iterator = serverVerifiedStates.entrySet().iterator();
//		while (iterator.hasNext()) {
//			Map.Entry<BlockPos, ServerVerifiedState> entry = iterator.next();
//			ServerVerifiedState state = entry.getValue();
//			if (state == null || state.blockState == null) {
//				iterator.remove();
//				continue;
//			}
//
//			if (state.sequenceIndex <= i) {
//				level.syncBlockState(entry.getKey(), state.blockState, state.playerPos);
//				iterator.remove();
//			}
//		}
//	}
//
//	static class ServerVerifiedState {
//		int sequenceIndex;
//		final Vec3 playerPos;
//		BlockState blockState;
//
//		public ServerVerifiedState(int sequenceIndex, Vec3 playerPos) {
//			this.sequenceIndex = sequenceIndex;
//			this.playerPos = playerPos;
//		}
//
//		public ServerVerifiedState setSequenceIndex(int sequenceIndex) {
//			this.sequenceIndex = sequenceIndex;
//			return this;
//		}
//
//		void setBlockState(BlockState blockState) {
//			this.blockState = blockState;
//		}
//	}
//}
