package com.colabriq.endpoint.graph.base;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraphFactory.GraphMaker;
import org.apache.jena.sparql.core.DatasetGraphMap;
import org.apache.jena.sparql.core.Quad;

/**
 * Creates a basic dataset, allowing us to overlay specific extra graphs
 */
public class BaseDatasetGraph extends DatasetGraphMap {
	private final GraphMaker extraGraphMaker;
	
	public BaseDatasetGraph(Graph graph, GraphMaker extraGraphMaker) {
		// we reimplement getGraphCreate so this only deals with the main graph
		super(new GraphMaker() {
			@Override
			public Graph create(Node name) {
				if (name == null) {
					return graph;
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
    
    @Override
    protected Graph getGraphCreate(Node graphNode) {
        return extraGraphMaker.create(graphNode);
    }
}
