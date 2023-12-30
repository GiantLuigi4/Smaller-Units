package tfc.smallerunits.data.access;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface PacketListenerAccessor {
	void setWorld(Level lvl);
	
	Player getPlayer();
}
