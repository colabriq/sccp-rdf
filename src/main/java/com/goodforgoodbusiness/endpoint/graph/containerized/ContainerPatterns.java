package com.goodforgoodbusiness.endpoint.graph.containerized;

import static com.goodforgoodbusiness.shared.TripleUtil.isConcrete;
import static com.goodforgoodbusiness.shared.TripleUtil.matchingCombinations;

import java.util.stream.Stream;

import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.endpoint.dht.share.ShareManager;
import com.goodforgoodbusiness.kpabe.key.KPABEPublicKey;
import com.goodforgoodbusiness.shared.Rounds;
import com.goodforgoodbusiness.shared.encode.CBOR;
import com.goodforgoodbusiness.shared.encode.Hash;
import com.goodforgoodbusiness.shared.encode.Hex;
import com.goodforgoodbusiness.shared.encode.RDFBinary;

/**
 * Build index patterns for publishing in to the warp.
 */
public final class ContainerPatterns {
	private static String hash(KPABEPublicKey key, Triple tt) {
		key.getEncoded();
		
		var cbor = CBOR.forObject(new Object [] { 
			key.toString(),
			RDFBinary.encodeTriple(tt)
		});
		
		// do two rounds of hashing
		return Hex.encode(Rounds.apply(Hash::sha512, cbor, 2)); 
	}
	
	/**
	 * Generate all possible pattern hashes.
	 * These also include the public key of the publishing party.
	 */
	public static Stream<String> forPublish(ShareManager keyManager, Triple tuple) {
		return matchingCombinations(tuple)
			// for DHT publish, tuple pattern must have either defined subject or defined object
			.filter(tt -> isConcrete(tt.getSubject()) || isConcrete(tt.getObject()))
			.map(tt -> hash(keyManager.getCreatorKey(), tt))
			.parallel()
		;
	}
	
	/** 
	 * Generate the pattern hash for this specific TriTuple only.
	 */
	public static String forSearch(KPABEPublicKey key, Triple tuple) {
		return hash(key, tuple);
	}
	
	private ContainerPatterns() {
	}
}
