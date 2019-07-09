package com.goodforgoodbusiness.endpoint.plugin.internal.builtin.reasoner;

import org.apache.jena.graph.Graph;

import com.goodforgoodbusiness.endpoint.plugin.GraphContainer;
import com.goodforgoodbusiness.endpoint.plugin.internal.InternalPlugin;
import com.goodforgoodbusiness.endpoint.plugin.internal.InternalPluginException;

/**
 * Abstracts away dealing with containers for plugins that don't care about that.
 */
public abstract class AbstractReasonerPlugin implements InternalPlugin {
	@Override
	public final void exec(GraphContainer newContainer, boolean inMainGraph) throws InternalPluginException {
		reason(newContainer.toGraph(), inMainGraph);
	}
	
	/**
	 * Implement to do reasoning on just newly added graph models
	 */
	public abstract void reason(Graph newGraph, boolean inMainGraph) throws InternalPluginException;
}
