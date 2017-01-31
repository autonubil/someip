package com.autonubil.someip.core.testutil;

import com.autonubil.someip.core.annotations.SomeIpField;
import com.autonubil.someip.core.annotations.SomeIpMessage;
import com.autonubil.someip.core.enums.ByteOrder;

@SomeIpMessage(serviceId = 2, methodId = 23)
public class TestWithSameOrder {

	@SomeIpField(align = 0, byteOrder = ByteOrder.BIG_ENDIAN, order = 0)
	private int a;

	@SomeIpField(align = 0, byteOrder = ByteOrder.BIG_ENDIAN, order = 0)
	private int b;

	@SomeIpField(align = 0, byteOrder = ByteOrder.BIG_ENDIAN, order = 0)
	private int c;

	public int getA() {
		return a;
	}

	public void setA(int a) {
		this.a = a;
	}

	public int getB() {
		return b;
	}

	public void setB(int b) {
		this.b = b;
	}

	public int getC() {
		return c;
	}

	public void setC(int c) {
		this.c = c;
	}

}
