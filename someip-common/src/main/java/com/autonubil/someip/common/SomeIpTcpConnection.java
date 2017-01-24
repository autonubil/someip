package com.autonubil.someip.common;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.autonubil.someip.common.utils.PseudoRingBuffer;

public class SomeIpTcpConnection {
	
	private static Log log = LogFactory.getLog(SomeIpTcpConnection.class);
	
	private SocketChannel socketChannel;
	private PseudoRingBuffer ringBuffer;
	
	private String id = UUID.randomUUID().toString();
	private SomeIpConnectionListener listener;

	public SomeIpTcpConnection(SocketChannel socketChannel, int buffSize, SomeIpConnectionListener listener) {
		this.socketChannel = socketChannel;
		this.listener = listener;
		ringBuffer = new PseudoRingBuffer(100000, this.socketChannel,this.socketChannel);
	}

	public boolean send(SomeIpMessage message) {
		try {
			if(ringBuffer.put(message)) {
				ringBuffer.write();
				return true;
			}
			ringBuffer.write();
			return false;
		} catch (Exception e) {
			log.error("error in send()",e);
			close();
			return false;
		}
	}
	
	
	public void receive() throws IOException {
		try {
			ringBuffer.read();
			SomeIpMessage m = null;
			while((m=ringBuffer.get())!=null) {
				m.setConnectionId(id);
				listener.handle(m);
			}
		} catch (Exception e) {
			log.error("error in receive()",e);
			close();
		}
	}

	public String getId() {
		return id;
	}

	public void close() {
		try {
			socketChannel.close();
		} catch (Exception e2) {
		}
		listener.connectionClosed(this);
	}
	
	
}
