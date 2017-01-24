package com.autonubil.someip.server;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "an.someip.server")
@Configuration
public class SomeIpServerConfig {

	private int port;
	private int backlog;
	private int workers;
	private int connectionsPerWorker;

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getBacklog() {
		return backlog;
	}

	public void setBacklog(int backlog) {
		this.backlog = backlog;
	}

	public int getWorkers() {
		return workers;
	}

	public void setWorkers(int workers) {
		this.workers = workers;
	}

	public int getConnectionsPerWorker() {
		return connectionsPerWorker;
	}

	public void setConnectionsPerWorker(int connectionsPerWorker) {
		this.connectionsPerWorker = connectionsPerWorker;
	}

}
