package tfc.smallerunits.mixin.core.access;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tfc.smallerunits.data.access.PacketListenerAccessor;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerPacketListenerMixin implements PacketListenerAccessor {
	@Shadow
	public ServerPlayer player;
	
	@Override
	public void setWorld(Level lvl) {
		this.player.setLevel((ServerLevel) lvl);
	}
	
	@Override
	public Player getPlayer() {
		return player;
	}
}
