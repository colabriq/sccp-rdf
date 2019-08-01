package com.goodforgoodbusiness.endpoint.dht.share.backend.impl;

import static com.goodforgoodbusiness.shared.TripleUtil.matchingCombinations;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.endpoint.crypto.key.EncodeableShareKey;
import com.goodforgoodbusiness.endpoint.dht.share.ShareKeyStore;
import com.goodforgoodbusiness.endpoint.dht.share.SharePattern;
import com.goodforgoodbusiness.endpoint.dht.share.backend.KeyStoreBackend;
import com.goodforgoodbusiness.kpabe.key.KPABEPublicKey;
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
