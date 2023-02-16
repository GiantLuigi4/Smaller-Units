package tfc.smallerunits.utils.config.annoconfg.annotation.format;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Name {
	String value();
}
