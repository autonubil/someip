package com.autonubil.someip.common.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class SomeIpServerTcpAccept implements  Runnable {

	private static Log log = LogFactory.getLog(SomeIpServerTcpAccept.class);

	private Thread t;
	
	private int port, backlog;
	private ServerSocketChannel socketChannel;
	
	@Autowired
	private SomeIpServerTcpWorkerPool serverService;

	public SomeIpServerTcpAccept(int port, int backlog) {
		this.port = port;
		this.backlog = backlog;
	}

	@PostConstruct
	public void init() throws IOException {
		log.info("start listening on port: "+port);
		this.socketChannel = ServerSocketChannel.open();
		this.socketChannel.bind(new InetSocketAddress(port),backlog);
		log.info("server socket is: "+this.socketChannel);
		this.start();
	}

	
	private void start() {
		
		try {
			t.interrupt();
		} catch (Exception e) {
		}
		
		Thread t = new Thread(this);
		t.start();

	}
	
	public void run() {
		while (true) {
			try {
				log.info("waiting for incoming connection ... ");
				SocketChannel sc = socketChannel.accept();
				sc.configureBlocking(false);
				sc.socket().setTcpNoDelay(true);
				sc.socket().setKeepAlive(true);
				sc.socket().setPerformancePreferences(0, 10, 5);
				sc.socket().setSendBufferSize(2048);
				if(!serverService.assign(sc)) {
					log.info("new incoming connection ... could not find anyone to handle it!");
					try {
						sc.close();
					} catch (Exception e) {
					}
				} else {
					log.info("new incoming connection ... done!");
				}
				
			} catch (Exception e) {
				log.error("error accepting connection: ",e);
				return;
			}
		}

	}
	
	
	
	@PreDestroy
	public void exit() {
		try {
			this.socketChannel.close();
		} catch (IOException e) {
			log.error("shutdown of server socket failed: ", e);
		}
	}

}
