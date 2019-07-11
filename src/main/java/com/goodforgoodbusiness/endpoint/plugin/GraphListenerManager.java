package com.goodforgoodbusiness.endpoint.plugin;

import static java.util.Collections.newSetFromMap;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import com.goodforgoodbusiness.endpoint.graph.base.BaseGraph;
import com.goodforgoodbusiness.model.StorableContainer;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Manages interactions between listeners and the underlying store
 * @author ijmad
 */
@Singleton
public class GraphListenerManager {
	private final ExecutorService service;
	private final Set<GraphListener> listeners = newSetFromMap(new ConcurrentHashMap<>());
	
	@Inject
	public GraphListenerManager(ExecutorService service) {
		this.service = service;
	}
	
	public void register(GraphListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Trigger all listeners for a specific container.
	 * This is an async operation that will run in the background.
	 */
	public void trigger(StorableContainer container) {
		// set task to build graph (async)
		service.execute(() -> {
			var graph = BaseGraph.newGraph();
			
			// XXX layer this on top of the old graph?
			container.getRemoved().forEach(graph::add); 
			container.getAdded().forEach(graph::add);
			
			listeners.forEach(listener -> {
				service.execute(new GraphListenerTriggerTask(graph, listener));
			});
		});
	}
}
