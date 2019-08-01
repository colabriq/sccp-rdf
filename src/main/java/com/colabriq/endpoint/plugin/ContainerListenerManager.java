package com.colabriq.endpoint.plugin;

import static java.util.Collections.newSetFromMap;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;

import com.colabriq.model.StorableContainer;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Manages interactions between listeners and the underlying store
 * @author ijmad
 */
@Singleton
public class ContainerListenerManager {
	private static final Logger log = Logger.getLogger(ContainerListenerManager.class);
	
	private final ExecutorService service;
	private final Set<ContainerListener> listeners = newSetFromMap(new ConcurrentHashMap<>());
	
	@Inject
	public ContainerListenerManager(ExecutorService service) {
		this.service = service;
	}
	
	public void register(ContainerListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Trigger all listeners for a specific container.
	 * This is an async operation that will run in the background.
	 */
	public void trigger(StorableContainer container) {
		log.info("Triggering listeners for " + container.getId());
		
		// set task to build graph (async)
		service.execute(() -> {
			var sgc = new StorableGraphContainer(container);
			
			listeners.forEach(listener -> {
				log.info("Scheduling " + listener.toString() + " for " + container.getId());
				service.execute(new ContainerListenerTriggerTask(sgc, listener));
			});
		});
	}
}
