package com.goodforgoodbusiness.endpoint.dht.keys;

import java.util.stream.Stream;

import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.endpoint.crypto.key.EncodeableShareKey;
import com.goodforgoodbusiness.kpabe.key.KPABEPublicKey;

public interface ShareKeyStore {
	/**
	 * Find any public key identities who shared something with us
	 */
	public Stream<KPABEPublicKey> knownContainerCreators(Triple pattern) throws ShareKeyStoreException;
	
	/**
	 * Retrieve all keys shared with us by a particular MPK (public key).
	 */
	public Stream<EncodeableShareKey> keysForDecrypt(KPABEPublicKey publicKey, Triple triple) throws ShareKeyStoreException;

	/**
	 * Save a key for future retrieval via the find... methods
	 */
	public void saveKey(Triple triple, EncodeableShareKey key) throws ShareKeyStoreException;
}
