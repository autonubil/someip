package com.autonubil.someip.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import com.autonubil.someip.common.SomeIpTcpConnection;
import com.autonubil.someip.common.SomeIpConnectionListener;
import com.autonubil.someip.common.SomeIpMessage;

public class SomeIpTestClientPool implements SomeIpConnectionListener {

	private static Log log = LogFactory.getLog(SomeIpTestClientPool.class);
	
	private ScheduledExecutorService e = Executors.newScheduledThreadPool(20, new CustomizableThreadFactory("client-pool"));
	
	private Selector selector; 
	
	private Map<String,SelectionKey> connections = new HashMap<String, SelectionKey>();
	
	private String host;
	private int port;
	private int numConnections;
	private int connectionDelay;
	private int interval;
	private TimeUnit unit;
	
	private AtomicLong messagesSent = new AtomicLong();
	private AtomicLong messagesReceived = new AtomicLong();
	private AtomicLong latency = new AtomicLong();
	private AtomicLong latencyDelivered = new AtomicLong();
	
	public SomeIpTestClientPool(
			String host,
			int port,
			int connections,
			int connectionDelay,
			int interval,
			TimeUnit unit
		) throws IOException {
		this.host = host;
		this.port = port;
		this.connectionDelay = connectionDelay;
		this.numConnections = connections;
		this.interval = interval;
		this.unit = unit;
		
		selector = Selector.open();

		log.info(" connections: connecting up to "+numConnections+" clients (one every "+connectionDelay+" "+unit+")");
		
		e.scheduleWithFixedDelay(new ConnectRunnable(), 0, connectionDelay, unit);
		e.scheduleWithFixedDelay(new WatchRunnable(), 0, 10, TimeUnit.SECONDS);
		e.scheduleWithFixedDelay(new ReceiveRunnable(), 0, 1, TimeUnit.MILLISECONDS);
		e.scheduleWithFixedDelay(new SendRunnable(), 0, interval, unit);
		e.scheduleWithFixedDelay(new StatsRunnable(), 10, 10, TimeUnit.SECONDS);
		
	}

	public void connectionClosed(SomeIpTcpConnection connection) {
		try {
			connections.get(connection.getId()).cancel();
		} catch (Exception e) {
		}
		try {
			connections.remove(connection.getId());
		} catch (Exception e) {
		}
	}

	public void handle(SomeIpMessage m) throws IOException {
		messagesReceived.incrementAndGet();
		long r = System.currentTimeMillis();
		long d = m.getLong();
		long s = m.getLong();
		latency.addAndGet(r-s);
		latencyDelivered.addAndGet(d-s);
	}
	
	
	public class ReceiveRunnable implements Runnable {
		
		public void run() {
			try {
				if(selector.keys().size()==0) {
					Thread.sleep(100);
					return;
				}
				int readyChannels = selector.select();
				if (readyChannels == 0) return;
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
	
	public class SendRunnable implements Runnable {
		
		public void run() {
			try {
				List<SelectionKey> ks = new ArrayList<SelectionKey>(connections.values());

				byte[] buff = new byte[1024];
				
				for(SelectionKey k : ks) {
					SomeIpMessage m = new SomeIpMessage(3000);
					//m.putLong(System.nanoTime());
					m.putLong(System.currentTimeMillis());
					m.put(buff);
					if(((SomeIpTcpConnection)k.attachment()).send(m)) {
						messagesSent.incrementAndGet();
					}
				}
			} catch (Exception e) {
				log.error("error accepting connection: ",e);
				return;
			}

		}	

	}
	
	public class ConnectRunnable implements Runnable {
		
		public void run() {
			try {
				if(connections.size() < numConnections) {
					SocketAddress sa = new InetSocketAddress(InetAddress.getByName(host),port);
					log.info("connecting: "+(connections.size()+1)+" of "+numConnections+" to: "+sa);
					SocketChannel sc = SocketChannel.open(sa);
					sc.configureBlocking(false);
					sc.socket().setTcpNoDelay(true);
					sc.socket().setSendBufferSize(2048);
					sc.socket().setKeepAlive(true);
					sc.socket().setPerformancePreferences(0, 10, 5);
					SomeIpTcpConnection c = new SomeIpTcpConnection(sc, 10000, SomeIpTestClientPool.this);
					selector.wakeup();
					SelectionKey k = sc.register(selector, SelectionKey.OP_READ, c);
					connections.put(c.getId(), k);
					log.info("connecting: "+(connections.size()+1)+" of "+numConnections+" to: "+sa+" ... done!");
				}
			} catch (Exception e) {
				log.error("error connecting: ",e);
				return;
			}
		}	

	}
	
	public class WatchRunnable implements Runnable {
		
		public void run() {
			try {
				for(SelectionKey k : connections.values()) {
					if(!k.channel().isOpen()) {
						((SomeIpTcpConnection)k.attachment()).close();
					}
				}
			} catch (Exception e) {
				log.error("error connecting: ",e);
				return;
			}
		}	

	}
	
	public class StatsRunnable implements Runnable {
		
		public void run() {
			try {
				log.info("msgs sent: "+messagesSent.get());
				log.info("msgs rcvd: "+messagesReceived.get());
				log.info("msgs drpd: "+(messagesSent.get() - messagesReceived.get()));
				
				double ms1 = Math.round((double)latency.get() / (double)messagesReceived.get() * 10d) / 10d;
				log.info("avg lat  : "+ms1+" ms (roundtrip)");
				double ms2 = Math.round((double)latencyDelivered.get() / (double)messagesReceived.get() * 10d) / 10d;
				log.info("         : "+ms2+" ms (processed)");
			} catch (Exception e) {
				log.error("error connecting: ",e);
				return;
			}
		}	

	}
	
	
}
