package com.colabriq.endpoint.dht;

import static com.colabriq.shared.TripleUtil.widerCombinations;

import java.time.Duration;
import java.util.Optional;

import org.apache.jena.graph.Triple;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * Influences the fetching of triples from the DHT side.
 */
@Singleton
public class DHTGovernor {
	private final DHTBlacklist blacklist;
	private final Cache<Triple, Boolean> cache;
	
	@Inject
	public DHTGovernor(DHTBlacklist blacklist, @Named("dht.refetchtime") int refetchtime) {
		this.blacklist = blacklist;
		
		this.cache = CacheBuilder
			.newBuilder()
			.maximumSize(1000)
			.expireAfterAccess(Duration.ofSeconds(600))
			.build()
		;
	}
	
	public Optional<Triple> checkRevise(Triple pattern) {
		if (blacklist.includes(pattern)) {
			return Optional.empty(); // don't fetch at all.
		}
		
		// have we marked this pattern as being accessed recently?
		if (cache.asMap().containsKey(pattern)) {
			return Optional.empty();
		}
		
		// if a request for a wider pattern was made, then treat this as a cache hit
		if (widerCombinations(pattern).map(cache.asMap()::containsKey).findFirst().isPresent()) {
			return Optional.empty();
		}
		
		// allow the pattern to be fetched but mark it as recent
		this.cache.put(pattern, Boolean.TRUE);
		return Optional.of(pattern);
	}
}
