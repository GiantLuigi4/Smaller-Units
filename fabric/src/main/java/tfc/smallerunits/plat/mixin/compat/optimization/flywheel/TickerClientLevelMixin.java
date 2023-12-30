package tfc.smallerunits.plat.mixin.compat.optimization.flywheel;

import com.jozufozu.flywheel.api.FlywheelWorld;
import org.spongepowered.asm.mixin.Mixin;
import tfc.smallerunits.simulation.level.client.TickerClientLevel;

@Mixin(TickerClientLevel.class)
public class TickerClientLevelMixin implements FlywheelWorld {
	@Override
	public boolean supportsFlywheel() {
		return true;
	}
}