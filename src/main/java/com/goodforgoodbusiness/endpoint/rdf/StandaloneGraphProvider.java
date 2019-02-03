package com.goodforgoodbusiness.endpoint.rdf;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.impl.TripleStore;
import org.apache.jena.mem.GraphMem;

import com.goodforgoodbusiness.endpoint.rdf.store.AdvanceMappingStore;
import com.google.inject.Provider;

public class StandaloneGraphProvider implements Provider<Graph> {
	@Override
	public Graph get() {
		return new GraphMem() {
			@Override
			protected TripleStore createTripleStore() {
				return new AdvanceMappingStore();
			}
		};
	}
}
