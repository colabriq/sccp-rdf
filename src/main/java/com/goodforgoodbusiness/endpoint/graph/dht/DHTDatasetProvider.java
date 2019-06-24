package com.goodforgoodbusiness.endpoint.graph.dht;

import static org.apache.jena.query.DatasetFactory.wrap;

import org.apache.jena.graph.Graph;
import org.apache.jena.query.Dataset;

import com.goodforgoodbusiness.endpoint.graph.CustomGraphUnion;
import com.goodforgoodbusiness.endpoint.graph.base.BaseDatasetGraph;
import com.goodforgoodbusiness.endpoint.graph.base.BaseDatasetProvider.Fetched;
import com.goodforgoodbusiness.endpoint.graph.base.BaseDatasetProvider.Inferred;
import com.goodforgoodbusiness.endpoint.graph.base.BaseDatasetProvider.Preloaded;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * Build the graph with the {@link DHTGraphMaker} for extra graphy goodness.
 */
@Singleton
public class DHTDatasetProvider implements Provider<Dataset> {
	private final Graph preloadedGraph, fetchedGraph, inferredGraph;
	private final DHTGraphMaker graphMaker;

	@Inject
	public DHTDatasetProvider(
		@Preloaded Graph preloadedGraph, @Fetched Graph fetchedGraph, @Inferred Graph inferredGraph, DHTGraphMaker graphMaker) {

		this.preloadedGraph = preloadedGraph;
		this.fetchedGraph = fetchedGraph;
		this.inferredGraph = inferredGraph;
		
		this.graphMaker = graphMaker;
	}

	@Override
	public Dataset get() {
		return wrap(
			new BaseDatasetGraph(
				new CustomGraphUnion(
					fetchedGraph,
					preloadedGraph,
					inferredGraph
				),
				graphMaker
			)
		);
	}
}
