package com.colabriq.endpoint.graph.containerized;

import static com.colabriq.shared.TripleUtil.matchingCombinations;

import java.util.stream.Stream;

import org.apache.jena.graph.Triple;

import com.colabriq.kpabe.key.KPABEPublicKey;
import com.colabriq.shared.Rounds;
import com.colabriq.shared.encode.CBOR;
import com.colabriq.shared.encode.Hash;
import com.colabriq.shared.encode.Hex;
import com.colabriq.shared.encode.RDFBinary;

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
	public static Stream<String> forPublish(KPABEPublicKey creator, Triple tuple) {
		return matchingCombinations(tuple)
			.map(tt -> hash(creator, tt))
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
