package com.goodforgoodbusiness.rdfjava.dht;

import org.apache.jena.graph.impl.TripleStore;
import org.apache.jena.mem.GraphMem;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.sparql.core.DatasetGraphMaker;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class DHTDatasetProvider implements Provider<Dataset> {
	private final DHTClient client;
	private final ClaimContextMap contextMap;
	private final ClaimCollector collector;
	
	@Inject
	public DHTDatasetProvider(DHTClient client, ClaimContextMap contextMap, ClaimCollector collector) {
		this.client = client;
		this.contextMap = contextMap;
		this.collector = collector;
	}
	
	@Override
	public Dataset get() {
		var dataGraphMaker = new DatasetGraphMaker(
			new GraphMem() {
				@Override
				protected TripleStore createTripleStore() {
					return new DHTTripleStore(this, client, contextMap, collector);
				}
			}
		);
				
		return DatasetFactory.create(dataGraphMaker);
	}
}
	
