package com.goodforgoodbusiness.endpoint.graph.container;

import com.goodforgoodbusiness.endpoint.graph.base.BaseGraph;
import com.goodforgoodbusiness.endpoint.graph.base.store.ContainerizedTripleStore;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * A standalone graph backed with our {@link ContainerizedTripleStore}.
 */
@Singleton
public class ContainerizedGraph extends BaseGraph<ContainerizedTripleStore> {	
	public ContainerizedGraph(ContainerizedTripleStore store) {
		super(store);
	}

	@Inject
	public ContainerizedGraph(ContainerStore containerStore, ContainerCollector containerCollector) {
		this(new ContainerizedTripleStore(containerStore, containerCollector));
	}
}
