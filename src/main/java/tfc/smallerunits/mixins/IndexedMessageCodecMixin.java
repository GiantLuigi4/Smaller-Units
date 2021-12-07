package tfc.smallerunits.mixins;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.simple.IndexedMessageCodec;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.config.SmallerUnitsConfig;
import tfc.smallerunits.helpers.ClientUtils;
import tfc.smallerunits.helpers.PacketHacksHelper;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Mixin(targets = "net.minecraftforge.fml.network.simple.IndexedMessageCodec$MessageHandler")
public class IndexedMessageCodecMixin<MSG> {
	@Shadow
	@Mutable
	@Final
	private Optional<BiConsumer<MSG, PacketBuffer>> encoder;
	@Shadow
	@Mutable
	@Final
	private Optional<Function<PacketBuffer, MSG>> decoder;
	@Shadow
	@Mutable
	@Final
	private BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer;
	
	@Inject(at = @At("TAIL"), method = "<init>")
	public void postInit(IndexedMessageCodec outer, int index, Class<MSG> messageType, BiConsumer<MSG, PacketBuffer> encoder, Function<PacketBuffer, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer, Optional<NetworkDirection> networkDirection, CallbackInfo ci) {
		if (!messageType.toString().contains("OpenContainer")) {
			if (messageType == null || messageType.toString().startsWith("class net.minecraftforge.fml")) return;
		}
		
		try {
			Optional<BiConsumer<MSG, PacketBuffer>> oldEncoder = this.encoder;
			Optional<Function<PacketBuffer, MSG>> oldDecoder = this.decoder;
			if (!oldDecoder.isPresent() || !oldEncoder.isPresent()) return;
			
			Optional<BiConsumer<MSG, PacketBuffer>> newEncoder = Optional.of((msg, buffer) -> SmallerUnits_packetEncode(oldEncoder, msg, buffer));
			
			AtomicReference<Boolean> isPosPresent = new AtomicReference<>();
			AtomicReference<BlockPos> posIfPresent = new AtomicReference<>();
			Optional<Function<PacketBuffer, MSG>> newDecoder = Optional.of((buffer) -> SmallerUnits_packetDecode(oldDecoder, buffer, isPosPresent, posIfPresent));
			
			BiConsumer<MSG, Supplier<NetworkEvent.Context>> oldMessageConsumer = this.messageConsumer;
			BiConsumer<MSG, Supplier<NetworkEvent.Context>> newMessageConsumer = (msg, context) -> SmallerUnits_consumePacket(oldMessageConsumer, msg, context, isPosPresent, posIfPresent);
			
			this.encoder = newEncoder;
			this.decoder = newDecoder;
			this.messageConsumer = newMessageConsumer;
		} catch (Throwable ignored) {
		}
	}
	
	public void SmallerUnits_packetEncode(Optional<BiConsumer<MSG, PacketBuffer>> oldEncoder, MSG msg, PacketBuffer buffer) {
		if (!SmallerUnitsConfig.SERVER.usePacketHandlerHacks.get()) {
			oldEncoder.ifPresent((consumer) -> consumer.accept(msg, buffer));
			return;
		}
		
		buffer.writeBoolean(PacketHacksHelper.getPosForPacket(msg) != null);
		if (PacketHacksHelper.getPosForPacket(msg) != null) {
			buffer.writeBlockPos(PacketHacksHelper.getPosForPacket(msg));
		}
		
		oldEncoder.ifPresent(msgPacketBufferBiConsumer -> msgPacketBufferBiConsumer.accept(msg, buffer));
		PacketHacksHelper.markPacketDone(msg);
	}
	
	public MSG SmallerUnits_packetDecode(Optional<Function<PacketBuffer, MSG>> oldDecoder, PacketBuffer buffer, AtomicReference<Boolean> isPosPresent, AtomicReference<BlockPos> posIfPresent) {
		if (!SmallerUnitsConfig.SERVER.usePacketHandlerHacks.get()) {
			MSG msg = null;
			if (oldDecoder.isPresent()) msg = oldDecoder.get().apply(buffer);
			return msg;
		}
		MSG msg = null;
		
		isPosPresent.set(buffer.readBoolean());
		if (isPosPresent.get()) {
			posIfPresent.set(buffer.readBlockPos());
		}
		
		if (oldDecoder.isPresent()) {
			msg = oldDecoder.get().apply(buffer);
		}
		return msg;
	}
	
	public void SmallerUnits_consumePacket(BiConsumer<MSG, Supplier<NetworkEvent.Context>> oldMessageConsumer, MSG msg, Supplier<NetworkEvent.Context> context, AtomicReference<Boolean> isPosPresent, AtomicReference<BlockPos> posIfPresent) {
		if (!SmallerUnitsConfig.SERVER.usePacketHandlerHacks.get()) {
			oldMessageConsumer.accept(msg, context);
			return;
		}
		if (isPosPresent.get()) {
			PlayerEntity playerEntity;
			if (context.get().getDirection().getOriginationSide().isServer())
				playerEntity = ClientUtils.getPlayer();
			else playerEntity = context.get().getSender();
			World world = playerEntity.getEntityWorld();
			TileEntity te = playerEntity.world.getTileEntity(posIfPresent.get());
			if (!(te instanceof UnitTileEntity)) {
				oldMessageConsumer.accept(msg, context);
				return;
			}
			playerEntity.setWorld(((UnitTileEntity) te).getFakeWorld());
			if (context.get().getDirection().getOriginationSide().isServer()) {
				ClientUtils.setWorld(((UnitTileEntity) te).getFakeWorld());
			}
			oldMessageConsumer.accept(msg, context);
			if (context.get().getDirection().getOriginationSide().isServer()) {
				ClientUtils.setWorld(world);
			}
			playerEntity.setWorld(world);
		} else {
			oldMessageConsumer.accept(msg, context);
		}
	}
}
