package com.autonubil.someip.common;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

public class SomeIpMessage {

	private int serviceId;  //Message ID (Service ID / Method ID) [16+16 = 32 bit]
	private int methodId;

	private int length = -1; //Length [32 bit]

	private int clientId; //Request ID (Client ID / Session ID) [16+16 = 32 bit]
	private int sessionId;

	private int protocolVersion; //Protocol Version [8 bit] Interface Version [8 bit] Message Type [8 bit] Return Code [8 bit]
	private int interfaceVersion;
	private int messageType;
	private int returnCode;
	
	private int headerStart = 0;
	private int payloadPosition = 0;
	private int payloadStart = 16;
	
	private String connectionId;
	private String poolId;
	
	private boolean locked = false;
	
	private ByteBuffer raw;
	
	public SomeIpMessage(int max) {
		this.raw = ByteBuffer.allocate(max+16);
		this.raw.limit(16);
	}
	
	public SomeIpMessage(ByteBuffer raw) {
		this(raw,raw.position(),raw.limit());
	}
	
	public SomeIpMessage(ByteBuffer raw, int offset, int limit) {
		this.locked = true;
		this.headerStart = offset;
		this.payloadStart = offset+16;
		this.raw = raw;
		readMetaData();
	}
	
	
	public int getServiceId() {
		return serviceId;
	}

	public void setServiceId(int serviceId) throws IOException {
		if(locked) {
			throw new IOException("trying to modify locked message");
		}
		this.serviceId = serviceId;
		writeMetaData();
	}

	public int getMethodId() {
		return methodId;
	}

	public void setMethodId(int methodId) throws IOException {
		if(locked) {
			throw new IOException("trying to modify locked message");
		}
		this.methodId = methodId;
		writeMetaData();
	}

	public int getLength() {
		return length;
	}

	public int getClientId() {
		return clientId;
	}

	public void setClientId(int clientId) throws IOException {
		if(locked) {
			throw new IOException("trying to modify locked message");
		}
		this.clientId = clientId;
		writeMetaData();
	}

	public int getSessionId() {
		return sessionId;
	}

	public void setSessionId(int sessionId) throws IOException {
		if(locked) {
			throw new IOException("trying to modify locked message");
		}
		this.sessionId = sessionId;
		int x = (clientId << 16) | (sessionId & 0xFFFF);
		this.raw.putInt(raw.position()+8,x);
		writeMetaData();
	}

	public int getProtocolVersion() {
		return protocolVersion;
	}

	public void setProtocolVersion(int protocolVersion) throws IOException {
		if(locked) {
			throw new IOException("trying to modify locked message");
		}
		this.protocolVersion = protocolVersion;
		writeMetaData();
	}

	public int getInterfaceVersion() {
		return interfaceVersion;
	}

	public void setInterfaceVersion(int interfaceVersion) throws IOException {
		if(locked) {
			throw new IOException("trying to modify locked message");
		}
		this.interfaceVersion = interfaceVersion;
		writeMetaData();
	}

	public int getMessageType() {
		return messageType;
	}

	public void setMessageType(int messageType) throws IOException {
		if(locked) {
			throw new IOException("trying to modify locked message");
		}
		this.messageType = messageType%256;
		writeMetaData();
	}

	public int getReturnCode() {
		return returnCode;
	}

	public void setReturnCode(int returnCode) throws IOException {
		if(locked) {
			throw new IOException("trying to modify locked message");
		}
		this.returnCode = returnCode;
		writeMetaData();
	}
	
	public void writeMetaData() {

		raw.position(headerStart);
		
		{
			int x = 0;
			x = (serviceId << 16) | x; 
			x = (methodId << 0) | x; 
			this.raw.putInt(x);
		}
		{
			this.raw.putInt(length);
		}
		{
			int x = 0;
			x = (clientId << 16) | x; 
			x = (sessionId << 0) | x; 
			this.raw.putInt(x);
		}
		{
			int x = 0;
			x = ((protocolVersion & 0xFF) << 24) | x; 
			x = ((interfaceVersion & 0xFF) << 16) | x; 
			x = ((messageType & 0xFF) << 8) | x; 
			x = ((returnCode & 0xFF) << 0) | x; 
			this.raw.putInt(headerStart+12,x);
		}
		
		raw.position(headerStart);
		
	}
	
