package com.autonubil.someip.common;

import java.io.IOException;

public interface SomeIpConnectionListener {
	
	public void connectionClosed(SomeIpTcpConnection connection);

	public void handle(SomeIpMessage m) throws IOException;

}
