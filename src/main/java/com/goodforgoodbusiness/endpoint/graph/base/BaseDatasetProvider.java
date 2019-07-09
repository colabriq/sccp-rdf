package com.goodforgoodbusiness.endpoint.graph.base;

import static org.apache.jena.query.DatasetFactory.wrap;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.sparql.core.DatasetGraphFactory.GraphMaker;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * Wrap graphs in to {@link Dataset}
 * @author ijmad
 */
@Singleton
public class BaseDatasetProvider implements Provider<Dataset> {
	private final Graph mainGraph;
	
	@Inject
	public BaseDatasetProvider(Graph mainGraph) {
		this.mainGraph = mainGraph;
	}
	
	@Override
	public Dataset get() {
		return wrap(
			new BaseDatasetGraph(
				mainGraph,
				// doing nothing for the base dataset
				new GraphMaker() {
					@Override
					public Graph create(Node name) {
						return null;
					}
				}
			)
		);
	}
}
