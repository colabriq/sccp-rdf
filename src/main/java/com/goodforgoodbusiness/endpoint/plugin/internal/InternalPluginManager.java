package com.goodforgoodbusiness.endpoint.plugin.internal;

import java.util.Set;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.endpoint.plugin.ContainerListener;
import com.goodforgoodbusiness.endpoint.plugin.ContainerListenerManager;
import com.goodforgoodbusiness.endpoint.plugin.StorableGraphContainer;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Manages all the {@link InternalPlugin}s.
 */
@Singleton
public class InternalPluginManager {
	private static final Logger log = Logger.getLogger(InternalPluginManager.class);
	
	/**
	 * Bridges {@link InternalPlugin} with {@link ContainerListener}
	 */
	class InternalReasonerPluginListener implements ContainerListener {
		private final InternalPlugin plugin;

		InternalReasonerPluginListener(InternalPlugin plugin) {
			this.plugin = plugin;
		}
		
		@Override
		public void newContainer(StorableGraphContainer container) {
			try {
				plugin.exec(container);
			}
			catch (InternalPluginException ie) {
				log.error("Error in reasoner " + plugin.getClass().getSimpleName(), ie);
			}
		}
		
		@Override
		public String toString() {
			return "InternalReasonerPluginListener(" + plugin.getClass().getSimpleName() + ")";
		}
	}
	
	private final ContainerListenerManager manager;
	private final Set<InternalPlugin> plugins;

	@Inject
	public InternalPluginManager(ContainerListenerManager manager, Set<InternalPlugin> plugins) {
		this.manager = manager;
		this.plugins = plugins;
	}

	public void init() {
		this.plugins.forEach(plugin -> {
			try {
				plugin.init();
				manager.register(new InternalReasonerPluginListener(plugin));
			}
			catch (InternalPluginException e) {
				log.error("Could not initialize " + plugin.getClass().getSimpleName(), e);
			}
		});
	}
}
