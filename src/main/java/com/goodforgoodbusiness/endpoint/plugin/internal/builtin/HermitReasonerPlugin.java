package com.goodforgoodbusiness.endpoint.plugin.internal.builtin;

import org.apache.jena.graph.Graph;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

public class HermitReasonerPlugin extends AbstractReasonerPlugin {
	private static final OWLReasonerFactory HERMIT_FACTORY = new org.semanticweb.HermiT.ReasonerFactory();
	
	public HermitReasonerPlugin(Graph graph) throws OWLOntologyCreationException {
		super(graph, HERMIT_FACTORY);
	}
}
