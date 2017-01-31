package com.autonubil.someip.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@PropertySource(value = { "an.someip.server.properties" })
@EnableConfigurationProperties
public class ServerApp {

	private static Log log = LogFactory.getLog(ServerApp.class);
	
	@Bean
	public SomeIpServerTcpAccept serverSocket(@Autowired SomeIpServerConfig serverConfig) {
		if(serverConfig.getPort()>0) {
			log.info(" #################################");
			log.info(" # port   : "+serverConfig.getPort());
			log.info(" # backlog: "+serverConfig.getBacklog());
			log.info(" #################################");
			return new SomeIpServerTcpAccept(serverConfig.getPort(),serverConfig.getBacklog());
		}
		return null; 
	}

	@Bean
	public SomeIpServerTcpWorkerPool serverService(@Autowired SomeIpServerConfig serverConfig) {
		
		log.info(" #################################");
		log.info(" # pools  : "+serverConfig.getWorkers());
		log.info(" # c/pool : "+serverConfig.getConnectionsPerWorker());
		log.info(" # conns  : "+(serverConfig.getConnectionsPerWorker()*serverConfig.getWorkers()));
		log.info(" #################################");

		if(serverConfig.getWorkers()>0) {
			return new SomeIpServerTcpWorkerPool(serverConfig.getWorkers(),serverConfig.getConnectionsPerWorker());
		}
		return null; 
	}
	

	
	public static void main(String[] args) {
		SpringApplication.run(ServerApp.class, args);
	}
	
}
