package com.colabriq.endpoint.graph.dht;

import com.colabriq.endpoint.dht.DHT;
import com.colabriq.endpoint.graph.base.BaseGraph;
import com.colabriq.endpoint.graph.containerized.ContainerCollector;
import com.colabriq.endpoint.graph.containerized.ContainerTripleStore;
import com.colabriq.endpoint.graph.rocks.RocksTripleStore;
import com.colabriq.endpoint.plugin.ContainerListenerManager;
import com.colabriq.endpoint.storage.TripleContexts;
import com.colabriq.rocks.RocksManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Wrap around RocksDB components with container aware components
 */
@Singleton
public class DHTPersistentGraph extends BaseGraph<DHTTripleStore> {
	@Inject
	public DHTPersistentGraph(DHT dht, ContainerCollector cc, ContainerListenerManager lm, TripleContexts tctx, RocksManager db) {
		super(new DHTTripleStore(
			dht,
			tctx, 
			lm,
			new ContainerTripleStore<>(new RocksTripleStore(db), tctx, cc)
		));
	}
	
	@Override
	public int graphBaseSize() {
		return getStore().size();
	}
}
