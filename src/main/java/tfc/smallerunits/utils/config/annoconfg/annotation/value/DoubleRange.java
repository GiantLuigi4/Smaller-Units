package tfc.smallerunits.utils.config.annoconfg.annotation.value;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface DoubleRange {
	double minV();
	double maxV();
}
