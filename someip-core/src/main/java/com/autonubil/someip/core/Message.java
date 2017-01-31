package com.autonubil.someip.core;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import org.yaml.snakeyaml.introspector.BeanAccess;

import com.autonubil.someip.core.enums.ByteOrder;
import com.autonubil.someip.core.enums.DataType;

public class Message {
	
	protected ByteBuffer byteBuffer;

	private int offset = 0;
	private int readPosition = 16;
	private int writePosition = 16;
	private int capacity = 16;
	
	public Message() {
		this(4096);
	}

	public Message(int initialSize) {
		this(ByteBuffer.allocateDirect(initialSize));
	}

	public Message(byte[] buff) {
		this(ByteBuffer.wrap(buff));
	}

	public Message(ByteBuffer buff) {
		this.byteBuffer = buff;
		this.offset = buff.position();
		long length = getLength();
		if(length>8) {
			this.writePosition = (int)(length + 8);
		} else {
			this.writePosition = 16;
		}
		this.readPosition = 16;
		this.capacity = this.byteBuffer.remaining();
	}
	
	public void setServiceId(int id) {
		if((id&0xFFFF^id)>0) throw new IllegalArgumentException("service id cannot be higher than 0xFFFF (failed: "+Integer.toHexString(id)+")");
		put(0,id,DataType.UINT16,ByteOrder.BIG_ENDIAN);
	}

	public int getServiceId() {
		return (int)get(0,DataType.UINT16,ByteOrder.BIG_ENDIAN);
	}

	public void setMethodId(int id) {
		if((id&0x7fff^id)>2) throw new IllegalArgumentException("method id cannot be higher than 0x7fff (failed: "+Integer.toHexString(id)+")");
		put(2,id,DataType.UINT16,ByteOrder.BIG_ENDIAN);
	}

	public int getMethodId() {
		return (int)get(2,DataType.UINT16,ByteOrder.BIG_ENDIAN);
	}

	public long getLength() {
		return get(4,DataType.UINT32,ByteOrder.BIG_ENDIAN);
	}
	
	public void setLength() {
		put(4,getTotalSize()-8,DataType.UINT32,ByteOrder.BIG_ENDIAN);
	}
	
	public void setClientId(int id) {
		put(8,id,DataType.UINT16,ByteOrder.BIG_ENDIAN);
	}

	public int getClientId() {
		return (int)get(8,DataType.UINT16,ByteOrder.BIG_ENDIAN);
	}

	public void setSessionId(int id) {
		put(10,id,DataType.UINT16,ByteOrder.BIG_ENDIAN);
	}

	public int getSessionId() {
		return (int)get(10,DataType.UINT16,ByteOrder.BIG_ENDIAN);
	}

	public void setProtocolVersion(int protocolVersion) {
		put(12,protocolVersion,DataType.UINT8,ByteOrder.BIG_ENDIAN);
	}

	public int getProtocolVersion() {
		return (int)get(12,DataType.UINT8,ByteOrder.BIG_ENDIAN);
	}
	
	public void setInterfaceVersion(int interfaceVersion) {
		put(13,interfaceVersion,DataType.UINT8,ByteOrder.BIG_ENDIAN);
	}

	public int getInterfaceVersion() {
		return (int)get(13,DataType.UINT8,ByteOrder.BIG_ENDIAN);
	}

	public void setMessageType(int msgType) {
		put(14,msgType,DataType.UINT8,ByteOrder.BIG_ENDIAN);
	}

	public int getMessageType() {
		return (int)get(14,DataType.UINT8,ByteOrder.BIG_ENDIAN);
	}

	public void setReturnCode(int returnCode) {
		put(15,returnCode,DataType.UINT8,ByteOrder.BIG_ENDIAN);
	}

	public int getReturnCode() {
		return (int)get(15,DataType.UINT8,ByteOrder.BIG_ENDIAN);
	}

	
	public void rewind() {
		readPosition = 16;
	}
	
	public void reset() {
		readPosition = 16;
		writePosition = 16;
	}
	
	public int getTotalSize() {
		return writePosition;
	}
	
	public long readPosition() {
		return this.readPosition;
	}

	public double getFloat(DataType dataType) {
		return getFloat(dataType,ByteOrder.BIG_ENDIAN);
	}
	
	public double getFloat(DataType dataType, ByteOrder byteOrder) {
		double x = getFloat(readPosition,dataType,ByteOrder.BIG_ENDIAN);
		readPosition = readPosition + dataType.getBytes();
		return x;
	}
	
