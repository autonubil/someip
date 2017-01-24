package com.autonubil.someip.api;

public interface Message {

	public static final int SOME_IP_1 = 0x01;
	
	
	public static final int MAGIC_SERVICE_ID = 0xFFFF;
	public static final int MAGIC_METHOD_ID = 0x0000;
	
	public static final int MAGIC_CLIENT_ID = 0xDEAD;
	public static final int MAGIC_SESSION_ID = 0xBEEF;
	public static final int MAGIC_PROTOCOL_VERSION = SOME_IP_1; 
	public static final int MAGIC_INTERFACE_VERSION = 0x01;
	public static final int MAGIC_MSG_TYPE_TO_SERVER = 0x01; 
	public static final int MAGIC_MSG_TYPE_TO_CLIENT = 0x02; 
	
	public static final int MAGIC_RETURN_CODE = 0x00;
	
	public static final int MESSAGE_TYPE_REQUEST = 0x00; //REQUEST A request expecting a response (even void)
	public static final int MESSAGE_TYPE_NO_RETURN = 0x01; //REQUEST_NO_RETURN A fire&forget request
	public static final int MESSAGE_TYPE_NOTIFICATION = 0x01; //0x02 NOTIFICATION
	
	public static final int MESSAGE_TYPE_REQUEST_ACK = 0x40; //Acknowledgment for REQUEST (optional)
	public static final int MESSAGE_TYPE_REQUEST_NO_RETURN_ACK = 0x41; //Acknowledgment for REQUEST_NO_RETURN (infor- mational)
	public static final int MESSAGE_TYPE_NOTIFICATION_ACK = 0x42; //Acknowledgment for NOTIFICATION (informational)

	public static final int MESSAGE_TYPE_RESPONSE = 0x80; //The response message
	public static final int MESSAGE_TYPE_ERROR = 0x81; //The response containing an error)

	public static final int MESSAGE_TYPE_RESPONSE_ACK = 0xC0; //The Acknowledgment for RESPONSE (informational)
	public static final int MESSAGE_TYPE_ERROR_ACK = 0xC1; //Acknowledgment for ERROR (infor- mational)	
	
	public static final int[] MESSAGE_TYPES = new int[] {
			MESSAGE_TYPE_ERROR, MESSAGE_TYPE_ERROR_ACK, MESSAGE_TYPE_NO_RETURN, MESSAGE_TYPE_NOTIFICATION, MESSAGE_TYPE_NOTIFICATION_ACK, MESSAGE_TYPE_REQUEST, MESSAGE_TYPE_REQUEST_ACK, MESSAGE_TYPE_REQUEST_NO_RETURN_ACK, MESSAGE_TYPE_RESPONSE, MESSAGE_TYPE_RESPONSE_ACK
	};
	
	
	public void setServiceId(int id); 
	public int getServiceId(); 
	
	public void setMethodId(int id); 
	public int getMethodId(); 
	
	public long getLength(); 
	
	public void setClientId(int id); 
	public int getClientId(); 
	
	public void setSessionId(int id); 
	public int getSessionId(); 
	
	public void setProtocolVersion(int id); 
	public int getProtocolVersion(); 
	
	public void setInterfaceVersion(int id); 
	public int getInterfaceVersion();
	
	public void setMessageType(int id); 
	public int getMessageType();
	
	public void setReturnCode(int id); 
	public int getReturnCode();
	
}
