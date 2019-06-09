package com.goodforgoodbusiness.endpoint.processor;

public class ImportProcessException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ImportProcessException(String message) {
		super(message);
	}
	
	public ImportProcessException(String message, Throwable cause) {
		super(message, cause);
	}
}
