package com.goodforgoodbusiness.endpoint.graph.persistent;

import java.util.Arrays;
import java.util.Optional;

import com.goodforgoodbusiness.shared.encode.Hex;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Information about the status of a Triple and where it's come from.
 */
public class TripleContext {
	/**
	 * The type of information contained in this {@link TripleContext}
	 */
	public static enum Type {
		/**
		 * Indicates this triple was the result of a preload operation
		 */
		PRELOADED,
		
		/**
		 * Captures the ID of any containers that included this triple
		 */
		CONTAINER_ID,
		
		/**
		 * Indicates the triple was created as a result of a reasoner being run
		 */
		REASONER
	}
	
	/**
	 * Each context gets a unique ID that won't collide
	 * this is to help with storage in our (rather basic) RocksDB. 
	 */
	private final byte [] id = new byte[16];
	
	@Expose
	@SerializedName("type")
	protected final Type type;
	
	@Expose
	@SerializedName("containerID")
	protected final String containerID;
	
	TripleContext(byte [] id, Type type, String containerID) {
		System.arraycopy(id, 0, this.id, 0, 16);
		
		this.type = type;
		this.containerID = containerID;
	}
	
	TripleContext() {
		this.type = null;
		this.containerID = null;
	}
	
	public byte [] getID() {
		return id;
	}
	
	public Type getType() {
		return this.type;
	}
	
	public Optional<String> getContainerID() {
		return Optional.ofNullable(containerID);
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(id);
	}
	
	@Override
	public boolean equals(Object o) {
		return o == this || ((o instanceof TripleContext) && Arrays.equals(id, ((TripleContext)o).id));
	}
	
	@Override
	public String toString() {
		return "TripleContext(" + Hex.encode(id) + ", " + type + ")";
	}
}
