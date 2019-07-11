package com.goodforgoodbusiness.endpoint.plugin.internal;

import java.util.Set;

import org.apache.jena.graph.Graph;
import org.apache.jena.query.Dataset;
import org.apache.log4j.Logger;

import com.goodforgoodbusiness.endpoint.plugin.GraphListener;
import com.goodforgoodbusiness.endpoint.plugin.GraphListenerManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Manages all the {@link InternalPlugin}s.
 */
@Singleton
public class InternalPluginManager {
	private static final Logger log = Logger.getLogger(InternalPluginManager.class);
	
	/**
	 * Bridges {@link InternalPlugin} with {@link GraphListener}
	 */
	class InternalReasonerPluginListener implements GraphListener {
		private final InternalPlugin plugin;

		InternalReasonerPluginListener(InternalPlugin plugin) {
			this.plugin = plugin;
		}
		
		@Override
		public void newGraph(Graph newGraph) {
			try {
				plugin.exec(newGraph);
			}
			catch (InternalPluginException ie) {
				log.error("Error in reasoner " + plugin.getClass().getSimpleName(), ie);
			}
		}
	}
	
	private final Graph graph;
	private final GraphListenerManager manager;
	private final Set<InternalPlugin> plugins;

	@Inject
	public InternalPluginManager(Dataset dataset, GraphListenerManager manager, Set<InternalPlugin> plugins) {
		this.graph = dataset.getDefaultModel().getGraph();
		this.manager = manager;
		this.plugins = plugins;
	}

	public void init() {
		this.plugins.forEach(plugin -> {
			try {
				plugin.init(graph);
				manager.register(new InternalReasonerPluginListener(plugin));
			}
			catch (InternalPluginException e) {
				log.error("Could not initialize " + plugin.getClass().getSimpleName(), e);
			}
		});
	}
}
