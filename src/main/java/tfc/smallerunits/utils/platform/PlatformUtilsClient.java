package tfc.smallerunits.utils.platform;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import tfc.smallerunits.simulation.level.client.FakeClientLevel;

public class PlatformUtilsClient {
	public static void postTick(FakeClientLevel fakeClientLevel) {
//		ClientTickEvents.END_WORLD_TICK.invoker().onEndTick(fakeClientLevel);
	}
	
	public static void preTick(FakeClientLevel fakeClientLevel) {
//		ClientTickEvents.START_WORLD_TICK.invoker().onStartTick(fakeClientLevel);
	}
	
	public static boolean checkRenderLayer(FluidState fluid, RenderType chunkBufferLayer) {
		return ItemBlockRenderTypes.getRenderLayer(fluid).equals(chunkBufferLayer);
	}
	
	public static boolean checkRenderLayer(BlockState state, RenderType chunkBufferLayer) {
		return ItemBlockRenderTypes.getChunkRenderType(state).equals(chunkBufferLayer);
	}
	
	public static void onLoad(FakeClientLevel fakeClientLevel) {
		// no-op?
	}
	
	public static void handlePacketClient(ClientGamePacketListener packetListener, ClientboundCustomPayloadPacket clientboundCustomPayloadPacket) {
		clientboundCustomPayloadPacket.handle(packetListener);
	}
	
	public static void recieveBeData(BlockEntity be, CompoundTag tag) {
		be.load(tag);
	}
}
