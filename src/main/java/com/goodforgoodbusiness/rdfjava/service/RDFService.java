package com.goodforgoodbusiness.rdfjava.service;

import java.io.IOException;

import com.goodforgoodbusiness.rdfjava.RDFRunner;
import com.goodforgoodbusiness.rdfjava.service.route.SparqlRoute;
import com.goodforgoodbusiness.shared.web.cors.CorsFilter;
import com.goodforgoodbusiness.shared.web.cors.CorsRoute;
import com.goodforgoodbusiness.shared.web.error.BadRequestException;
import com.goodforgoodbusiness.shared.web.error.BadRequestExceptionHandler;
import com.goodforgoodbusiness.shared.web.error.IOExceptionHandler;

import spark.Service;

public class RDFService {
	protected final int port;
	protected final RDFRunner runner;
	
	protected Service service = null;
	
	public RDFService(int port, RDFRunner runner) {
		this.port = port;
		this.runner = runner;
	}
	
	protected void configure() {
		service.options("/*", new CorsRoute());
		service.before(new CorsFilter());
		
		service.get("/sparql", new SparqlRoute(runner));
		service.post("/sparql", new SparqlRoute(runner));
		
		service.exception(BadRequestException.class, new BadRequestExceptionHandler());
		service.exception(IOException.class, new IOExceptionHandler());
	}
	
	public final void start() {
		service = Service.ignite();
		service.port(port);
		
		configure();
		
		service.awaitInitialization();
	}
}
