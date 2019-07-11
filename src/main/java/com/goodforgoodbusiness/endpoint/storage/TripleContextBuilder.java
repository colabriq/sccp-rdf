package com.goodforgoodbusiness.endpoint.storage;

import java.util.Random;

import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.endpoint.storage.TripleContext.Type;
import com.goodforgoodbusiness.endpoint.storage.rocks.context.TripleContextStore;

/**
 * Builds a new {@link TripleContext} object.
 * @author ijmad
 */
public class TripleContextBuilder {
	private static final Random RANDOM;
	
	static {
		RANDOM = new Random();
	}
	
	private final Triple triple;
	private final TripleContextStore store;
	
	private final byte [] id = new byte[16];
	private Type type = null;
	private String containerID = null;
	
	TripleContextBuilder(Triple triple, TripleContextStore store) {
		this.triple = triple;
		this.store = store;
		
		RANDOM.nextBytes(id);
	}
	
	/**
	 * Set the type of this context (required)
	 */
	public TripleContextBuilder withType(Type _type) {
		this.type = _type;
		return this;
	}
	
	/**
	 * Set the container ID for this context
	 * For Type CONTAINER_ID
	 */
	public TripleContextBuilder withContainerID(String _containerID) {
		this.containerID = _containerID;
		return this;
	}
	
	/**
	 * Create the new TripleContext object
	 */
	public TripleContext save() {
		if (type == null) {
			throw new IllegalArgumentException("Type is not set");
		}
		
		var tc = new TripleContext(id, type, containerID);
		store.addContext(triple, tc);
		return tc;
	}
}
