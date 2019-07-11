package com.goodforgoodbusiness.endpoint.dht;

import static java.util.Collections.singleton;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.endpoint.ShareManager;
import com.goodforgoodbusiness.endpoint.crypto.key.EncodeableSecretKey;
import com.goodforgoodbusiness.endpoint.crypto.key.EncodeableShareKey;
import com.goodforgoodbusiness.endpoint.dht.keys.ShareKeyStore;
import com.goodforgoodbusiness.endpoint.graph.containerized.ContainerPatterns;
import com.goodforgoodbusiness.endpoint.temp.DHTBackend;
import com.goodforgoodbusiness.kpabe.KPABEDecryption;
import com.goodforgoodbusiness.kpabe.KPABEEncryption;
import com.goodforgoodbusiness.kpabe.KPABEException;
import com.goodforgoodbusiness.kpabe.key.KPABEPublicKey;
import com.goodforgoodbusiness.model.Pointer;
import com.goodforgoodbusiness.model.TriTuple;
import com.goodforgoodbusiness.shared.encode.JSON;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.vertx.core.Future;

/** 
 * Methods that work the Warp (container search indexing).
 */
@Singleton
public class DHTWarpDriver {
	private static final Logger log = Logger.getLogger(DHTWarpDriver.class);
	
	private static final SecureRandom RANDOM;
	
	static {
		try {
			RANDOM = SecureRandom.getInstanceStrong();
		}
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	
	private final DHTBackend backend;
	
	private final ShareManager shareManager;
	private final ShareKeyStore keyStore;
	
	@Inject
	public DHTWarpDriver(DHTBackend backend, ShareManager shareManager, ShareKeyStore keyStore) {
		this.backend = backend;
		this.shareManager = shareManager;
		this.keyStore = keyStore;
	}
	
	/**
	 * Publish data against a new pattern to the Warp
	 * Data is already on the weft, provide the ID
	 */
	public void publish(String id, String pattern, String policy, EncodeableSecretKey key, Future<DHTWarpPublish> future) {
		log.debug("Publishing warp pattern: " + pattern);
			
		try {
			var pointer = new Pointer(
				id,
				key.toEncodedString(),
				RANDOM.nextLong()
			);
			
			var data = encrypt(pointer, policy);
			var location = backend.publish(singleton(pattern), data);
			
			future.complete(new DHTWarpPublish(pointer, location, data));
		}
		catch (KPABEException e) {
			log.error("Encryption Error", e);
			future.fail(e);
		}
	}
	
	/**
	 * Encrypt a pointer using KP-ABE
	 */
	private String encrypt(Pointer pointer, String accessPolicy) throws KPABEException {
		return KPABEEncryption.getInstance(shareManager.getCurrentKeys()).encrypt(JSON.encodeToString(pointer), accessPolicy);
	}
	
	/**
	 * Search for pointers with a particular triple signature
	 * This will lead to containers with results in them
	 */
	public Stream<Pointer> search(TriTuple tuple) {
		log.debug("Searching warp for triple " + tuple);
		
		// look for anyone who's ever shared a key matching these triples with us
		// (possibly more than one) and fetch containers for each of them from the DHT
		
		return keyStore
			.knownContainerCreators(tuple)
			.distinct()
			.flatMap(containerCreator -> search(containerCreator, tuple))
		;
	}
	
	/**
	 * Search for containers by a specific creator
	 */
	private Stream<Pointer> search(KPABEPublicKey creator, TriTuple tuple) {
		var patternHash = ContainerPatterns.forSearch(creator, tuple);
		log.debug("Searching warp for containers from " + creator.toString().substring(0, 10) + 
			"... with patterns " + patternHash.substring(0,  10) + "...");
		
		// have to process this stream because state is changed
		return backend.search(patternHash)
			.distinct()
			.map(location -> backend.fetch(location))
				.filter(Optional::isPresent)
				.map(Optional::get)
			.map(data -> decrypt(creator, tuple, data))
				.filter(Optional::isPresent)
				.map(Optional::get)
		;
	}
	
	/**
	 * Attempt to decrypt a pointer.
	 * Specify publicKey so we know which keys to try against the data
	 */
	private Optional<Pointer> decrypt(KPABEPublicKey creator, TriTuple pattern, String data) {
		log.info("Got data for " + pattern + " from " + creator.toString().substring(0, 10) + "...");
		
		return
			keyStore.keysForDecrypt(creator, pattern) // XXX think about expiry?
				.parallel()
				.map(EncodeableShareKey::toKeyPair)
				.map(keyPair -> {
					try {
						var dec = KPABEDecryption.getInstance();
						var result = dec.decrypt(data, keyPair);
						if (result != null) {
							log.debug("Decrypt success");
						}
						else {
							log.debug("Decrypt failure (this can be normal)");
						}
							
						return result;
					}
					catch (KPABEException | InvalidKeyException e) {
						log.error("Error decrypting pointer", e);
						return null;
					}
				})
				.filter(Objects::nonNull)
				.findFirst()
				.map(json -> JSON.decode(json, Pointer.class))
			;
	}
}
