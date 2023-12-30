package tfc.smallerunits.mixin.core.access;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tfc.smallerunits.data.access.PacketListenerAccessor;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin implements PacketListenerAccessor {
	@Shadow
	private ClientLevel level;
	
	@Override
	public void setWorld(Level lvl) {
		this.level = (ClientLevel) lvl;
		Minecraft.getInstance().player.level = lvl;
		Minecraft.getInstance().player.clientLevel = (ClientLevel) lvl;
		Minecraft.getInstance().level = (ClientLevel) lvl;
	}
	
	@Override
	public Player getPlayer() {
		return Minecraft.getInstance().player;
	}
}
