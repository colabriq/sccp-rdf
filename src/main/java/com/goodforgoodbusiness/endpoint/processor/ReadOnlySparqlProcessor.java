package com.goodforgoodbusiness.endpoint.processor;

import org.apache.jena.query.Dataset;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Rejects any updates.
 */
public class ReadOnlySparqlProcessor extends SparqlProcessor {
	@Inject
	public ReadOnlySparqlProcessor(Provider<Dataset> datasetProvider) {
		super(datasetProvider);
	}
	
	@Override
	public void update(String updateStmt) throws SparqlProcessException {
		throw new SparqlProcessException("Read only endpoint");
	}
}
