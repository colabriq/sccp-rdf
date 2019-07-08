package com.goodforgoodbusiness.endpoint.processor;

public class TaskException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public TaskException(String message) {
		super(message);
	}
	
	public TaskException(String message, Throwable cause) {
		super(message, cause);
	}
}
