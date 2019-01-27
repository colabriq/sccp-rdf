package com.goodforgoodbusiness.endpoint.dht;

import org.apache.jena.graph.impl.TripleStore;
import org.apache.jena.mem.GraphMem;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DHTGraph extends GraphMem {
	private final DHTEngineClient client;
	private final ClaimContextMap contextMap;
	private final ClaimCollector collector;
	
	@Inject
	public DHTGraph(DHTEngineClient client, ClaimContextMap contextMap, ClaimCollector collector) {
		this.client = client;
		this.contextMap = contextMap;
		this.collector = collector;
		
	}
	
	@Override
	protected TripleStore createTripleStore() {
		return new DHTTripleStore(this, client, contextMap, collector);
	}
}
