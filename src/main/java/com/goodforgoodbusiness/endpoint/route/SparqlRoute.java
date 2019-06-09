package com.goodforgoodbusiness.endpoint.route;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.endpoint.MIMEMappings;
import com.goodforgoodbusiness.endpoint.processor.ImportProcessException;
import com.goodforgoodbusiness.endpoint.processor.SparqlProcessor;
import com.goodforgoodbusiness.webapp.ContentType;
import com.goodforgoodbusiness.webapp.error.BadRequestException;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import spark.Request;
import spark.Response;
import spark.Route;

@Singleton
public class SparqlRoute implements Route {
	private static final Logger log = Logger.getLogger(SparqlRoute.class);
	
	private final SparqlProcessor runner;
	
	@Inject
	public SparqlRoute(SparqlProcessor runner) {
		this.runner = runner;
	}
	
	@Override
	public Object handle(Request req, Response res) throws Exception {
		// post data
		if (req.contentType() != null) {
			if (req.contentType().toLowerCase().equals(ContentType.sparql_query.getContentTypeString())) {
				return doQuery(req, res, req.body());
			}
			
			if (req.contentType().toLowerCase().equals(ContentType.sparql_update.getContentTypeString())) {
				return doUpdate(req, res, req.body());
			}
		}
		
		// get data
		var query = req.queryParams("query");
		if (query != null) {
			return doQuery(req, res, query);
		}
		
		var update = req.queryParams("update");
		if (update != null) {
			return doUpdate(req, res, update);
		}
		
		throw new BadRequestException("Must specify SPARQL query or update");
	}
	
	public Object doQuery(Request req, Response res, String sparqlStmt) throws BadRequestException, IOException {
		log.info("Query with accept=" + req.headers("accept"));
		
		var contentType = MIMEMappings.getContentType(Optional.ofNullable(req.headers("accept")));
		
		if (!contentType.isPresent()) {
			throw new BadRequestException("Must specify a valid accept header");
		}
		
		log.info("Replying with contentType=" + contentType.get());
		
		res.status(200);
		res.type(contentType.get());
		res.raw().setContentType(contentType.get());

		try (OutputStream stream = res.raw().getOutputStream()) {
			runner.query(sparqlStmt, contentType.get(), stream);
			return 200;
		}
		catch (ImportProcessException e) {
			throw new BadRequestException(e.getMessage(), e);
		}
	}

	public Object doUpdate(Request req, Response res, String sparqlStmt) throws BadRequestException {
		res.type(ContentType.json.getContentTypeString());
		
		try {
			runner.update(sparqlStmt);
			return "{}";
		}
		catch (ImportProcessException e) {
			throw new BadRequestException(e.getMessage(), e);
		}
	}
}
