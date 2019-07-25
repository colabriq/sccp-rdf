package com.goodforgoodbusiness.endpoint.dht.share.impl;

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
import com.goodforgoodbusiness.endpoint.dht.share.ShareResponse;
import com.goodforgoodbusiness.kpabe.key.KPABEPublicKey;
import com.goodforgoodbusiness.shared.encode.JSON;
import com.google.inject.Singleton;

/**
 * An in-memory implementation of {@link ShareKeyStore} for testing purposes.
 */
@Singleton
public class MemKeyStore implements ShareKeyStore {
	private Map<ShareResponse, Set<String>> sharers = new HashMap<>();
	private Map<KPABEPublicKey, Set<String>> shareKeys = new HashMap<>();
	
	@Override
	public Stream<KPABEPublicKey> knownContainerCreators(Triple pattern) {
		return matchingCombinations(pattern)
			.map(
				c -> {
					return new ShareResponse().setTriple(c);
				}
			)
			.flatMap(
				sr -> {
					return sharers.getOrDefault(sr, emptySet()).stream();
				}
			)
			.map(storedKey -> new KPABEPublicKey(storedKey))
			.collect(toSet())
			.stream()
		;
	}
	
	@Override
	public Stream<EncodeableShareKey> keysForDecrypt(KPABEPublicKey publicKey, Triple tuple) {
		var result = shareKeys.getOrDefault(publicKey, emptySet());
		return result.stream()
			.map(storedShareKey -> 
				JSON.decode(storedShareKey, EncodeableShareKey.class))
		;
	}
	
	@Override
	public void saveKey(ShareResponse request) {
		sharers.computeIfAbsent(request, key -> new HashSet<>());
		sharers.get(request).add(request.getKey().getPublic().toString());
		
		shareKeys.computeIfAbsent(request.getKey().getPublic(), key -> new HashSet<>());
		shareKeys.get(request.getKey().getPublic()).add(JSON.encodeToString(request.getKey()));
	}
}
