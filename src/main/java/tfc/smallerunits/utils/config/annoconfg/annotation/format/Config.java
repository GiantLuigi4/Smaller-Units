package tfc.smallerunits.utils.config.annoconfg.annotation.format;

import tfc.smallerunits.utils.config.annoconfg.ConfigSide;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Config {
	ConfigSide type();
	String namespace();
	String path() default "";
}
