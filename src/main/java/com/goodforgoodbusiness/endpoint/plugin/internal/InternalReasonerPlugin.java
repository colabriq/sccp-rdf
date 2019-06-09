package com.goodforgoodbusiness.endpoint.plugin.internal;

import org.apache.jena.graph.Graph;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * Harness for reasoner plugins
 */
public interface InternalReasonerPlugin {
	public void initialized(Graph baseGraph) throws OWLOntologyCreationException;
	
	public void updated(Graph updateGraph) throws OWLOntologyCreationException;
}
