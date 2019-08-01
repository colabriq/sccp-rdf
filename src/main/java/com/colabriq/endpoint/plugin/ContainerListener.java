package com.goodforgoodbusiness.endpoint.plugin;

/**
 * Listen for new containers being added in to the system.
 * @author ijmad
 */
public interface ContainerListener {
	/**
	 * Trigger when a new container has been added to the graph
	 */
	public void newContainer(StorableGraphContainer container);
}
