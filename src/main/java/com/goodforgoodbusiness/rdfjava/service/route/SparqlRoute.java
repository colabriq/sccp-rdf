package com.goodforgoodbusiness.rdfjava.service.route;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.rdfjava.RDFException;
import com.goodforgoodbusiness.rdfjava.RDFRunner;
import com.goodforgoodbusiness.shared.web.ContentType;
import com.goodforgoodbusiness.shared.web.MIMEMappings;
import com.goodforgoodbusiness.shared.web.error.BadRequestException;

import spark.Request;
import spark.Response;
import spark.Route;

public class SparqlRoute implements Route {
	private static final Logger log = Logger.getLogger(SparqlRoute.class);
	
	private final RDFRunner runner;
	
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
		
		var contentType = MIMEMappings.getContentType(req.headers("accept"));
		
		if (contentType == null) {
			throw new BadRequestException("Must specify a valid accept header");
		}
		
		log.info("Replying with contentType=" + contentType);
		
		res.status(200);
		res.type(contentType);
		res.raw().setContentType(contentType);

		try (OutputStream stream = res.raw().getOutputStream()) {
			runner.query(sparqlStmt, contentType, stream);
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
