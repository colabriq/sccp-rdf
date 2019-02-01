package com.goodforgoodbusiness.endpoint.dht;

import java.util.Optional;

import com.goodforgoodbusiness.endpoint.rdf.RDFException;
import com.goodforgoodbusiness.model.SubmittableClaim;
import com.goodforgoodbusiness.model.SubmittedClaim;
import com.google.inject.Inject;

/**
 * Provides the logic level above raw submission of claims to the DHT
 */
public class DHTSubmitter {
	private final ClaimContext context;
	private final DHTEngineClient client;
	
	@Inject
	public DHTSubmitter(DHTEngineClient client, ClaimContext context) {
		this.client = client;
		this.context = context;
	}
	
	public Optional<SubmittedClaim> submit(SubmittableClaim submittableClaim) {
		try {
			// now see if anything has been collected
			// if so, post it to the DHT
			if (submittableClaim.isEmpty()) {
				return Optional.empty();
			}
			else {
				var submittedClaim = client.submit(submittableClaim);

				// record submitted claim ID in to any triples collected
				submittableClaim
					.getTriples()
					.forEach(trup -> context.add(trup, submittedClaim));
				
				return Optional.of(submittedClaim);
			}
		}
		catch (Exception e) {
			throw new RDFException(e.getMessage(), e);
		}
	}
}
