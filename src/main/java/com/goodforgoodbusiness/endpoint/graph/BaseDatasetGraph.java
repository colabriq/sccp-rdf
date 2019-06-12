package com.goodforgoodbusiness.endpoint.graph;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraphFactory.GraphMaker;
import org.apache.jena.sparql.core.DatasetGraphMap;
import org.apache.jena.sparql.core.Quad;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Provides containers as graphs that can be queried via SPARQL
 */
@Singleton
public class BaseDatasetGraph extends DatasetGraphMap {
	private final GraphMaker extraGraphMaker;
	
	@Inject
	public BaseDatasetGraph(Graph mainGraph, GraphMaker extraGraphMaker) {
		// we reimplement getGraphCreate so this only deals with mainGraph
		super(new GraphMaker() {
			@Override
			public Graph create(Node name) {
				if (name == null) {
					return mainGraph;
				}
				
				throw new UnsupportedOperationException();
			}
		}); 
		
		this.extraGraphMaker = extraGraphMaker;
	}
	
    @Override
    public boolean containsGraph(Node graphNode) {
        if (Quad.isDefaultGraph(graphNode) || Quad.isUnionGraph(graphNode)) {
        	return true;
        }
        
        // vary behaviour from base class by 
        // ensuring the graph is created and not
        // just checking the existing graphs
        
        var g = super.getGraph(graphNode);
        return g != null && !g.isEmpty(); 
    }
    
    protected Graph getGraphCreate(Node graphNode) {
        return extraGraphMaker.create(graphNode);
    }
}
