package com.goodforgoodbusiness.endpoint.dht;

import com.goodforgoodbusiness.model.Pointer;

/**
 * A result of publishing to the warp.
 */
public class DHTWarpPublish {
	private final Pointer pointer;
	private final byte[] publishedData;
	
	public DHTWarpPublish(Pointer pointer, byte[] publishedData) {
		this.pointer = pointer;
		this.publishedData = publishedData;
	}
	
	public Pointer getPointer() {
		return pointer;
	}
	
	/**
	 * This will be the encrypted pointer
	 */
	public byte[] getPublishedData() {
		return publishedData;
	}
}