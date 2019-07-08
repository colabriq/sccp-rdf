//package com.goodforgoodbusiness.endpoint.webapp.dht;
//
//import static com.goodforgoodbusiness.endpoint.webapp.dht.DHTRequestUtil.processCustodyChainHeader;
//import static com.goodforgoodbusiness.shared.TimingRecorder.timer;
//
//import com.goodforgoodbusiness.endpoint.graph.container.ContainerCollector;
//import com.goodforgoodbusiness.endpoint.graph.dht.DHTContainerSubmitter;
//import com.goodforgoodbusiness.endpoint.processor.SparqlProcessor;
//import com.goodforgoodbusiness.endpoint.webapp.handler.SparqlGetHandler;
//import com.goodforgoodbusiness.shared.TimingRecorder.TimingCategory;
//import com.goodforgoodbusiness.webapp.error.BadRequestException;
//import com.google.gson.JsonObject;
//import com.google.inject.Inject;
//import com.google.inject.Singleton;
//
//import io.vertx.core.Handler;
//import io.vertx.ext.web.RoutingContext;
//import spark.Request;
//import spark.Response;
//import spark.Route;
//
//@Singleton
//public class DHTSparqlRoute extends SparqlGetHandler implements Handler<RoutingContext> {
//	private final ContainerCollector collector;
//	private final DHTContainerSubmitter submitter;
//	
//	@Inject
//	public DHTSparqlRoute(ContainerCollector collector, SparqlProcessor runner, DHTContainerSubmitter submitter) {
//		super(collector, runner);
//		
//		this.collector = collector;
//		this.submitter = submitter;
//	}
//
//	@Override
//	public Object doUpdate(Request req, Response res, String sparqlStmt) throws BadRequestException {
//		var container = collector.begin();
//		
//		try (var timer = timer(TimingCategory.RDF_ROUTE_SPARQL)) {
//			processCustodyChainHeader(req).forEach(container::linked);			
//			super.doUpdate(req, res, sparqlStmt);
//			
//			// submit if collected
//			// return result.
//			return submitter
//				.submit(container)
//				.map(r -> {
//					// return created container
//					JsonObject o = new JsonObject();
//					
//					o.addProperty("id", r.getId());
//					o.addProperty("added", r.getAdded().count());
//					o.addProperty("removed", r.getRemoved().count());
//					
//					return o.toString();
//				})
//				.orElse("{}")
//			;
//		}
//		finally {
//			collector.clear();
//		}
//	}
//}
