package com.goodforgoodbusiness.endpoint.dht;

import com.goodforgoodbusiness.endpoint.dht.container.GraphContainer;

/**
 * Listen for new containers being added in to the system.
 * @author ijmad
 */
public interface DHTContainerListener {
	/**
	 * @param inMainGraph indicates if the triples are already in the main graph
	 */
	public void containerAdded(GraphContainer container, boolean inMainGraph);
}
 