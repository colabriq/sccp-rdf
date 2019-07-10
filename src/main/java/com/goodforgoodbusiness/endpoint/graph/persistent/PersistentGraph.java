package com.goodforgoodbusiness.endpoint.graph.persistent;

import com.goodforgoodbusiness.endpoint.graph.base.BaseGraph;
import com.goodforgoodbusiness.endpoint.graph.persistent.rocks.RocksManager;
import com.goodforgoodbusiness.endpoint.graph.persistent.rocks.triples.RocksTripleStore;
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
