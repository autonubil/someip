package com.autonubil.someip.core;

import org.junit.Assert;
import org.junit.Test;

import com.autonubil.someip.core.enums.DataType;
import com.autonubil.someip.core.serialization.AnnotationBaseSerializer;
import com.autonubil.someip.core.serialization.SerializationException;
import com.autonubil.someip.core.testutil.TestClass1;
import com.autonubil.someip.core.testutil.TestClass2;
import com.autonubil.someip.core.testutil.TestClassWithCollisionClass;
import com.autonubil.someip.core.testutil.TestClassWithCollisionSubClass;
import com.autonubil.someip.core.testutil.TestWithCorrectOrder;
import com.autonubil.someip.core.testutil.TestWithSameOrder;

public class AnnotationSerializeTest {
	
	@Test(expected=IllegalArgumentException.class)
	public void testUnannotatedClassExpectFalse() throws SerializationException {
		AnnotationBaseSerializer abs = new AnnotationBaseSerializer();
		Assert.assertEquals(false, abs.supports(new TestClass1()));
		Assert.assertEquals(true, abs.supports(new TestClass2()));
		abs.serialize(0, 0, 0, new TestClass1());
	}


	@Test
	public void testSimpleValuesExpectSuccess() throws SerializationException {
		AnnotationBaseSerializer abs = new AnnotationBaseSerializer();
		TestWithCorrectOrder t = new TestWithCorrectOrder();
		t.setA(123);
		t.setB(456);
		t.setC(789);
		long x = System.currentTimeMillis();
		Message m = null;
		int g = 10000;
		for(int i=0;i<g;i++) {
			m = abs.serialize(0, 0, 0, t);
			m.rewind();
			Assert.assertEquals(123, m.get(DataType.UINT8));
			Assert.assertEquals(789, m.get(DataType.UINT16));
			Assert.assertEquals(456, m.get(DataType.UINT32));
		}
		long y = System.currentTimeMillis();
		System.err.println((double)(y-x) / (double)g);
	}
	
	@Test(expected=SerializationException.class)
	public void testSameOrderFieldsExpectException() throws SerializationException {
		AnnotationBaseSerializer abs = new AnnotationBaseSerializer();
		abs.serialize(0, 0, 0, new TestWithSameOrder());
	}
	
	
	@Test(expected=SerializationException.class)
	public void testSameOrderFieldInInterfaceExpectException() throws SerializationException {
		AnnotationBaseSerializer abs = new AnnotationBaseSerializer();
		abs.serialize(0, 0, 0, new TestClassWithCollisionClass());
	}
	
	
	@Test(expected=SerializationException.class)
	public void testSameOrderFieldInSuperclassExpectException() throws SerializationException {
		AnnotationBaseSerializer abs = new AnnotationBaseSerializer();
		abs.serialize(0, 0, 0, new TestClassWithCollisionSubClass());
	}
	
	
	
	
}
