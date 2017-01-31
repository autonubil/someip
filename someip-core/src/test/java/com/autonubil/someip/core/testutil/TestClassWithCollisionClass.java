package com.autonubil.someip.core.testutil;

import com.autonubil.someip.core.annotations.SomeIpField;
import com.autonubil.someip.core.annotations.SomeIpMessage;

@SomeIpMessage(serviceId=5,methodId=2)
public class TestClassWithCollisionClass implements TestClassWithCollisionInterface {
	
	
	@SomeIpField(align=0,order=0)
	private int fieldTwo;
	

}
