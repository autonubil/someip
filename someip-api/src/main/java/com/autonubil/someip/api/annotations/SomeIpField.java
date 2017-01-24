package com.autonubil.someip.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value={ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SomeIpField {

	int value() default 0;
	boolean optional() default false;
	int align() default 0;
	ByteOrder byteOrder() default ByteOrder.BIG_ENDIAN;
	DataType as() default DataType.SINT32;
	
}
