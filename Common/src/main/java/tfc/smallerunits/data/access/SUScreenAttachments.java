package tfc.smallerunits.data.access;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import tfc.smallerunits.UnitSpace;
import tfc.smallerunits.networking.hackery.NetworkingHacks;
import tfc.smallerunits.utils.PositionalInfo;

public interface SUScreenAttachments {
	void update(Player player);
	
	void setup(SUScreenAttachments attachments);
	
	void setup(PositionalInfo info, UnitSpace unit);
	
	void setup(PositionalInfo info, Level targetLevel, NetworkingHacks.LevelDescriptor descriptor);
	
	PositionalInfo getPositionalInfo();
	
	Level getTarget();
	
	NetworkingHacks.LevelDescriptor getDescriptor();
}
