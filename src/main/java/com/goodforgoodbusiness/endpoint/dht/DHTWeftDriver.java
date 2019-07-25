package com.goodforgoodbusiness.endpoint.dht;

import java.util.Optional;

import javax.crypto.SecretKey;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.endpoint.crypto.EncryptionException;
import com.goodforgoodbusiness.endpoint.crypto.SymmetricEncryption;
import com.goodforgoodbusiness.endpoint.dht.backend.DHTBackend;
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
	public void publish(StorableContainer container, Future<DHTWeftPublishResult> future) throws EncryptionException {
		var secretKey = SymmetricEncryption.createKey();
			
		var encryptedContainer = encrypt(container, secretKey);
		var data = JSON.encodeToString(encryptedContainer).getBytes();
		
		backend.publishContainer(container.getId(), data, Future.<String>future().setHandler(
			weftLocationResult -> {
				if (weftLocationResult.succeeded()) {
					future.complete(
						new DHTWeftPublishResult(
							secretKey,
							encryptedContainer,
							weftLocationResult.result(),
							data
						)
					);
				}
				else {
					future.fail(weftLocationResult.cause());
				}
			}
		));
		

	}
	
	/**
	 * Retrieve a container (in encrypted form)
	 */
	public void fetch(String location, Future<Optional<EncryptedContainer>> future) {
		log.debug("Fetching container at " + location);
		
		// first find a location where this id has been stored
		// then fetch the data.
		
		backend.fetchContainer(location, Future.<Optional<byte[]>>future().setHandler(
			dataResult -> {
				if (dataResult.succeeded()) {
					if (dataResult.result().isPresent()) {
						future.complete(
							dataResult.result()
								.map(String::new)
								.map(s -> JSON.decode(s, EncryptedContainer.class))
							)
						;
					}
					else {
						future.complete(Optional.empty());
					}
				}
				else {
					future.fail(dataResult.cause());
				}
			}
		));
	}
	
	/**
	 * Retrieve a container (and try to decrypt)
	 */
	public void fetch(String location, SecretKey secretKey, Future<Optional<StorableContainer>> future) throws EncryptionException {
		fetch(location, Future.<Optional<EncryptedContainer>>future().setHandler(fetchResult -> {
			if (fetchResult.succeeded()) {
				future.complete(
					fetchResult.result().map(
						c -> decrypt(c, secretKey)
					)
				);
			}
			else {
				future.fail(fetchResult.cause());
			}
		}));
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
