package com.goodforgoodbusiness.endpoint.graph.dht;

import com.goodforgoodbusiness.endpoint.graph.base.BaseGraph;
import com.goodforgoodbusiness.endpoint.graph.containerized.ContainerCollector;
import com.goodforgoodbusiness.endpoint.graph.containerized.ContainerTripleStore;
import com.goodforgoodbusiness.endpoint.graph.rocks.RocksTripleStore;
import com.goodforgoodbusiness.endpoint.plugin.ContainerListenerManager;
import com.goodforgoodbusiness.endpoint.storage.TripleContexts;
import com.goodforgoodbusiness.rocks.RocksManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Wrap around RocksDB components with container aware components
 */
@Singleton
public class DHTPersistentGraph extends BaseGraph<DHTTripleStore> {
	@Inject
	public DHTPersistentGraph(ContainerCollector collector, ContainerListenerManager glManager, TripleContexts tctx, RocksManager dbManager) {
		super(new DHTTripleStore(
			tctx, 
			glManager,
			new ContainerTripleStore<>(new RocksTripleStore(dbManager), tctx, collector)
		));
	}
	
	@Override
	public int graphBaseSize() {
		return getStore().size();
	}
}
