//package com.goodforgoodbusiness.endpoint.webapp.dht;
//
//import static com.goodforgoodbusiness.endpoint.webapp.dht.DHTRequestUtil.processCustodyChainHeader;
//import static com.goodforgoodbusiness.shared.TimingRecorder.timer;
//import static com.goodforgoodbusiness.shared.TimingRecorder.TimingCategory.RDF_UPLOADING;
//
//import com.goodforgoodbusiness.endpoint.graph.container.ContainerCollector;
//import com.goodforgoodbusiness.endpoint.graph.dht.DHTContainerSubmitter;
//import com.goodforgoodbusiness.endpoint.processor.ImportProcessor;
//import com.goodforgoodbusiness.endpoint.webapp.handler.UploadHandler;
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
//public class DHTUploadRoute extends UploadHandler implements Handler<RoutingContext> {
//	private final ContainerCollector collector;
//	private final DHTContainerSubmitter submitter;
//	
//	@Inject
//	public DHTUploadRoute(ContainerCollector collector, ImportProcessor runner, DHTContainerSubmitter submitter) {
//		super(collector, runner);
//		
//		this.collector = collector;
//		this.submitter = submitter;
//	}
//	
//	@Override
//	public Object handle(Request req, Response res) throws Exception {
//		var container = collector.begin();
//		
//		try (var timer = timer(RDF_UPLOADING)) {
//			processCustodyChainHeader(req).forEach(container::linked);
//			super.handle(req, res);
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
