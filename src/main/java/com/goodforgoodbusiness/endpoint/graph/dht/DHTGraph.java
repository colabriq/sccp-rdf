package com.goodforgoodbusiness.endpoint.graph.dht;

import com.goodforgoodbusiness.endpoint.graph.container.ContainerCollector;
import com.goodforgoodbusiness.endpoint.graph.container.ContainerStore;
import com.goodforgoodbusiness.endpoint.graph.container.ContainerizedGraph;
import com.goodforgoodbusiness.endpoint.graph.dht.store.DHTTripleStore;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Graph backed by DHT.
 * There can only be one of these in the JVM, create with Guice. 
 */
@Singleton
public class DHTGraph extends ContainerizedGraph {
	@Inject
	public DHTGraph(DHTEngineClient client, ContainerStore containerStore, ContainerCollector collector) {
		super(new DHTTripleStore(client, containerStore, collector));
	}
}
