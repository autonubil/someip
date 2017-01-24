package com.autonubil.someip.client;


import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@PropertySource(value = { "an.someip.client.properties" })
@EnableConfigurationProperties
public class SomeIpClientApp {

	
	private static Log log = LogFactory.getLog(SomeIpClientApp.class);
	
	@Bean
	public SomeIpTestClientPool serverSocket(@Autowired SomeIpClientConfig cc) throws IOException {
		log.info(" #################################");
		log.info(" ## starting client connections ");
		log.info(" #################################");
		return new SomeIpTestClientPool(cc.getHost(),cc.getPort(),cc.getConnections(),cc.getConnectionDelay(),cc.getInterval(),cc.getUnit());
	}

	
	public static void main(String[] args) {
		ApplicationContext ctx = SpringApplication.run(SomeIpClientApp.class, args);
	}
	
}
