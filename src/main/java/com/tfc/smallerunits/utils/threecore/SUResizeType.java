package com.tfc.smallerunits.utils.threecore;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.threetag.threecore.capability.ISizeChanging;
import net.threetag.threecore.entity.attributes.TCAttributes;
import net.threetag.threecore.sizechanging.DefaultSizeChangeType;
import net.threetag.threecore.sizechanging.SizeChangeType;

public class SUResizeType extends DefaultSizeChangeType {
	public static final DeferredRegister<SizeChangeType> suSizeChangeTypes = DeferredRegister.create(SizeChangeType.class, "smallerunits");
	public static final RegistryObject<SizeChangeType> SU_CHANGE_TYPE = suSizeChangeTypes.register("su_size_change", SUResizeType::new);
	
	@Override
	public int getSizeChangingTime(Entity entity, ISizeChanging data, float estimatedSize) {
		System.out.println(data.getScale());
		System.out.println(estimatedSize);
		return (int) Math.abs((data.getScale() - estimatedSize) * 10);
	}
	
	@Override
	public void onSizeChanged(Entity entity, ISizeChanging data, float size) {
		super.onSizeChanged(entity, data, size);
		if (entity instanceof LivingEntity) {
			System.out.println(size);
			System.out.println((4 / (1f / size)) / 4);
			System.out.println(((4 / (1f / size)) - 4));
			float scl = 1f / (1 - (1f / size));
			if (scl < 0) {
				scl = (0 - scl) - 8;
				scl = ((scl / 8f) / 13f);
			}
			if (size >= 1f / 4) scl = 0;
			this.setAttribute((LivingEntity) entity, Attributes.MOVEMENT_SPEED, scl, AttributeModifier.Operation.ADDITION, SizeChangeType.ATTRIBUTE_UUID);
			this.setAttribute((LivingEntity) entity, (Attribute) TCAttributes.JUMP_HEIGHT.get(), -1, AttributeModifier.Operation.ADDITION, SizeChangeType.ATTRIBUTE_UUID);
		}
	}
}
