package com.goodforgoodbusiness.endpoint.graph.dht;

import static org.apache.jena.query.DatasetFactory.wrap;

import org.apache.jena.graph.Graph;
import org.apache.jena.query.Dataset;

import com.goodforgoodbusiness.endpoint.graph.base.BaseDatasetGraph;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * Build the graph with the {@link DHTGraphMaker} for extra graphy goodness.
 */
@Singleton
public class DHTDatasetProvider implements Provider<Dataset> {
	private final Graph graph;
	private final DHTGraphMaker graphMaker;

	@Inject
	public DHTDatasetProvider(Graph graph, DHTGraphMaker graphMaker) {
		this.graph = graph;
		this.graphMaker = graphMaker;
	}

	@Override
	public Dataset get() {
		return wrap(new BaseDatasetGraph(graph, graphMaker));
	}
}
