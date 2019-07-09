package com.goodforgoodbusiness.endpoint.graph.dht;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraphFactory.GraphMaker;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Makes extra graphs for the DHT
 */
@Singleton
public class DHTGraphMaker implements GraphMaker {
	public static final String CONTAINER_URI_PREFIX = "container:";
	
//	private final ContainerStore containerStore;
	
	@Inject
	public DHTGraphMaker(/*ContainerStore containerStore*/) {
//		this.containerStore = containerStore;
	}

	@Override
	public Graph create(Node name) {
//		if (name.isURI()) {
//			var uri = name.getURI();
//			if (uri.startsWith(CONTAINER_URI_PREFIX)) {
//				var id = uri.substring(CONTAINER_URI_PREFIX.length());
//				
//				return containerStore
//					.getContainer(id)
//					.map(GraphContainer::toGraph)
//					.orElse(null)
//				;
//			}
//		}
		
		return null;
	}
}
