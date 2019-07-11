package com.goodforgoodbusiness.endpoint.storage;

import com.goodforgoodbusiness.endpoint.graph.base.BaseGraph;
import com.goodforgoodbusiness.endpoint.graph.rocks.RocksTripleStore;
import com.goodforgoodbusiness.endpoint.storage.rocks.RocksManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * A graph backed by RocksDB for storage of triples inside the RDF endpoint module's environment.
 */
@Singleton
public class PersistentGraph extends BaseGraph<RocksTripleStore> {
	@Inject
	public PersistentGraph(RocksManager manager) {
		this(new RocksTripleStore(manager));
	}
	
	public PersistentGraph(RocksTripleStore store) {
		super(store);
	}
	
	@Override
	public int graphBaseSize() {
		return getStore().size();
	}
}
