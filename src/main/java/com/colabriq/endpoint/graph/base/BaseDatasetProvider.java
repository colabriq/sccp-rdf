package com.colabriq.endpoint.graph.base;

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
	// default behaviour = do nothing.
	public static final GraphMaker NONE = 
		new GraphMaker() {
			@Override
			public Graph create(Node name) {
				return null;
			}
		}
	;
	
	private final Graph baseGraph;
	private final GraphMaker graphMaker;
	
	@Inject
	public BaseDatasetProvider(Graph baseGraph, GraphMaker graphMaker) {
		this.baseGraph = baseGraph;
		this.graphMaker = graphMaker;
	}
	
	@Override
	public Dataset get() {
		return wrap(new BaseDatasetGraph(baseGraph, graphMaker));
	}
}
