package com.goodforgoodbusiness.endpoint.storage;

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
		 * Indicates this triple was the result of a preload operation.
		 * Like LOCAL_ONLY, these will not be published.
		 */
		PRELOADED,
		
		/**
		 * Indicates the Triple came from a container.
		 * Should also set @see {@link TripleContextBuilder#withContainerID(String)}
		 */
		CONTAINER,
		
		/**
		 * Indicates the triple was created as a result of a reasoner being run
		 * Should also set @see {@link TripleContextBuilder#withContainerID(String)}
		 * And @see {@link TripleContextBuilder#}
		 */
		REASONER,
		
		/**
		 * Indicates the Triple was loaded as 'local only' triple and is not for publish.
		 */
		LOCAL_ONLY,
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
	
	@Expose
	@SerializedName("reasoner")
	protected final String reasoner;
	
	TripleContext(byte [] id, Type type, String containerID, String reasoner) {
		System.arraycopy(id, 0, this.id, 0, 16);
		
		this.type = type;
		this.containerID = containerID;
		this.reasoner = reasoner;
	}
	
	TripleContext() {
		this.type = null;
		this.containerID = null;
		this.reasoner = null;
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
