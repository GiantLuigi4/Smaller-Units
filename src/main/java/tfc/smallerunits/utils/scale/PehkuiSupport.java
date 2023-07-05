package tfc.smallerunits.utils.scale;

import net.minecraft.resources.ResourceLocation;
import tfc.smallerunits.logging.Loggers;
import tfc.smallerunits.utils.platform.PlatformUtils;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleModifier;
import virtuoel.pehkui.api.ScaleRegistries;
import virtuoel.pehkui.api.ScaleType;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class PehkuiSupport {
	public static final AtomicReference<ScaleModifier> SUScaleModifier = new AtomicReference<>();
	public static final AtomicReference<ScaleType> SUScaleType = new AtomicReference<>();
	
	protected static final String MOD_NAME = PlatformUtils.isLoaded("pehkui") ? "pehkui": "scopic";
	
	public static void setup() {
		Loggers.SU_LOGGER.info(
				MOD_NAME.substring(0, 1).toUpperCase() + MOD_NAME.substring(1) +
				" detected; enabling support"
		);
		
		ScaleModifier modifier = new ScaleModifier() {
			@Override
			public float modifyScale(ScaleData scaleData, float modifiedScale, float delta) {
				return SUScaleType.get().getScaleData(scaleData.getEntity()).getScale(delta) * modifiedScale;
			}
		};
		ScaleRegistries.register(ScaleRegistries.SCALE_MODIFIERS, new ResourceLocation("smallerunits:su_resize"), modifier);
		SUScaleModifier.set(modifier);
		ScaleType suType = ScaleType.Builder.create()
				.affectsDimensions()
				.defaultTickDelay(4)
				.addDependentModifier(SUScaleModifier.get())
				.build();
		ScaleRegistries.register(ScaleRegistries.SCALE_TYPES, new ResourceLocation("smallerunits:su_resize"), suType);
		Optional<ScaleType> baseType = getType("base");
		// suppress warning because I don't want to risk accidental class loading, nor do I want intelliJ constantly warning me about the fact that I do this
		//noinspection OptionalIsPresent
		if (baseType.isPresent())
			baseType.get().getDefaultBaseValueModifiers().add(modifier);
		SUScaleType.set(suType);
	}
	
	// using optional to prevent accidental class loading
	public static Optional<ScaleType> getType(String name) {
		return Optional.of(ScaleRegistries.getEntry(ScaleRegistries.SCALE_TYPES, new ResourceLocation(MOD_NAME, name)));
	}
}