	public double getFloat(int position, DataType dataType, ByteOrder byteOrder) {

		if(byteOrder==null) throw new IllegalArgumentException("byte order must not be null");
		if(dataType==null) throw new IllegalArgumentException("data type must not be null");

		if(!dataType.isFloatingPoint()) {
			throw new RuntimeException("trying to retrieve a long as float ("+dataType+")");
		}
		
		byte[] buff = get(position, dataType.getBytes());

		return BinaryUtils.floatFromByteArray(buff, byteOrder);
	}
	
	
	public long get(DataType dataType) {
		return get(dataType,ByteOrder.BIG_ENDIAN);
	}

	public long get(DataType out, ByteOrder byteOrder) {
		long x = get(this.readPosition, out, byteOrder);
		this.readPosition = this.readPosition + out.getBytes();
		return x;
	}
	
	public long get(int position, DataType dataType, ByteOrder byteOrder) {

		if(byteOrder==null) throw new IllegalArgumentException("byte order must not be null");
		if(dataType==null) throw new IllegalArgumentException("data type must not be null");

		if(dataType.isFloatingPoint()) {
			throw new RuntimeException("trying to retrieve a float as a long ("+dataType+")");
		}
		

		byte[] buff = get(position, dataType.getBytes()); 
		
		long out = BinaryUtils.fromByteArray(buff, dataType.getBytes(), dataType.isSigned(), byteOrder);
		
		return out;
	}
	

	public byte[] get(int position, int length) {
		if(position < 0 || position+length > writePosition) {
			throw new IndexOutOfBoundsException("illegal search position: "+offset+" <= "+(offset+position)+" < "+(offset+position+length)+" <= "+(offset+writePosition));
		}
		byte[] out = new byte[length];
		for(int i=0;i<length;i++) {
			out[i] = byteBuffer.get(offset+position+i);
		}
		return out;
	}

	public Message putFloat(double d, DataType dataType) {
		putFloat(d,dataType,ByteOrder.BIG_ENDIAN);
		return this;
	}
	
	public Message putFloat(double d, DataType dataType, ByteOrder byteOrder) {
		putFloat(writePosition, d, dataType, byteOrder);
		writePosition = writePosition + dataType.getBytes();
		return this;
	}
	
	public Message putFloat(int position, double in, DataType dataType, ByteOrder byteOrder) {
		if(dataType==null) throw new IllegalArgumentException("data type must not be null");
		byte[] buff = BinaryUtils.floatToByteArray(in, dataType.getBytes(), byteOrder);
		put(position, buff);
		return this;
	}
	

	
	
	public Message put(long in, DataType out) {
		put(in,out,ByteOrder.BIG_ENDIAN);
		return this;
	}

	public void put(int position, long in, DataType out) {
		put(position,in,out,ByteOrder.BIG_ENDIAN);
	}
	
	public Message put(long in, DataType dataType, ByteOrder byteOrder) {
		if(dataType==null) throw new IllegalArgumentException("data type must not be null");
		byte[] b = BinaryUtils.toByteArray(in, dataType.getBytes(), dataType.isSigned(), byteOrder);
		put(b);
		return this;
	}

	public void put(int position, long in, DataType dataType, ByteOrder byteOrder) {
		if(dataType==null) throw new IllegalArgumentException("data type must not be null");
		byte[] b = BinaryUtils.toByteArray(in, dataType.getBytes(), dataType.isSigned(), byteOrder);
		put(position,b);
	}
	
	public Message put(byte[] data) {
		put(writePosition, data);
		writePosition = writePosition + data.length;
		return this;
	}

	public Message put(int position, byte[] data) {
		if(position < 0) {
			throw new IndexOutOfBoundsException("attempt to write bytes outside of available range "+(offset)+" <= "+(offset+position)+" < "+(offset+position+data.length)+" <= "+(offset+capacity));
		}else if (position+data.length > capacity) {
			throw new IndexOutOfBoundsException("attempt to write bytes outside of available range "+(offset)+" <= "+(offset+position)+" < "+(offset+position+data.length)+" <= "+(offset+capacity));
		}
		for(int i=0;i<data.length;i++) {
			byteBuffer.put(offset+position+i,data[i]);
		}
		return this;
	}

	public void writeTo(WritableByteChannel channel) throws IOException {
		put(4,getTotalSize()-8,DataType.UINT32,ByteOrder.BIG_ENDIAN);
		ByteBuffer bb = byteBuffer.duplicate();
		bb.position(offset);
		bb.limit(offset+writePosition);
		channel.write(bb);
	}
	
	public void writeTo(ByteBuffer buffer) throws IOException {
		put(4,getTotalSize()-8,DataType.UINT32,ByteOrder.BIG_ENDIAN);
		ByteBuffer bb = byteBuffer.duplicate();
		bb.position(offset);
		bb.limit(offset+writePosition);
		buffer.put(bb);
	}

	
	
}
