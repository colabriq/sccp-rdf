package com.goodforgoodbusiness.endpoint.webapp.dht;

import static com.goodforgoodbusiness.endpoint.webapp.dht.DHTHeaders.processCustodyChainHeader;
import static com.goodforgoodbusiness.endpoint.webapp.dht.DHTHeaders.processSubmitModeHeader;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import org.apache.jena.query.Dataset;

import com.goodforgoodbusiness.endpoint.graph.containerized.ContainerCollector;
import com.goodforgoodbusiness.endpoint.processor.task.Importer;
import com.goodforgoodbusiness.endpoint.processor.task.dht.DHTImportTask;
import com.goodforgoodbusiness.endpoint.processor.task.dht.DHTPublishResult;
import com.goodforgoodbusiness.endpoint.processor.task.dht.DHTUpdateTask;
import com.goodforgoodbusiness.endpoint.webapp.SparqlTaskLauncher;
import com.goodforgoodbusiness.model.SubmittableContainer.SubmitMode;
import com.goodforgoodbusiness.rpclib.stream.InputWriteStream;
import com.goodforgoodbusiness.webapp.ContentType;
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
		
		// get the publish mode
		var publishMode = processSubmitModeHeader(ctx).orElse(SubmitMode.getDefault());
		
		// process custody chain header
		var custodyChainFromHeader = processCustodyChainHeader(ctx);
		
		service.submit(new DHTUpdateTask(
			collector,
			dataset,
			publishMode,
			custodyChainFromHeader,
			stmt.toString(),
			Future.<DHTPublishResult>future().setHandler(result -> {
				if (result.failed()) {
					ctx.fail(result.cause());
				}
				else {
					ctx.response().end(result.result().toJson());
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
		
		// get the publish mode
		var publishMode = processSubmitModeHeader(ctx).orElse(SubmitMode.getDefault());
		
		// process custody chain header
		var custodyChainFromHeader = DHTHeaders.processCustodyChainHeader(ctx);
		
		// pipe the file into our InputWriteStream
		InputWriteStream iws;
		
		try {
			iws = new InputWriteStream();
			file.pipeTo(iws);
		}
		catch (IOException e) {
			ctx.fail(e);
			return;
		}
		
		service.submit(
			new DHTImportTask(
				collector,
			    importer,
			    publishMode,
			    custodyChainFromHeader,
			    iws.getInputStream(),
			    lang,
				Future.<DHTPublishResult>future().setHandler(result -> {
					file.close();
					
					if (result.failed()) {
						ctx.fail(result.cause());
					}
					else {
						// standard JSON result
						ctx.response().end(result.result().toJson());
					}
				})
			)
		);
	}
}
