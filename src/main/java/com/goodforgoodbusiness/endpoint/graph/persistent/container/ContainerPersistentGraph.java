package com.goodforgoodbusiness.endpoint.graph.persistent.container;

import com.goodforgoodbusiness.endpoint.graph.base.BaseGraph;
import com.goodforgoodbusiness.endpoint.graph.persistent.TripleContexts;
import com.goodforgoodbusiness.endpoint.graph.persistent.rocks.RocksManager;
import com.goodforgoodbusiness.endpoint.graph.persistent.rocks.triples.RocksTripleStore;
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
