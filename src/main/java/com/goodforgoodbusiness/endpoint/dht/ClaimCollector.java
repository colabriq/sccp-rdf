package com.goodforgoodbusiness.endpoint.dht;

import java.util.Optional;

import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.model.Link;
import com.goodforgoodbusiness.model.SubmittableClaim;
import com.google.inject.Singleton;

@Singleton
public class ClaimCollector {
	private final ThreadLocal<SubmittableClaim> claimLocal = new ThreadLocal<>();
	
	public SubmittableClaim begin() {
		var claim = new SubmittableClaim();
		claimLocal.set(claim);
		return claim;
	}
	
	public void clear() {
		claimLocal.remove();
	}
	
	public Optional<SubmittableClaim> current() {
		return Optional.ofNullable(claimLocal.get());
	}
	
	public void added(Triple trup) {
		current().ifPresent(claim -> claim.added(trup));
	}

	public void removed(Triple trup) {
		current().ifPresent(claim -> claim.removed(trup));
	}
	
	public void linked(Link link) {
		current().ifPresent(claim -> claim.linked(link));
	}
}
