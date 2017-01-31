package com.autonubil.someip.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SomeIpMessage {

	public int size() default 4096;
	public int serviceId();
	public int methodId();
	public int protocolVersion() default 1;
	public int interfaceVersion() default 1;
	
}
