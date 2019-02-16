package com.goodforgoodbusiness.endpoint.dht;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.impl.TripleStore;
import org.apache.jena.mem.GraphMem;

import com.goodforgoodbusiness.endpoint.dht.store.DHTTripleStore;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class DHTGraphProvider implements Provider<Graph> {
	private final DHTEngineClient client;
	private final ContainerContexts contextMap;
	private final ContainerCollector collector;
	
	@Inject
	public DHTGraphProvider(DHTEngineClient client, ContainerContexts contextMap, ContainerCollector collector) {
		this.client = client;
		this.contextMap = contextMap;
		this.collector = collector;
	}
	
	@Override
	public Graph get() {
		return new GraphMem() {
			@Override
			protected TripleStore createTripleStore() {
				return new DHTTripleStore(this, client, contextMap, collector);
			}
		};
	}
}
