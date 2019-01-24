package com.goodforgoodbusiness.rdfjava.dht;

import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.model.Link;
import com.goodforgoodbusiness.model.SubmittableClaim;
import com.google.inject.Singleton;

@Singleton
public class ClaimCollector {
	private final ThreadLocal<SubmittableClaim> claimLocal = new ThreadLocal<>();
	
	public SubmittableClaim begin() {
		SubmittableClaim claim = new SubmittableClaim();
		claimLocal.set(claim);
		return claim;
	}
	
	public void added(Triple trup) {
		SubmittableClaim claim = claimLocal.get();
		if (claim != null) {
			claim.added(trup);
		}
	}

	public void removed(Triple trup) {
		SubmittableClaim claim = claimLocal.get();
		if (claim != null) {
			claim.removed(trup);
		}
	}
	
	public void linked(Link link) {
		SubmittableClaim claim = claimLocal.get();
		if (claim != null) {
			claim.linked(link);
		}
	}
}
