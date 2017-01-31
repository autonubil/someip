package com.autonubil.someip.core.serialization;

public class SerializationException extends Exception {

	private static final long serialVersionUID = 2598842951305717429L;

	public SerializationException() {
		super();
	}

	public SerializationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SerializationException(String message, Throwable cause) {
		super(message, cause);
	}

	public SerializationException(String message) {
		super(message);
	}

	public SerializationException(Throwable cause) {
		super(cause);
	}

}
