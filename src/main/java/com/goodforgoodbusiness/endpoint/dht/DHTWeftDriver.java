package com.goodforgoodbusiness.endpoint.dht;

import static java.util.Collections.singleton;

import java.util.Optional;

import javax.crypto.SecretKey;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.endpoint.crypto.EncryptionException;
import com.goodforgoodbusiness.endpoint.crypto.SymmetricEncryption;
import com.goodforgoodbusiness.endpoint.temp.DHTBackend;
import com.goodforgoodbusiness.model.Contents;
import com.goodforgoodbusiness.model.EncryptedContainer;
import com.goodforgoodbusiness.model.EncryptedEnvelope;
import com.goodforgoodbusiness.model.Envelope;
import com.goodforgoodbusiness.model.StorableContainer;
import com.goodforgoodbusiness.shared.TimingRecorder;
import com.goodforgoodbusiness.shared.TimingRecorder.TimingCategory;
import com.goodforgoodbusiness.shared.encode.JSON;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.vertx.core.Future;

/** 
 * Instance of the Weft, where containers are stored.
 */
@Singleton
public class DHTWeftDriver {
	private static final Logger log = Logger.getLogger(DHTWeftDriver.class);
	
	private final DHTBackend backend;
	
	@Inject
	public DHTWeftDriver(DHTBackend backend) {
		this.backend = backend;
	}
	
	/** 
	 * Push a container on to the {@link DHTWeftDriver}.
	 */
	public void publish(StorableContainer container, Future<DHTWeftPublish> future) throws EncryptionException {
		var secretKey = SymmetricEncryption.createKey();
			
		var encryptedContainer = encrypt(container, secretKey);
		var data = JSON.encodeToString(encryptedContainer);
		var location = backend.publish(singleton(encryptedContainer.getId()), data);
			
		future.complete(
			new DHTWeftPublish(
				secretKey,
				encryptedContainer,
				location,
				data
			)
		);
	}
	
	/**
	 * Retrieve a container (in encrypted form)
	 */
	public Optional<EncryptedContainer> fetch(String id) {
		log.debug("Fetching container: " + id);
		
		// first find the location where this id has been stored
		// then fetch the data.
		
		return backend
			.search(id)
			.findFirst()
			.flatMap(backend::fetch)
			.map(data -> JSON.decode(data, EncryptedContainer.class))
		;
	}
	
	/**
	 * Retrieve a container (and try to decrypt)
	 */
	public Optional<StorableContainer> fetch(String id, SecretKey secretKey) throws EncryptionException {
		var containerHolder = fetch(id);
		
		if (containerHolder.isPresent()) {
			return Optional.of(decrypt(containerHolder.get(), secretKey));
		}
		else {
			return Optional.empty();
		}
	}
	
	private static EncryptedContainer encrypt(StorableContainer container, SecretKey secretKey) throws EncryptionException {
		var contents = JSON.encodeToString(container.getInnerEnvelope().getContents());
		
		try (var timer = TimingRecorder.timer(TimingCategory.SYMMETRIC_ENCRYPT)) {
			// encryption round 1: convergent encryption (using id)
			var encryptRound1 = SymmetricEncryption.encrypt(contents, container.getConvergentKey());
			
			// encryption round 2: secret key
			var encryptRound2 = SymmetricEncryption.encrypt(encryptRound1, secretKey);
			
			return new EncryptedContainer(
				new EncryptedEnvelope(
					container.getId(),
					encryptRound2,
					container.getInnerEnvelope().getLinkVerifier(),
					container.getInnerEnvelope().getSignature()
				),
				container.getLinks(),
				container.getSignature()
			);
		}
	}
	
	private static StorableContainer decrypt(EncryptedContainer container, SecretKey secretKey) throws EncryptionException {
		try (var timer = TimingRecorder.timer(TimingCategory.SYMMETRIC_DECRYPT)) {
			// decryption round 1: secret key
			var decryptRound1 = SymmetricEncryption.decrypt(container.getInnerEnvelope().getContents(), secretKey);
			
			// decryption round 2: convergent encryption (using id)
			var decryptRound2 = SymmetricEncryption.decrypt(decryptRound1, container.getConvergentKey());
		
			return new StorableContainer(
				new Envelope(
					JSON.decode(decryptRound2, Contents.class),
					container.getInnerEnvelope().getLinkVerifier(),
					container.getInnerEnvelope().getSignature()
				),
				container.getLinks(),
				container.getSignature()
			);
		}
	}
}
