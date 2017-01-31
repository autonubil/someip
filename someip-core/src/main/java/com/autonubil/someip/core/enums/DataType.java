package com.autonubil.someip.core.enums;

public enum DataType {
	
	UINT8(1,false,false,0xFFl,0x00l),
	SINT8(1,true,false,0x7Fl,0x80l),
	UINT16(2,false,false,0xFFFFl,0x0000l),
	SINT16(2,true,false,0x7FFFl,0x8000l),
	UINT32(4,false,false,0xFFFFFFFFl,0x00000000l),
	SINT32(4,true,false,0x7FFFFFFFl,0x80000000l),
	FLOAT32(4,false,true,0,0),
	FLOAT64(8,false,true,0,0)
	;
	
	private final int bytes;
	private final boolean fp;
	private final boolean signed;
	private final long mask;
	private final long signMask;
	
	private DataType(int bytes, boolean signed, boolean fp, long mask, long signMask) {
		this.bytes = bytes;
		this.signed = signed;
		this.fp = fp;
		this.mask = mask;
		this.signMask = signMask;
	}

	public int getBytes() {
		return bytes;
	}

	public boolean isSigned() {
		return signed;
	}

	public boolean isFloatingPoint() {
		return fp;
	}

	public long getMask() {
		return mask;
	}

	public long getSignMask() {
		return signMask;
	}
	
	
	
}