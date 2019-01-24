package com.goodforgoodbusiness.rdfjava.service;

import com.goodforgoodbusiness.rdfjava.rdf.RDFRunner;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class SchemaService extends RDFService {
	@Inject
	public SchemaService(@Named("port") int port, RDFRunner runner) {
		super(port, runner);
	}
}
