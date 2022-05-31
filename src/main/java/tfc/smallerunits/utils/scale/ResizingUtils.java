package tfc.smallerunits.utils.scale;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.fml.ModList;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

// TODO: decide if I should support gullivern/shrink/threecore
public class ResizingUtils {
	private static final UUID uuidHeight = UUID.fromString("5440b01a-974f-4495-bb9a-c7c87424bca4");
	private static final UUID uuidWidth = UUID.fromString("3949d2ed-b6cc-4330-9c13-98777f48ea51");
	
	public static void resize(Entity entity, int amt) {
		//TODO: chiseled me integration
		float newSize = getSize(entity);
		
		if (amt > 0) {
			if (1f / getSize(entity) <= 4)
				newSize = Math.max(getSize(entity) - (amt / 8f), 1f / 8);
		} else if (getSize(entity) <= 2)
			newSize = Math.min(getSize(entity) - (amt / 2f), 2);
		
		if (ModList.get().isLoaded("pehkui")) {
			PehkuiSupport.SUScaleType.get().getScaleData(entity).setTargetScale(newSize);
		}
	}
	
	public static float getSize(Entity entity) {
		AtomicReference<Float> size = new AtomicReference<>(1f);
		if (ModList.get().isLoaded("pehkui"))
			size.set(size.get() * PehkuiSupport.SUScaleType.get().getScaleData(entity).getTargetScale());
		return size.get();
	}
	
	public static void resizeForUnit(Entity entity, float amt) {
		//TODO: chiseled me integration
		if (ModList.get().isLoaded("pehkui")) {
			PehkuiSupport.SUScaleType.get().getScaleData(entity).setScale(amt);
		}
	}
	
	public static boolean isResizingModPresent() {
		return
//				ModList.get().isLoaded("threecore") ||
//						ModList.get().isLoaded("shrink") ||
//						ModList.get().isLoaded("gullivern") ||
				ModList.get().isLoaded("pehkui");
	}
}
