package com.goodforgoodbusiness.endpoint.route.dht;

import static com.goodforgoodbusiness.endpoint.route.dht.DHTRequestUtil.processCustodyChainHeader;

import com.goodforgoodbusiness.endpoint.dht.ClaimCollector;
import com.goodforgoodbusiness.endpoint.dht.DHTSubmitter;
import com.goodforgoodbusiness.endpoint.rdf.RDFRunner;
import com.goodforgoodbusiness.endpoint.route.SparqlRoute;
import com.goodforgoodbusiness.webapp.error.BadRequestException;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import spark.Request;
import spark.Response;
import spark.Route;

@Singleton
public class DHTSparqlRoute extends SparqlRoute implements Route {
	private final ClaimCollector collector;
	private final DHTSubmitter submitter;
	
	@Inject
	public DHTSparqlRoute(ClaimCollector collector, RDFRunner runner, DHTSubmitter submitter) {
		super(runner);
		
		this.collector = collector;
		this.submitter = submitter;
	}

	public Object doUpdate(Request req, Response res, String sparqlStmt) throws BadRequestException {
		var claim = collector.begin();
		processCustodyChainHeader(req).forEach(claim::linked);
		
		super.doUpdate(req, res, sparqlStmt);
		
		// submit if collected
		// return result.
		return submitter
			.submit(claim)
			.map(r -> {
				// return created claim
				JsonObject o = new JsonObject();
				o.addProperty("id", r.getId());
				return o.toString();
			})
			.orElse("{}")
		;
	}
}
