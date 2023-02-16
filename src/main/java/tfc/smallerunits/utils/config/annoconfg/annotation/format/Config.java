package tfc.smallerunits.utils.config.annoconfg.annotation.format;

import net.minecraftforge.fml.config.ModConfig;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Config {
	ModConfig.Type type();
	String path() default "";
}
