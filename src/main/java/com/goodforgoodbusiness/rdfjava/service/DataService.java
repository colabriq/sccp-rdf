package com.goodforgoodbusiness.rdfjava.service;

import com.goodforgoodbusiness.rdfjava.RDFRunner;
import com.goodforgoodbusiness.rdfjava.service.route.UploadRoute;

public class DataService extends RDFService {
	public DataService(int port, RDFRunner runner) {
		super(port, runner);
	}
	
	@Override
	protected void configure() {
		super.configure();
		this.service.post("/upload", new UploadRoute(runner));
	}
}
