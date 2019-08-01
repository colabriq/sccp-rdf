package com.colabriq.endpoint.dht;

import com.colabriq.endpoint.crypto.key.EncodeableSecretKey;
import com.colabriq.model.EncryptedContainer;

/**
 * Result of Weft publish.
 */
public final class DHTWeftPublishResult {
	private final EncodeableSecretKey key;
	private final EncryptedContainer container;
	
	private final byte[] publishedData;
	private final String publishedLocation;
	
	public DHTWeftPublishResult(EncodeableSecretKey key, EncryptedContainer container, String publishedLocation, byte[] publishedData) {
		this.key = key;
		this.container = container;
		
		this.publishedLocation = publishedLocation;
		this.publishedData = publishedData;
	}

	public EncodeableSecretKey getKey() {
		return key;
	}
	
	public EncryptedContainer getContainer() {
		return container;
	}
	
	/**
	 * This will be the container's JSON, encrypted.
	 */
	public byte[] getPublishedData() {
		return publishedData;
	}
	
	public String getPublishedLocation() {
		return publishedLocation;
	}
}
