package com.goodforgoodbusiness.endpoint.dht;

import java.util.Optional;

import org.apache.jena.graph.Triple;

/**
 * Influences the fetching of triples from the DHT side.
 */
public class DHTGovernor {
	public Optional<Triple> checkRevise(Triple pattern) {
		return Optional.of(pattern);
	}
}
