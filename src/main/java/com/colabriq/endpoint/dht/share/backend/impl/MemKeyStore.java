package com.colabriq.endpoint.dht.share.backend.impl;

import static com.colabriq.shared.TripleUtil.matchingCombinations;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.jena.graph.Triple;

import com.colabriq.endpoint.crypto.key.EncodeableShareKey;
import com.colabriq.endpoint.dht.share.ShareKeyStore;
import com.colabriq.endpoint.dht.share.SharePattern;
import com.colabriq.endpoint.dht.share.backend.KeyStoreBackend;
import com.colabriq.kpabe.key.KPABEPublicKey;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * An in-memory implementation of {@link ShareKeyStore} for testing purposes.
 */
@Singleton
public class MemKeyStore implements KeyStoreBackend {
	private Map<SharePattern, Set<KPABEPublicKey>> sharers = new HashMap<>();
	private Map<KPABEPublicKey, Set<EncodeableShareKey>> shareKeys = new HashMap<>();
	
	@Inject
	public MemKeyStore() {
	}
	
	@Override
	public Stream<KPABEPublicKey> getCreators(Triple pattern) {
		return matchingCombinations(pattern)
			.map(SharePattern::new)
			.flatMap(sr -> sharers.getOrDefault(sr, emptySet()).stream())
			.collect(toSet())
			.stream()
		;
	}
	
	@Override
	public Stream<EncodeableShareKey> getKeys(KPABEPublicKey publicKey, Triple tuple) {
		return shareKeys.getOrDefault(publicKey, emptySet()).stream();
	}
	
	@Override
	public void saveKey(SharePattern pattern, EncodeableShareKey key) {
		sharers.computeIfAbsent(pattern, k -> new HashSet<>());
		sharers.get(pattern).add(key.getPublic());
		
		shareKeys.computeIfAbsent(key.getPublic(), k -> new HashSet<>());
		shareKeys.get(key.getPublic()).add(key);
	}
}
