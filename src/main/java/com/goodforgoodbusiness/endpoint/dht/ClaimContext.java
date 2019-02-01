package com.goodforgoodbusiness.endpoint.dht;

import static java.util.Collections.newSetFromMap;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.model.Claim;
import com.google.inject.Singleton;

/**
 * Stores the claims associated with the Triples while they're stored locally.
 */
@Singleton
public class ClaimContext {
	private final ConcurrentHashMap<Triple, Set<Claim>> map = new ConcurrentHashMap<>();
	
	public Set<Claim> get(Triple trup) {
		return map.getOrDefault(trup, Collections.emptySet());
	}

	public void add(Triple trup, Claim claim) {
		map.computeIfAbsent(trup, t -> newSetFromMap(new ConcurrentHashMap<>()));
		map.get(trup).add(claim);
	}
}
