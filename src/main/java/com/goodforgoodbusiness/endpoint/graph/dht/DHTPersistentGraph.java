package com.goodforgoodbusiness.endpoint.graph.dht;

import com.goodforgoodbusiness.endpoint.dht.DHTSearch;
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
	public DHTPersistentGraph(DHTSearch search, ContainerCollector cc, ContainerListenerManager lm, TripleContexts tctx, RocksManager db) {
		super(new DHTTripleStore(
			search,
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
