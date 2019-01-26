package com.goodforgoodbusiness.endpoint.dht;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.model.Claim;
import com.google.inject.Singleton;

/**
 * Stores the claims associated with the Triples while they're stored locally.
 */
@Singleton
public class ClaimContextMap {
	private final Map<Triple, Set<Claim>> map = new HashMap<>();
	
	public Set<Claim> get(Triple trup) {
		return map.getOrDefault(trup, Collections.emptySet());
	}

	public void add(Triple trup, Claim claim) {
		if (map.containsKey(trup)) {
			map.get(trup).add(claim);
		}
		else {
			var set = new HashSet<Claim>();
			set.add(claim);
			map.put(trup, set);
		}
	}
}
