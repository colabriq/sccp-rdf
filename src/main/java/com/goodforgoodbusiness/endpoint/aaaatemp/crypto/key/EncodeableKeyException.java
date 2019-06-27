package com.goodforgoodbusiness.endpoint.aaaatemp.crypto.key;

public class EncodeableKeyException extends Exception {
	private static final long serialVersionUID = 1L;
	
	EncodeableKeyException(String message) {
		super(message);
	}
	
	EncodeableKeyException(String message, Throwable cause) {
		super(message, cause);
	}
}
