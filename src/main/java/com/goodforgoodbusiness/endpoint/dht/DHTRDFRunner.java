package com.goodforgoodbusiness.endpoint.dht;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;

import com.goodforgoodbusiness.endpoint.rdf.RDFException;
import com.goodforgoodbusiness.endpoint.rdf.RDFRunner;
import com.goodforgoodbusiness.model.SubmittableClaim;
import com.goodforgoodbusiness.model.SubmittedClaim;
import com.google.inject.Inject;

public class DHTRDFRunner extends RDFRunner {
	private DHTEngineClient client;
	private ClaimContextMap contextMap;
	private ClaimCollector collector;
	
	@Inject
	public DHTRDFRunner(Dataset dataset, Model model, DHTEngineClient client, ClaimContextMap contextMap, ClaimCollector collector) {
		super(dataset, model);
		
		this.client = client;
		this.contextMap = contextMap;
		this.collector = collector;
	}
	
	@Override
	public void update(String updateStmt) throws RDFException {
		SubmittableClaim claim = collector.begin();
		
		super.update(updateStmt);
		
		try {
			// now see if a claim has been created
			// if so, post it to the DHT
			if (!claim.isEmpty()) {
				var results = client.submit(claim);

				for (SubmittedClaim sc : results) {
					claim.getTriples().forEach(t -> contextMap.add(t, sc));
				}
			}
		}
		catch (Exception e) {
			throw new RDFException(e.getMessage(), e);
		}
	}
}
