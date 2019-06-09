package com.goodforgoodbusiness.endpoint.processor;

public class SparqlProcessException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public SparqlProcessException(String message) {
		super(message);
	}
	
	public SparqlProcessException(String message, Throwable cause) {
		super(message, cause);
	}
}
