package com.goodforgoodbusiness.endpoint.rdf;

public class RDFException extends RuntimeException {
	public RDFException(String message) {
		super(message);
	}
	
	public RDFException(String message, Throwable cause) {
		super(message, cause);
	}
}
