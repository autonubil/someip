package com.autonubil.someip.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class PseudoRingBuffer {

	private ByteBuffer readBufferRead;
	private ByteBuffer readBufferGet;

	private ByteBuffer writeBufferWrite;
	private ByteBuffer writeBufferPut;
	
	private ReadableByteChannel readChannel;
	private WritableByteChannel writeChannel;

	public PseudoRingBuffer(int size, ReadableByteChannel readChannel, WritableByteChannel writeChannel) {
		readBufferRead = ByteBuffer.allocate(size);
		readBufferGet = (ByteBuffer)readBufferRead.duplicate().limit(0);
		writeBufferPut = ByteBuffer.allocate(size);
		writeBufferWrite = (ByteBuffer)writeBufferPut.duplicate().limit(0);
		this.readChannel = readChannel;
		this.writeChannel = writeChannel;
	}
	
	public boolean put(Message offer) throws IOException {
		if(writeBufferWrite.remaining()==0) {
			// we're empty, so we compact here
			writeBufferWrite.position(0);
			writeBufferWrite.limit(0);
			writeBufferPut.position(0);
		} 
		if (writeBufferPut.position()+offer.getTotalSize() > writeBufferPut.capacity()) {
			// next one won't fit, so we have to compact
			writeBufferWrite.compact();
			writeBufferWrite.limit(writeBufferWrite.position());
			writeBufferWrite.position(0);
			writeBufferPut.position(writeBufferWrite.limit());
		}
		if(writeBufferPut.remaining() >= offer.getTotalSize()) {
			offer.writeTo(writeBufferPut);
			writeBufferWrite.limit(writeBufferPut.position());
			return true;
		}
		return false;
	}
	
	public void write() throws IOException {
		writeChannel.write(writeBufferWrite);
	}
	
	public Message get() {

		if(readBufferGet.remaining()<16) {
			return null;
		}
		
		int length = readBufferGet.getInt(readBufferGet.position()+4);

		if(readBufferGet.remaining()<length+8) {
			return null;
		}
		ByteBuffer msg = readBufferGet.duplicate();
		msg.limit(msg.position()+length+8);
		readBufferGet.position(msg.limit());
		return new Message(msg);		
	}
	
	
	public void read() throws IOException {
		if(readBufferGet.remaining()==0) {
			//we're empty, we might as well compact
			readBufferGet.position(0);
			readBufferGet.limit(0);
			readBufferRead.position(0);
		}
		if(readBufferRead.remaining()<16) {
			//we're at the end, with less than a header length to go. we have to compact
			readBufferGet.compact();
			readBufferGet.limit(readBufferGet.position());
			readBufferGet.position(0);
			readBufferRead.position(readBufferGet.limit());
		}
		
		readChannel.read(readBufferRead);
		/**
		long x1 = readBufferRead.position();
		long x2 = readBufferRead.position();
		System.err.println("read bytes: "+(x2-x1));
		**/
		readBufferGet.limit(readBufferRead.position());
		
	}
	
	
}
