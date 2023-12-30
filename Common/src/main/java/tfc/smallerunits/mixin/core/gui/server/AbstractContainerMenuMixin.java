package tfc.smallerunits.mixin.core.gui.server;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.UnitSpace;
import tfc.smallerunits.data.access.SUScreenAttachments;
import tfc.smallerunits.networking.hackery.NetworkingHacks;
import tfc.smallerunits.plat.util.PlatformUtils;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.utils.PositionalInfo;

@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin implements SUScreenAttachments {
	@Unique
	PositionalInfo info;
	@Unique
	Level targetLevel;
	@Unique
	NetworkingHacks.LevelDescriptor descriptor;
	
	@Override
	public void update(Player player) {
		if (info != null) {
			synchronized (this) {
				info = new PositionalInfo(player);
			}
		}
	}
	@Override
	public void setup(PositionalInfo info, UnitSpace unit) {
		this.info = info;
		targetLevel = unit.getMyLevel();
		descriptor = ((ITickerLevel) unit.getMyLevel()).getDescriptor();
	}
	
	@Override
	public void setup(PositionalInfo info, Level targetLevel, NetworkingHacks.LevelDescriptor descriptor) {
		this.info = info;
		this.targetLevel = targetLevel;
		this.descriptor = descriptor;
	}
	
	@Override
	public void setup(SUScreenAttachments attachments) {
		this.info = attachments.getPositionalInfo();
		this.targetLevel = attachments.getTarget();
		this.descriptor = attachments.getDescriptor();
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
	public NetworkingHacks.LevelDescriptor getDescriptor() {
		return descriptor;
	}
}
