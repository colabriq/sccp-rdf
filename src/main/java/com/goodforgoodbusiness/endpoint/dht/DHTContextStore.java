package com.goodforgoodbusiness.endpoint.dht;

import static java.util.Collections.newSetFromMap;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.model.Container;
import com.google.inject.Singleton;

/**
 * Stores the context associated with the Triples while they're stored locally.
 */
@Singleton
public class DHTContextStore {
	private final Set<Container> containerSet = newSetFromMap(new ConcurrentHashMap<Container, Boolean>());
	private final ConcurrentHashMap<Triple, Set<Container>> tripleMap = new ConcurrentHashMap<>();
	
	public boolean contains(Container container) {
		return containerSet.contains(container);
	}
	
	public Set<Container> get(Triple trup) {
		return tripleMap.getOrDefault(trup, Collections.emptySet());
	}

	public void add(Triple trup, Container container) {
		containerSet.add(container);
		
		tripleMap.computeIfAbsent(trup, t -> newSetFromMap(new ConcurrentHashMap<>()));
		tripleMap.get(trup).add(container);
	}
}
