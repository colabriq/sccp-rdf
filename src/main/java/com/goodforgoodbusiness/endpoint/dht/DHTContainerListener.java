package com.goodforgoodbusiness.endpoint.dht;

import com.goodforgoodbusiness.endpoint.dht.container.GraphContainer;

/**
 * Listen for new containers being added in to the system.
 * @author ijmad
 */
public interface DHTContainerListener {
	public void containerAdded(GraphContainer container);
}
