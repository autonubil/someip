package com.autonubil.someip.core.serialization;

import com.autonubil.someip.core.Message;

public interface Serializer {

	public boolean supports(Object o);
	
	public Message serialize(int clientId, int sessionId, int returnCode, Object o) throws SerializationException;
	
}
