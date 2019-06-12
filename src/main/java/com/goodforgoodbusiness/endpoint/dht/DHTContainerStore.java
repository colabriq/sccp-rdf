package com.goodforgoodbusiness.endpoint.dht;

import static java.util.Collections.newSetFromMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.endpoint.dht.container.GraphContainer;
import com.google.inject.Singleton;

/**
 * Stores the containers and context associated with the Triples while they're stored locally.
 */
@Singleton
public class DHTContainerStore {
	private final List<DHTContainerListener> listeners = new ArrayList<>();
	
	private final ConcurrentHashMap<String, GraphContainer> containerMap = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<Triple, Set<GraphContainer>> tripleMap = new ConcurrentHashMap<>();
	
	public void addContainer(GraphContainer container) {
		if (containerMap.put(container.getId(), container) == null) {
			// it's a new container. do container add actions.
			listeners.forEach(listener -> listener.containerAdded(container));
		}
	}
	
	public boolean hasContainer(GraphContainer container) {
		return containerMap.containsValue(container);
	}
	
	public Optional<GraphContainer> getContainer(String id) {
		return Optional.ofNullable(containerMap.get(id));
	}

	public void addSource(Triple trup, GraphContainer container) {
		tripleMap.computeIfAbsent(trup, t -> newSetFromMap(new ConcurrentHashMap<>()));
		tripleMap.get(trup).add(container);
	}

	public Set<GraphContainer> getSources(Triple trup) {
		return tripleMap.getOrDefault(trup, Collections.emptySet());
	}
}
