package com.goodforgoodbusiness.endpoint.plugin.internal;

import org.apache.jena.graph.Graph;

/**
 * Harness for reasoner plugins
 */
public interface InternalReasonerPlugin {
	/**
	 * Initialize this plugin.
	 * 
	 * @param mainGraph The provided {@link Graph} for reading all triples from.
	 * @param inferredGraph The target graph that all reasoner outputs should be placed into.
	 */
	public void init(Graph mainGraph, Graph inferredGraph) throws InternalReasonerException;
	
	/**
	 * Perform reasoning on some new triples, just before they're added to mainGraph.
	 */
	public void reason(Graph newGraph, boolean inMainGraph) throws InternalReasonerException;
}
