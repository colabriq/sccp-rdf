package com.goodforgoodbusiness.endpoint.graph.persistent.container;

import org.rocksdb.RocksDBException;

import com.goodforgoodbusiness.endpoint.graph.base.BaseGraph;
import com.goodforgoodbusiness.endpoint.graph.persistent.rocks.RocksManager;
import com.goodforgoodbusiness.endpoint.graph.persistent.rocks.store.RocksTripleStore;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * Wrap around RocksDB components with container aware components
 */
@Singleton
public class ContainerPersistentGraph extends BaseGraph<ContainerTripleStore<RocksTripleStore>> {
	@Inject
	public ContainerPersistentGraph(ContainerCollector collector, @Named("storage.path") String path) throws RocksDBException {
		super(
			new ContainerTripleStore<>(
				new RocksTripleStore(new RocksManager(path)),
				collector
			)
		);
		
		// still need to start RocksDB
		getStore().getUnderlyingStore().getManager().start();
	}
}
