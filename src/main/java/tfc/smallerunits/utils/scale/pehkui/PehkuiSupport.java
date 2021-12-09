package tfc.smallerunits.utils.scale.pehkui;

import net.minecraft.util.ResourceLocation;
import tfc.smallerunits.Smallerunits;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleModifier;
import virtuoel.pehkui.api.ScaleRegistries;
import virtuoel.pehkui.api.ScaleType;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class PehkuiSupport {
	public static final AtomicReference<ScaleModifier> SUScaleModifier = new AtomicReference<>();
	public static final AtomicReference<ScaleType> SUScaleType = new AtomicReference<>();
	
	public static void setup() {
		Smallerunits.LOGGER.info("Pehkui detected; enabling support");
		
		ScaleModifier modifier = new ScaleModifier() {
			@Override
			public float modifyScale(ScaleData scaleData, float modifiedScale, float delta) {
				return SUScaleType.get().getScaleData(scaleData.getEntity()).getScale(delta) * modifiedScale;
			}
		};
		ScaleRegistries.SCALE_MODIFIERS.put(new ResourceLocation("smallerunits:su_resize"), modifier);
		SUScaleModifier.set(modifier);
		ScaleType suType = ScaleType.Builder.create()
				.affectsDimensions()
				.addDependentModifier(SUScaleModifier.get())
				.build();
		ScaleRegistries.SCALE_TYPES.put(new ResourceLocation("smallerunits:su_resize"), suType);
		Optional<ScaleType> baseType = getType("base");
		// suppress warning because I don't want to risk accidental class loading, nor do I want intelliJ constantly warning me about the fact that I do this
		//noinspection OptionalIsPresent
		if (baseType.isPresent())
			baseType.get().getDefaultBaseValueModifiers().add(modifier);
		SUScaleType.set(suType);
	}
	
	// using optional to prevent accidental class loading
	public static Optional<ScaleType> getType(String name) {
		return Optional.of(ScaleRegistries.getEntry(ScaleRegistries.SCALE_TYPES, new ResourceLocation("pehkui", name)));
	}
}
