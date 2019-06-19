package com.goodforgoodbusiness.endpoint.plugin.internal;

import java.util.Set;

import org.apache.jena.graph.Graph;
import org.apache.jena.query.Dataset;
import org.apache.log4j.Logger;

import com.goodforgoodbusiness.endpoint.dht.DHTContainerListener;
import com.goodforgoodbusiness.endpoint.dht.DHTContainerStore;
import com.goodforgoodbusiness.endpoint.dht.container.GraphContainer;
import com.goodforgoodbusiness.endpoint.graph.BaseDatasetProvider.Inferred;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Manages all the {@link InternalReasonerPlugin}s.
 */
@Singleton
public class InternalReasonerManager {
	private static final Logger log = Logger.getLogger(InternalReasonerManager.class);
	
	/**
	 * Bridges {@link InternalReasonerPlugin} with {@link DHTContainerListener}
	 */
	class InternalReasonerPluginListener implements DHTContainerListener {
		private final InternalReasonerPlugin plugin;

		InternalReasonerPluginListener(InternalReasonerPlugin plugin) {
			this.plugin = plugin;
		}
		
		@Override
		public void containerAdded(GraphContainer container, boolean inMainGraph) {
			try {
				log.info("Reasoning on " + container.getId());
				plugin.reason(container.toGraph(), inMainGraph);
			}
			catch (InternalReasonerException ie) {
				log.error("Error in reasoner " + plugin.getClass().getSimpleName(), ie);
			}
		}
	}
	
	private final Graph mainGraph, inferredGraph;
	private final DHTContainerStore store;
	private final Set<InternalReasonerPlugin> plugins;

	@Inject
	public InternalReasonerManager(Dataset dataset, @Inferred Graph inferredGraph, 
		DHTContainerStore store, Set<InternalReasonerPlugin> plugins) {
		
		this.mainGraph = dataset.getDefaultModel().getGraph();
		this.inferredGraph = inferredGraph;
		this.store = store;
		this.plugins = plugins;
	}

	public void init() {
		this.plugins.forEach(plugin -> {
			try {
				plugin.init(mainGraph, inferredGraph);
				store.addListener(new InternalReasonerPluginListener(plugin));
			}
			catch (InternalReasonerException e) {
				log.error("Could not initialize " + plugin.getClass().getSimpleName(), e);
			}
		});
	}
}
