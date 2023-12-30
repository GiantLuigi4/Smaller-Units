package tfc.smallerunits.utils.config.annoconfg.annotation.value;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface IntBounds {
	int minV();
	int midV();
	int maxV();
	
	int rangeMin();
	int rangeMax();
	
	record Bound(int min, int middle, int max) {
	}
}
