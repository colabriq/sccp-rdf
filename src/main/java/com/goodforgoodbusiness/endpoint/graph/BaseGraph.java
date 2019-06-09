package com.goodforgoodbusiness.endpoint.graph;

import org.apache.jena.graph.impl.TripleStore;
import org.apache.jena.mem.GraphMem;

import com.goodforgoodbusiness.endpoint.graph.store.AdvanceMappingStore;

/**
 * A standalone graph backed with our {@link AdvanceMappingStore}.
 */
public class BaseGraph extends GraphMem {
	@Override
	protected TripleStore createTripleStore() {
		return new AdvanceMappingStore();
	}
}
