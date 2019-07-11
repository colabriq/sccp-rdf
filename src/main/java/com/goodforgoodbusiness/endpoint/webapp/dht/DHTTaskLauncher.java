package com.goodforgoodbusiness.endpoint.webapp.dht;

import java.util.concurrent.ExecutorService;

import org.apache.jena.query.Dataset;

import com.goodforgoodbusiness.endpoint.graph.containerized.ContainerCollector;
import com.goodforgoodbusiness.endpoint.processor.task.Importer;
import com.goodforgoodbusiness.endpoint.processor.task.dht.DHTImportTask;
import com.goodforgoodbusiness.endpoint.processor.task.dht.DHTPublishResult;
import com.goodforgoodbusiness.endpoint.processor.task.dht.DHTUpdateTask;
import com.goodforgoodbusiness.endpoint.webapp.SparqlTaskLauncher;
import com.goodforgoodbusiness.webapp.ContentType;
import com.goodforgoodbusiness.webapp.stream.InputReadStream;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;

/**
 * A version of 'update' that is DHT/container aware
 */
@Singleton
public class DHTTaskLauncher extends SparqlTaskLauncher {
	protected final ContainerCollector collector;
	
	@Inject
	public DHTTaskLauncher(ExecutorService service, Dataset dataset, Importer importer, ContainerCollector collector) {
		super(service, dataset, importer);
		this.collector = collector;
	}
	
	/**
	 * Run a 'update' SPARQL query
	 * Overrides to use DHT aware version of the update task.
	 */
	@Override
	public void update(RoutingContext ctx, Buffer stmt) {
		ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, ContentType.JSON.getContentTypeString());
		
		// process custody chain header
		var custodyChainFromHeader = DHTCustodyChain.processCustodyChainHeader(ctx);
		
		service.submit(new DHTUpdateTask(
			collector,
			dataset,
			custodyChainFromHeader,
			stmt.toString(),
			Future.<DHTPublishResult>future().setHandler(result -> {
				if (result.failed()) {
					ctx.fail(result.cause());
				}
				else {
					ctx.response().end(result.result().toJson());
//					ctx.next();
				}
			})
		));
	}
	
	/**
	 * Starts an import from an uploaded file
	 * Overrides to use DHT aware version of the update task.
	 */
	@Override
	public void importFile(RoutingContext ctx, String lang, AsyncFile file) {
		ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, ContentType.JSON.getContentTypeString());
		
		// process custody chain header
		var custodyChainFromHeader = DHTCustodyChain.processCustodyChainHeader(ctx);
		
		service.submit(
			new DHTImportTask(
				collector,
			    importer,
			    custodyChainFromHeader,
			    new InputReadStream(file),
			    lang,
				Future.<DHTPublishResult>future().setHandler(result -> {
					file.close();
					
					if (result.failed()) {
						ctx.fail(result.cause());
					}
					else {
						// standard JSON result
						ctx.response().end(result.result().toJson());
//						ctx.next();
					}
				})
			)
		);
	}
}
