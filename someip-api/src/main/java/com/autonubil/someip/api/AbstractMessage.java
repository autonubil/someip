package com.autonubil.someip.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import com.autonubil.someip.api.annotations.ByteOrder;
import com.autonubil.someip.api.annotations.CharacterEncoding;
import com.autonubil.someip.api.annotations.DataType;

public abstract class AbstractMessage implements Message {
	
	protected ByteBuffer byteBuffer;

	private int offset = 0;
	private int readPosition = 16;
	private int writePosition = 16;
	private int capacity = 16;
	
	
	public AbstractMessage() {
		this(4096);
	}

	public AbstractMessage(int initialSize) {
		this(ByteBuffer.allocateDirect(initialSize));
	}

	public AbstractMessage(byte[] buff) {
		this(ByteBuffer.wrap(buff));
	}

	public AbstractMessage(ByteBuffer buff) {
		this.byteBuffer = buff;
		this.offset = buff.position();
		this.readPosition = 16;
		this.writePosition = 16;
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

		byte[] buff = get(position, dataType.getBytes()); 
		
		long out = fromByteArray(buff, dataType, byteOrder);
		
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
	
	public long fromByteArray(byte[] buff, DataType dataType, ByteOrder byteOrder) {

		if(byteOrder==null) throw new IllegalArgumentException("byte order must not be null");
		if(dataType==null) throw new IllegalArgumentException("data type must not be null");
		
		int bytes = dataType.getBytes();
		long x = 0;

		if(byteOrder == ByteOrder.BIG_ENDIAN) {
			for(byte p=0;p<bytes;p++) {
				int h = ((int)buff[p]  & 0xFF);
				//System.err.println("get("+(offset+position+p)+"): "+Long.toHexString(h));
				x = (x << 8) | h;
			}
		} else {
			for(byte p=0;p<bytes;p++) {
				int h = ((int)buff[bytes-(p+1)]  & 0xFF);
				//System.err.println("get("+(offset+position+p)+"): "+Long.toHexString(h));
				x = (x << 8) | h;
			}
		}

		if(dataType.isSigned()) {
			long s = x & dataType.getMask();
			if((x & dataType.getSignMask()) > 0) {
				x = s * -1;
			} else {
				x = s;
			}
		}
		return x;
	}
	
	public Message putFixed(String string, int fixedLength, CharacterEncoding encoding) throws IOException {
		
		byte[] b = new byte[fixedLength];
		byte[] bs;
		
		if(encoding == CharacterEncoding.UTF8) {
			bs = string.getBytes("utf-8");
		} else if(encoding == CharacterEncoding.UTF16BE || encoding == CharacterEncoding.UTF16LE) {
			bs = string.getBytes("utf-8");
		} else {
			throw new UnsupportedEncodingException();
		}
		
		System.arraycopy(bs, 0, b, 0, bs.length);
		
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
		byte[] b = toByteArray(in, dataType, byteOrder);
		put(b);
		return this;
	}

	public void put(int position, long in, DataType dataType, ByteOrder byteOrder) {
		byte[] b = toByteArray(in, dataType, byteOrder);
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
			//System.err.println("put("+(offset+position+i)+"): "+Long.toHexString((int)data[i] & 0xFF));			
		}
		return this;
	}

	public byte[] toByteArray(long in, DataType dataType, ByteOrder byteOrder) {

		if(byteOrder==null) throw new IllegalArgumentException("byte order must not be null");
		if(dataType==null) throw new IllegalArgumentException("data type must not be null");
		
		int bytes = dataType.getBytes();

		long min = 0;
		long max = 0;
		
		long bits = 0;
		
		if(dataType.isSigned()) {
			max = dataType.getSignMask() -1;
			min = -1 * max;
			bits  = Math.abs(in);
			if(in < 0) {
				bits = bits | dataType.getSignMask();
			}
		} else {
			max = dataType.getMask();
			bits = in;
		}
		
		if(in > max || in < min) throw new IllegalArgumentException("invalid value for "+dataType+" ("+max+" > "+in+" > "+min+")");

		byte[] o = new byte[bytes];
		
		if(byteOrder == ByteOrder.BIG_ENDIAN) {
			int pos = bytes;
			for(byte p=0;p<bytes;p++) {
				pos--;
				o[pos] = (byte)(bits & 0xFF); 
				bits = bits  >> 8;
			}
		} else {
			for(byte p=0;p<bytes;p++) {
				o[p] = (byte)(bits & 0xFF);
				bits = bits  >> 8;
			}
		}
		return o;
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
