package com.autonubil.someip.common;

import java.io.IOException;

public interface SomeIpMessageHandler {

	public void handle(SomeIpMessage message) throws IOException;
	
}
