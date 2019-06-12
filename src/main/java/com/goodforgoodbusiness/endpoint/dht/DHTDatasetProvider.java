package com.goodforgoodbusiness.endpoint.dht;

import org.apache.jena.graph.Graph;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;

import com.goodforgoodbusiness.endpoint.graph.BaseDatasetGraph;
import com.goodforgoodbusiness.endpoint.graph.BaseDatasetProvider;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * Build the graph with the {@link DHTGraphMaker} for extra graphy goodness.
 */
@Singleton
public class DHTDatasetProvider extends BaseDatasetProvider implements Provider<Dataset> {
	private final DHTGraphMaker graphMaker;

	@Inject
	public DHTDatasetProvider(
		@Preloaded Graph preloadedGraph, @Fetched Graph fetchedGraph, @Inferred Graph inferredGraph, DHTGraphMaker graphMaker) {
		
		super(preloadedGraph, fetchedGraph, inferredGraph);
		this.graphMaker = graphMaker;
	}

	@Override
	public Dataset get() {
		return DatasetFactory.wrap(
			new BaseDatasetGraph(defaultGraph, graphMaker)
		);
	}
}
