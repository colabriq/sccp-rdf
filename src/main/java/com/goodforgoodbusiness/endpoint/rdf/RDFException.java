package com.goodforgoodbusiness.endpoint.rdf;

public class RDFException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public RDFException(String message) {
		super(message);
	}
	
	public RDFException(String message, Throwable cause) {
		super(message, cause);
	}
}
