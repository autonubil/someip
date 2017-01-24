package com.autonubil.someip.common.server;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import com.autonubil.someip.common.SomeIpMessage;
import com.autonubil.someip.common.SomeIpMessageHandler;
import com.autonubil.someip.common.stats.TcpWorkerStats;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class SomeIpServerTcpWorkerPool implements SomeIpMessageHandler {
	
	private static Log log = LogFactory.getLog(SomeIpServerTcpWorkerPool.class);

	private int pools, connectionsPerPool;
	
	private List<SomeIpServerTcpWorker> serverPool = new ArrayList<SomeIpServerTcpWorker>();
	private Map<String,SomeIpServerTcpWorker> returnChannels = new HashMap<String, SomeIpServerTcpWorker>();
	
	private ScheduledExecutorService e = Executors.newScheduledThreadPool(10,new CustomizableThreadFactory("worker-pool"));

	private long handledMessages = 0;
	
	public SomeIpServerTcpWorkerPool(int pools, int connectionsPerPool) {
		this.pools = pools;
		this.connectionsPerPool = connectionsPerPool;
	}
	
	@PostConstruct
	public void init() throws IOException {
		for(int i=0;i<pools;i++) {
			SomeIpServerTcpWorker p = new SomeIpServerTcpWorker(connectionsPerPool,this);
			serverPool.add(p);
			returnChannels.put(p.getId(),p);
		}
		e.scheduleAtFixedRate(new Runnable() {
			public void run() {
				for(SomeIpServerTcpWorker p : serverPool) {
					p.checkConnections();
				}
			}
		},10,10,TimeUnit.SECONDS);
		
		e.scheduleAtFixedRate(new Runnable() {
			
			public void run() {
				List<String> poolIds = new ArrayList<String>(returnChannels.keySet());
				Collections.sort(poolIds);
				List<TcpWorkerStats> ps = new ArrayList<TcpWorkerStats>();
				for(String s : poolIds) {
					ps.add(returnChannels.get(s).getStats());
				}
				
				try {
					String s = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).writeValueAsString(ps);
					log.info("       pools: "+serverPool.size());
					log.info("msgs handled: "+handledMessages);
					log.info("\n"+s+"\n");
					
				} catch (Exception e) {
					// TODO: handle exception
				}
				
				
				
			}
		}, 10, 10, TimeUnit.SECONDS);
		
	}
	
	public void reply(SomeIpMessage msg) {
		returnChannels.get(msg.getPoolId()).send(msg);
	}
	
	public void handle(SomeIpMessage msg) throws IOException {
		handledMessages++;
		SomeIpMessage reply = msg.reply(msg.getTotalSize()+8);
		reply.putLong(System.currentTimeMillis());
		reply.putLong(msg.getLong());
		reply.put(msg.get(msg.getTotalSize()-24));
		try {
			returnChannels.get(msg.getPoolId()).send(reply);
		} catch (Exception e) {
			log.info("error replying .... ",e);
		}
	}
	
	public boolean assign(SocketChannel channel) throws IOException, InterruptedException {
		log.info("new connection from: "+channel.getRemoteAddress());
		Collections.sort(serverPool);
		for(SomeIpServerTcpWorker p : serverPool) {
			if(p.assign(channel)) {
				log.info("connection from: "+channel.getRemoteAddress()+" assigned to: "+p.getId());
				return true;
			}
		}
		return false;
	}
	
	

}
