package com.autonubil.someip.common.stats;

public class TcpWorkerStats {
	
	private long discardedMessages;
	private long receivedMessages;
	private long responseMessages;
	private long receivedBytes;
	private long responseBytes;

	private int maxSize;
	private int currentSize;
	
	private int connectionsHandled;
	
	private long time = System.currentTimeMillis();
	
	public TcpWorkerStats() {
	}
	
	
	public void clear() {
		discardedMessages = 0;
		receivedMessages = 0;
		responseMessages = 0;
		receivedBytes = 0;
		responseBytes = 0;
		time = System.currentTimeMillis();
	}

	public long addDiscardedMessage() {
		return discardedMessages++;
	}

	public long getDiscardedMessages() {
		return discardedMessages;
	}

	public long addReceivedMessage() {
		return receivedMessages++;
	}

	public long getReceivedMessages() {
		return receivedMessages;
	}

	public long addResponseMessage() {
		return responseMessages++;
	}


	public long getResponseMessages() {
		return responseMessages;
	}

	public long addReceivedBytes(int bytes) {
		return responseBytes += bytes;
	}

	public long getReceivedBytes() {
		return receivedBytes;
	}

	public long addResponseBytes(int bytes) {
		return responseBytes += bytes;
	}

	public long getResponseBytes() {
		return responseBytes;
	}
	
	public long getTime() {
		return time;
	}


	public int getMaxSize() {
		return maxSize;
	}


	public int getCurrentSize() {
		return currentSize;
	}


	public int addConnectionsHandled() {
		return connectionsHandled++;
	}

	public int getConnectionsHandled() {
		return connectionsHandled;
	}


	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}


	public void setCurrentSize(int currentSize) {
		this.currentSize = currentSize;
	}
	
	
	

}
