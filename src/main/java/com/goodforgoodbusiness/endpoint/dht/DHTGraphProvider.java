package com.goodforgoodbusiness.endpoint.dht;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.impl.TripleStore;

import com.goodforgoodbusiness.endpoint.graph.BaseGraph;
import com.goodforgoodbusiness.endpoint.graph.store.DHTBackedStore;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * Graph backed by DHT.
 * There can only be one of these in the JVM, create with Guice. 
 */
@Singleton
public class DHTGraphProvider implements Provider<Graph> {
	private final DHTEngineClient client;
	private final DHTContainerStore containerStore;
	private final DHTContainerCollector collector;
	
	@Inject
	public DHTGraphProvider(DHTEngineClient client, DHTContainerStore containerStore, DHTContainerCollector collector) {
		this.client = client;
		this.containerStore = containerStore;
		this.collector = collector;
	}

	@Override
	public Graph get() {
		return new BaseGraph() {
			@Override
			protected TripleStore createTripleStore() {
				return new DHTBackedStore(this, client, containerStore, collector);
			}
		};
	}
}
