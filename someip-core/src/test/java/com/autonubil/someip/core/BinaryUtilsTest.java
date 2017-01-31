package com.autonubil.someip.core;

import org.junit.Assert;
import org.junit.Test;

import com.autonubil.someip.core.enums.ByteOrder;

public class BinaryUtilsTest {
	
	// **************
	// BASIC EXCEPTIONS
	
	@Test(expected=IllegalArgumentException.class)
	public void testSimpleNoByteOrderExpectException() {
		BinaryUtils.toByteArray(-128, 1, true, null);
	}
	
	// **************
	// SINT8
	
	@Test
	public void testSimpleS8ExpectSuccess () {
		for(int i : new int[] {-127, -1,0, 23, 55, 127 }) {
			byte[] buff = BinaryUtils.toByteArray(i, 1, true, ByteOrder.BIG_ENDIAN);
			long i2 = BinaryUtils.fromByteArray(buff, 1, true, ByteOrder.BIG_ENDIAN);
			Assert.assertEquals(i, i2);
		}
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testSimpleS8TooLowExpectException() {
		BinaryUtils.toByteArray(-128, 1, true, ByteOrder.BIG_ENDIAN);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testSimpleS8TooHighExpectException() {
		BinaryUtils.toByteArray(128, 1, true, ByteOrder.BIG_ENDIAN);
	}
	
	// **************
	// UINT8

	@Test
	public void testSimpleU8ExpectSuccess () {
		for(int i : new int[] {0, 23, 55, 127,128,255 }) {
			byte[] buff = BinaryUtils.toByteArray(i, 1, false, ByteOrder.BIG_ENDIAN);
			long i2 = BinaryUtils.fromByteArray(buff, 1, false, ByteOrder.BIG_ENDIAN);
			Assert.assertEquals(i, i2);
		}
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testSimpleU8TooLowExpectException() {
		BinaryUtils.toByteArray(-1, 1, false, ByteOrder.BIG_ENDIAN);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testSimpleU8TooHighExpectException() {
		BinaryUtils.toByteArray(256, 1, false, ByteOrder.BIG_ENDIAN);
	}
	
	// **************
	// SINT16

	@Test
	public void testSimpleS16ExpectSuccess () {
		for(int i : new int[] { 0, -1, 1, 0x0F0F, 0x7FFF, -0x7FFF}) {
			byte[] buff = BinaryUtils.toByteArray(i, 2, true, ByteOrder.BIG_ENDIAN);
			long i2 = BinaryUtils.fromByteArray(buff, 2, true, ByteOrder.BIG_ENDIAN);
			Assert.assertEquals(i, i2);
		}
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testSimpleS16TooLowExpectException() {
		BinaryUtils.toByteArray(-0xFFFF, 2, true, ByteOrder.BIG_ENDIAN);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testSimpleS16TooHighExpectException() {
		BinaryUtils.toByteArray(0xFFFF, 2, true, ByteOrder.BIG_ENDIAN);
	}
	
	// **************
	// UINT16

	@Test
	public void testSimpleU16ExpectSuccess () {
		for(int i : new int[] { 0, 1, 128, 0x0F88, 0xFFFF  }) {
			byte[] buff = BinaryUtils.toByteArray(i, 2, false, ByteOrder.BIG_ENDIAN);
			long i2 = BinaryUtils.fromByteArray(buff, 2, false, ByteOrder.BIG_ENDIAN);
			Assert.assertEquals(i, i2);
		}
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testSimpleU16TooLowExpectException() {
		BinaryUtils.toByteArray(-1, 2, false, ByteOrder.BIG_ENDIAN);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testSimpleU16TooHighExpectException() {
		BinaryUtils.toByteArray(0xFFFF+1, 2, false, ByteOrder.BIG_ENDIAN);
	}
	
	
	// **************
	// UINT32 

	@Test
	public void testLittleVsBigEndianExpectSuccess() {
		
		{
			byte[] buff1 = BinaryUtils.toByteArray(0xFFEEDDCCl, 4,false, ByteOrder.BIG_ENDIAN);
			byte[] buff2 = BinaryUtils.toByteArray(0xFFEEDDCCl, 4,false, ByteOrder.LITTLE_ENDIAN);
			
			for(int i=0; i < 4; i++) {
				Assert.assertEquals(buff1[i], buff2[3-i]);
			}
			
			Assert.assertEquals(0xFF, ((int)buff1[0] & 0xFF));
			
			System.err.println(Integer.toHexString(buff1[0]));
			
		}
		{
			byte[] buff1 = BinaryUtils.toByteArray(0xFFEEDDCCl, 4,false, ByteOrder.LITTLE_ENDIAN);
			byte[] buff2 = BinaryUtils.toByteArray(0xFFEEDDCCl, 4,false, ByteOrder.BIG_ENDIAN);
			
			for(int i=0; i < 4; i++) {
				Assert.assertEquals(buff1[i], buff2[3-i]);
			}
			
			Assert.assertEquals(0xCC, ((int)buff1[0] & 0xFF));
			
			System.err.println(Integer.toHexString(buff1[0]));
			
		}
		{
			byte[] buff1 = BinaryUtils.toByteArray(0xFFEEDDCCl, 4,false, ByteOrder.LITTLE_ENDIAN);
			long l = BinaryUtils.fromByteArray(buff1, 4,false, ByteOrder.BIG_ENDIAN);
			byte[] buff2 = BinaryUtils.toByteArray(0xFFEEDDCCl, 4,false, ByteOrder.BIG_ENDIAN);
			
			for(int i=0; i < 4; i++) {
				Assert.assertEquals(buff1[i], buff2[3-i]);
			}
			
			Assert.assertEquals(0xCC, ((int)buff1[0] & 0xFF));
			
			System.err.println(Integer.toHexString(buff1[0]));
			
		}
		{
			byte[] buff1 = BinaryUtils.toByteArray(0xFFEEDDCCl, 4,false, ByteOrder.BIG_ENDIAN);
			long l = BinaryUtils.fromByteArray(buff1, 4,false, ByteOrder.LITTLE_ENDIAN);
			byte[] buff2 = BinaryUtils.toByteArray(0xFFEEDDCCl, 4,false, ByteOrder.LITTLE_ENDIAN);
			
			for(int i=0; i < 4; i++) {
				Assert.assertEquals(buff1[i], buff2[3-i]);
			}
			
			Assert.assertEquals(0xCC, ((int)buff2[0] & 0xFF));
			
			System.err.println(Integer.toHexString(buff1[0]));
			
		}
	}

	// **************
	// UINT32 

	@Test
	public void testSimpleFloat32BE() {
		for(float f1 : new float[] { 0f, 1f, 128f, 0.99923f, Float.MAX_VALUE , Float.MIN_VALUE }) {
			byte[] buff = BinaryUtils.floatToByteArray(f1, 4, ByteOrder.BIG_ENDIAN);
			double f2 = BinaryUtils.floatFromByteArray(buff, ByteOrder.BIG_ENDIAN);
			Assert.assertEquals(f1, f2,0);
		}
	}		
	
	@Test
	public void testSimpleFloat64BE() {
		for(double f1 : new double[] { 0f, 1d, 128d, 0.99923d, Double.MAX_VALUE , Double.MIN_VALUE }) {
			byte[] buff = BinaryUtils.floatToByteArray(f1, 8, ByteOrder.BIG_ENDIAN);
			double f2 = BinaryUtils.floatFromByteArray(buff, ByteOrder.BIG_ENDIAN);
			Assert.assertEquals(f1, f2,0);
		}
	}		

	@Test
	public void testSimpleFloat32LE() {
		for(float f1 : new float[] { 0f, 1f, 128f, 0.99923f, Float.MAX_VALUE , Float.MIN_VALUE }) {
			byte[] buff = BinaryUtils.floatToByteArray(f1, 4, ByteOrder.LITTLE_ENDIAN);
			double f2 = BinaryUtils.floatFromByteArray(buff, ByteOrder.LITTLE_ENDIAN);
			Assert.assertEquals(f1, f2,0);
		}
	}		
	
	@Test
	public void testSimpleFloat64LE() {
		for(double f1 : new double[] { 0f, 1f, 128f, 0.99923f, Double.MAX_VALUE , Double.MIN_VALUE }) {
			byte[] buff = BinaryUtils.floatToByteArray(f1, 8, ByteOrder.LITTLE_ENDIAN);
			double f2 = BinaryUtils.floatFromByteArray(buff, ByteOrder.LITTLE_ENDIAN);
			Assert.assertEquals(f1, f2,0);
		}
	}		

	@Test
	public void testFloat32LEExpectSuccess() {
		for(float f : new float[] { 0f, 0.1f, 0f, -1f, -128f, 0x7FFF}) {
			byte[] b = BinaryUtils.floatToByteArray(f, 4, ByteOrder.LITTLE_ENDIAN);
			float f2 = (float)BinaryUtils.floatFromByteArray(b, ByteOrder.LITTLE_ENDIAN);
			Assert.assertEquals(f, f2, 0);
		}
	}		
	
	@Test
	public void testFloat64LEExpectSuccess() {
		for(float f : new float[] { 0f, 0.1f, 0f, -1f, -128f, 0x7FFF}) {
			byte[] b = BinaryUtils.floatToByteArray(f, 8, ByteOrder.LITTLE_ENDIAN);
			float f2 = (float)BinaryUtils.floatFromByteArray(b, ByteOrder.LITTLE_ENDIAN);
			Assert.assertEquals(f, f2, 0);
		}
	}		
	
	@Test(expected=IllegalArgumentException.class)
	public void testFloatNeither32not64ExpectException() {
		BinaryUtils.floatToByteArray(0.1f, 9, ByteOrder.LITTLE_ENDIAN);
	}		
	
	@Test(expected=IllegalArgumentException.class)
	public void testFloat7BytesExpectException() {
		BinaryUtils.floatFromByteArray(new byte[7], ByteOrder.BIG_ENDIAN);
	}		
	
	
}
