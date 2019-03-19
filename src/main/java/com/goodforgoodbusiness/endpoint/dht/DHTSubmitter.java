package com.goodforgoodbusiness.endpoint.dht;

import java.util.Optional;

import com.goodforgoodbusiness.endpoint.rdf.RDFException;
import com.goodforgoodbusiness.model.SubmittableContainer;
import com.goodforgoodbusiness.model.SubmittedContainer;
import com.google.inject.Inject;

/**
 * Provides the logic level above raw submission of containers to the DHT
 */
public class DHTSubmitter {
	private final TripleContextStore contextStore;
	private final DHTEngineClient client;
	
	@Inject
	public DHTSubmitter(DHTEngineClient client, TripleContextStore contexts) {
		this.client = client;
		this.contextStore = contexts;
	}
	
	public Optional<SubmittedContainer> submit(SubmittableContainer submittableContainer) {
		try {
			// now see if anything has been collected
			// if so, post it to the DHT
			if (submittableContainer.isEmpty()) {
				return Optional.empty();
			}
			else {
				var submittedContainer = client.submit(submittableContainer);

				// record submitted container ID in to any triples collected
				submittableContainer
					.getTriples()
					.forEach(trup -> contextStore.add(trup, submittedContainer));
				
				return Optional.of(submittedContainer);
			}
		}
		catch (Exception e) {
			throw new RDFException(e.getMessage(), e);
		}
	}
}
