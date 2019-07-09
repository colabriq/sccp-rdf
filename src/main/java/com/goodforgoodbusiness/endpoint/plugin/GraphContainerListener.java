package com.goodforgoodbusiness.endpoint.plugin;

/**
 * Listen for new containers being added in to the system.
 * @author ijmad
 */
public interface GraphContainerListener {
	/**
	 * @param inMainGraph indicates if the triples are already in the main graph
	 */
	public void containerAdded(GraphContainer container, boolean inMainGraph);
}
 