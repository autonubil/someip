package com.autonubil.someip.core;

import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.junit.Assert;
import org.junit.Test;

import com.autonubil.someip.core.Message;
import com.autonubil.someip.core.enums.ByteOrder;
import com.autonubil.someip.core.enums.DataType;

public class MessageTest {

	@Test
	public void setClientAndRequestIdExcpectSuccess() {
		Message am = new Message() {
		};
		for (int i : new int[] { 0xFFFF, 0x0001, 0x0505, 0x8765, 0x0000 }) {
			am.setClientId(i);
			am.setSessionId(0xFFFF - i);
			Assert.assertEquals(i, am.getClientId());
			Assert.assertEquals(0xFFFF - i, am.getSessionId());
		}

	}

	@Test
	public void setServiceIdExcpectSuccess() {
		Message am = new Message() {
		};
		for (int i : new int[] { 0xFFFF, 0x0001, 0x0505, 0x8765 }) {
			am.setServiceId(i);
			Assert.assertEquals(i, am.getServiceId());
		}

	}

	@Test(expected = IllegalArgumentException.class)
	public void setServiceIdExcpectException() {
		Message am = new Message() {
		};
		am.setServiceId(0xFFFF + 1);
	}

	@Test
	public void setMethodIdExcpectSuccess() {
		Message am = new Message() {
		};
		for (int i : new int[] { 0x7FFF, 0x0001, 0x0505, 0x7765 }) {
			am.setMethodId(i);
			Assert.assertEquals(i, am.getMethodId());
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void setMethodIdExcpectException() {
		Message am = new Message() {
		};
		am.setMethodId(0x7FFF + 1);
	}

	@Test
	public void setBytesMetadata() {
		Message am = new Message() {
		};
		for (int i : new int[] { 0xFF, 0x01, 0x90, 0x00 }) {
			am.setProtocolVersion(i);
			am.setInterfaceVersion(0xFF - i);
			am.setMessageType(i);
			am.setReturnCode(0xFF - i);
			Assert.assertEquals(i, am.getProtocolVersion());
			Assert.assertEquals(0xFF - i, am.getInterfaceVersion());
			Assert.assertEquals(i, am.getMessageType());
			Assert.assertEquals(0xFF - i, am.getReturnCode());
		}
	}

	@Test
	public void readWriteUINT8() {

		int[] x = new int[] { 0xFF, 0x01, 0x00, 0x80 };

		Message am = new Message() {
		};
		for (int i : x) {
			am.put(i, DataType.UINT8);
		}

		for (int i : x) {
			Number k = am.get(DataType.UINT8);
			Assert.assertEquals(i, k.longValue());
		}
	}

	@Test
	public void readWriteSINT8() {
		int[] x = new int[] { 0x7F, 0x01, 0x00, 0x70 };

		Message am = new Message() {
		};
		for (int i : x) {
			am.put(i, DataType.SINT8);
		}

		for (int i : x) {
			Number k = am.get(DataType.SINT8);
			Assert.assertEquals(i, k.longValue());
		}

	}

	@Test(expected = IllegalArgumentException.class)
	public void testreadWriteUINT8ExpectException() {
		Message am = new Message() {
		};
		am.put(0xFFFF, DataType.UINT8);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testreadWriteSINT8SetMaxPlusOneExpectException() {
		Message am = new Message() {
		};
		am.put(128, DataType.SINT8);
	}

	@Test
	public void testreadWriteSINT8SetMaxExpectSucces() {
		Message am = new Message() {
		};
		am.put(127, DataType.SINT8);
	}

	@Test
	public void testreadWriteSINT8SetMinExpectSucces() {
		Message am = new Message() {
		};
		am.put(-127, DataType.SINT8);
	}

	@Test
	public void readWriteUINT16() {
		int[] x = new int[] { 0x7F01, 0x00FF, 0x0000, 0x70FF, 0x8001, 0xFFFF };

		Message am = new Message() {
		};
		for (int i : x) {
			am.put(i, DataType.UINT16, ByteOrder.BIG_ENDIAN);
		}

		for (int i : x) {
			Number k = am.get(DataType.UINT16, ByteOrder.BIG_ENDIAN);
			Assert.assertEquals(i, new Long(k.longValue()).longValue());
		}

		{

			am.put(0x708F, DataType.UINT16, ByteOrder.BIG_ENDIAN);

			Number k = am.get(DataType.UINT16, ByteOrder.LITTLE_ENDIAN);
			Assert.assertEquals(0x8F70, new Long(k.longValue()).longValue());
		}
		{

			am.put(0x708F, DataType.UINT16, ByteOrder.LITTLE_ENDIAN);

			Number k = am.get(DataType.UINT16, ByteOrder.BIG_ENDIAN);
			Assert.assertEquals(0x8F70, new Long(k.longValue()).longValue());
		}

	}

	@Test
	public void readWriteSINT16() {
		long[] x = new long[] { 0x7F01l, 0x00FFl, 0x0000l, 0x70FFl, 0x7001l, 0x7FFFl, -0x7FFFl, -0x01l };

		Message am = new Message() {
		};
		for (long i : x) {
			System.err.println(i);
			am.put(i, DataType.SINT16, ByteOrder.BIG_ENDIAN);
		}

		for (long i : x) {
			Number k = am.get(DataType.SINT16, ByteOrder.BIG_ENDIAN);
			Assert.assertEquals(i, new Long(k.longValue()).longValue());
		}

		{

			am.put(0x707Fl, DataType.SINT16, ByteOrder.BIG_ENDIAN);

			Number k = am.get(DataType.SINT16, ByteOrder.LITTLE_ENDIAN);
			Assert.assertEquals(0x7F70, new Long(k.longValue()).longValue());
		}
		{

			am.put(0x707Fl, DataType.SINT16, ByteOrder.LITTLE_ENDIAN);

			Number k = am.get(DataType.SINT16, ByteOrder.BIG_ENDIAN);
			Assert.assertEquals(0x7F70, new Long(k.longValue()).longValue());
		}

	}

	@Test
	public void readWriteSINT32() {
		int[] x = new int[] { 0xFFFFFFFF, -0x7F, 0x01, -0x01, 0x00, 0x70 };

		Message am = new Message() {
		};
		for (int i : x) {
			am.put(i, DataType.SINT32);
		}

		for (int i : x) {
			Number k = am.get(DataType.SINT32);
			Assert.assertEquals(i, new Long(k.longValue()).longValue());
		}

		am.put(-127, DataType.SINT32);

		Number k = am.get(DataType.SINT32);
		Assert.assertEquals(-127, new Long(k.longValue()).longValue());

	}

	@Test
	public void readWriteUINT32() {
		long[] x = new long[] { 0xFFFFFFFFl, 0x01, 0x80008000l, 0x7000FF01 };

		Message am = new Message() {
		};
		for (long i : x) {
			am.put(i, DataType.UINT32, ByteOrder.BIG_ENDIAN);
		}

		for (long i : x) {
			Number k = am.get(DataType.UINT32, ByteOrder.BIG_ENDIAN);
			Assert.assertEquals(i, new Long(k.longValue()).longValue());
		}

		am.put(0xF1E1D1C1l, DataType.UINT32, ByteOrder.LITTLE_ENDIAN);

		Number k = am.get(DataType.UINT32, ByteOrder.BIG_ENDIAN);
		Assert.assertEquals(0xC1D1E1F1l, new Long(k.longValue()).longValue());

	}

	@Test
	public void readWriteByte() {

		byte[] buff = new byte[] { 0x01, 0x02, 0x03, 0x04, 0x11, 0x12, 0x13, 0x14, 0x21, 0x22, 0x23, 0x24, 0x31, 0x32,
				0x33, 0x34, 0x7F, (byte) 0xFF };

		Message am = new Message(buff) {
		};

		System.err.println(Long.toHexString(am.getServiceId()));
		
		Assert.assertEquals(0x0102, am.getServiceId());
		Assert.assertEquals(0x0304, am.getMethodId());
		Assert.assertEquals(0x11121314, am.getLength());

		Assert.assertEquals(0x33, am.getMessageType());
		Assert.assertEquals(0x34, am.getReturnCode());

	}

	@Test
	public void readWriteByteWithOffset() {

		byte[] buff = new byte[] {
				0x00, 0x00, 0x00, 0x00, 0x00, 
				0x01, 0x02, 0x03, 0x04, 0x11, 0x12, 0x13, 0x14, 0x21, 0x22, 0x23, 0x24, 0x31, 0x32,
				0x33, 0x34, 0x7F, (byte) 0xFF };

		ByteBuffer bb = ByteBuffer.wrap(buff);
		bb.position(5);
		
		Message am = new Message(bb) {
		};

		Assert.assertEquals(0x0102, am.getServiceId());
		Assert.assertEquals(0x0304, am.getMethodId());
		Assert.assertEquals(0x11121314, am.getLength());

		Assert.assertEquals(0x33, am.getMessageType());
		Assert.assertEquals(0x34, am.getReturnCode());

	}

	@Test
	public void readWriteByteBufferWithOffset() {

		
		byte[] bytes = new byte[8000];
		
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		bb.position(25);
		
		Message am = new Message(bb) {
		};
		
		Assert.assertEquals(16, am.getTotalSize());
		
		am.setServiceId(0x7F7D);
		
		Assert.assertEquals(0x7F, bytes[25]);
		Assert.assertEquals(0x7D, bytes[26]);
		
		am.put(0x01020304,DataType.UINT32);
		Assert.assertEquals(20, am.getTotalSize());
		am.rewind();
		
		
		long x = am.get(DataType.UINT32);
		
		System.err.println(Long.toHexString(x));
		
		Assert.assertEquals(0x01020304, x);
		am.rewind();
		Assert.assertEquals(0x0102, am.get(DataType.UINT16));
		Assert.assertEquals(0x0304, am.get(DataType.UINT16));
		Assert.assertEquals(20, am.getTotalSize());
		
		am.put(0x00010002,DataType.UINT32);
		Assert.assertEquals(24, am.getTotalSize());
		Assert.assertEquals(0x0001, am.get(DataType.UINT16));
		Assert.assertEquals(0x0002, am.get(DataType.UINT16));
		
		
	}
	
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void writeOutsideCapacityExpectException() {
		byte[] bytes = new byte[18];
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		Message am = new Message(bb) {
		};
		Assert.assertEquals(16, am.getTotalSize());
		am.put(0x01020304,DataType.UINT32);
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void readOutsideLimitExpectException() {
		byte[] bytes = new byte[20];
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		Message am = new Message(bb) {
		};
		Assert.assertEquals(16, am.getTotalSize());
		am.put(0x01020304,DataType.UINT32);
		am.get(DataType.UINT32);
		am.get(DataType.UINT32);
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void readBeforeStartExpectException() {
		byte[] bytes = new byte[20];
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		Message am = new Message(bb) {
		};
		am.get(-1,3);
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void writeBeforeStartExpectException() {
		byte[] bytes = new byte[20];
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		Message am = new Message(bb) {
		};
		am.put(-1,3,DataType.UINT16);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void writeWithNullByteOrderExpectException() {
		byte[] bytes = new byte[20];
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		Message am = new Message(bb) {
		};
		am.put(0x01020304,(DataType)null,ByteOrder.BIG_ENDIAN);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void writeWithNullDataTypeExpectException() {
		byte[] bytes = new byte[20];
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		Message am = new Message(bb) {
		};
		am.put(0x01020304,DataType.UINT32,null);
	}
	
	@Test
	public void writeAndResetExepectEmpty() {
		byte[] bytes = new byte[199];
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		Message am = new Message(bb);
		Assert.assertEquals(16, am.getTotalSize());
		am.put(0x01020304,DataType.UINT32,ByteOrder.BIG_ENDIAN);
		Assert.assertEquals(20, am.getTotalSize());
		am.reset();
		Assert.assertEquals(16, am.getTotalSize());
	}
	
	@Test
	public void testReadWriteFloat32() throws NoSuchAlgorithmException {
		
		SecureRandom sr = SecureRandom.getInstanceStrong();
		
		Message am = new Message();
		for(int i=0; i < 10000; i++) {
			float f = sr.nextFloat();
			am.reset();
			am.putFloat(f, DataType.FLOAT32);
			am.rewind();
			float f2 = new Double(am.getFloat(DataType.FLOAT32)).floatValue();
			Assert.assertEquals(f, f2, 0);
		}
		
	}
	

	@Test
	public void testReadWriteFloat64() throws NoSuchAlgorithmException {
		
		SecureRandom sr = SecureRandom.getInstanceStrong();
		
		Message am = new Message();
		for(int i=0; i < 10000; i++) {
			double f = sr.nextDouble();
			am.reset();
			am.putFloat(f, DataType.FLOAT64);
			am.rewind();
			double f2 = new Double(am.getFloat(DataType.FLOAT64)).doubleValue();
			Assert.assertEquals(f, f2, 0);
		}
	}
	
	
	

}
