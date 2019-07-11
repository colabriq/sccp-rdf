package com.goodforgoodbusiness.endpoint.plugin.internal;

import org.apache.jena.graph.Graph;

/**
 * Harness for reasoner plugins
 */
public interface InternalPlugin {
	/**
	 * Initialize this plugin.
	 * 
	 * @param mainGraph The provided {@link Graph} for reading/writing all triples to/from.
	 */
	public void init(Graph graph) throws InternalPluginException;
	
	/**
	 * Perform reasoning on some new triples added to the main graph
	 */
	public void exec(Graph graph) throws InternalPluginException;
}
