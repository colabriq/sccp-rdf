package com.goodforgoodbusiness.endpoint.dht;

import java.util.Optional;

import com.goodforgoodbusiness.endpoint.processor.ImportProcessException;
import com.goodforgoodbusiness.model.SubmittableContainer;
import com.goodforgoodbusiness.model.SubmittedContainer;
import com.google.inject.Inject;

/**
 * Provides the logic level above raw submission of containers to the DHT
 */
public class DHTContainerSubmitter {
	private final DHTContainerStore containerStore;
	private final DHTEngineClient client;
	
	@Inject
	public DHTContainerSubmitter(DHTEngineClient client, DHTContainerStore containerStore) {
		this.client = client;
		this.containerStore = containerStore;
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
				if (containerStore.addContainer(submittedContainer, true)) {
					submittableContainer
						.getTriples()
						.forEach(trup -> containerStore.addSource(trup, submittedContainer));
					
					return Optional.of(submittedContainer);
				}
				return Optional.empty();
			}
		}
		catch (Exception e) {
			throw new ImportProcessException(e.getMessage(), e); // XXX
		}
	}
}
