//package com.goodforgoodbusiness.endpoint.plugin.internal;
//
//import java.util.Set;
//
//import org.apache.jena.graph.Graph;
//import org.apache.jena.query.Dataset;
//import org.apache.log4j.Logger;
//
//import com.goodforgoodbusiness.endpoint.graph.base.BaseDatasetProvider.Inferred;
//import com.goodforgoodbusiness.endpoint.graph.container.ContainerStore;
//import com.goodforgoodbusiness.endpoint.plugin.GraphContainer;
//import com.goodforgoodbusiness.endpoint.plugin.GraphContainerListener;
//import com.google.inject.Inject;
//import com.google.inject.Singleton;
//
///**
// * Manages all the {@link InternalPlugin}s.
// */
//@Singleton
//public class InternalPluginManager {
//	private static final Logger log = Logger.getLogger(InternalPluginManager.class);
//	
//	/**
//	 * Bridges {@link InternalPlugin} with {@link GraphContainerListener}
//	 */
//	class InternalReasonerPluginListener implements GraphContainerListener {
//		private final InternalPlugin plugin;
//
//		InternalReasonerPluginListener(InternalPlugin plugin) {
//			this.plugin = plugin;
//		}
//		
//		@Override
//		public void containerAdded(GraphContainer container, boolean inMainGraph) {
//			try {
//				log.info("Running plugins on " + container.getId());
//				plugin.exec(container, inMainGraph);
//			}
//			catch (InternalPluginException ie) {
//				log.error("Error in reasoner " + plugin.getClass().getSimpleName(), ie);
//			}
//		}
//	}
//	
//	private final Graph mainGraph, inferredGraph;
//	private final ContainerStore store;
//	private final Set<InternalPlugin> plugins;
//
//	@Inject
//	public InternalPluginManager(Dataset dataset, @Inferred Graph inferredGraph, 
//		ContainerStore store, Set<InternalPlugin> plugins) {
//		
//		this.mainGraph = dataset.getDefaultModel().getGraph();
//		this.inferredGraph = inferredGraph;
//		this.store = store;
//		this.plugins = plugins;
//	}
//
//	public void init() {
//		this.plugins.forEach(plugin -> {
//			try {
//				plugin.init(mainGraph, inferredGraph);
//				store.addListener(new InternalReasonerPluginListener(plugin));
//			}
//			catch (InternalPluginException e) {
//				log.error("Could not initialize " + plugin.getClass().getSimpleName(), e);
//			}
//		});
//	}
//}
