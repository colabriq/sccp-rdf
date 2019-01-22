package com.goodforgoodbusiness.rdfjava.dht;

import org.apache.jena.graph.impl.TripleStore;
import org.apache.jena.mem.GraphMem;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.sparql.core.DatasetGraphMaker;

public class DHTDatasetFactory {
	private ClaimContextMap claimContextMap;
	private ClaimCollector claimCollector;
	
	public DHTDatasetFactory() {
		this.claimContextMap = new ClaimContextMap();
		this.claimCollector = new ClaimCollector();
	}
	
	public Dataset create() {
		var dataGraphMaker = new DatasetGraphMaker(
			new GraphMem() {
				@Override
				protected TripleStore createTripleStore() {
					return new DHTTripleStore( this, claimContextMap, claimCollector );
				}
			}
		);
			
		return DatasetFactory.create(dataGraphMaker);
	}

	public ClaimCollector getClaimCollector() {
		return claimCollector;
	}

	public ClaimContextMap getClaimContextMap() {
		return claimContextMap;
	}
}
