package tfc.smallerunits.utils.scale;

import net.minecraft.world.entity.Entity;
import tfc.smallerunits.SmallerUnits;
import tfc.smallerunits.utils.platform.PlatformUtils;
import virtuoel.pehkui.util.ScaleUtils;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static tfc.smallerunits.utils.config.ServerConfig.GameplayOptions.EntityScaleOptions;

// TODO: decide if I should support gullivern/shrink/threecore
public class ResizingUtils {
	private static final UUID uuidHeight = UUID.fromString("5440b01a-974f-4495-bb9a-c7c87424bca4");
	private static final UUID uuidWidth = UUID.fromString("3949d2ed-b6cc-4330-9c13-98777f48ea51");
	
	public static void resize(Entity entity, int amt) {
		//TODO: chiseled me integration
		if (entity == null) return;
		float newSize = getSize(entity);
		
		if (amt > 0) {
			if (getSize(entity) >= EntityScaleOptions.minSize)
				newSize = (float) Math.max(getSize(entity) - (amt * EntityScaleOptions.downscaleRate), EntityScaleOptions.minSize);
		} else if (getSize(entity) <= EntityScaleOptions.maxSize)
			newSize = (float) Math.min(getSize(entity) - (amt / EntityScaleOptions.upscaleRate), EntityScaleOptions.maxSize);
		
		if (SmallerUnits.isIsPehkuiPresent()) {
			PehkuiSupport.SUScaleType.get().getScaleData(entity).setTargetScale(newSize);
		}
	}
	
	public static float getSize(Entity entity) {
		if (entity == null) return 1;
		AtomicReference<Float> size = new AtomicReference<>(1f);
		if (SmallerUnits.isIsPehkuiPresent())
			size.set(size.get() * PehkuiSupport.SUScaleType.get().getScaleData(entity).getTargetScale());
		return size.get();
	}
	
	public static float getActualSize(Entity entity) {
		if (entity == null) return 1;
		AtomicReference<Float> size = new AtomicReference<>(1f);
		if (SmallerUnits.isIsPehkuiPresent())
			size.set(size.get() * ScaleUtils.getBoundingBoxHeightScale(entity));
		return size.get();
	}
	
	public static void resizeForUnit(Entity entity, float amt) {
		if (entity == null) return;
		//TODO: chiseled me integration
		if (SmallerUnits.isIsPehkuiPresent()) {
			PehkuiSupport.SUScaleType.get().getScaleData(entity).setScale(amt);
		}
	}
	
	public static boolean isResizingModPresent() {
		return
//				ModList.get().isLoaded("threecore") ||
//						ModList.get().isLoaded("shrink") ||
//						ModList.get().isLoaded("gullivern") ||
				SmallerUnits.isIsPehkuiPresent();
	}
}
