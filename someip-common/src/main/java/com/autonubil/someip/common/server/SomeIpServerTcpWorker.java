package com.autonubil.someip.common.server;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.autonubil.someip.common.SomeIpTcpConnection;
import com.autonubil.someip.common.SomeIpConnectionListener;
import com.autonubil.someip.common.SomeIpMessage;
import com.autonubil.someip.common.SomeIpMessageHandler;
import com.autonubil.someip.common.stats.TcpWorkerStats;

public class SomeIpServerTcpWorker implements Comparable<SomeIpServerTcpWorker>, SomeIpConnectionListener {

	private static Log log = LogFactory.getLog(SomeIpServerTcpWorker.class);
	
	private int max;

	private Thread receiveThread;
	
	private Thread sendThread;
	
	private Selector selector;
	
	private LinkedBlockingQueue<SocketChannel> accepted = new LinkedBlockingQueue<SocketChannel>();
	
	private Map<String,SelectionKey> keys = new HashMap<String, SelectionKey>();
	
	private String id = UUID.randomUUID().toString();
	
	private SomeIpMessageHandler handler;
	
	private LinkedBlockingQueue<SomeIpMessage> messages = new LinkedBlockingQueue<SomeIpMessage>();
	
	private TcpWorkerStats stats = new TcpWorkerStats();
	
	public SomeIpServerTcpWorker(int max, SomeIpMessageHandler handler) throws IOException {
		this.max = max;
		this.selector = Selector.open();
		this.handler = handler;
		start();
	}
	
	private void start() {
		synchronized (this) {
			receiveThread = new Thread(new ReceiveRunnable(),"receive-thread-"+id);
			receiveThread.setDaemon(true);
			receiveThread.setPriority(Thread.MIN_PRIORITY);
			receiveThread.start();
			
			sendThread = new Thread(new SendRunnable(),"send-thread-"+id);
			sendThread.setDaemon(true);
			sendThread.setPriority(Thread.MIN_PRIORITY);
			sendThread.start();
		}

	}
	
	public void send(SomeIpMessage msg) {
		messages.add(msg);
	}
	
	
	public void handle(SomeIpMessage msg) throws IOException {
		msg.setPoolId(id);
		stats.addReceivedMessage();
		stats.addReceivedBytes(msg.getTotalSize());
		handler.handle(msg);
	}
	
	public Integer size() {
		return keys.size();
	}
	
	public int compareTo(SomeIpServerTcpWorker o) {
		return this.size().compareTo(o.size());
	}

	public boolean assign(SocketChannel channel) throws IOException, InterruptedException {
		if(keys.size()+accepted.size()>=max) {
			return false;
		}
		accepted.put(channel);
		selector.wakeup();
		return true;
	}


	public void connectionClosed(SomeIpTcpConnection connection) {
		try {
			keys.get(connection.getId()).cancel();
		} catch (Exception e) {
		}
		try {
			keys.remove(connection.getId());
		} catch (Exception e) {
		}
		
	}

	public String getId() {
		return id;
	}

	private void add(SocketChannel channel) throws ClosedChannelException {
		SomeIpTcpConnection sic = new SomeIpTcpConnection(channel, 10000, this);
		SelectionKey key = channel.register(selector, SelectionKey.OP_READ);
		key.attach(sic);
		keys.put(sic.getId(), key);
		try {
			log.info("connection from: "+channel.getRemoteAddress()+" added!");
		} catch (IOException e) {
		}
	}
	
	
	private class ReceiveRunnable implements Runnable {

		public void run() {
			while (true) {
				try {
					if(selector.keys().size()==0) {
						SocketChannel sc = accepted.take();
						add(sc);
						continue;
					}
					while(accepted.size()>0) {
						SocketChannel sc = accepted.poll();
						if(sc!=null) {
							add(sc);
						}
					}
					int readyChannels = selector.select();
					if (readyChannels == 0) continue;
					Set<SelectionKey> selectedKeys = selector.selectedKeys();
					Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
					while (keyIterator.hasNext()) {
						SelectionKey key = keyIterator.next();
						SomeIpTcpConnection c = ((SomeIpTcpConnection)key.attachment());
						if(c!=null) {
							if (key.isReadable()) {
								c.receive();
							}
						}
						keyIterator.remove();
					}
				} catch (Exception e) {
					log.error("error accepting connection: ",e);
					return;
				}
			}

		}	
		

	}
	
	private class SendRunnable implements Runnable {

		public void run() {
			while (true) {
				try {
					// how?
					SomeIpMessage m = messages.take();
					SelectionKey k = keys.get(m.getConnectionId());
					if(k==null) {
						stats.addDiscardedMessage();
						continue;
					}
					
					SomeIpTcpConnection c = (SomeIpTcpConnection) k.attachment();
					if(c==null) {
						stats.addDiscardedMessage();
						continue;
					}
					
					if(c.send(m)) {
						stats.addResponseMessage();
						stats.addResponseBytes(m.getTotalSize());
					}

				} catch (Exception e) {
					log.error("error accepting connection: ",e);
					return;
				}
			}

		}	
		

	}

	public TcpWorkerStats getStats() {
		stats.setMaxSize(max);
		stats.setCurrentSize(keys.size());
		return stats;
	}

	public void checkConnections() {
		List<SelectionKey> ks = new ArrayList<SelectionKey>(keys.values());
		for(SelectionKey k : ks) {
			try {
				if(!k.channel().isOpen()) {
					((SomeIpTcpConnection)k.attachment()).close();
				}
			} catch (Exception e) {
			}
		}
		// TODO Auto-generated method stub
		
	}
	
	

}
