package com.goodforgoodbusiness.endpoint.route.dht;

import static com.goodforgoodbusiness.endpoint.route.dht.DHTRequestUtil.processCustodyChainHeader;

import com.goodforgoodbusiness.endpoint.dht.ClaimCollector;
import com.goodforgoodbusiness.endpoint.dht.DHTSubmitter;
import com.goodforgoodbusiness.endpoint.rdf.RDFRunner;
import com.goodforgoodbusiness.endpoint.route.UploadRoute;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import spark.Request;
import spark.Response;
import spark.Route;

@Singleton
public class DHTUploadRoute extends UploadRoute implements Route {
	private final ClaimCollector collector;
	private final DHTSubmitter submitter;
	
	@Inject
	public DHTUploadRoute(ClaimCollector collector, RDFRunner runner, DHTSubmitter submitter) {
		super(runner);
		
		this.collector = collector;
		this.submitter = submitter;
	}
	
	@Override
	public Object handle(Request req, Response res) throws Exception {
		var claim = collector.begin();
		processCustodyChainHeader(req).forEach(claim::linked);
		
		super.handle(req, res);
		
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
