package com.colabriq.endpoint.dht.share;

import static com.colabriq.endpoint.graph.containerized.ContainerAttributes.SHARE_ALL;
import static java.util.stream.Stream.concat;

import java.util.stream.Stream;

import org.apache.jena.graph.Triple;

import com.colabriq.endpoint.crypto.key.EncodeableShareKey;
import com.colabriq.endpoint.dht.share.backend.KeyStoreBackend;
import com.colabriq.endpoint.graph.containerized.ContainerAttributes;
import com.colabriq.kpabe.KPABEEncryption;
import com.colabriq.kpabe.KPABEException;
import com.colabriq.kpabe.key.KPABEKeyPair;
import com.colabriq.kpabe.key.KPABEPublicKey;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Manages keys used to decrypt triples.
 */
@Singleton
public class ShareKeyStore {
	private final KeyStoreBackend backend;
	private final KPABEKeyPair keyPair;
	
	private final EncodeableShareKey selfKey;
	
	@Inject
	public ShareKeyStore(KPABEKeyPair keyPair, KeyStoreBackend backend) throws KPABEException {
		this.keyPair = keyPair;
		this.backend = backend;
		this.selfKey = new EncodeableShareKey(KPABEEncryption.getInstance(getCurrentKeyPair()).shareKey(SHARE_ALL));
	}
	
	/**
	 * Find any public key identities who shared something with us
	 */
	public Stream<KPABEPublicKey> getCreators(Triple pattern) throws ShareKeyStoreException {
		// always add 'self' to the list of possible creators
		// we will fetch triples we have created from the DHT side
		return concat(Stream.of(keyPair.getPublic()), backend.getCreators(pattern));
	}
	
	/**
	 * Retrieve all keys shared with us by a particular MPK (public key).
	 */
	public Stream<EncodeableShareKey> getKeys(KPABEPublicKey publicKey, Triple triple) throws ShareKeyStoreException {
		if (publicKey.equals(keyPair.getPublic())) {
			return Stream.of(selfKey);
		}
		else {
			return backend.getKeys(publicKey, triple);
		}
	}

	/**
	 * Save a key for future retrieval via the find... methods
	 * {@link ShareResponse} version.
	 */
	public void saveKey(ShareResponse response) throws ShareKeyStoreException {
		saveKey(response.getPattern(), response.getKey());
	}
	
	/**
	 * Save a key for future retrieval via the find... methods
	 * {@link ShareResponse} version.
	 */
	public void saveKey(Triple pattern, EncodeableShareKey key) throws ShareKeyStoreException {
		saveKey(new SharePattern(pattern), key);
	}
	
	/**
	 * Save a key for future retrieval via the find... methods
	 * {@link Triple} version.
	 */
	public void saveKey(SharePattern pattern, EncodeableShareKey key) throws ShareKeyStoreException {
		backend.saveKey(pattern, key);
	}
	
	/**
	 * Returns instance of KP-ABE for the keys we're currently using to encrypt
	 */
	public KPABEKeyPair getCurrentKeyPair() {
		return keyPair;
	}
	
	/**
	 * Create a share key from a {@link ShareRequest}.
	 */
	public EncodeableShareKey newShareKey(ShareRequest request) throws KPABEException {
		// XXX: will need to work out what key was in use during the time range?
		var kpabe = KPABEEncryption.getInstance(getCurrentKeyPair());
		return new EncodeableShareKey(kpabe.shareKey(ContainerAttributes.forShare(getCurrentKeyPair().getPublic(), request)));
	}
}
