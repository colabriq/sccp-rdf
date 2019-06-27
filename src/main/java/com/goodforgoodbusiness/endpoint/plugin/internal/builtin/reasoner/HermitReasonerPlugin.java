package com.goodforgoodbusiness.endpoint.plugin.internal.builtin.reasoner;

import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import com.google.inject.Singleton;

@Singleton
public class HermitReasonerPlugin extends OWLReasonerPlugin {
	private static final OWLReasonerFactory HERMIT_FACTORY = new org.semanticweb.HermiT.ReasonerFactory();
	
	public HermitReasonerPlugin() {
		super(HERMIT_FACTORY);
	}
}
