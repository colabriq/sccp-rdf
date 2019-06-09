package com.goodforgoodbusiness.endpoint.processor;

import org.apache.jena.graph.Graph;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Rejects any updates.
 */
public class ReadOnlySparqlProcessor extends SparqlProcessor {
	@Inject
	public ReadOnlySparqlProcessor(Provider<Graph> graphProvider) {
		super(graphProvider);
	}
	
	@Override
	public void update(String updateStmt) throws SparqlProcessException {
		throw new SparqlProcessException("Read only endpoint");
	}
}
