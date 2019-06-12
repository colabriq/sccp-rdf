package com.goodforgoodbusiness.endpoint.dht;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraphFactory.GraphMaker;

import com.goodforgoodbusiness.endpoint.dht.container.GraphContainer;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Makes extra graphs for the DHT
 */
@Singleton
public class DHTGraphMaker implements GraphMaker {
	public static final String CONTAINER_URI_PREFIX = "container:";
	
	private final DHTContainerStore containerStore;
	
	@Inject
	public DHTGraphMaker(DHTContainerStore containerStore) {
		this.containerStore = containerStore;
	}

	@Override
	public Graph create(Node name) {
		if (name.isURI()) {
			var uri = name.getURI();
			if (uri.startsWith(CONTAINER_URI_PREFIX)) {
				var id = uri.substring(CONTAINER_URI_PREFIX.length());
				
				return containerStore
					.getContainer(id)
					.map(GraphContainer::toGraph)
					.orElse(null)
				;
			}
		}
		
		return null;
	}
}
