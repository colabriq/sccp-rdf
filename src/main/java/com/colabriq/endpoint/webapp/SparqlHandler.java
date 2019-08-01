package com.goodforgoodbusiness.endpoint.webapp;
//package com.goodforgoodbusiness.endpoint.webapp.handler;
//
//import java.util.Optional;
//import java.util.concurrent.ExecutorService;
//
//import org.apache.jena.query.Dataset;
//import org.apache.log4j.Logger;
//
//import com.goodforgoodbusiness.endpoint.MIMEMappings;
//import com.goodforgoodbusiness.endpoint.graph.container.ContainerCollector;
//import com.goodforgoodbusiness.endpoint.processor.ImportProcessException;
//import com.goodforgoodbusiness.endpoint.processor.task.QueryTask;
//import com.goodforgoodbusiness.endpoint.processor.task.UpdateTask;
//import com.goodforgoodbusiness.webapp.ContentType;
//import com.goodforgoodbusiness.webapp.error.BadRequestException;
//import com.google.gson.JsonObject;
//import com.google.inject.Inject;
//
//import io.vertx.core.Future;
//import io.vertx.core.Handler;
//import io.vertx.core.Verticle;
//import io.vertx.core.buffer.Buffer;
//import io.vertx.core.http.HttpHeaders;
//import io.vertx.ext.web.RoutingContext;
//
///**
// * Common handler internals for queries and updates
// * @author ijmad
// *
// */
//public abstract class SparqlHandler implements Handler<RoutingContext> {
//	private static final Logger log = Logger.getLogger(SparqlHandler.class);
//	
//	protected final Verticle parent;
//	protected final Dataset dataset;
//	protected final ContainerCollector collector;
//	protected final ExecutorService service;
//	
//	@Inject
//	protected SparqlHandler(Verticle parent, Dataset dataset, ContainerCollector collector, ExecutorService service) {
//		this.parent = parent;
//		this.dataset = dataset;
//		this.collector = collector;
//		this.service = service;
//	}
//	

//	
//
//}
