package com.goodforgoodbusiness.endpoint.dht;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
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

import io.vertx.core.CompositeFuture;
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
			backend.publishPointer(pattern, data, Future.<Void>future().setHandler(
				result -> {
					if (result.succeeded()) {
						future.complete(new DHTWarpPublish(pointer, data));
					}
					else {
						future.fail(result.cause());
					}
				}
			));
		}
		catch (KPABEException e) {
			log.error("Encryption Error", e);
			future.fail(e);
		}
	}
	
	/**
	 * Encrypt a pointer using KP-ABE
	 */
	private byte[] encrypt(Pointer pointer, String accessPolicy) throws KPABEException {
		return KPABEEncryption.getInstance(shareManager.getCurrentKeys())
			.encrypt(JSON.encodeToString(pointer), accessPolicy).getBytes();
	}
	
	/**
	 * Search for pointers with a particular triple signature
	 * This will lead to containers with results in them
	 */
	public void search(TriTuple tuple, Future<Stream<Pointer>> future) {
		log.debug("Searching warp for triple " + tuple);
		
		// look for anyone who's ever shared a key matching these triples with us
		// (possibly more than one) and fetch containers for each of them from the DHT
		
		@SuppressWarnings("rawtypes") // because CompositeFuture isn't genericised properly
		List<Future> creators = keyStore
			.knownContainerCreators(tuple)
			.distinct()
			.map(creator -> {
				// kick off a search per creator
				var searchFuture = Future.<Stream<Pointer>>future();
				search(creator, tuple, searchFuture);
				return searchFuture;
			})
			.collect(Collectors.toList())
		;
		
		// wait for all futures to complete, then return
		CompositeFuture.all(creators).setHandler(results -> {
			if (results.succeeded()) {
				// create a concatenated stream
				Stream<Pointer> stream = Stream.empty();
				
				for (var x = 0; x < results.result().size(); x++) {
					results.result().resultAt(x);
					stream = Stream.concat(results.result().resultAt(x), stream);
				}
				
				future.complete(stream);
			}
			else {
				future.fail(results.cause());
			}
		});
	}
	
	/**
	 * Search for containers by a specific creator
	 */
	private void search(KPABEPublicKey creator, TriTuple tuple, Future<Stream<Pointer>> future) {
		var patternHash = ContainerPatterns.forSearch(creator, tuple);
		log.debug("Searching warp for containers from " + creator.toString().substring(0, 10) + 
			"... with patterns " + patternHash.substring(0,  10) + "...");
		
		// have to process this stream because state is changed
		backend.searchForPointers(patternHash, Future.<Stream<byte[]>>future().setHandler(
			results -> {
				if (results.succeeded()) {
					future.complete(
						results.result()
							.map(data -> decrypt(creator, tuple, data))
								.filter(Optional::isPresent)
								.map(Optional::get)
					);
				}
				else {
					future.fail(results.cause());
				}
			}
		));
	}
	
	/**
	 * Attempt to decrypt a pointer.
	 * Specify publicKey so we know which keys to try against the data
	 */
	private Optional<Pointer> decrypt(KPABEPublicKey creator, TriTuple pattern, byte[] data) {
		log.info("Got data for " + pattern + " from " + creator.toString().substring(0, 10) + "...");
		
		return
			keyStore.keysForDecrypt(creator, pattern) // XXX think about expiry?
				.parallel()
				.map(EncodeableShareKey::toKeyPair)
				.map(keyPair -> {
					try {
						var dec = KPABEDecryption.getInstance();
						var result = dec.decrypt(new String(data), keyPair);
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
