package com.goodforgoodbusiness.rdfjava.dht;

import org.apache.jena.query.Dataset;

import com.goodforgoodbusiness.model.SubmittableClaim;
import com.goodforgoodbusiness.model.SubmittedClaim;
import com.goodforgoodbusiness.rdfjava.RDFException;
import com.goodforgoodbusiness.rdfjava.RDFRunner;

public class DHTRDFRunner extends RDFRunner {
	private ClaimContextMap claimContextMap;
	private ClaimCollector claimCollector;
	
	public DHTRDFRunner(String logName, DHTDatasetFactory dhtDF) {
		this(logName, dhtDF.create(), dhtDF.getClaimContextMap(), dhtDF.getClaimCollector());
	}
	
	public DHTRDFRunner(String logName, Dataset dataset, ClaimContextMap claimContextMap, ClaimCollector claimCollector) {
		super(logName, dataset);
		this.claimContextMap = claimContextMap;
		this.claimCollector = claimCollector;
	}
	
	@Override
	public void update(String updateStmt) throws RDFException {
		SubmittableClaim claim = claimCollector.begin();
		
		super.update(updateStmt);
		
		try {
			// now see if a claim has been created
			// if so, post it to the DHT
			if (!claim.isEmpty()) {
				var results = DHTClient.submit(claim);

				for (SubmittedClaim sc : results) {
					claim.getTriples().forEach(t -> claimContextMap.add(t, sc));
				}
			}
		}
		catch (Exception e) {
			throw new RDFException(e.getMessage(), e);
		}
	}
}
