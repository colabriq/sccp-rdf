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
	private final ContainerContexts context;
	private final DHTEngineClient client;
	
	@Inject
	public DHTSubmitter(DHTEngineClient client, ContainerContexts context) {
		this.client = client;
		this.context = context;
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
					.forEach(trup -> context.add(trup, submittedContainer));
				
				return Optional.of(submittedContainer);
			}
		}
		catch (Exception e) {
			throw new RDFException(e.getMessage(), e);
		}
	}
}
