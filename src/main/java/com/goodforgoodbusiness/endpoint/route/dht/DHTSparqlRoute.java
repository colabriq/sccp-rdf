package com.goodforgoodbusiness.endpoint.route.dht;

import static com.goodforgoodbusiness.endpoint.route.dht.DHTRequestUtil.processCustodyChainHeader;

import com.goodforgoodbusiness.endpoint.dht.DHTContainerCollector;
import com.goodforgoodbusiness.endpoint.dht.DHTContainerSubmitter;
import com.goodforgoodbusiness.endpoint.processor.SparqlProcessor;
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
	private final DHTContainerCollector collector;
	private final DHTContainerSubmitter submitter;
	
	@Inject
	public DHTSparqlRoute(DHTContainerCollector collector, SparqlProcessor runner, DHTContainerSubmitter submitter) {
		super(runner);
		
		this.collector = collector;
		this.submitter = submitter;
	}

	@Override
	public Object doUpdate(Request req, Response res, String sparqlStmt) throws BadRequestException {
		var container = collector.begin();
		processCustodyChainHeader(req).forEach(container::linked);
		
		super.doUpdate(req, res, sparqlStmt);
		
		// submit if collected
		// return result.
		return submitter
			.submit(container)
			.map(r -> {
				// return created container
				JsonObject o = new JsonObject();
				o.addProperty("id", r.getId());
				return o.toString();
			})
			.orElse("{}")
		;
	}
}
