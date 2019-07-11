package com.goodforgoodbusiness.endpoint.plugin;

import org.apache.jena.graph.Graph;

/**
 * Listen for new containers being added in to the system.
 * @author ijmad
 */
public interface GraphListener {
	/**
	 * Trigger when a new container has been added to the graph
	 */
	public void newGraph(Graph newGraph);
}
 