package com.autonubil.someip.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.autonubil.someip.core.enums.ByteOrder;
import com.autonubil.someip.core.enums.DataType;

@Target(value={ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SomeIpField {

	int value() default 0;
	int order() ;
	boolean optional() default false;
	int align() default 0;
	ByteOrder byteOrder() default ByteOrder.BIG_ENDIAN;
	DataType as() default DataType.SINT32;
	
	
}
