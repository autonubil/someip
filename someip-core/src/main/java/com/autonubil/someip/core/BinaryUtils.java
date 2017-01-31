package com.autonubil.someip.core;

import com.autonubil.someip.core.enums.ByteOrder;

public class BinaryUtils {

	public static double floatFromByteArray(byte[] buff, ByteOrder byteOrder) {
		
		int bytes = buff.length;
		int p = 0;
		int s = 1;
		
		if(byteOrder == ByteOrder.LITTLE_ENDIAN) {
			p = bytes-1;
			s = -1;
		}

		if(bytes==4) {

			int v = 0;  
			
			for(int i=0;i<bytes;i++) {
				v = (int)((v << 8) | (int)buff[p] & 0xFF);
				p+=s;
			}

			return Float.intBitsToFloat(v);
			
		} else if(bytes==8) {

			long v = 0;  
			for(int i=0;i<bytes;i++) {
				v = (long)((v << 8) | (int)buff[p] & 0xFF);
				p+=s;
			}
			return Double.longBitsToDouble(v);
			
		} else {
			throw new IllegalArgumentException("invalid length: "+bytes);
		}
	}
	
	

	public static byte[] floatToByteArray(double in, int bytes, ByteOrder byteOrder) {
		
		if(byteOrder==null) throw new IllegalArgumentException("byte order must not be null");

		int p = 0;
		int s = 1;
		
		if(byteOrder == ByteOrder.BIG_ENDIAN) {
			p = bytes-1;
			s = -1;
		}

		if(bytes==4) {
			byte[] buff = new byte[bytes];
			int bits = Float.floatToRawIntBits(new Double(in).floatValue());

			for(int i=0;i<bytes;i++) {
				buff[p] = (byte)(bits & 0xFF);
				bits = bits >> 8;
				p+=s;
			}
			return buff;
		} else if(bytes==8) {
			byte[] buff = new byte[bytes];
			long bits = Double.doubleToLongBits(in);
			for(int i=0;i<bytes;i++) {
				buff[p] = (byte)(bits & 0xFF);
				bits = bits >> 8;
				p+=s;
			}
			return buff;
		} else {
			throw new IllegalArgumentException("invalid length for float: "+bytes);
		}
	}
	
	public static long fromByteArray(byte[] buff, int bytes, boolean signed, ByteOrder byteOrder) {
		long x = fromByteArray(buff, bytes, byteOrder);
		long valueMask = (0x1l << (8*bytes)-1) -1;
		if(signed) {
			long signMask = ~valueMask;
			if((x & signMask) > 0) {
				x = (x & valueMask) * -1;
			}
		}
		return x;
	}

	public static long fromByteArray(byte[] buff, int bytes, ByteOrder byteOrder) {
	
		int p = 0;
		int s = 1;
	
		if(byteOrder == ByteOrder.LITTLE_ENDIAN) {
			p = bytes-1;
			s = -1;
		}
		
		long x = 0;
		
		for(int i=0;i<bytes;i++) {
			x = (x << 8);
			x = x | ((int)buff[p]  & 0xFF);
			p += s;
		}
		
		return x;
	}

	public static byte[] toByteArray(long in, int bytes, boolean signed, ByteOrder byteOrder) {
	
		long bits = Math.abs(in);
	
		long valueMask = (0x1l << (8*bytes)) -1;
		//System.err.println("full byte: "+Long.toBinaryString(valueMask));
	
		if(signed) {
			valueMask = valueMask >> 1l;
			//System.err.println("one less: "+Long.toBinaryString(valueMask));
		} else if (in < 0) {
			throw new IllegalArgumentException("invalid value: "+bytes+" bytes, "+(signed?"signed":"unsigned")+": "+Long.toHexString(in));
		}
	
		if((bits & (~valueMask)) > 0) {
			//System.err.println(Long.toBinaryString(bits));
			//System.err.println(Long.toBinaryString(valueMask));
			throw new IllegalArgumentException("invalid value: "+bytes+" bytes, "+(signed?"signed":"unsigned")+": "+Long.toHexString(in));
		}
		
		if(in < 0) {
			bits = bits | (0x1 << ((8*bytes)-1));
		}
		
		return toByteArray(bits, bytes, byteOrder);
		
	}

	public static byte[] toByteArray(long in, int bytes, ByteOrder byteOrder) {
	
		if(byteOrder==null) throw new IllegalArgumentException("byte order must not be null");
	
		byte[] o = new byte[bytes];
		
		int p = bytes-1;
		int s = -1;
		
		long bits = in;
		
		if(byteOrder == ByteOrder.LITTLE_ENDIAN) {
			p = 0;
			s = 1;
		}
		
		for(int i=0;i<bytes;i++)  {
			o[p] = (byte)(bits & 0xFF); 
			bits = bits >> 8;
			p += s;
			//System.err.println(in+": "+Long.toHexString(~in)+": "+Long.toHexString(~bits));
		}
	
		return o;
	}

}
