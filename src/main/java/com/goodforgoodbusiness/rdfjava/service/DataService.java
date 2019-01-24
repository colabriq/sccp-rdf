package com.goodforgoodbusiness.rdfjava.service;

import com.goodforgoodbusiness.rdfjava.rdf.RDFRunner;
import com.goodforgoodbusiness.rdfjava.service.route.UploadRoute;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class DataService extends RDFService {
	@Inject
	public DataService(@Named("port") int port, RDFRunner runner) {
		super(port, runner);
	}
	
	@Override
	protected void configure() {
		super.configure();
		this.service.post("/upload", new UploadRoute(runner));
	}
}
