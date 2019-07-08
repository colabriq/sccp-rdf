package com.goodforgoodbusiness.endpoint.graph.persistent;

import org.rocksdb.RocksDBException;

import com.goodforgoodbusiness.endpoint.graph.base.BaseGraph;
import com.goodforgoodbusiness.endpoint.graph.persistent.rocks.RocksManager;
import com.goodforgoodbusiness.endpoint.graph.persistent.rocks.store.RocksTripleStore;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * A graph backed by RocksDB for storage of triples inside the RDF endpoint module's environment.
 */
@Singleton
public class PersistentGraph extends BaseGraph<RocksTripleStore> {
@Inject
	public PersistentGraph(@Named("storage.path") String path) throws RocksDBException {
		super(new RocksTripleStore(new RocksManager(path)));
		super.getStore().getManager().start();
	}
}
