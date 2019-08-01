package com.colabriq.endpoint.storage;

import java.util.EnumSet;
import java.util.stream.Stream;

import org.apache.jena.graph.Triple;

import com.colabriq.endpoint.storage.TripleContext.Type;
import com.colabriq.endpoint.storage.rocks.context.TripleContextStore;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Wrapper around {@link TripleContextStore} letting you create context
 * @author ijmad
 *
 */
@Singleton
public class TripleContexts {
	private final TripleContextStore store;
	
	@Inject
	public TripleContexts(TripleContextStore store) {
		this.store = store;
	}
	
	/**
	 * Create a new context
	 * Call save!
	 */
	public TripleContextBuilder create(Triple triple) {
		return new TripleContextBuilder(triple, store);
	}

	/**
	 * Fetch all contexts for a triple
	 */
	public Stream<TripleContext> getContexts(Triple t) {
		return store.getContexts(t).parallelStream();
	}
	
	/**
	 * Fetch contexts of types for a triple
	 */
	public Stream<TripleContext> getContexts(Triple t, EnumSet<Type> types) {
		return store.getContexts(t).stream().filter(c -> types.contains(c.getType()));
	}
}
