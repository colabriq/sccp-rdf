package com.goodforgoodbusiness.endpoint.dht.share.backend;

import java.util.stream.Stream;

import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.endpoint.crypto.key.EncodeableShareKey;
import com.goodforgoodbusiness.endpoint.dht.share.ShareKeyStoreException;
import com.goodforgoodbusiness.endpoint.dht.share.SharePattern;
import com.goodforgoodbusiness.kpabe.key.KPABEPublicKey;

public interface KeyStoreBackend {
	/**
	 * Find any public key identities who shared something with us
	 */
	public Stream<KPABEPublicKey> getCreators(Triple pattern) throws ShareKeyStoreException;
	
	/**
	 * Retrieve all keys shared with us by a particular MPK (public key).
	 */
	public Stream<EncodeableShareKey> getKeys(KPABEPublicKey publicKey, Triple triple) throws ShareKeyStoreException;

	/**
	 * Save a key for future retrieval via the find... methods
	 * {@link Triple} version.
	 */
	public void saveKey(SharePattern pattern, EncodeableShareKey key) throws ShareKeyStoreException;
}
