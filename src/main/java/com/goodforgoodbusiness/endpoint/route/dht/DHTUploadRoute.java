package com.goodforgoodbusiness.endpoint.route.dht;

import static com.goodforgoodbusiness.endpoint.route.dht.DHTRequestUtil.processCustodyChainHeader;

import com.goodforgoodbusiness.endpoint.graph.container.ContainerCollector;
import com.goodforgoodbusiness.endpoint.graph.dht.DHTContainerSubmitter;
import com.goodforgoodbusiness.endpoint.processor.ImportProcessor;
import com.goodforgoodbusiness.endpoint.route.UploadRoute;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import spark.Request;
import spark.Response;
import spark.Route;

@Singleton
public class DHTUploadRoute extends UploadRoute implements Route {
	private final ContainerCollector collector;
	private final DHTContainerSubmitter submitter;
	
	@Inject
	public DHTUploadRoute(ContainerCollector collector, ImportProcessor runner, DHTContainerSubmitter submitter) {
		super(collector, runner);
		
		this.collector = collector;
		this.submitter = submitter;
	}
	
	@Override
	public Object handle(Request req, Response res) throws Exception {
		var container = collector.begin();
		
		try {
			processCustodyChainHeader(req).forEach(container::linked);
			super.handle(req, res);
		}
		finally {
			collector.clear();
		}
		
		// submit if collected
		// return result.
		return submitter
			.submit(container)
			.map(r -> {
				// return created container
				JsonObject o = new JsonObject();
				
				o.addProperty("id", r.getId());
				o.addProperty("added", r.getAdded().count());
				o.addProperty("removed", r.getRemoved().count());
				
				return o.toString();
			})
			.orElse("{}")
		;
	}
}
