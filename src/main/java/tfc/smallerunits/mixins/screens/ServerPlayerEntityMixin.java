package tfc.smallerunits.mixins.screens;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.CommandBlockTileEntity;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.Smallerunits;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.helpers.PacketHacksHelper;
import tfc.smallerunits.networking.SLittleTileEntityUpdatePacket;
import tfc.smallerunits.networking.screens.SOpenLittleSignPacket;
import tfc.smallerunits.utils.world.server.FakeServerWorld;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
	@Inject(at = @At("HEAD"), method = "openCommandBlock", cancellable = true)
	public void preOpenCommandBlock(CommandBlockTileEntity tile, CallbackInfo ci) {
		if (tile.getWorld() instanceof FakeServerWorld) {
			tile.setSendToClient(true);
			
			SUpdateTileEntityPacket packet = tile.getUpdatePacket();
			if (packet == null) return;
			UnitTileEntity owner = ((FakeServerWorld) tile.getWorld()).owner;
			BlockPos packetPos = PacketHacksHelper.unitPos;
			PacketHacksHelper.unitPos = null;
			SLittleTileEntityUpdatePacket packet1 = new SLittleTileEntityUpdatePacket(owner.getPos(), tile.getPos(), packet.tileEntityType, packet.nbt);
			Smallerunits.NETWORK_INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) (Object) this), packet1);
			PacketHacksHelper.unitPos = packetPos;
			ci.cancel();
		}
	}
	
	@Inject(at = @At("HEAD"), method = "openSignEditor", cancellable = true)
	public void preOpenSignEditor(SignTileEntity tile, CallbackInfo ci) {
		if (tile.getWorld() instanceof FakeServerWorld) {
			tile.setPlayer((ServerPlayerEntity) (Object) this);
			
			UnitTileEntity owner = ((FakeServerWorld) tile.getWorld()).owner;
			BlockPos packetPos = PacketHacksHelper.unitPos;
			PacketHacksHelper.unitPos = null;
			Smallerunits.NETWORK_INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) (Object) this), new SOpenLittleSignPacket(owner.getPos(), tile.getPos()));
			PacketHacksHelper.unitPos = packetPos;
			ci.cancel();
		}
	}
}
