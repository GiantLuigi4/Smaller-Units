package tfc.smallerunits.data.access;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import tfc.smallerunits.UnitSpace;
import tfc.smallerunits.data.storage.RegionPos;
import tfc.smallerunits.utils.PositionalInfo;

public interface SUScreenAttachments {
	void update(Player player);
	
	void setup(PositionalInfo info, UnitSpace unit);
	
	void setup(PositionalInfo info, Level targetLevel, int upb, RegionPos regionPos);
	
	PositionalInfo getPositionalInfo();
	
	Level getTarget();
	
	int getUpb();
	
	RegionPos regionPos();
}
