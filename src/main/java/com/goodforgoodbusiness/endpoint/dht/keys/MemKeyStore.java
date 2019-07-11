package com.goodforgoodbusiness.endpoint.dht.keys;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.goodforgoodbusiness.endpoint.crypto.key.EncodeableShareKey;
import com.goodforgoodbusiness.kpabe.key.KPABEPublicKey;
import com.goodforgoodbusiness.model.TriTuple;
import com.goodforgoodbusiness.shared.encode.JSON;
import com.google.inject.Singleton;

/**
 * An in-memory implementation of {@link ShareKeyStore} for testing purposes.
 */
@Singleton
public class MemKeyStore implements ShareKeyStore {
	private Map<TriTuple, Set<String>> sharers = new HashMap<>();
	private Map<KPABEPublicKey, Set<String>> shareKeys = new HashMap<>();
	
	@Override
	public Stream<KPABEPublicKey> knownContainerCreators(TriTuple pattern) {
		return pattern.matchingCombinations()
			.flatMap(tuple -> sharers.getOrDefault(tuple, emptySet()).stream())
			.map(storedKey -> new KPABEPublicKey(storedKey))
			.collect(toSet())
			.stream()
		;
	}
	
	@Override
	public Stream<EncodeableShareKey> keysForDecrypt(KPABEPublicKey publicKey, TriTuple tuple) {
		var result = shareKeys.getOrDefault(publicKey, emptySet());
		return result.stream()
			.map(storedShareKey -> 
				JSON.decode(storedShareKey, EncodeableShareKey.class))
		;
	}
	
	@Override
	public void saveKey(TriTuple tuple, EncodeableShareKey shareKey) {
		sharers.computeIfAbsent(tuple, key -> new HashSet<>());
		sharers.get(tuple).add(shareKey.getPublic().toString());
		
		shareKeys.computeIfAbsent(shareKey.getPublic(), key -> new HashSet<>());
		shareKeys.get(shareKey.getPublic()).add(JSON.encodeToString(shareKey));
	}
}
