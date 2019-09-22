package com.colabriq.endpoint.graph.dht;

import com.colabriq.endpoint.dht.DHT;
import com.colabriq.endpoint.graph.base.BaseGraph;
import com.colabriq.endpoint.graph.containerized.ContainerCollector;
import com.colabriq.endpoint.graph.containerized.ContainerTripleStore;
import com.colabriq.endpoint.graph.rocks.RocksTripleStore;
import com.colabriq.endpoint.plugin.ContainerListenerManager;
import com.colabriq.endpoint.storage.TripleContexts;
import com.colabriq.endpoint.storage.rocks.context.ContainerTrackerStore;
import com.colabriq.rocks.RocksManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Wrap around RocksDB components with container aware components
 */
@Singleton
public class DHTPersistentGraph extends BaseGraph<DHTTripleStore> {
	@Inject
	public DHTPersistentGraph(DHT dht, ContainerCollector collector, ContainerListenerManager listenerManager,
		ContainerTrackerStore tracker, TripleContexts contexts, RocksManager db) {
		
		super(new DHTTripleStore(
			dht,
			contexts,
			tracker,
			listenerManager,
			new ContainerTripleStore<>(new RocksTripleStore(db), contexts, collector)
		));
	}
	
	@Override
	public int graphBaseSize() {
		return getStore().size();
	}
}
