package tfc.smallerunits.utils.config.annoconfg.annotation.value;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface LongRange {
	long minV();
	long maxV();
}
