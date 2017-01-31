package com.autonubil.someip.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.autonubil.someip.core.enums.ByteOrder;

@Target(value={ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SomeIpArray {

	public static final int SIZE_DYNAMIC = -1;
	
	int value() default 0;
	int[] dynamic() default { SIZE_DYNAMIC };
	int align() default 0;
	ByteOrder byteOrder() default ByteOrder.BIG_ENDIAN;
	
}
