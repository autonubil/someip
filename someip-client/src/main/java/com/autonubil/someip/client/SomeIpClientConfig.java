package com.autonubil.someip.client;

import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "an.someip.client")
@Configuration
public class SomeIpClientConfig {

	private static Log log = LogFactory.getLog(SomeIpClientConfig.class);

	private String host;
	private int port;
	private int connections;
	private int connectionDelay;
	private int interval;
	private TimeUnit unit;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getConnections() {
		return connections;
	}

	public void setConnections(int connections) {
		this.connections = connections;
	}

	public int getConnectionDelay() {
		return connectionDelay;
	}

	public void setConnectionDelay(int connectionDelay) {
		this.connectionDelay = connectionDelay;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public TimeUnit getUnit() {
		return unit;
	}

	public void setUnit(TimeUnit unit) {
		this.unit = unit;
	}

}
