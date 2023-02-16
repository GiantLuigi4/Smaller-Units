package tfc.smallerunits.utils.config.annoconfg.annotation.value;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Default {
	byte valueB() default 0;
	short valueS() default 0;
	int valueI() default 0;
	long valueL() default 0;
	float valueF() default 0;
	double valueD() default 0;
	boolean valueBoolean() default false;
}
