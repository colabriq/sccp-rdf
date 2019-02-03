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
	private final Set<Claim> claimSet = newSetFromMap(new ConcurrentHashMap<Claim, Boolean>());
	private final ConcurrentHashMap<Triple, Set<Claim>> tripleMap = new ConcurrentHashMap<>();
	
	public boolean contains(Claim claim) {
		return claimSet.contains(claim);
	}
	
	public Set<Claim> get(Triple trup) {
		return tripleMap.getOrDefault(trup, Collections.emptySet());
	}

	public void add(Triple trup, Claim claim) {
		claimSet.add(claim);
		
		tripleMap.computeIfAbsent(trup, t -> newSetFromMap(new ConcurrentHashMap<>()));
		tripleMap.get(trup).add(claim);
	}
}
