package com.goodforgoodbusiness.endpoint.graph;

import org.apache.jena.graph.impl.TripleStore;
import org.apache.jena.mem.GraphMem;

import com.goodforgoodbusiness.endpoint.dht.DHTContainerCollector;
import com.goodforgoodbusiness.endpoint.dht.DHTEngineClient;
import com.goodforgoodbusiness.endpoint.dht.DHTContextStore;
import com.goodforgoodbusiness.endpoint.graph.store.DHTBackedStore;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Graph backed by DHT.
 * There can only be one of these in the JVM, create with Guice. 
 */
@Singleton
public class DHTGraph extends GraphMem {
	private final DHTEngineClient client;
	private final DHTContextStore contextStore;
	private final DHTContainerCollector collector;
	
	@Inject
	public DHTGraph(DHTEngineClient client, DHTContextStore contextStore, DHTContainerCollector collector) {
		this.client = client;
		this.contextStore = contextStore;
		this.collector = collector;
	}
	
	@Override
	protected TripleStore createTripleStore() {
		return new DHTBackedStore(this, client, contextStore, collector);
	}
}
