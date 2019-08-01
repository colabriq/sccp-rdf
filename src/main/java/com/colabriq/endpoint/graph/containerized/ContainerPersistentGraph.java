package com.colabriq.endpoint.graph.containerized;

import com.colabriq.endpoint.graph.base.BaseGraph;
import com.colabriq.endpoint.graph.rocks.RocksTripleStore;
import com.colabriq.endpoint.storage.TripleContexts;
import com.colabriq.rocks.RocksManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Wrap around RocksDB components with container aware components
 */
@Singleton
public class ContainerPersistentGraph extends BaseGraph<ContainerTripleStore<RocksTripleStore>> {
	@Inject
	public ContainerPersistentGraph(ContainerCollector collector, TripleContexts contexts, RocksManager manager) {
		super(new ContainerTripleStore<>(new RocksTripleStore(manager), contexts, collector));
	}
	
	@Override
	public int graphBaseSize() {
		return getStore().size();
	}
}
