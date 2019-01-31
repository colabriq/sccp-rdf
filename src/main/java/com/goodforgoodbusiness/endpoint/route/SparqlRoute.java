package com.goodforgoodbusiness.endpoint.route;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.endpoint.MIMEMappings;
import com.goodforgoodbusiness.endpoint.rdf.RDFException;
import com.goodforgoodbusiness.endpoint.rdf.RDFRunner;
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
	
	private final RDFRunner runner;
	
	@Inject
	public SparqlRoute(RDFRunner runner) {
		this.runner = runner;
	}
	
	@Override
	public Object handle(Request req, Response res) throws Exception {
		req.requestMethod();
		
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
		
		log.info("Replying with contentType=" + contentType);
		
		res.status(200);
		res.type(contentType.get());
		res.raw().setContentType(contentType.get());

		try (OutputStream stream = res.raw().getOutputStream()) {
			runner.query(sparqlStmt, contentType.get(), stream);
			return 200;
		}
		catch (RDFException e) {
			throw new BadRequestException(e.getMessage(), e);
		}
	}

	public Object doUpdate(Request req, Response res, String sparqlStmt) throws BadRequestException {
//		  if dhtgraph is not None:
//		    context = dhtstore.new_context()
//		    dhtgraph.update(stmt)
//		    
//		    # any novel claim made?
//		    claim = context.get_claim()
//		    if claim:
//		      submission = claim.submit()
//		    
//		      # save claim in to localstore
//		      submission.record(localstore)
//		    
//		      # return claim id
//		      result = { 'id': submission.claim_id }
//		    else:
//		      result = { 'id': None }
//
//		    return Response(
//		      json.dumps(result, indent=2) + '\n\n',
//		      mimetype='application/json'
//		    )
//		  else:
//		    localgraph.update(stmt)
//		    return Response(
//		      json.dumps({ 'id': None }, indent=2) + '\n\n',
//		      mimetype='application/json'
//		    )
		
		try {
			runner.update(sparqlStmt);
			return "OK";
		}
		catch (RDFException e) {
			throw new BadRequestException(e.getMessage(), e);
		}
	}
}
