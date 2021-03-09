package com.tfc.smallerunits.utils;

import com.tfc.smallerunits.utils.threecore.SUResizeType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.fml.ModList;
import net.teamfruit.gulliver.GulliverSize;
import net.teamfruit.gulliver.attributes.Attributes;
import net.threetag.threecore.capability.CapabilitySizeChanging;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

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
		
		if (ModList.get().isLoaded("gulliver") && entity instanceof LivingEntity) {
			if (ModList.get().isLoaded("threecore")) {
				entity.getCapability(CapabilitySizeChanging.SIZE_CHANGING).ifPresent((cap) -> {
					if (cap.getSizeChangeType().equals(SUResizeType.SU_CHANGE_TYPE.get())) {
						resizeForUnit(entity, 1);
					}
				});
			}
			GulliverSize.changeSize((LivingEntity) entity, newSize);
		} else if (ModList.get().isLoaded("threecore")) {
			float finalNewSize = newSize;
			entity.getCapability(CapabilitySizeChanging.SIZE_CHANGING).ifPresent((cap) -> {
				cap.startSizeChange(SUResizeType.SU_CHANGE_TYPE.get(), finalNewSize);
//				if (amt > 0) {
//					if (1f / getSize(entity) <= 4) {
//						cap.startSizeChange(SUResizeType.SU_CHANGE_TYPE.get(), Math.max(getSize(entity) - (amt / 8f), 1f / 8));
//					}
//				} else {
//					if (getSize(entity) <= 2) {
//						cap.startSizeChange(SUResizeType.SU_CHANGE_TYPE.get(), Math.min(getSize(entity) - (amt / 2f), 2));
//					}
//				}
			});
		}
	}
	
	public static float getSize(Entity entity) {
		AtomicReference<Float> size = new AtomicReference<>(1f);
		if (ModList.get().isLoaded("gulliver") && entity instanceof LivingEntity) {
			if (((LivingEntity) entity).getAttribute(Attributes.ENTITY_HEIGHT.get()) != null) {
				AttributeModifier modifier = ((LivingEntity) entity).getAttribute(Attributes.ENTITY_HEIGHT.get()).getModifier(uuidHeight);
				
				if (modifier != null) {
					float s;
					
					if (modifier.getOperation().equals(AttributeModifier.Operation.ADDITION))
						s = size.get() + (float) modifier.getAmount();
					else s = size.get() * (float) modifier.getAmount();
					
					s = 1 + s;
					s = Math.max(1f / 8, s);
					
					size.set(s);
				}
			}
		}
		if (ModList.get().isLoaded("threecore")) {
			entity.getCapability(CapabilitySizeChanging.SIZE_CHANGING).ifPresent((cap) -> {
				float val = size.get();
				size.set(val * cap.getScale());
			});
		}
		return size.get();
	}
	
	public static void resizeForUnit(Entity entity, float amt) {
		//TODO: chiseled me integration
		if (ModList.get().isLoaded("gullivern")) {
		
		} else if (ModList.get().isLoaded("threecore")) {
			entity.getCapability(CapabilitySizeChanging.SIZE_CHANGING).ifPresent((cap) -> {
				cap.setSizeDirectly(SUResizeType.SU_CHANGE_TYPE.get(), amt);
			});
		}
	}
}
