package com.goodforgoodbusiness.rdfjava.dht;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.model.SubmittedClaim;

/**
 * Stores the claims associated with the Triples while they're stored locally.
 */
public class ClaimContextMap {
	private final Map<Triple, Set<SubmittedClaim>> map = new HashMap<>();
	
	public Set<SubmittedClaim> get(Triple trup) {
		return map.getOrDefault(trup, Collections.emptySet());
	}

	public void add(Triple trup, SubmittedClaim claim) {
		if (map.containsKey(trup)) {
			map.get(trup).add(claim);
		}
		else {
			var set = new HashSet<SubmittedClaim>();
			set.add(claim);
			map.put(trup, set);
		}
	}
}
