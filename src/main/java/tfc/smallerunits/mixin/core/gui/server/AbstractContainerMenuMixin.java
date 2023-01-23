package tfc.smallerunits.mixin.core.gui.server;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import tfc.smallerunits.UnitSpace;
import tfc.smallerunits.data.access.SUScreenAttachments;
import tfc.smallerunits.data.storage.RegionPos;
import tfc.smallerunits.utils.PositionalInfo;

@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin implements SUScreenAttachments {
	@Unique
	PositionalInfo info;
	@Unique
	Level targetLevel;
	@Unique
	int upb;
	@Unique
	RegionPos regionPos;
	
	@Override
	public void setup(PositionalInfo info, UnitSpace unit) {
		this.info = info;
		targetLevel = unit.getMyLevel();
		upb = unit.unitsPerBlock;
		regionPos = unit.regionPos;
	}
	
	@Override
	public void setup(PositionalInfo info, Level targetLevel, int upb, RegionPos regionPos) {
		this.info = info;
		this.targetLevel = targetLevel;
		this.upb = upb;
		this.regionPos = regionPos;
	}
	
	@Override
	public PositionalInfo getPositionalInfo() {
		return info;
	}
	
	@Override
	public Level getTarget() {
		return targetLevel;
	}
	
	@Override
	public int getUpb() {
		return upb;
	}
	
	@Override
	public RegionPos regionPos() {
		return regionPos;
	}
}