	public void readMetaData() {

		raw.position(headerStart);

		{
			byte[] mId = new byte[4];
			raw.get(mId);
			serviceId = ((mId[0] & 0xFF) << 8) | (mId[1] &0xFF) ;
			methodId  = ((mId[2] & 0xFF) << 8) | (mId[3] &0xFF) ;
		}
		{
			length = raw.getInt();
		}
		{
			byte[] rId = new byte[4];
			raw.get(rId);
			
			int x = 0;
			x =  ((rId[0] & 0xFF) << 8) | x;
			x =  ((rId[1] & 0xFF) << 0) | x;
			clientId = x;
			sessionId  = ((rId[2] & 0xFF) << 8) | (rId[3] &0xFF) ;
		}
		{
			byte[] md = new byte[4];
			raw.get(md);
			
			protocolVersion = md[0] & 0xFF;
			interfaceVersion = md[1] & 0xFF;
			messageType = md[2] & 0xFF;
			returnCode = md[3] & 0xFF;
		}
		
		raw.position(headerStart);
	}
	
	
	public void writeTo(WritableByteChannel channel) throws IOException {
		writeMetaData();
		channel.write(raw);
	}
	
	public void writeTo(ByteBuffer buff) {
		writeMetaData();
		raw.position(headerStart);
		buff.put(raw);
	}

	public void position(int payloadPosition) {
		this.payloadPosition = payloadPosition;
	}
	
	public int position() {
		return this.payloadPosition;
	}
	
	public long getLong() {
		long out = raw.getLong(payloadStart+payloadPosition);
		payloadPosition+=8;
		return out;
	}
	
	public int getInt() {
		int out = raw.getInt(payloadStart+payloadPosition);
		payloadPosition+=4;
		return out;
	}
	
	public byte[] get(int length) {
		byte[] buff = new byte[length];
		raw.position(payloadStart+payloadPosition);
		raw.get(buff);
		raw.position(headerStart);
		return buff;
	}
	
	
	public void putLong(long x) throws IOException {
		if(locked) {
			throw new IOException("trying to modify locked message");
		}
		raw.limit(Math.max(raw.limit(), payloadStart+payloadPosition+8));
		raw.putLong(payloadStart+payloadPosition,x);
		payloadPosition+=8;
		length=raw.remaining()-8;
	}
	
	public void putInt(int x) throws IOException {
		if(locked) {
			throw new IOException("trying to modify locked message");
		}
		raw.limit(Math.max(raw.limit(), payloadStart+payloadPosition+4));
		raw.putInt(payloadStart+payloadPosition,x);
		payloadPosition+=4;
		length=raw.remaining()-8;
	}
	
	public void put(byte[] buff) throws IOException {
		if(locked) {
			throw new IOException("trying to modify locked message");
		}
		raw.position(payloadStart+payloadPosition);
		raw.limit(Math.max(raw.limit(), payloadStart+payloadPosition+buff.length));
		raw.put(buff);
		raw.position(headerStart);
		payloadPosition+=buff.length;
		length=raw.remaining()-8;
	}
	
	public int getTotalSize() {
		return raw.limit() - headerStart;
	}

	public String getConnectionId() {
		return connectionId;
	}

	public void setConnectionId(String connectionId) {
		this.connectionId = connectionId;
	}

	public String getPoolId() {
		return poolId;
	}

	public void setPoolId(String poolId) {
		this.poolId = poolId;
	}
	
	public SomeIpMessage reply() throws IOException {
		return reply(getTotalSize());
	}
	
	public SomeIpMessage reply(int size) throws IOException {
		SomeIpMessage out = new SomeIpMessage(size);
		out.setClientId(clientId);
		out.setServiceId(serviceId);
		out.setMethodId(methodId);
		out.setConnectionId(connectionId);
		out.setPoolId(poolId);
		out.setSessionId(sessionId);
		return out;
	}
	
	public static SomeIpMessage create(ByteBuffer buff) {
		if(buff.remaining()<16) {
			return null;
		}
		
		int length = buff.getInt(buff.position()+4);

		if(buff.remaining()<length+8) {
			return null;
		}
		ByteBuffer msg = buff.duplicate();
		msg.limit(msg.position()+length+8);
		buff.position(msg.limit());
		return new SomeIpMessage(msg);
	}
	
	public static SomeIpMessage create(int maxSize) {
		return new SomeIpMessage(maxSize);
	}
	
	
}
