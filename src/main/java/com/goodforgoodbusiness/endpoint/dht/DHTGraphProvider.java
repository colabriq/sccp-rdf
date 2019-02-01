package com.goodforgoodbusiness.endpoint.dht;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.impl.TripleStore;
import org.apache.jena.mem.GraphMem;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class DHTGraphProvider implements Provider<Graph> {
	private final DHTEngineClient client;
	private final ClaimContext contextMap;
	private final ClaimCollector collector;
	
	@Inject
	public DHTGraphProvider(DHTEngineClient client, ClaimContext contextMap, ClaimCollector collector) {
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
